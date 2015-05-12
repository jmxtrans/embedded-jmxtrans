package org.jmxtrans.embedded.util.pool;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
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
public class UDPSocketWriterPoolFactory extends BaseKeyedPooledObjectFactory<HostAndPort, SocketWriter> implements KeyedPooledObjectFactory<HostAndPort, SocketWriter> {

    private final Charset charset;

    public UDPSocketWriterPoolFactory(String charset) {
        this.charset = Charset.forName(charset);
    }

    public UDPSocketWriterPoolFactory(Charset charset) {
        this.charset = charset;
    }

    @Override
    public SocketWriter create(HostAndPort hostAndPort) throws Exception {
        DatagramSocket datagramSocket = new DatagramSocket(null);
        datagramSocket.connect(new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort()));
        return new SocketWriter(datagramSocket, charset);
    }

    @Override
    public PooledObject<SocketWriter> wrap(SocketWriter socketWriter) {
        return new DefaultPooledObject<SocketWriter>(socketWriter);
    }

    @Override
    public boolean validateObject(HostAndPort hostAndPort, PooledObject<SocketWriter> socketWriterRef) {
        DatagramSocket datagramSocket = socketWriterRef.getObject().getDatagramSocket();
        return datagramSocket.isConnected()
                && !datagramSocket.isClosed();
    }

    @Override
    public void destroyObject(HostAndPort hostAndPort, PooledObject<SocketWriter> socketWriterRef) throws Exception {
        super.destroyObject(hostAndPort, socketWriterRef);
        SocketWriter socketWriter = socketWriterRef.getObject();
        socketWriter.close();
        socketWriter.getDatagramSocket().close();
    }
}
