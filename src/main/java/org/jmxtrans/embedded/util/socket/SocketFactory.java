package org.jmxtrans.embedded.util.socket;

import java.io.IOException;
import java.net.Socket;

public interface SocketFactory {

    Socket createSocket(String host, int port, int socketConnectTimeoutInMillis) throws IOException;

}
