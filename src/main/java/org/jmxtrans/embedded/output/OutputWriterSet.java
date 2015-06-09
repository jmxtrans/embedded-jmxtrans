package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static void tryToWriteQueryResultsBatchToAll(List<QueryResult> queryResults, OutputWriterSet... outputWriterSets) {
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

    public OutputWriterSet() {
        // Use of LinkedHashMap to deduplicate during configuration merger, while maintaining declaration order
        outputWritersWithState = new LinkedHashMap<OutputWriter, AtomicBoolean>();
    }

    public void addAll(Collection<OutputWriter> collection) {
        for (OutputWriter ow : collection) {
            add(ow);
        }
    }

    public void add(OutputWriter outputWriter) {
        this.outputWritersWithState.put(outputWriter, new AtomicBoolean(false));
    }

    public void remove(OutputWriter outputWriter) {
        this.outputWritersWithState.remove(outputWriter);
    }

    public void clear() {
        this.outputWritersWithState.clear();
    }

    public synchronized void startAll() {
        int errorCount = 0;
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
        if (errorCount > 0) {
            throw new OutputWriterSetStartException("Failed to start all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    public synchronized void stopAll() {
        int errorCount = 0;
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
        if (errorCount > 0) {
            throw new OutputWriterSetStopException("Failed to stop all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    public int size() {
        return this.outputWritersWithState.size();
    }

    public Collection<OutputWriter> findEnabled() {
        // Google Guava predicates would be nicer but we don't include guava to ease embeddability
        List<OutputWriter> result = new ArrayList<OutputWriter>();
        for (OutputWriter ow : outputWritersWithState.keySet()) {
            if (ow.isEnabled()) {
                result.add(ow);
            }
        }
        return result;
    }

    public synchronized void writeAll(Iterable<QueryResult> results) {
        if (!globallyStarted.get()) {
            // should we throw an Exception or just log a warning?
            logger.warn("OutputWriters are stopped, QueryResult may not be written");
            //throw new IllegalStateException("OutputWriters are stopped, should not write");
        }
        int errorCount = 0;
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
        if (errorCount > 0) {
            throw new OutputWriterSetWriteException("Failed to write to all of the OutputWriters, got " + errorCount + " failures", errorCount);
        }
    }

    @Override
    public Iterator<OutputWriter> iterator() {
        return Collections.unmodifiableCollection(outputWritersWithState.keySet()).iterator();
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
