package org.jmxtrans.embedded.util.socket;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.jmxtrans.embedded.util.ssl.LoggingTrustStrategy;
import org.jmxtrans.embedded.util.ssl.NeverTrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SslSocketFactory implements SocketFactory {

    private final boolean allowSelfSignedCertificates;

    /**
     * default constructor is mandatory
     */
    public SslSocketFactory() {
        this(false);
    }

    public SslSocketFactory(boolean trustingSelfSignedCertificates) {
        super();
        this.allowSelfSignedCertificates = trustingSelfSignedCertificates;
    }

    @Override
    public Socket createSocket(String host, int port, int socketConnectTimeoutInMillis) throws IOException {
        SSLContext sslContext;
        sslContext = buildSSLContext();
        Socket socket = sslContext.getSocketFactory().createSocket(host, port);
        //socket.setKeepAlive(true);
        //socket.setSoTimeout(socketConnectTimeoutInMillis);
        return socket;
    }

    protected SSLContext buildSSLContext() {
        try {
            return buildSSLContextWithExceptions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SSLContext buildSSLContextWithExceptions() throws Exception {
        final KeyStore defaultTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final SSLContextBuilder builder = new SSLContextBuilder();

        TrustStrategy trustStrategy = allowSelfSignedCertificates ? new TrustSelfSignedStrategy() : new NeverTrustStrategy();
        TrustStrategy loggingTrustStrategy = new LoggingTrustStrategy(trustStrategy);
        builder.loadTrustMaterial(defaultTrustStore, loggingTrustStrategy);

        builder.setSecureRandom(new SecureRandom());
        //builder.useTLS();
        return builder.build();
    }


}
