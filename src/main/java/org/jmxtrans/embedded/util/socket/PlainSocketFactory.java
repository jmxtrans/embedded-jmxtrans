package org.jmxtrans.embedded.util.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PlainSocketFactory implements SocketFactory {

    /**
     * default constructor is mandatory
     */
    public PlainSocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(String host, int port, int socketConnectTimeoutInMillis) throws IOException {
        Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress(host, port), socketConnectTimeoutInMillis);
        return socket;
    }

}
