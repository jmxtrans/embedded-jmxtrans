package org.jmxtrans.embedded.util.net.ssl;

import com.sun.net.httpserver.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.security.KeyStore;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class SslUtilsTest {

    @Test
    public void test_keystore_in_classpath_google_dot_com() throws Exception {

        String keyStore = "classpath:org/jmxtrans/embedded/util/net/ssl/cacerts";
        String passphrase = "changeit";
        SSLSocketFactory sslSocketFactory = SslUtils.getSSLSocketFactory(keyStore, passphrase, keyStore, passphrase);

        URL url = new URL("https://www.google.com");

        HttpsURLConnection cnn = (HttpsURLConnection) url.openConnection();
        cnn.setSSLSocketFactory(sslSocketFactory);

        int responseCode = cnn.getResponseCode();

        Assert.assertThat(responseCode, Matchers.is(200));
    }

    @Test
    public void test_keystore_on_file_system_google_dot_com() throws Exception {

        String keyStore = "org/jmxtrans/embedded/util/net/ssl/cacerts";
        URL keyStoreUrl = Thread.currentThread().getContextClassLoader().getResource(keyStore);
        File keyStoreFile = new File(keyStoreUrl.toURI());
        String keystorePassword = "changeit";
        String keyStoreFilePath = keyStoreFile.getAbsolutePath();

        SSLSocketFactory sslSocketFactory = SslUtils.getSSLSocketFactory(keyStoreFilePath, keystorePassword, keyStoreFilePath, keystorePassword);

        URL url = new URL("https://www.google.com");
        HttpsURLConnection cnn = (HttpsURLConnection) url.openConnection();
        cnn.setSSLSocketFactory(sslSocketFactory);

        int responseCode = cnn.getResponseCode();

        Assert.assertThat(responseCode, Matchers.is(200));
    }


    @Test
    public void test_selfsigned_certificate() throws Exception {
        String password = "password";
        String keyStore = "classpath:org/jmxtrans/embedded/util/net/ssl/keystore.jks";
        String trustStore = "classpath:org/jmxtrans/embedded/util/net/ssl/truststore.jks";

        HttpsServer server = getHttpsServer(keyStore, password);

        URL testUrl = new URL("https://127.0.0.1:" + server.getAddress().getPort() + "/");

        HttpsURLConnection cnn = (HttpsURLConnection) testUrl.openConnection();
        cnn.setSSLSocketFactory(SslUtils.getSSLSocketFactory(null, null, trustStore, password));

        int responseCode = cnn.getResponseCode();

        Assert.assertThat(responseCode, Matchers.is(200));
        server.stop(5); // seconds
    }

    /**
     * @param keyStore keystore containing the certificate used by the HTTPS server
     * @param password password of the keystore
     * @return HTTP server listening on a random port
     * @throws IOException
     */
    @Nonnull
    private HttpsServer getHttpsServer(String keyStore, String password) throws IOException {
        HttpsServer server;
        SSLContext sslContext = SslUtils.getSslContext(keyStore, password, null, null);
        server = HttpsServer.create(new InetSocketAddress(0), 5);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Hello world";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                SSLContext c = getSSLContext();
                SSLParameters sslParams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslParams);


            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        return server;
    }
}
