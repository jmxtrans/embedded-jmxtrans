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
import org.jmxtrans.embedded.util.net.SocketWriter;
import org.jmxtrans.embedded.util.socket.SocketFactory;

import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Factory for {@linkplain SocketWriter} instances created from {@linkplain HostAndPort}.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class SocketWriterPoolFactory extends BaseKeyedPooledObjectFactory<HostAndPort, SocketWriter> implements KeyedPooledObjectFactory<HostAndPort, SocketWriter> {

    public static final int DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS = 500;
    private final Charset charset;
    private final int socketConnectTimeoutInMillis;
    private final SocketFactory socketFactory;

    public SocketWriterPoolFactory(Charset charset, int socketConnectTimeoutInMillis, SocketFactory socketFactory) {
        this.charset = charset;
        this.socketConnectTimeoutInMillis = socketConnectTimeoutInMillis;
        this.socketFactory = socketFactory;
    }

    @Override
    public SocketWriter create(HostAndPort hostAndPort) throws Exception {
        Socket socket = socketFactory.createSocket(hostAndPort.getHost(), hostAndPort.getPort(), socketConnectTimeoutInMillis);

        return new SocketWriter(socket, charset);
    }

    @Override
    public void destroyObject(HostAndPort hostAndPort, PooledObject<SocketWriter> socketWriterRef) throws Exception {
        super.destroyObject(hostAndPort, socketWriterRef);
        SocketWriter socketWriter = socketWriterRef.getObject();
        socketWriter.close();
        socketWriter.getSocket().close();
    }

    @Override
    public PooledObject<SocketWriter> wrap(SocketWriter socketWriter) {
        return new DefaultPooledObject<SocketWriter>(socketWriter);
    }

    /**
     * Defensive approach: we test all the "<code>Socket.isXXX()</code>" flags.
     */
    @Override
    public boolean validateObject(HostAndPort hostAndPort, PooledObject<SocketWriter> socketWriterRef) {
        Socket socket = socketWriterRef.getObject().getSocket();
        return socket.isConnected()
                && socket.isBound()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }
}
