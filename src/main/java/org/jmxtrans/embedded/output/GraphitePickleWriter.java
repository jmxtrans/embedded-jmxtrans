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
package org.jmxtrans.embedded.output;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.jmxtrans.embedded.EmbeddedJmxTransException;
import org.jmxtrans.embedded.QueryResult;
import org.jmxtrans.embedded.util.net.HostAndPort;
import org.jmxtrans.embedded.util.net.SocketOutputStream;
import org.jmxtrans.embedded.util.net.SocketWriter;
import org.jmxtrans.embedded.util.pool.SocketOutputStreamPoolFactory;
import org.python.core.*;
import org.python.modules.cPickle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://graphite.readthedocs.org/">Graphite</a> implementation of the {@linkplain OutputWriter}.
 *
 * This implementation uses <a href="http://graphite.readthedocs.org/en/0.9.10/feeding-carbon.html#the-pickle-protocol">
 * Carbon Pickle protocol</a> over TCP/IP.
 *
 * Settings:
 * <ul>
 * <li>"host": hostname or ip address of the Graphite server. Mandatory</li>
 * <li>"port": listen port for the TCP Plain Text Protocol of the Graphite server.
 * Optional, default value: {@value #DEFAULT_GRAPHITE_SERVER_PORT}.</li>
 * <li>"namePrefix": prefix append to the metrics name.
 * Optional, default value: {@value #DEFAULT_NAME_PREFIX}.</li>
 * <li>"enabled": flag to enable/disable the writer. Optional, default value: {$code true}.</li>
 * <li>"graphite.socketConnectTimeoutInMillis": timeout for the socketConnect in millis.
 * Optional, default value {@link SocketOutputStreamPoolFactory#DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS}</li>
 * </ul>
 * <p>All the results of {@link #write(Iterable)} are sent in one single {@code cPickle} message.</p>
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@ExplicitEpoch
public class GraphitePickleWriter extends AbstractOutputWriter implements OutputWriter {

    public static final int DEFAULT_GRAPHITE_SERVER_PORT = 2004;
    public static final String DEFAULT_NAME_PREFIX = "servers.#hostname#.";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * Metric path prefix. Ends with "." if not empty;
     */
    private String metricPathPrefix;
    private HostAndPort graphiteServerHostAndPort;
    private GenericKeyedObjectPool<HostAndPort, SocketOutputStream> socketOutputStreamPool;

    /**
     * Load settings, initialize the {@link SocketWriter} pool and test the connection to the graphite server.
     *
     * a {@link Logger#warn(String)} message is emitted if the connection to the graphite server fails.
     */
    @Override
    public void start() {
        int port = getIntSetting(SETTING_PORT, DEFAULT_GRAPHITE_SERVER_PORT);
        String host = getStringSetting(SETTING_HOST);
        graphiteServerHostAndPort = new HostAndPort(host, port);

        logger.info("Start Graphite Pickle writer connected to '{}'...", graphiteServerHostAndPort);

        metricPathPrefix = getStringSetting(SETTING_NAME_PREFIX, DEFAULT_NAME_PREFIX);
        metricPathPrefix = getStrategy().resolveExpression(metricPathPrefix);
        if (!metricPathPrefix.isEmpty() && !metricPathPrefix.endsWith(".")) {
            metricPathPrefix = metricPathPrefix + ".";
        }

        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setTestOnBorrow(getBooleanSetting("pool.testOnBorrow", true));
        config.setTestWhileIdle(getBooleanSetting("pool.testWhileIdle", true));
        config.setMaxTotal(getIntSetting("pool.maxActive", -1));
        config.setMaxIdlePerKey(getIntSetting("pool.maxIdle", -1));
        config.setMinEvictableIdleTimeMillis(getLongSetting("pool.minEvictableIdleTimeMillis", TimeUnit.MINUTES.toMillis(5)));
        config.setTimeBetweenEvictionRunsMillis(getLongSetting("pool.timeBetweenEvictionRunsMillis", TimeUnit.MINUTES.toMillis(5)));
        config.setJmxNameBase("org.jmxtrans.embedded:type=GenericKeyedObjectPool,writer=GraphitePickleWriter,name=");
        config.setJmxNamePrefix(graphiteServerHostAndPort.getHost() + "_" + graphiteServerHostAndPort.getPort());

        int socketConnectTimeoutInMillis = getIntSetting("graphite.socketConnectTimeoutInMillis", SocketOutputStreamPoolFactory.DEFAULT_SOCKET_CONNECT_TIMEOUT_IN_MILLIS);

        socketOutputStreamPool = new GenericKeyedObjectPool<HostAndPort, SocketOutputStream>(new SocketOutputStreamPoolFactory(socketConnectTimeoutInMillis), config);

        if (isEnabled()) {
            try {
                SocketOutputStream socketOutputStream = socketOutputStreamPool.borrowObject(graphiteServerHostAndPort);
                socketOutputStreamPool.returnObject(graphiteServerHostAndPort, socketOutputStream);
            } catch (Exception e) {
                logger.warn("Test Connection: FAILURE to connect to Graphite server '{}'", graphiteServerHostAndPort, e);
            }
        }
        try {
            Class.forName("org.python.modules.cPickle");
        } catch (ClassNotFoundException e) {
            throw new EmbeddedJmxTransException("jython librarie is required by the " + getClass().getSimpleName() +
                    " but is not found in the classpath. Please add org.python:jython:2.5.3+ to the classpath.");
        }
    }

    /**
     * Send given metrics to the Graphite server.
     */
    @Override
    public void write(Iterable<QueryResult> results) {
        logger.debug("Export to '{}' results {}", graphiteServerHostAndPort, results);
        SocketOutputStream socketOutputStream = null;
        try {
            socketOutputStream = socketOutputStreamPool.borrowObject(graphiteServerHostAndPort);
            PyList list = new PyList();

            for (QueryResult result : results) {
                String metricName = metricPathPrefix + result.getName();
                int time = (int) result.getEpoch(TimeUnit.SECONDS);
                PyObject pyValue;
                if (result.getValue() instanceof Integer) {
                    pyValue = new PyInteger((Integer) result.getValue());
                } else if (result.getValue() instanceof Long) {
                    pyValue = new PyLong((Long) result.getValue());
                } else if (result.getValue() instanceof Float) {
                    pyValue = new PyFloat((Float) result.getValue());
                } else if (result.getValue() instanceof Double) {
                    pyValue = new PyFloat((Double) result.getValue());
                } else if (result.getValue() instanceof Date) {
                    pyValue = new PyLong(TimeUnit.SECONDS.convert(((Date) result.getValue()).getTime(), TimeUnit.MILLISECONDS));
                } else {
                    pyValue = new PyString(result.getValue().toString());
                }
                list.add(new PyTuple(new PyString(metricName), new PyTuple(new PyInteger(time), pyValue)));

                logger.debug("Export '{}': ", metricName, result);
            }

            PyString payload = cPickle.dumps(list);

            byte[] header = ByteBuffer.allocate(4).putInt(payload.__len__()).array();

            socketOutputStream.write(header);
            socketOutputStream.write(payload.toBytes());

            socketOutputStream.flush();
            socketOutputStreamPool.returnObject(graphiteServerHostAndPort, socketOutputStream);
        } catch (Exception e) {
            logger.warn("Failure to send result to graphite server '{}' with {}", graphiteServerHostAndPort, socketOutputStream, e);
            if (socketOutputStream != null) {
                try {
                    socketOutputStreamPool.invalidateObject(graphiteServerHostAndPort, socketOutputStream);
                } catch (Exception e2) {
                    logger.warn("Exception invalidating socketWriter connected to graphite server '{}': {}", graphiteServerHostAndPort, socketOutputStream, e2);
                }
            }
        }
    }

    /**
     * Close the {@link SocketWriter} pool.
     */
    @Override
    public void stop() throws Exception {
        logger.info("Stop GraphitePickleWriter connected to '{}' ...", graphiteServerHostAndPort);
        super.stop();
        socketOutputStreamPool.close();
    }
}
