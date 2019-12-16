package eniso.common.hik;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * HikCasLoginInterceptor
 *
 * @author Eniso
 */
@Slf4j
@EverythingIsNonNull
class HikCasLoginInterceptor implements Interceptor {

    private static final String CAS_LOGIN = "/cas/login";
    private static final String TAG_FORM = "form";
    private static final String TAG_INPUT = "input";
    private static final String ATTR_ACTION = "action";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_VALUE = "value";
    private static final String V_CODE = "vcode";
    private static final String CAS_LOGIN_URL = "cas-login-url";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";

    private final String username;
    private final String password;

    HikCasLoginInterceptor(String username, String password) {
        this.username = username;
        this.password = ByteUtils.toHexString(new SHA256.Digest().digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    private String encryptPassword(String vCode) {
        byte[] data = (password + vCode).getBytes(StandardCharsets.UTF_8);
        return ByteUtils.toHexString(new SHA256.Digest().digest(data));
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        // 失败时直接返回。非登录页面也直接返回
        if (!response.isSuccessful() || !isLoginPage(response)) {
            return response;
        }

        // 如果登录页面异常，直接返回即可
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return response;
        }

        // 解析登录页面，如果解析不到登录表单，直接返回即可
        HttpUrl loginPageUrl = response.request().url();
        String html = responseBody.string();
        HashMap<String, String> loginForm = parseLoginForm(loginPageUrl, html);
        if (loginForm.isEmpty()) {
            return response.newBuilder()
                    .body(ResponseBody.create(responseBody.contentType(), html))
                    .build();
        }

        // 登录
        String loginUrl = loginForm.remove(CAS_LOGIN_URL);
        RequestBody loginBody = makeLoginBody(loginForm);
        Request loginRequest = new Request.Builder()
                .url(loginUrl)
                .method(HTTP_POST, loginBody)
                .build();
        Response loginResponse = chain.proceed(loginRequest);
        if (!loginResponse.isSuccessful() || HTTP_GET.equals(request.method())) {
            // 失败时直接返回。如果成功了，并且原始请求是 GET 方式，那么直接返回即可
            return loginResponse;
        }

        // 程序执行到这里表示登录成功了，并且原始请求为非 GET 请求
        // 所以需要再发起一次原始请求，这次直接返回结果即可
        if (loginResponse.body() != null) {
            loginResponse.close();
        }
        return chain.proceed(request);
    }

    private boolean isLoginPage(Response response) {
        // 如果 response 是登录页面，必须满足 GET 请求，并且以 /cas/login 开始
        Request request = response.request();
        HttpUrl url = request.url();
        return HTTP_GET.equals(request.method()) && url.encodedPath().startsWith(CAS_LOGIN);
    }

    /**
     * 生成登录请求体
     *
     * @param loginForm 登录表单
     * @return 登录请求体
     */
    private RequestBody makeLoginBody(Map<String, String> loginForm) {
        loginForm.put("username", username);
        loginForm.put("password", encryptPassword(loginForm.get(V_CODE)));
        loginForm.put("pwdStrength", "3");

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = loginForm.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> keyValue = it.next();
            sb.append(keyValue.getKey());
            sb.append("=");
            sb.append(keyValue.getValue());
            if (it.hasNext()) {
                sb.append("&");
            }
        }
        return RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                sb.toString());
    }

    /**
     * 解析登录表单
     *
     * @param html HTML 登录页面
     * @return 登录表单
     */
    private static HashMap<String, String> parseLoginForm(HttpUrl url, String html) {
        HashMap<String, String> values = new HashMap<>(16);
        // 解析页面，并得到 <body> 标签下的所有 <form> 标签
        Document doc = Jsoup.parse(html);
        Elements forms = doc.body().getElementsByTag(TAG_FORM);
        // 遍历 <form> 标签，找到登录 <form>
        String host = url.scheme() + "://" + url.host();
        for (Element form : forms) {
            HttpUrl actionUrl;
            String action = form.attr(ATTR_ACTION);
            // action 属性的值以 "/cas/login" 开头为登录 <form>
            try {
                if (action.startsWith(CAS_LOGIN)) {
                    actionUrl = HttpUrl.get(host + action);
                } else {
                    actionUrl = HttpUrl.get(action);
                    if (!url.encodedPath().startsWith(CAS_LOGIN)) {
                        continue;
                    }
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
                continue;
            }

            values.put(CAS_LOGIN_URL, actionUrl.toString());
            // 把表单的 <input> 记录下来
            Elements inputs = form.getElementsByTag(TAG_INPUT);
            for (Element input : inputs) {
                values.put(input.attr(ATTR_NAME), input.attr(ATTR_VALUE));
            }
            values.put("serviceIP", url.host());
            return values;
        }
        throw new RuntimeException("Not found login form");
    }

}
