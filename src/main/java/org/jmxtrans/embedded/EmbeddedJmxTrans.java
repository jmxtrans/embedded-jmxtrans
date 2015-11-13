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
import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * <strong>JMX Queries</strong>
 *
 * If the JMX query returns several mbeans (thanks to '*' or '?' wildcards),
 * then the configured attributes are collected on all the returned mbeans.
 *
 *
 * <strong>Output Writers</strong>
 *
 * {@linkplain OutputWriter}s can be defined at the query level or globally at the {@link EmbeddedJmxTrans} level.
 * The {@linkplain OutputWriter}s that are effective for a {@linkplain Query} are accessible
 * via {@link Query#getEffectiveOutputWriters()}
 *
 *
 * <strong>Collected Metrics / Query Results</strong>
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

        private final AtomicBoolean alreadyExecuted = new AtomicBoolean(false);

        public EmbeddedJmxTransShutdownHook() {
            setName(getClass().getSimpleName() + "-" + getName());
        }

        @Override
        public void run() {
            // this method will be executed by the Runtime as a Shutdown Hook
            execute("EmbeddedJmxTransShutdownHook");
        }

        private void execute(String invokerName) {
            if (alreadyExecuted.compareAndSet(false, true)) {
                try {
                    collectMetrics();
                    exportCollectedMetrics();
                    logger.info("{} successfully collected and exported metrics one last time", invokerName);
                } catch (RuntimeException e) {
                    logger.warn("{} failed to collect and export metrics one last time", invokerName);
                }

                try {
                    for (Query query : queries) {
                        query.stop();
                    }
                } catch (Exception e) {
                    logger.warn("Failure while stopping queries", e);
                }

                try {
                    outputWriters.stopAll();
                } catch (Exception e) {
                    logger.warn("Failure while stopping outputWriters", e);
                }
            }
        }

        public boolean isAlreadyExecuted() {
            return alreadyExecuted.get();
        }

        public void registerToRuntime() {
            Runtime.getRuntime().addShutdownHook(this);
        }

        public void unregisterFromRuntime() {
            try {
                boolean shutdownHookRemoved = Runtime.getRuntime().removeShutdownHook(this);
                if (shutdownHookRemoved) {
                    logger.debug("ShutdownHook successfully removed");
                } else {
                    logger.warn("Failure to remove ShutdownHook");
                }
            } catch (RuntimeException e) {
                logger.warn("Failure to remove ShutdownHook", e);
            }
        }

        public void onStop() {
            // if the shutdown hook was already executed by the Runtime
            // then we should not try to remove it from the Runtime otherwise we would get an IllegalStateException: Shutdown in progress

            if (!isAlreadyExecuted()) {
                unregisterFromRuntime();

                // as the shutdownHook was not already executed
                // we want to execute it in order to trigger the last collection and export
                execute("EmbeddedJmxTrans Stop Handler");
            }
        }

    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running = false;

    private ScheduledExecutorService collectScheduledExecutor;

    private ScheduledExecutorService exportScheduledExecutor;

    @Nonnull
    private MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

    @Nonnull
    private final List<Query> queries = new ArrayList<Query>();

    /**
     * Use of {@linkplain Set} to deduplicate during configuration merger
     */
    private final OutputWriterSet outputWriters = new OutputWriterSet();

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
    public synchronized void start() throws Exception {
        if(running) {
            logger.debug("Ignore start() command for already running instance");
            return;
        }

        for (Query query : queries) {
            query.start();
        }
        outputWriters.startAll();

        collectScheduledExecutor = Executors.newScheduledThreadPool(getNumQueryThreads(), new NamedThreadFactory("jmxtrans-collect-", true));
        exportScheduledExecutor = Executors.newScheduledThreadPool(getNumExportThreads(), new NamedThreadFactory("jmxtrans-export-", true));

        for (final Query query : getQueries()) {
            collectScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    query.collectMetrics();
                }
            }, 0, getQueryIntervalInSeconds(), TimeUnit.SECONDS);
            // start export just after first collect
            exportScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    query.exportCollectedMetrics();
                }
            }, getQueryIntervalInSeconds() + 1, getExportIntervalInSeconds(), TimeUnit.SECONDS);
        }

        shutdownHook = new EmbeddedJmxTransShutdownHook();
        shutdownHook.registerToRuntime();
        running = true;
        logger.info("EmbeddedJmxTrans started");
    }


    /**
     * Stop scheduled executors and collect-and-export metrics one last time.
     */
    @PreDestroy
    public synchronized void stop() throws Exception {
        if(!running) {
            logger.debug("Ignore stop() command for not running instance");
            return;
        }
        try {
            collectScheduledExecutor.shutdownNow();
            exportScheduledExecutor.shutdownNow();
        } catch (RuntimeException e) {
            logger.warn("Failure while shutting down ExecutorServices", e);
        }
        shutdownHook.onStop();
        running = false;
    }

    /**
     * Exposed for manual / JMX invocation
     */
    @Override
    public void collectMetrics() {
        for (Query query : getQueries()) {
            query.collectMetrics();
        }
    }

    /**
     * Exposed for manual / JMX invocation
     */
    @Override
    public void exportCollectedMetrics() {
        for (Query query : getQueries()) {
            query.exportCollectedMetrics();
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
                " queries=" + queries +
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
    public OutputWriterSet getOutputWriters() {
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
}
