package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayListQueryResultSink implements QueryResultSink {

    private final List<QueryResult> collection;

    public ArrayListQueryResultSink() {
        collection = new ArrayList<QueryResult>();
    }

    public ArrayListQueryResultSink(int initialCapacity) {
        collection = new ArrayList<QueryResult>(initialCapacity);
    }

    @Override
    public void accept(QueryResult queryResult) {
        collection.add(queryResult);
    }

    @Override
    public void accept(Collection<QueryResult> queryResults) {
        collection.addAll(queryResults);
    }

    /**
     * Returns the number of discarded elements in the {@link #collection} queue
     * or <code>-1</code> if the queue is not a {@link org.jmxtrans.embedded.util.concurrent.DiscardingBlockingQueue}.
     */
    public int getDiscardedResultsCount() {
        return -1;
    }

    public int size() {
        return this.collection.size();
    }

    public Collection<QueryResult> toCollection() {
        return Collections.unmodifiableCollection(collection);
    }

    public void clear() {
        this.collection.clear();
    }

    public boolean isEmpty() {
        return this.collection.isEmpty();
    }
}
