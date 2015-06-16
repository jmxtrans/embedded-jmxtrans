package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class MultiSourcesQueryResultSourceTest {

    private final ThrowsException defaultMockAnswer = new ThrowsException(new UnsupportedOperationException());

    @Test
    public void should_not_drain_when_requested_count_is_zero() {
        QueryResultSource s1 = null;
        QueryResultSource s2 = null;

        MultiSourcesQueryResultSource target = new MultiSourcesQueryResultSource();
        target.addSource(s1);
        target.addSource(s2);

        int count = 0;

        QueryResultSink sink = null;

        target.drainTo(sink, count);
    }

    @Test
    public void should_loop_over_original_sources_and_never_publish_when_all_sources_are_empty() {
        QueryResultSource s1 = mock(QueryResultSource.class, defaultMockAnswer);
        QueryResultSource s2 = mock(QueryResultSource.class, defaultMockAnswer);

        MultiSourcesQueryResultSource target = new MultiSourcesQueryResultSource();
        target.addSource(s1);
        target.addSource(s2);

        doReturn(0).when(s1).drainTo(any(QueryResultSink.class), anyInt());
        doReturn(0).when(s2).drainTo(any(QueryResultSink.class), anyInt());

        int count = 5;

        QueryResultSink sink = mock(QueryResultSink.class, defaultMockAnswer);

        target.drainTo(sink, count);

        verify(s1, times(1)).drainTo(same(sink), eq(count));
        verify(s2, times(1)).drainTo(same(sink), eq(count));
        verify(sink, never()).accept(any(QueryResult.class));
    }

    @Test
    public void should_stop_draining_original_source_when_requested_count_reached_and_restart_at_same_when_requested_again() {
        QueryResultSource s1 = mock(QueryResultSource.class, defaultMockAnswer);
        QueryResultSource s2 = mock(QueryResultSource.class, defaultMockAnswer);

        MultiSourcesQueryResultSource target = new MultiSourcesQueryResultSource();
        target.addSource(s1);
        target.addSource(s2);

        final int count = 5;

        doAnswer(new Answer() {

            int readyCount = count * 2;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                QueryResultSink sink = (QueryResultSink) invocationOnMock.getArguments()[0];
                Integer requestedCount = (Integer) invocationOnMock.getArguments()[1];
                int returnedCount = 0;
                for (int i = 0; i < requestedCount && readyCount > 0; i++) {
                    sink.accept(new QueryResult("foo-" + i, "bar", 12345));
                    returnedCount++;
                    readyCount--;
                }
                return requestedCount;
            }
        }).when(s1).drainTo(any(QueryResultSink.class), anyInt());
        doReturn(0).when(s2).drainTo(any(QueryResultSink.class), anyInt());


        QueryResultSink sink = mock(QueryResultSink.class, defaultMockAnswer);
        doNothing().when(sink).accept(any(QueryResult.class));

        final int drain1Result = target.drainTo(sink, count);
        assertEquals(count, drain1Result);

        verify(s1, times(1)).drainTo(same(sink), eq(count));
        verify(s2, never()).drainTo(same(sink), eq(count));
        verify(sink, times(count)).accept(any(QueryResult.class));

        final int drain2Result = target.drainTo(sink, 1);
        assertEquals(1, drain2Result);

        verify(s1, times(2)).drainTo(same(sink), anyInt());
        verify(s2, times(1)).drainTo(same(sink), anyInt());
        verify(sink, times(count + 1)).accept(any(QueryResult.class));
    }

}
