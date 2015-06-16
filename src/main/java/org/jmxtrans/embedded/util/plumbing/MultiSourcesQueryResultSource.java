package org.jmxtrans.embedded.util.plumbing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MultiSourcesQueryResultSource implements QueryResultSource {

    private final List<QueryResultSource> originalSources = new ArrayList<QueryResultSource>();

    private volatile int nextDrainIndex = 0;

    private final ReadWriteLock readWriteLock;

    private final Lock readLock;

    private final Lock writeLock;

    public MultiSourcesQueryResultSource() {
        super();
        readWriteLock = new ReentrantReadWriteLock(true);
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    public int drainTo(QueryResultSink sink, int count) {
        readLock.lock();
        try {
            int effectiveCount = 0;
            int currentIndex = nextDrainIndex;
            int remainingLoopCount = originalSources.size();

            int remainingCountToRequest = count;

            while (remainingCountToRequest > 0 && remainingLoopCount > 0) {
                QueryResultSource queryResultSource = originalSources.get(currentIndex);
                final int newlyDrainedCount = queryResultSource.drainTo(sink, remainingCountToRequest);
                remainingCountToRequest -= newlyDrainedCount;
                effectiveCount += newlyDrainedCount;
                currentIndex++;
                if (currentIndex >= originalSources.size()) {
                    currentIndex = 0;
                }
                nextDrainIndex = currentIndex;
                remainingLoopCount--;
            }

            return effectiveCount;
        } finally {
            readLock.unlock();
        }
    }

    public void addSource(QueryResultSource source) {
        writeLock.lock();
        try {
            this.originalSources.add(source);
        } finally {
            writeLock.unlock();
        }
    }

}
