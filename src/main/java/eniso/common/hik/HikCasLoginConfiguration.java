package eniso.common.hik;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * HikCasLoginConfiguration
 * <p>
 * 海康登录信息配置
 *
 * @author Eniso
 */
@Data
@Component
@ConfigurationProperties(prefix = HikCasLoginConfiguration.PREFIX)
public class HikCasLoginConfiguration {

    public static final String PREFIX = "hik.cas";

    /**
     * 服务器地址
     */
    private String host;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * http 链接超时时间（秒）
     */
    private long connectTimeout = 60L;

    /**
     * http 读取超时时间（秒）
     */
    private long readTimeout = 60L;

    /**
     * http 写入超时时间（秒）
     */
    private long writeTimeout = 60L;

    /**
     * http 应用日志级别
     */

    private HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.NONE;

    /**
     * http 网络日志级别
     */
    private HttpLoggingInterceptor.Level networkLoggingLevel = HttpLoggingInterceptor.Level.NONE;

}
