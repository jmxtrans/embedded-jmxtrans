package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriter;
import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.plumbing.ArrayListQueryResultSink;
import org.jmxtrans.embedded.util.plumbing.QueryResultSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * this class holds a state in order to be able to continue on the last buffer in the event of a failure during the last export
 */
public abstract class QueryResultsExporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicInteger exportedMetricsCount = new AtomicInteger();

    private final AtomicLong exportDurationInNanos = new AtomicLong();

    private final AtomicInteger exportCount = new AtomicInteger();

    private final ReentrantLock lock = new ReentrantLock();

    private volatile ArrayListQueryResultSink lastQueueCheckout;

    public QueryResultsExporter() {
        super();
    }

    protected abstract OutputWriterSet[] getOutputWriterSets();

    protected abstract int getExportBatchSize();

    protected abstract QueryResultSource getQueryResultSource();

    /**
     * Export the collected metrics to the {@linkplain OutputWriter}s associated with this {@linkplain Query}
     * Metrics are batched according to {@link EmbeddedJmxTrans#getExportBatchSize()}
     *
     * @return the number of exported {@linkplain QueryResult}
     */
    public int exportCollectedMetrics() {
        if (lock.isLocked()) {
            // an export for this Query is already in progress
            // there is no need for multiple threads to export the same Query
            // this should never occurs, but just in case it does, it's better returning now, to avoid blocking the current Thread until the other Thread finish
            return 0;
        }
        lock.lock();
        try {
            long nanosBefore = System.nanoTime();
            int successfullyExportedMetricsCount = doExportCollectedMetrics();
            exportDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
            exportCount.incrementAndGet();
            return successfullyExportedMetricsCount;
        } finally {
            lock.unlock();
        }
    }

    public int doExportCollectedMetrics() {
        final int exportBatchSize = getExportBatchSize();
        final QueryResultSource localQueryResultSource = getQueryResultSource();
        return doExportCollectedMetrics(localQueryResultSource, exportBatchSize);
    }

    public int doExportCollectedMetrics(final QueryResultSource localQueryResultSource, final int exportBatchSize) {
        int successfullyExportedMetricsCount = 0;

        ArrayListQueryResultSink queueCheckout = lastQueueCheckout;
        successfullyExportedMetricsCount += recoverLastExportIfNeeded(queueCheckout);

        // instantiate a new List in order to make sure the current Thread owns it, and in order to avoid making it a long-live Object
        queueCheckout = new ArrayListQueryResultSink(exportBatchSize);
        lastQueueCheckout = queueCheckout;

        int size;
        while ((size = localQueryResultSource.drainTo(queueCheckout, exportBatchSize)) > 0) {
            if (Thread.interrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
            Collection<QueryResult> queryResults = queueCheckout.toCollection();
            logger.debug("Trying to write {} results", queryResults.size());
            OutputWriterSet.tryToWriteQueryResultsBatchToAll(queryResults, getOutputWriterSets());
            successfullyExportedMetricsCount += size;
            exportedMetricsCount.addAndGet(size);
            queueCheckout.clear();
        }

        lastQueueCheckout = null;

        return successfullyExportedMetricsCount;
    }


    protected int recoverLastExportIfNeeded(ArrayListQueryResultSink queueCheckout) {
        // if the last invocation failed for any reason, then we should retry the latest export attempt
        // this ensure we do not lose the content of the buffer that was already drain out of the queryResults Queue
        // in the worse case, we may send again the metrics to OutputWriter, we assume it's ok  
        if (queueCheckout != null) {
            final int size = queueCheckout.size();
            if (size > 0) {
                Collection<QueryResult> queryResults = queueCheckout.toCollection();
                logger.debug("Trying to write {} results (recovery)", queryResults.size());
                OutputWriterSet.tryToWriteQueryResultsBatchToAll(queryResults, getOutputWriterSets());
                exportedMetricsCount.addAndGet(size);
                return size;
            }
        }
        return 0;
    }

    public int getExportedMetricsCount() {
        return exportedMetricsCount.get();
    }

    public long getExportDurationInNanos() {
        return exportDurationInNanos.get();
    }

    public int getExportCount() {
        return exportCount.get();
    }

}
