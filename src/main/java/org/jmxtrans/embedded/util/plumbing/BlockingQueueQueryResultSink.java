package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;
import org.jmxtrans.embedded.util.concurrent.DiscardingBlockingQueue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueQueryResultSink implements QueryResultSink, QueryResultSource {

    private BlockingQueue<QueryResult> collection = new DiscardingBlockingQueue<QueryResult>(200);

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
     * or <code>-1</code> if the queue is not a {@link DiscardingBlockingQueue}.
     */
    public int getDiscardedResultsCount() {
        if (collection instanceof DiscardingBlockingQueue) {
            DiscardingBlockingQueue discardingBlockingQueue = (DiscardingBlockingQueue) collection;
            return discardingBlockingQueue.getDiscardedElementCount();
        } else {
            return -1;
        }
    }

    @Override
    public int drainTo(final QueryResultSink sink, int count) {
        return collection.drainTo(new QueryResultSinkToCollectionAdaptor(sink), count);
    }

    public int size() {
        return this.collection.size();
    }

    public QueryResult poll() {
        return collection.poll();
    }

    public BlockingQueue<QueryResult> getCollection() {
        return collection;
    }

    public void setCollection(BlockingQueue<QueryResult> collection) {
        this.collection = collection;
    }

}
