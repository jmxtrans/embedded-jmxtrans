package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class OutputWriterSetTest {

    @Test
    public void should_StartAll_When_Empty() {
        OutputWriterSet target = new OutputWriterSet();
        target.startAll();
    }

    @Test
    public void should_StopAll_When_Empty() {
        OutputWriterSet target = new OutputWriterSet();
        target.startAll();
        target.stopAll();
    }

    @Test
    public void should_StopAll_When_Empty_And_Not_Started() {
        OutputWriterSet target = new OutputWriterSet();
        target.stopAll();
    }

    @Test
    public void should_Start_Only_Once_When_StartAll_Multiple_Times() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);

        target.startAll();
        target.startAll();

        verify(ow1, times(1)).start();
    }

    @Test
    public void should_Stop_Only_Once_When_StopAll_Multiple_Times() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);

        target.startAll();
        target.stopAll();
        target.stopAll();

        verify(ow1, times(1)).start();
        verify(ow1, times(1)).stop();
    }

    @Test
    public void should_write_on_all_enabled() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();

        OutputWriter ow2 = mock(OutputWriter.class);
        doReturn(false).when(ow2).isEnabled();

        OutputWriter ow3 = mock(OutputWriter.class);
        doReturn(true).when(ow3).isEnabled();

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);
        target.add(ow2);
        target.add(ow3);

        target.startAll();

        verify(ow1, times(1)).start();
        verify(ow2, times(1)).start();
        verify(ow2, times(1)).start();

        Iterable<QueryResult> resultsToWrite = mock(Iterable.class);

        target.writeAll(resultsToWrite);

        verify(ow1, times(1)).write(eq(resultsToWrite));
        verify(ow2, times(0)).write(any(Iterable.class));
        verify(ow3, times(1)).write(eq(resultsToWrite));
    }


    @Test
    public void should_not_write_on_unsuccessfully_started_writers() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();
        doThrow(new RuntimeException("Fake Exception on ow1 start")).when(ow1).start();

        OutputWriter ow2 = mock(OutputWriter.class);
        doReturn(false).when(ow2).isEnabled();
        doThrow(new RuntimeException("Fake Exception on ow2 start")).when(ow2).start();

        OutputWriter ow3 = mock(OutputWriter.class);
        doReturn(true).when(ow3).isEnabled();

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);
        target.add(ow2);
        target.add(ow3);

        try {
            target.startAll();
            fail("Expected to throw an Exception");
        } catch (OutputWriterSet.OutputWriterSetStartException e) {
            assertEquals("Failed to start all of the OutputWriters, got 2 failures", e.getMessage());
        }

        verify(ow1, times(1)).start();
        verify(ow2, times(1)).start();
        verify(ow2, times(1)).start();

        Iterable<QueryResult> resultsToWrite = mock(Iterable.class);

        target.writeAll(resultsToWrite);

        verify(ow1, times(0)).write(eq(resultsToWrite));
        verify(ow2, times(0)).write(any(Iterable.class));
        verify(ow3, times(1)).write(eq(resultsToWrite));
    }

    @Test
    public void should_not_write_on_stopped_writers() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();

        OutputWriter ow2 = mock(OutputWriter.class);
        doReturn(true).when(ow2).isEnabled();

        OutputWriter ow3 = mock(OutputWriter.class);
        doReturn(true).when(ow3).isEnabled();

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);
        target.add(ow2);
        target.add(ow3);

        target.startAll();
        target.stopAll();

        verify(ow1, times(1)).start();
        verify(ow2, times(1)).start();
        verify(ow2, times(1)).start();

        verify(ow1, times(1)).stop();
        verify(ow2, times(1)).stop();
        verify(ow2, times(1)).stop();

        Iterable<QueryResult> resultsToWrite = mock(Iterable.class);

        target.writeAll(resultsToWrite);

        verify(ow1, times(0)).write(eq(resultsToWrite));
        verify(ow2, times(0)).write(any(Iterable.class));
        verify(ow3, times(0)).write(eq(resultsToWrite));
    }

    @Test
    public void should_write_on_all_writers_even_when_others_throw_exceptions() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();
        doThrow(new RuntimeException("Fake Exception on ow1 write")).when(ow1).write(any(Iterable.class));

        OutputWriter ow2 = mock(OutputWriter.class);
        doReturn(true).when(ow2).isEnabled();
        doThrow(new RuntimeException("Fake Exception on ow2 write")).when(ow2).write(any(Iterable.class));

        OutputWriter ow3 = mock(OutputWriter.class);
        doReturn(true).when(ow3).isEnabled();

        OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);
        target.add(ow2);
        target.add(ow3);

        target.startAll();

        Iterable<QueryResult> resultsToWrite = mock(Iterable.class);
        try {
            target.writeAll(resultsToWrite);
            fail("Expected to throw an Exception");
        } catch (OutputWriterSet.OutputWriterSetWriteException e) {
            assertEquals("Failed to write to all of the OutputWriters, got 2 failures", e.getMessage());
        }

        verify(ow1, times(1)).write(eq(resultsToWrite));
        verify(ow2, times(1)).write(eq(resultsToWrite));
        verify(ow3, times(1)).write(eq(resultsToWrite));
    }


    @Test
    public void should_support_concurrent_writes_fast() throws Exception {
        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();

        final OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);

        target.startAll();

        verify(ow1, times(1)).start();

        final Iterable<QueryResult> resultsToWrite = mock(Iterable.class);

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                target.writeAll(resultsToWrite);
                countDownLatch.countDown();
            }
        };

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread1.start();
        thread2.start();

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(ow1, times(2)).write(eq(resultsToWrite));
    }

    @Test
    public void should_support_concurrent_writes_slow() throws Exception {
        final long writeDuration = 2000;

        OutputWriter ow1 = mock(OutputWriter.class);
        doReturn(true).when(ow1).isEnabled();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Iterable<QueryResult> queryResults = (Iterable<QueryResult>) invocationOnMock.getArguments()[0];

                // simulate a real long write by sleeping for a long time
                TimeUnit.MILLISECONDS.sleep(writeDuration);

                AtomicInteger size = new AtomicInteger(0);
                for (QueryResult qr : queryResults) {
                    size.incrementAndGet();
                }
                return size.get();
            }
        }).when(ow1).write(anyListOf(QueryResult.class));

        final OutputWriterSet target = new OutputWriterSet();
        target.add(ow1);

        target.startAll();

        verify(ow1, times(1)).start();

        final Iterable<QueryResult> resultsToWrite = mock(Iterable.class);
        doReturn(new ArrayList<QueryResult>().iterator()).when(resultsToWrite).iterator();

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        final AtomicInteger writeAllInvocationSuccessfullyFinishedCounter = new AtomicInteger(0);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                target.writeAll(resultsToWrite);
                writeAllInvocationSuccessfullyFinishedCounter.incrementAndGet();
                countDownLatch.countDown();
            }
        };

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        thread1.start();
        thread2.start();

        // the time taken by the 2 concurrent writes should be about the same as for 1 single threaded write
        // we just add a safety margin of 100 milliseconds so that the test does not fail randomly when threads are slow to start
        countDownLatch.await(100 + writeDuration, TimeUnit.MILLISECONDS);

        assertEquals(2, writeAllInvocationSuccessfullyFinishedCounter.get());
        verify(ow1, times(2)).write(eq(resultsToWrite));
    }


}
