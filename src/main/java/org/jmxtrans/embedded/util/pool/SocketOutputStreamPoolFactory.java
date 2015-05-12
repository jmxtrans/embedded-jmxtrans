/*
 * Copyright (c) 2010-2013 the original author or authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.jmxtrans.embedded.util.pool;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jmxtrans.embedded.util.net.HostAndPort;
import org.jmxtrans.embedded.util.net.SocketOutputStream;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Factory for {@linkplain org.jmxtrans.embedded.util.net.SocketOutputStream} instances created from {@linkplain HostAndPort}.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class SocketOutputStreamPoolFactory extends BaseKeyedPooledObjectFactory<HostAndPort, SocketOutputStream> implements KeyedPooledObjectFactory<HostAndPort, SocketOutputStream> {

    public static final int DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS = 500;
    private final int socketConnectTimeoutInMillis;

    public SocketOutputStreamPoolFactory(int socketConnectTimeoutInMillis) {
        this.socketConnectTimeoutInMillis = socketConnectTimeoutInMillis;
    }

    @Override
    public SocketOutputStream create(HostAndPort hostAndPort) throws Exception {
        Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort()), socketConnectTimeoutInMillis);
        return new SocketOutputStream(socket);
    }

    @Override
    public PooledObject<SocketOutputStream> wrap(SocketOutputStream outputStream) {
        return new DefaultPooledObject<SocketOutputStream>(outputStream);
    }

    @Override
    public void destroyObject(HostAndPort hostAndPort, PooledObject<SocketOutputStream> socketOutputStreamRef) throws Exception {
        SocketOutputStream socketOutputStream = socketOutputStreamRef.getObject();
        socketOutputStream.close();
        socketOutputStream.getSocket().close();
    }

    /**
     * Defensive approach: we test all the "<code>Socket.isXXX()</code>" flags.
     */
    @Override
    public boolean validateObject(HostAndPort hostAndPort, PooledObject<SocketOutputStream> socketOutputStreamRef) {
        Socket socket = socketOutputStreamRef.getObject().getSocket();
        return socket.isConnected()
                && socket.isBound()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }
}
