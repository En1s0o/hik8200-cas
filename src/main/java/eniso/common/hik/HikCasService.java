package eniso.common.hik;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import eniso.common.SSLHelper;
import eniso.common.cookie.InMemoryCookieJar;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 海康 Cas 服务
 *
 * @author Eniso
 */
@Slf4j
@Service
public class HikCasService {

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";


    private ListeningExecutorService workExecutor;
    private HttpUrl host;
    private HikCasApi casApi;

    @Autowired
    public HikCasService(HikCasLoginConfiguration config) {
        host = HttpUrl.get(config.getHost());
        SSLHelper sslHelper = new SSLHelper();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .cookieJar(new InMemoryCookieJar())
                .addInterceptor(new HttpLoggingInterceptor(log::info)
                        .setLevel(config.getLoggingLevel()))
                .addInterceptor(new HikCasLoginInterceptor(config.getUsername(), config.getPassword()))
                .addNetworkInterceptor(new HikCasLoginNetworkInterceptor())
                .addNetworkInterceptor(new HttpLoggingInterceptor(log::info)
                        .setLevel(config.getNetworkLoggingLevel()))
                .sslSocketFactory(sslHelper.getSSLContext(), sslHelper.getX509TrustManager())
                .hostnameVerifier(sslHelper.getHostnameVerifier())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(config.getHost())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();

        casApi = retrofit.create(HikCasApi.class);
    }

    @PostConstruct
    public void init() {
        workExecutor = MoreExecutors.listeningDecorator(new ForkJoinPool(64, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true));
    }

    @PreDestroy
    public void destroy() {
        if (workExecutor != null) {
            workExecutor.shutdownNow();
        }
    }

    private ListenableFuture<String> process(Call<String> call) {
        return workExecutor.submit(() -> {
            Response<String> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                return errorBody.string();
            }
            return null;
        });
    }

    /**
     * 扁平请求
     * <p>
     * 例如，获取设备能力集合,
     * <pre><code>
     * {
     *     "method": "GET",
     *     "url": "/vms/linkageInfo!fetchCapabilitySet.action"
     * }
     * </code></pre>
     * <p>
     * 例如，获取设备列表,
     * <pre><code>
     * {
     *     "method": "POST",
     *     "url": "/vms/deviceInfo!fetchDeviceInfoListN.action",
     *     "values": {
     *         "start": "0",
     *         "limit": "20",
     *         "unitId": "1"
     *     }
     * }
     * </code></pre>
     * <p>
     * 例如，获取添加设备窗口编号,
     * <pre><code>
     * {
     *     "method": "POST",
     *     "url": "/vms/deviceInfo!showAddDeviceWindowN.action",
     *     "values": {
     *         "unitId": "1"
     *     }
     * }
     * </code></pre>
     *
     * @param flatRequest 扁平请求
     * @return 结果（尝试以 json 方式返回）
     * @throws Exception 发生异常
     */
    public Object flatRequest(CasFlatRequest flatRequest) throws Exception {
        HttpUrl url;
        try {
            url = HttpUrl.get(flatRequest.getUrl());
        } catch (Exception e) {
            url = host.resolve(flatRequest.getUrl());
            if (url == null) {
                throw new IllegalArgumentException(flatRequest.getUrl());
            }
        }
        ListenableFuture<String> future;
        if (HTTP_GET.equalsIgnoreCase(flatRequest.getMethod())) {
            future = process(casApi.flatGet(url));
        } else if (HTTP_POST.equalsIgnoreCase(flatRequest.getMethod())) {
            Map<String, String> values = flatRequest.getValues();
            if (values != null) {
                // values 内容为空，还是会以 application/x-www-form-urlencoded 发送出去的
                future = process(casApi.flatPost(url, values));
            } else {
                future = process(casApi.flatPost(url));
            }
        } else {
            throw new IllegalArgumentException("Unsupported Http method: " + flatRequest.getMethod());
        }

        String response = future.get();
        try {
            return mapper.readValue(response, Object.class);
        } catch (Exception e) {
            return response;
        }
    }

}
