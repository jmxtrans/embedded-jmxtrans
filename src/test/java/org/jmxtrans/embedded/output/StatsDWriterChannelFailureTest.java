package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StatsDWriterChannelFailureTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final int bufferSize = 1000;

    private StatsDWriter statsdWriter;

    private DatagramChannel channel;

    @Before
    public void before() throws IOException {
        logger.info("started");

        channel = mock(DatagramChannel.class);
        doAnswer(new CustomSendMethodAnswer()).when(channel).send(any(ByteBuffer.class), any(SocketAddress.class));

        statsdWriter = spy(new StatsDWriter());
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put(AbstractOutputWriter.SETTING_HOST, "whatever");
        settings.put(AbstractOutputWriter.SETTING_PORT, 8125);
        settings.put(StatsDWriter.SETTING_BUFFER_SIZE, bufferSize);
        settings.put(StatsDWriter.SETTING_NAME_PREFIX, "");
        statsdWriter.setSettings(settings);

        statsdWriter.start();

        statsdWriter.setChannel(channel);
    }

    @After
    public void after() throws Exception {
        logger.info("Finished");
    }

    @Test
    public void testWithBigResults_SmallerThanBufferSize() throws Exception {
        final int count = 4;
        Collection<QueryResult> results = new ArrayList<QueryResult>();
        for (int i = 0; i < count; i++) {
            QueryResult queryResult = new QueryResult(createRandomString(bufferSize / (count + 1)), 10, System.currentTimeMillis());
            results.add(queryResult);
        }
        statsdWriter.write(results);

        verify(statsdWriter, times(0)).notifyDataTooBig(anyString(), any(byte[].class));
        verify(channel, times(1)).send(any(ByteBuffer.class), any(SocketAddress.class));
    }


    @Test
    public void testWithBigResults_a_little_bit_more_than_1_buffersize() throws Exception {
        final int count = 5;
        Collection<QueryResult> results = new ArrayList<QueryResult>();
        for (int i = 0; i < count; i++) {
            QueryResult queryResult = new QueryResult(createRandomString(bufferSize / count), 10, System.currentTimeMillis());
            results.add(queryResult);
        }
        statsdWriter.write(results);

        verify(statsdWriter, times(0)).notifyDataTooBig(anyString(), any(byte[].class));
        verify(channel, times(2)).send(any(ByteBuffer.class), any(SocketAddress.class));
    }


    @Test
    public void testWithBigResults_more_than_2_buffersize() throws Exception {
        final int count = 20;
        Collection<QueryResult> results = new ArrayList<QueryResult>();
        for (int i = 0; i < count; i++) {
            QueryResult queryResult = new QueryResult(createRandomString(bufferSize / (count / 4)), 10, System.currentTimeMillis());
            results.add(queryResult);
        }
        statsdWriter.write(results);

        verify(statsdWriter, times(0)).notifyDataTooBig(anyString(), any(byte[].class));
        verify(channel, times(5)).send(any(ByteBuffer.class), any(SocketAddress.class));
    }

    private static String createRandomString(int len) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    private static class CustomSendMethodAnswer implements Answer {

        private final AtomicInteger invocationCounter = new AtomicInteger(0);

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            int invocationCount = invocationCounter.incrementAndGet();
            if (invocationCount < 10) {
                throw new IOException("Fake IOException on invocation #" + invocationCount + " of DatagramChannel send");
            }
            ByteBuffer bytebuffer = (ByteBuffer) invocationOnMock.getArguments()[0];
            return bytebuffer.limit();
        }
    }

}
