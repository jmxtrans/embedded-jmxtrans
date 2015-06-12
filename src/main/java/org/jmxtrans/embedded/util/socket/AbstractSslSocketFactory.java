package org.jmxtrans.embedded.util.socket;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.Socket;

public abstract class AbstractSslSocketFactory implements SocketFactory {

    /**
     * default constructor is mandatory
     */
    public AbstractSslSocketFactory() {
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
            throw new RuntimeException("Failure while creating SSLContext", e);
        }
    }

    protected abstract SSLContext buildSSLContextWithExceptions() throws Exception;

}
