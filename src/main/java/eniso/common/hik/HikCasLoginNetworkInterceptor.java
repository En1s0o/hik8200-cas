package eniso.common.hik;

import java.io.IOException;
import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * HikCasLoginNetworkInterceptor
 *
 * @author Eniso
 */
@Slf4j
@EverythingIsNonNull
class HikCasLoginNetworkInterceptor implements Interceptor {

    private static final String HEADER_LOCATION = "Location";
    private static final String CAS_LOGIN = "/cas/login";
    private static final String HTTP_GET = "GET";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        // 由于重定向后会变为 GET 请求，如果原始请求为非 GET 请求，则会得到错误结果
        if (response.isRedirect() && !HTTP_GET.equals(request.method())) {
            String location = response.headers().get(HEADER_LOCATION);
            if (location != null && !location.trim().isEmpty()) {
                HttpUrl url = HttpUrl.get(location);
                // 如果是重定向到登录页面，那么可以在登录成功后让其重定向到另一个返回数据较少的 GET 接口
                if (url.encodedPath().startsWith(CAS_LOGIN)) {
                    String host = url.scheme() + "://" + url.host();
                    String service = URLEncoder.encode(host + "/vms/deviceInfo!getTypeData.action", "UTF-8");
                    return response.newBuilder()
                            .addHeader(HEADER_LOCATION, host + "/cas/login?service=" + service)
                            .build();
                }
            }
        }

        return response;
    }

}
