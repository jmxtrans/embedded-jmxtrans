package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OutputWriterSet implements Iterable {

    public static class OutputWriterSetWriteException extends RuntimeException {
        private final int errorCount;

        public OutputWriterSetWriteException(String message, int errorCount) {
            super(message);
            this.errorCount = errorCount;
        }

        public int getErrorCount() {
            return errorCount;
        }
    }

    public static class OutputWriterSetStartException extends RuntimeException {
        private final int errorCount;

        public OutputWriterSetStartException(String message, int errorCount) {
            super(message);
            this.errorCount = errorCount;
        }

        public int getErrorCount() {
            return errorCount;
        }
    }

    public static class OutputWriterSetStopException extends RuntimeException {
        private final int errorCount;

        public OutputWriterSetStopException(String message, int errorCount) {
            super(message);
            this.errorCount = errorCount;
        }

        public int getErrorCount() {
            return errorCount;
        }
    }

    public static void tryToWriteQueryResultsBatchToAll(Collection<QueryResult> queryResults, OutputWriterSet... outputWriterSets) {
        int totalErrorCount = 0;
        for (OutputWriterSet owSet : outputWriterSets) {
            try {
                owSet.writeAll(queryResults);
            } catch (OutputWriterSet.OutputWriterSetWriteException e) {
                totalErrorCount += e.getErrorCount();
            }
        }
        if (totalErrorCount > 0) {
            throw new OutputWriterSetWriteException("Failed to write to all of the OutputWriters, got " + totalErrorCount + " failures", totalErrorCount);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<OutputWriter, AtomicBoolean> outputWritersWithState;

    private final AtomicBoolean globallyStarted = new AtomicBoolean(false);

    /**
     * Write lock must be used when the state is changed. Read lock must be used when the state is unchanged.
     * ReadWriteLock is better than using <code>synchronized methods</code>, because it allows multiple threads to invoke the <code>writeAll</code> method
     */
    private final ReadWriteLock readWriteLock;

    private final Lock readLock;

    private final Lock writeLock;

    public OutputWriterSet() {
        readWriteLock = new ReentrantReadWriteLock(true);
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();

        // Use of LinkedHashMap to deduplicate during configuration merger, while maintaining declaration order
        outputWritersWithState = new LinkedHashMap<OutputWriter, AtomicBoolean>();
    }

    public void addAll(Collection<OutputWriter> collection) {
        writeLock.lock();
        try {
            for (OutputWriter ow : collection) {
                innerAdd(ow);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void add(OutputWriter outputWriter) {
        writeLock.lock();
        try {
            innerAdd(outputWriter);
        } finally {
            writeLock.unlock();
        }
    }

    private void innerAdd(OutputWriter outputWriter) {
        this.outputWritersWithState.put(outputWriter, new AtomicBoolean(false));
    }

    public void remove(OutputWriter outputWriter) {
        writeLock.lock();
        try {
            this.outputWritersWithState.remove(outputWriter);
        } finally {
            writeLock.unlock();
        }
    }

    public void clear() {
        writeLock.lock();
        try {
            this.outputWritersWithState.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public void startAll() {
        int errorCount = 0;
        writeLock.lock();
        try {
            for (Map.Entry<OutputWriter, AtomicBoolean> entry : outputWritersWithState.entrySet()) {
                AtomicBoolean started = entry.getValue();
                if (!started.get()) {
                    OutputWriter ow = entry.getKey();
                    boolean startSuccessful = tryStart(ow);
                    if (startSuccessful) {
                        started.set(true);
                    } else {
                        errorCount++;
                    }
                }
            }
            globallyStarted.set(true);
        } finally {
            writeLock.unlock();
        }
        if (errorCount > 0) {
            throw new OutputWriterSetStartException("Failed to start all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    public void stopAll() {
        int errorCount = 0;
        writeLock.lock();
        try {
            for (Map.Entry<OutputWriter, AtomicBoolean> entry : outputWritersWithState.entrySet()) {
                AtomicBoolean started = entry.getValue();
                if (started.get()) {
                    OutputWriter ow = entry.getKey();
                    boolean stopSuccessful = tryStop(ow);
                    if (stopSuccessful) {
                        started.set(false);
                    } else {
                        errorCount++;
                    }
                }
            }
            globallyStarted.set(false);
        } finally {
            writeLock.unlock();
        }
        if (errorCount > 0) {
            throw new OutputWriterSetStopException("Failed to stop all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    public int size() {
        readLock.lock();
        try {
            return this.outputWritersWithState.size();
        } finally {
            readLock.unlock();
        }
    }

    public Collection<OutputWriter> findEnabled() {
        readLock.lock();
        try {
            // Google Guava predicates would be nicer but we don't include guava to ease embeddability
            List<OutputWriter> result = new ArrayList<OutputWriter>();
            for (OutputWriter ow : outputWritersWithState.keySet()) {
                if (ow.isEnabled()) {
                    result.add(ow);
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    public void writeAll(Iterable<QueryResult> results) {
        int errorCount = 0;
        readLock.lock();
        try {
            if (!globallyStarted.get()) {
                // should we throw an Exception or just log a warning?
                logger.warn("OutputWriters are stopped, QueryResult may not be written");
                //throw new IllegalStateException("OutputWriters are stopped, should not write");
            }
            for (Map.Entry<OutputWriter, AtomicBoolean> entry : outputWritersWithState.entrySet()) {
                OutputWriter ow = entry.getKey();
                AtomicBoolean started = entry.getValue();
                if (ow.isEnabled() && started.get()) {
                    boolean writeSuccessful = tryWrite(ow, results);
                    if (!writeSuccessful) {
                        errorCount++;
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        if (errorCount > 0) {
            throw new OutputWriterSetWriteException("Failed to write to all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    @Override
    public Iterator<OutputWriter> iterator() {
        readLock.lock();
        try {
            return Collections.unmodifiableCollection(outputWritersWithState.keySet()).iterator();
        } finally {
            readLock.unlock();
        }
    }

    private boolean tryStart(OutputWriter ow) {
        try {
            ow.start();
            return true;
        } catch (Exception e) {
            logger.warn("Failed to start OutputWriter {}", ow, e);
            return false;
        }
    }

    private boolean tryStop(OutputWriter ow) {
        try {
            ow.stop();
            return true;
        } catch (Exception e) {
            logger.warn("Failed to stop OutputWriter {}", ow, e);
            return false;
        }
    }

    private boolean tryWrite(OutputWriter ow, Iterable<QueryResult> results) {
        try {
            ow.write(results);
            return true;
        } catch (RuntimeException e) {
            logger.warn("Failed to write with OutputWriter {}", ow, e);
            return false;
        }
    }

}
