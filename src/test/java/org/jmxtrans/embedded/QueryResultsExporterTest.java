package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.plumbing.ArrayListQueryResultSink;
import org.jmxtrans.embedded.util.plumbing.BlockingQueueQueryResultSink;
import org.jmxtrans.embedded.util.plumbing.QueryResultSink;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

public class QueryResultsExporterTest {

    private final ThrowsException defaultMockAnswer = new ThrowsException(new UnsupportedOperationException());

    @Test
    public void shouldSupportAndReturnZeroWhenQueueIsEmpty() {
        final int maxQueueDrainResponse = 0;
        checkSuccessfulExport(1, maxQueueDrainResponse);
    }

    @Test
    public void shouldExportWhenDrainReturnsSingleItemOnce() {
        final int maxQueueDrainResponse = 1;
        checkSuccessfulExport(1, maxQueueDrainResponse);
    }

    @Test
    public void shouldExportWhenDrainReturnsMultipleItemsOnce() {
        final int maxQueueDrainResponse = 1;
        checkSuccessfulExport(7, maxQueueDrainResponse);
    }

    @Test
    public void shouldExportWhenDrainReturnsSingleItemMultipleTimes() {
        final int maxQueueDrainResponse = 10;
        checkSuccessfulExport(1, maxQueueDrainResponse);
    }

    @Test
    public void shouldExportWhenDrainReturnsMultipleItemsMultipleTimes() {
        final int maxQueueDrainResponse = 10;
        checkSuccessfulExport(7, maxQueueDrainResponse);
    }

    private void checkSuccessfulExport(int numberOfResultsToAddPerInvocation, int maxQueueDrainResponse) {
        final Integer configuredBatchSize = 10;

        OutputWriterSet embeddedJmxTransOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(embeddedJmxTransOutputWriters).writeAll(any(Collection.class));

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doReturn(configuredBatchSize).when(embeddedJmxTrans).getExportBatchSize();
        doReturn(embeddedJmxTransOutputWriters).when(embeddedJmxTrans).getOutputWriters();

        BlockingQueueQueryResultSink queueResults = mock(BlockingQueueQueryResultSink.class, defaultMockAnswer);
        doAnswer(new DrainToAnswer(configuredBatchSize, numberOfResultsToAddPerInvocation, maxQueueDrainResponse)).when(queueResults).drainTo(any(QueryResultSink.class), anyInt());

        OutputWriterSet queryOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(queryOutputWriters).writeAll(any(Collection.class));

        Query query = mock(Query.class, defaultMockAnswer);
        doReturn(embeddedJmxTrans).when(query).getEmbeddedJmxTrans();
        doReturn(queueResults).when(query).getQueryResultSink();
        doReturn(queueResults).when(query).getQueryResultSource();
        doReturn(queryOutputWriters).when(query).getOutputWriters();

        QueryResultsExporter target = new QuerySpecificQueryResultsExporter(query, true);
        int exportedCount = target.exportCollectedMetrics();
        assertEquals(maxQueueDrainResponse * numberOfResultsToAddPerInvocation, exportedCount);

        verify(embeddedJmxTransOutputWriters, times(maxQueueDrainResponse)).writeAll(anyListOf(QueryResult.class));
        verify(queryOutputWriters, times(maxQueueDrainResponse)).writeAll(anyListOf(QueryResult.class));
    }

    @Test
    public void shouldRecoverFromPreviousWriteFailureOnGlobalWriters() {
        final Integer configuredBatchSize = 10;
        final int numberOfResultsToAddPerInvocation = 1 + new Random().nextInt(9); // whatever value > 1
        final int maxQueueDrainResponse = 1 + new Random().nextInt(9); // whatever value > 1

        OutputWriterSet.OutputWriterSetWriteException expectedException = new OutputWriterSet.OutputWriterSetWriteException("this is a fake Exception when writing", 1);

        OutputWriterSet embeddedJmxTransOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doThrow(expectedException).when(embeddedJmxTransOutputWriters).writeAll(any(Collection.class));

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doReturn(configuredBatchSize).when(embeddedJmxTrans).getExportBatchSize();
        doReturn(embeddedJmxTransOutputWriters).when(embeddedJmxTrans).getOutputWriters();

        BlockingQueueQueryResultSink queueResults = mock(BlockingQueueQueryResultSink.class, defaultMockAnswer);
        doAnswer(new DrainToAnswer(configuredBatchSize, numberOfResultsToAddPerInvocation, maxQueueDrainResponse)).when(queueResults).drainTo(any(QueryResultSink.class), anyInt());

        OutputWriterSet queryOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(queryOutputWriters).writeAll(any(Collection.class));

        Query query = mock(Query.class, defaultMockAnswer);
        doReturn(embeddedJmxTrans).when(query).getEmbeddedJmxTrans();
        doReturn(queueResults).when(query).getQueryResultSink();
        doReturn(queueResults).when(query).getQueryResultSource();
        doReturn(queryOutputWriters).when(query).getOutputWriters();

        QueryResultsExporter target = new QuerySpecificQueryResultsExporter(query, true);
        try {
            target.exportCollectedMetrics();
            fail();
        } catch (OutputWriterSet.OutputWriterSetWriteException e) {
            // this is expected
            assertEquals("Failed to write to all of the OutputWriters, got 1 failures", e.getMessage());
        }

        doNothing().when(embeddedJmxTransOutputWriters).writeAll(any(Collection.class));

        int exportedCount = target.exportCollectedMetrics();
        assertEquals(maxQueueDrainResponse * numberOfResultsToAddPerInvocation, exportedCount);

        verify(embeddedJmxTransOutputWriters, times(maxQueueDrainResponse + 1)).writeAll(anyListOf(QueryResult.class));
        verify(queryOutputWriters, times(maxQueueDrainResponse + 1)).writeAll(anyListOf(QueryResult.class));
    }


    @Test
    public void shouldRecoverFromPreviousWriteFailureOnQueryWriters() {
        final Integer configuredBatchSize = 10;
        final int numberOfResultsToAddPerInvocation = 1 + new Random().nextInt(9); // whatever value > 1
        final int maxQueueDrainResponse = 1 + new Random().nextInt(9); // whatever value > 1

        OutputWriterSet embeddedJmxTransOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(embeddedJmxTransOutputWriters).writeAll(any(Collection.class));

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doReturn(configuredBatchSize).when(embeddedJmxTrans).getExportBatchSize();
        doReturn(embeddedJmxTransOutputWriters).when(embeddedJmxTrans).getOutputWriters();

        BlockingQueueQueryResultSink queueResults = mock(BlockingQueueQueryResultSink.class, defaultMockAnswer);
        doAnswer(new DrainToAnswer(configuredBatchSize, numberOfResultsToAddPerInvocation, maxQueueDrainResponse)).when(queueResults).drainTo(any(QueryResultSink.class), anyInt());

        OutputWriterSet.OutputWriterSetWriteException expectedException = new OutputWriterSet.OutputWriterSetWriteException("this is a fake Exception when writing", 1);

        OutputWriterSet queryOutputWriters = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(queryOutputWriters).writeAll(any(Collection.class));
        doThrow(expectedException).when(queryOutputWriters).writeAll(any(Collection.class));

        Query query = mock(Query.class, defaultMockAnswer);
        doReturn(embeddedJmxTrans).when(query).getEmbeddedJmxTrans();
        doReturn(queueResults).when(query).getQueryResultSink();
        doReturn(queueResults).when(query).getQueryResultSource();
        doReturn(queryOutputWriters).when(query).getOutputWriters();

        QueryResultsExporter target = new QuerySpecificQueryResultsExporter(query, true);
        try {
            target.exportCollectedMetrics();
            fail();
        } catch (OutputWriterSet.OutputWriterSetWriteException e) {
            // this is expected
            assertEquals("Failed to write to all of the OutputWriters, got 1 failures", e.getMessage());
        }

        doNothing().when(queryOutputWriters).writeAll(any(Collection.class));

        int exportedCount = target.exportCollectedMetrics();
        assertEquals(maxQueueDrainResponse * numberOfResultsToAddPerInvocation, exportedCount);

        verify(embeddedJmxTransOutputWriters, times(maxQueueDrainResponse + 1)).writeAll(anyListOf(QueryResult.class));
        verify(queryOutputWriters, times(maxQueueDrainResponse + 1)).writeAll(anyListOf(QueryResult.class));
    }


    private static class DrainToAnswer implements Answer {
        private final int configuredBatchSize;
        private final int numberOfResultsToAddPerInvocation;
        private final int maxInvocationCount;
        private final AtomicInteger invocationCount = new AtomicInteger(0);

        public DrainToAnswer(int configuredBatchSize, int numberOfResultsToAddPerInvocation, int maxInvocationCount) {
            this.configuredBatchSize = configuredBatchSize;
            this.numberOfResultsToAddPerInvocation = numberOfResultsToAddPerInvocation;
            this.maxInvocationCount = maxInvocationCount;
            assertTrue(numberOfResultsToAddPerInvocation < configuredBatchSize);
        }

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            ArrayListQueryResultSink buffer = (ArrayListQueryResultSink) invocationOnMock.getArguments()[0];
            Integer batchSize = (Integer) invocationOnMock.getArguments()[1];
            assertNotNull(buffer);
            assertTrue(buffer.isEmpty());
            assertEquals(Integer.valueOf(configuredBatchSize), batchSize);

            invocationCount.incrementAndGet();
            if (invocationCount.get() <= maxInvocationCount) {
                for (int i = 0; i < numberOfResultsToAddPerInvocation; i++) {
                    buffer.accept(new QueryResult("foo-" + i, "bar", 12345));
                }
            }
            return buffer.size();
        }
    }
}
