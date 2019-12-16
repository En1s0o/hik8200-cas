package eniso.common.hik;

import java.util.Map;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * 海康 Cas 接口
 *
 * @author Eniso
 */
interface HikCasApi {

    /**
     * 扁平 GET 请求
     *
     * @param url 请求地址
     * @return 请求对象
     */
    @Headers({"Accept: */*"})
    @GET
    Call<String> flatGet(@Url HttpUrl url);

    /**
     * 扁平 POST 请求
     *
     * @param url 请求地址
     * @return 请求对象
     */
    @Headers({"Accept: */*"})
    @POST
    Call<String> flatPost(@Url HttpUrl url);

    /**
     * 扁平 POST 请求
     *
     * @param url    请求地址
     * @param fields 请求参数
     * @return 请求对象
     */
    @Headers({"Accept: */*"})
    @FormUrlEncoded
    @POST
    Call<String> flatPost(@Url HttpUrl url, @FieldMap Map<String, String> fields);

}
