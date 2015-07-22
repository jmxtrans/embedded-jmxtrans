package org.jmxtrans.embedded.util.net.ssl;

import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import java.net.URL;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class TrustAllSSLSocketFactoryTest {

    @Ignore
    @Test
    public void test_trust_invalid_ssl_certificate_with_google_ip() throws Exception {
        String url = "https://216.58.211.68"; // 216.58.211.68 is google.com
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        SSLSocketFactory socketFactory = new TrustAllSSLSocketFactory();

        connection.setSSLSocketFactory(socketFactory);

        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                return true;
            }
        });
        int responseCode = connection.getResponseCode();

        System.out.println(url + "\t->\t" + responseCode);

    }
}
