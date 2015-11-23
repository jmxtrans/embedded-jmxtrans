/*
 * Copyright (c) 2010-2013 the original author or authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriter;
import org.jmxtrans.embedded.util.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <h2>JMX Queries</h2>
 *
 * If the JMX query returns several mbeans (thanks to '*' or '?' wildcards),
 * then the configured attributes are collected on all the returned mbeans.
 *
 * <h2>Output Writers</h2>
 *
 * {@linkplain OutputWriter}s can be defined at the query level or globally at the {@link EmbeddedJmxTrans} level.
 * The {@linkplain OutputWriter}s that are effective for a {@linkplain Query} are accessible
 * via {@link Query#getEffectiveOutputWriters()}
 *
 *
 * <h2>Collected Metrics / Query Results</h2>
 *
 * Default behavior is to store the query results at the query level (see {@linkplain Query#queryResults}) to resolve the
 * effective {@linkplain OutputWriter}s at result export time ({@linkplain org.jmxtrans.embedded.Query#getEffectiveOutputWriters()}).
 *
 * The drawback is to limit the benefits of batching result
 * to a backend (see {@link org.jmxtrans.embedded.Query#exportCollectedMetrics()}) and the size limit of the results list to prevent
 * {@linkplain OutOfMemoryError} in case of export slowness.
 *
 * An optimization would be, if only one {@linkplain OutputWriter} is defined in the whole {@linkplain EmbeddedJmxTrans}, to
 * replace all the query-local result queues by one global result-queue.
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Jon Stevens
 */
public class EmbeddedJmxTrans implements EmbeddedJmxTransMBean {

    public EmbeddedJmxTrans() {
        super();
    }

    public EmbeddedJmxTrans(MBeanServer mbeanServer) {
        super();
        this.mbeanServer = mbeanServer;
    }

    /**
     * Shutdown hook to collect and export metrics a last time if {@link EmbeddedJmxTrans#stop()} was not called.
     */
    private class EmbeddedJmxTransShutdownHook extends Thread {

        private boolean removed = false;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        public EmbeddedJmxTransShutdownHook() {
            setName(getClass().getSimpleName() + "-" + getName());
        }

        @Override
        public void run() {
            try {
                EmbeddedJmxTrans.this.stop();
            } catch(Exception e) {
                logger.warn("Exception shutting down", e);
            }
        }

        public void registerToRuntime() {
            Runtime.getRuntime().addShutdownHook(this);
        }

        public void unregisterFromRuntime() {
            if (removed) {
                logger.debug("Shutdown hook already removed");
            }
            try {
                boolean shutdownHookRemoved = Runtime.getRuntime().removeShutdownHook(this);
                if (shutdownHookRemoved) {
                    removed = true;
                    logger.debug("ShutdownHook successfully removed");
                } else {
                    logger.warn("Failure to remove ShutdownHook");
                }
            } catch (IllegalStateException e) {
                logger.debug("Failure to remove ShutdownHook, probably 'Shutdown in progress'", e);
            } catch (RuntimeException e) {
                logger.warn("Failure to remove ShutdownHook", e);
            }
        }

    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    enum State {STOPPED, STARTED, ERROR}

    private State state = State.STOPPED;

    private final ReadWriteLock lifecycleLock = new ReentrantReadWriteLock();

    private ScheduledExecutorService collectScheduledExecutor;

    private ScheduledExecutorService exportScheduledExecutor;

    @Nonnull
    private MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    @Nonnull
    private final List<Query> queries = new ArrayList<Query>();

    /**
     * Use a {@linkplain Set} to deduplicate during configuration merger
     */
    private Set<OutputWriter> outputWriters = new HashSet<OutputWriter>();

    private int numQueryThreads = 1;

    private int numExportThreads = 1;

    private int queryIntervalInSeconds = 30;

    private int exportIntervalInSeconds = 5;

    private int exportBatchSize = 50;

    private EmbeddedJmxTransShutdownHook shutdownHook;

    /**
     * Start the exporter: initialize underlying queries, start scheduled executors, register shutdown hook
     */
    @PostConstruct
    public void start() throws Exception {
        lifecycleLock.writeLock().lock();
        try {
            if (!State.STOPPED.equals(state)) {
                logger.warn("Ignore start() command for {} instance", state);
                return;
            }
            logger.info("Start...");

            for (Query query : queries) {
                query.start();
            }
            for (OutputWriter outputWriter : outputWriters) {
                outputWriter.start();
            }

            collectScheduledExecutor = Executors.newScheduledThreadPool(getNumQueryThreads(), new NamedThreadFactory("jmxtrans-collect-", true));
            exportScheduledExecutor = Executors.newScheduledThreadPool(getNumExportThreads(), new NamedThreadFactory("jmxtrans-export-", true));

            logger.info("Start queries and output writers...");
            for (final Query query : getQueries()) {
                collectScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        lifecycleLock.readLock().lock();
                        try {
                            if (!State.STARTED.equals(state)) {
                                logger.debug("Ignore query.collectMetrics() command for {} instance", state);
                                return;
                            }
                            query.collectMetrics();
                        } finally {
                            lifecycleLock.readLock().unlock();
                        }
                    }

                    @Override
                    public String toString() {
                        return "Collector[" + query + "]";
                    }
                }, 0, getQueryIntervalInSeconds(), TimeUnit.SECONDS);

                // start export just after first collect
                exportScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        lifecycleLock.readLock().lock();
                        try {
                            if (!State.STARTED.equals(state)) {
                                logger.debug("Ignore query.exportCollectedMetrics() command for {} instance", state);
                                return;
                            }

                            query.exportCollectedMetrics();
                        } finally {
                            lifecycleLock.readLock().unlock();
                        }
                    }

                    @Override
                    public String toString() {
                        return "Exporter[" + query + "]";
                    }
                }, getQueryIntervalInSeconds() + 1, getExportIntervalInSeconds(), TimeUnit.SECONDS);
            }

            shutdownHook = new EmbeddedJmxTransShutdownHook();
            shutdownHook.registerToRuntime();
            state = State.STARTED;
            logger.info("EmbeddedJmxTrans started");
        } catch (RuntimeException e) {
            this.state = State.ERROR;
            if (logger.isDebugEnabled()) {
                // to troubleshoot JMX call errors or equivalent, it may be useful to log and rethrow
                logger.warn("Exception starting EmbeddedJmxTrans", e);
            }
            throw e;
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    /**
     * Stop scheduled executors and collect-and-export metrics one last time.
     */
    @PreDestroy
    public void stop() {
        logger.info("Stop...");
        lifecycleLock.writeLock().lock();
        try {
            if (!State.STARTED.equals(state)) {
                logger.debug("Ignore stop() command for " + state + " instance");
                return;
            }
            logger.info("Unregister shutdown hook");
            this.shutdownHook.unregisterFromRuntime();

            logger.info("Shutdown collectScheduledExecutor and exportScheduledExecutor...");
            // no need to `shutdown()` and `awaitTermination()` before `shutdownNow()` as we invoke `collectMetrics()` and `exportCollectedMetrics()`
            // `shutdownNow()` can be invoked before `collectMetrics()` and `exportCollectedMetrics()`
            collectScheduledExecutor.shutdownNow();
            exportScheduledExecutor.shutdownNow();

            try {
                logger.info("Collect metrics...");
                collectMetrics();
                logger.info("Export metrics...");
                exportCollectedMetrics();
            } catch (RuntimeException e) {
                logger.warn("Ignore failure collecting and exporting metrics during stop", e);
            }

            // queries and outputwriters can be stopped even if exports threads are running thanks to the lifecycleLock
            logger.info("Stop queries...");
            for (Query query : queries) {
                try {
                    query.stop();
                } catch (Exception e) {
                    logger.warn("Ignore exception stopping query {}", query, e);
                }
            }

            logger.info("Stop output writers...");
            for (OutputWriter outputWriter : outputWriters) {
                try {
                    outputWriter.stop();
                } catch (Exception e) {
                    logger.warn("Ignore exception stopping outputWriters", e);
                }
            }

            state = State.STOPPED;
            logger.info("Set state to {}", state);
        } catch (RuntimeException e) {
            state = State.ERROR;
            if (logger.isDebugEnabled()) {
                // to troubleshoot JMX call errors or equivalent, it may be useful to log and rethrow
                logger.warn("Exception stopping EmbeddedJmxTrans", e);
            }
            throw e;
        } finally {
            lifecycleLock.writeLock().unlock();
        }
        logger.info("Stopped");
    }


    /**
     * Exposed for manual / JMX invocation
     */
    @Override
    public void collectMetrics() {
        lifecycleLock.readLock().lock();
        try {
            if (!State.STARTED.equals(state)) {
                logger.debug("Ignore collectMetrics() command for " + state + " instance");
                return;
            }
            for (Query query : getQueries()) {
                query.collectMetrics();
            }
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    /**
     * Exposed for manual / JMX invocation
     */
    @Override
    public void exportCollectedMetrics() {
        lifecycleLock.readLock().lock();
        try {
            if (!State.STARTED.equals(state)) {
                logger.debug("Ignore collectMetrics() command for " + state + " instance");
                return;
            }
            for (Query query : getQueries()) {
                query.exportCollectedMetrics();
            }
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }

    @Nonnull
    public List<Query> getQueries() {
        return queries;
    }

    public void addQuery(@Nonnull Query query) {
        query.setEmbeddedJmxTrans(this);
        this.queries.add(query);
    }

    @Override
    public String toString() {
        return "EmbeddedJmxTrans{" +
                "state=" + getState() +
                ", queries=" + queries +
                ", outputWriters=" + outputWriters +
                ", numQueryThreads=" + numQueryThreads +
                ", queryIntervalInSeconds=" + queryIntervalInSeconds +
                ", numExportThreads=" + numExportThreads +
                ", exportIntervalInSeconds=" + exportIntervalInSeconds +
                ", exportBatchSize=" + exportBatchSize +
                '}';
    }

    public int getNumQueryThreads() {
        return numQueryThreads;
    }

    public void setNumQueryThreads(int numQueryThreads) {
        this.numQueryThreads = numQueryThreads;
    }

    @Override
    public int getQueryIntervalInSeconds() {
        return queryIntervalInSeconds;
    }

    public void setQueryIntervalInSeconds(int queryIntervalInSeconds) {
        this.queryIntervalInSeconds = queryIntervalInSeconds;
    }

    @Override
    public int getExportIntervalInSeconds() {
        return exportIntervalInSeconds;
    }

    public void setExportIntervalInSeconds(int exportIntervalInSeconds) {
        this.exportIntervalInSeconds = exportIntervalInSeconds;
    }

    @Override
    public int getNumExportThreads() {
        return numExportThreads;
    }

    public void setNumExportThreads(int numExportThreads) {
        this.numExportThreads = numExportThreads;
    }

    @Nonnull
    public Set<OutputWriter> getOutputWriters() {
        return outputWriters;
    }

    /**
     * Max number of {@linkplain QueryResult} exported at each call of {@link OutputWriter#write(Iterable)}
     */
    public int getExportBatchSize() {
        return exportBatchSize;
    }

    public void setExportBatchSize(int exportBatchSize) {
        this.exportBatchSize = exportBatchSize;
    }

    @Nonnull
    public MBeanServer getMbeanServer() {
        return mbeanServer;
    }

    @Override
    public int getCollectedMetricsCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getCollectedMetricsCount();
        }
        return result;
    }

    @Override
    public long getCollectionDurationInNanos() {
        long result = 0;
        for (Query query : queries) {
            result += query.getCollectionDurationInNanos();
        }
        return result;
    }


    @Override
    public long getCollectionDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(getCollectionDurationInNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int getCollectionCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getCollectionCount();
        }
        return result;
    }

    @Override
    public int getExportedMetricsCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getExportedMetricsCount();
        }
        return result;
    }

    @Override
    public long getExportDurationInNanos() {
        long result = 0;
        for (Query query : queries) {
            result += query.getExportDurationInNanos();
        }
        return result;
    }

    @Override
    public long getExportDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(getExportDurationInNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int getExportCount() {
        int result = 0;
        for (Query query : queries) {
            result += query.getExportCount();
        }
        return result;
    }

    public int getDiscardedResultsCount() {
        int result = 0;
        for (Query query : queries) {
            int discardedResultsCount = query.getDiscardedResultsCount();
            if (discardedResultsCount != -1) {
                result += discardedResultsCount;
            }
        }
        return result;
    }

    // return a String and not an embedded-jmxtrans class/enum to be portable and usable in JMX tools such as VisualVM
    @Nullable
    public String getState() {
        lifecycleLock.readLock().lock();
        try {
            return state == null ? null : state.toString();
        } finally {
            lifecycleLock.readLock().unlock();
        }
    }
}
