package org.jmxtrans.embedded.util.net.ssl;

import org.jmxtrans.embedded.HttpProxyUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class TrustAllSSLSocketFactoryIntegrationTest {

    @Test
    public void test_trust_invalid_ssl_certificate_with_google_ip() throws Exception {
        String urlString = "https://www.google.com";

        Proxy proxy = HttpProxyUtils.getInstance().guessProxy();
        URL url = new URL(urlString);

        URLConnection urlConnection;
        if (proxy == null) {
            urlConnection = url.openConnection();
        } else {
            urlConnection = url.openConnection(proxy);
        }

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, null);
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        HttpsURLConnection connection = (HttpsURLConnection) urlConnection;
        connection.setSSLSocketFactory(socketFactory);

        int responseCode = connection.getResponseCode();

        System.out.println(url + "\t->\t" + responseCode);
    }
}
