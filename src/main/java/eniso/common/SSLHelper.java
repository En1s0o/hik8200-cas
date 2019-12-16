package eniso.common;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;

/**
 * SSLHelper
 * <p>
 * SSL 助手
 *
 * @author Eniso
 */
@Slf4j
public class SSLHelper {

    private X509TrustManager x509TrustManager = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

    };

    public X509TrustManager getX509TrustManager() {
        return x509TrustManager;
    }

    public SSLSocketFactory getSSLContext() {
        // Install the all-trusting trust manager
        try {
            TrustManager[] trustManagers = new TrustManager[]{getX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustManagers, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> {
            // needs verify?
            return true;
        };
    }

}
