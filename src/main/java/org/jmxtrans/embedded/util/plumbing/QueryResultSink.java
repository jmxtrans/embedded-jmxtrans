package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;

import java.util.Collection;

public interface QueryResultSink {

    void accept(QueryResult queryResult);

    void accept(Collection<QueryResult> queryResults);

    int getDiscardedResultsCount();

}
