/*
 * Copyright (c) 2010-2015 the original author or authors
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

import org.jmxtrans.embedded.QueryResult;
import org.jmxtrans.embedded.util.CachingReference;
import org.jmxtrans.embedded.util.StringUtils2;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * {@linkplain OutputWriter} for [StatsD](https://github.com/etsy/statsd).
 *
 * @author Fabiano Vicente
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 * @since 1.0.15
 */
@ImplicitEpoch
public class StatsDWriter extends AbstractOutputWriter implements OutputWriter {

    public final static String SETTING_BUFFER_SIZE = "bufferSize";
    private final static int SETTING_DEFAULT_BUFFER_SIZE = 1024;
    public static final String DEFAULT_NAME_PREFIX = "servers.#escaped_hostname#.";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private ByteBuffer sendBuffer;
    /**
     * Using a {@link CachingReference} instead of a raw {@link InetSocketAddress} allows to handle a change
     */
    private CachingReference<InetSocketAddress> addressReference;
    private DatagramChannel channel;

    /**
     * Metric path prefix. Ends with "." if not empty;
     */
    private String metricPathPrefix;

    @Override
    public synchronized void start() {
        final String host = getStringSetting(SETTING_HOST);
        final Integer port = getIntSetting(SETTING_PORT);
        metricPathPrefix = getStringSetting(SETTING_NAME_PREFIX, DEFAULT_NAME_PREFIX);
        metricPathPrefix = getStrategy().resolveExpression(metricPathPrefix);
        if (!metricPathPrefix.isEmpty() && !metricPathPrefix.endsWith(".")) {
            metricPathPrefix = metricPathPrefix + ".";
        }

        int bufferSize = getIntSetting(SETTING_BUFFER_SIZE, SETTING_DEFAULT_BUFFER_SIZE);
        sendBuffer = ByteBuffer.allocate(bufferSize);

        addressReference = new CachingReference<InetSocketAddress>(30, TimeUnit.SECONDS) {
            @Nonnull
            @Override
            protected InetSocketAddress newObject() {
                return new InetSocketAddress(host, port);
            }
        };
        try {
            channel = DatagramChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("Exception opening datagram channel", e);
        }

        logger.info("Started {}", this);
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        channel.close();
    }

    @Override
    public synchronized void write(Iterable<QueryResult> results) {
        logger.debug("Export to {} results {}", addressReference.get(), results);
        for (QueryResult result : results) {

            String stat = metricPathPrefix + result.getName() + ":" + result.getValue() + "|" + getStatsdMetricType(result) + "\n";

            logger.debug("Export '{}'", stat);
            final byte[] data = stat.getBytes(UTF_8);

            // If we're going to go past the threshold of the buffer then flush.
            // the +1 is for the potential '\n' in multi_metrics below
            if (sendBuffer.remaining() < (data.length + 1)) {
                flush();
            }

            if (sendBuffer.remaining() < (data.length + 1)) {
                notifyDataTooBig(stat, data);
                continue;
            }

            sendBuffer.put(data); // append the data

        }
        flush();
    }

    protected void notifyDataTooBig(String stat, byte[] data) {
        logger.warn("Given data too big (" + data.length + "bytes) for the buffer size (" + sendBuffer.remaining() + "bytes), skip it: "
                + StringUtils2.abbreviate(stat, 20));
    }

    public synchronized void flush() {
        final int sizeOfBuffer = sendBuffer.position();
        if (sizeOfBuffer == 0) {
            // empty buffer
            return;
        }
        InetSocketAddress address = addressReference.get();
        try {
            // send and reset the buffer
            sendBuffer.flip();
            final int nbSentBytes = channel.send(sendBuffer, address);

            if (sizeOfBuffer != nbSentBytes) {
                logger.warn("Could not send entirely stat {} to host {}:{}. Only sent {} bytes out of {} bytes",
                        sendBuffer, address.getHostName(), address.getPort(), nbSentBytes, sizeOfBuffer);
            }
        } catch (IOException e) {
            addressReference.purge();
            logger.warn("Could not send stat {} to host {}:{}", sendBuffer, address.getHostName(), address.getPort(), e);
        } finally {
            sendBuffer.clear();
        }
    }

    /**
     * See <a href="https://github.com/etsy/statsd/blob/master/docs/metric_types.md">StatsD Metric Types</a>.
     *
     * @param result
     * @return StatsD metric type such as 'c', 'g' or 's'. Default to 'c' if not known, return the metric type itself if its length == 1
     */
    protected char getStatsdMetricType(@Nonnull QueryResult result) {
        String metricType = result.getType();
        char statsBucketType;
        if(metricType == null || metricType.equalsIgnoreCase("counter")) {
            statsBucketType = 'c';
        } else if(metricType.equalsIgnoreCase("gauge")) {
            statsBucketType = 'g';
        } else if (metricType.equalsIgnoreCase("set")) {
            statsBucketType = 's';
        } else if(metricType.length() == 1) {
            statsBucketType = metricType.charAt(0);
        } else {
            logger.warn("Unknown metric type for {}, default to 'c'", result);
            statsBucketType = 'c';
        }
        return statsBucketType;
    }

    @Override
    public String toString() {
        return "StatsDOutputWriter{" +
                "addressReference=" + addressReference +
                ", metricPathPrefix='" + metricPathPrefix + '\'' +
                ", sendBuffer=" + sendBuffer +
                '}';
    }

    public synchronized void setChannel(DatagramChannel channel) {
        this.channel = channel;
    }
}
