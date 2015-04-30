package org.jmxtrans.embedded.util.pool;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.jmxtrans.embedded.util.net.HostAndPort;
import org.jmxtrans.embedded.util.net.SocketWriter;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Factory for UDP {@linkplain SocketWriter} instances. *
 *
 * @author <a href="mailto:patrick.bruehlmann@gmail.com">Patrick Brühlmann</a>
 *
 */
public class UDPSocketWriterPoolFactory extends BaseKeyedPoolableObjectFactory<HostAndPort, SocketWriter> implements KeyedPoolableObjectFactory<HostAndPort, SocketWriter> {

    private final Charset charset;

    public UDPSocketWriterPoolFactory(String charset) {
        this.charset = Charset.forName(charset);
    }

    public UDPSocketWriterPoolFactory(Charset charset) {
        this.charset = charset;
    }

    @Override
    public SocketWriter makeObject(HostAndPort HostAndPort) throws Exception {
        DatagramSocket datagramSocket = new DatagramSocket(null);
        datagramSocket.connect(new InetSocketAddress(HostAndPort.getHost(), HostAndPort.getPort()));
        return new SocketWriter(datagramSocket, charset);
    }

    @Override
    public void destroyObject(HostAndPort HostAndPort, SocketWriter socketWriter) throws Exception {
        super.destroyObject(HostAndPort, socketWriter);
        socketWriter.close();
        socketWriter.getDatagramSocket().close();
    }

    /**
     * Defensive approach: we test all the "<code>Socket.isXXX()</code>" flags.
     */
    @Override
    public boolean validateObject(HostAndPort HostAndPort, SocketWriter socketWriter) {
        DatagramSocket datagramSocket = socketWriter.getDatagramSocket();
        return datagramSocket.isConnected()
                && !datagramSocket.isClosed();
    }

}
