package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DemuxQueryResultSink implements QueryResultSink {

    private final List<QueryResultSink> delegates = new ArrayList<QueryResultSink>();

    public DemuxQueryResultSink() {
        
    }

    public DemuxQueryResultSink(QueryResultSink... delegates) {
        addDelegates(delegates);
    }

    public void addDelegates(QueryResultSink... delegates) {
        for (QueryResultSink s : delegates) {
            addDelegate(s);
        }
    }

    public void addDelegate(QueryResultSink s) {
        delegates.add(s);
    }

    @Override
    public void accept(QueryResult queryResult) {
        for (QueryResultSink s : delegates) {
            s.accept(queryResult);
        }
    }

    @Override
    public void accept(Collection<QueryResult> queryResults) {
        for (QueryResultSink s : delegates) {
            s.accept(queryResults);
        }
    }

    @Override
    public int getDiscardedResultsCount() {
        // what should we do here?
        throw new UnsupportedOperationException();
    }

}
