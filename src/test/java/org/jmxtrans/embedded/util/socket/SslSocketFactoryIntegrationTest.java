package org.jmxtrans.embedded.util.socket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class SslSocketFactoryIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(SslSocketFactoryIntegrationTest.class);

    @Test
    public void shouldLoadSecureUrlWithDefaultSslSocketFactory() throws IOException {
        doLoadSecureUrl(new DefaultSslSocketFactory());
    }

    @Test
    public void shouldLoadSecureUrlWithApacheHCSslSocketFactory() throws IOException {
        doLoadSecureUrl(new ApacheHCSslSocketFactory());
    }

    @Test
    public void shouldLoadSecureUrlWithSelfSignedAcceptedSslSocketFactory() throws IOException {
        doLoadSecureUrl(new SelfSignedAcceptedApacheHCSslSocketFactory());
    }

    private void doLoadSecureUrl(AbstractSslSocketFactory target) throws IOException {
        SSLContext sslContext = target.buildSSLContext();
        Assert.assertNotNull(sslContext);

        tryLoadingSecureUrl(sslContext);
    }

    private void tryLoadingSecureUrl(SSLContext sslContext) throws IOException {
        Proxy proxy = guessProxy();

        String urlString = "https://www.google.com";
        URL url = new URL(urlString);
        URLConnection urlConnection;
        if (proxy == null) {
            urlConnection = url.openConnection();
        } else {
            urlConnection = url.openConnection(proxy);
        }
        HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        logger.info("setting the SSLSocketFactory");
        httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);

        InputStream inputStream = null;
        try {
            inputStream = httpsUrlConnection.getInputStream();
            //IOUtils.copy(inputStream, System.out);
            IOUtils.copy(inputStream, new NullOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private Proxy guessProxy() throws MalformedURLException {
        String httpsProxyUrlProperty = System.getenv("https_proxy");
        if (httpsProxyUrlProperty == null || httpsProxyUrlProperty.isEmpty()) {
            return null;
        }
        URL proxyUrl = new URL(httpsProxyUrlProperty);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
    }

}
