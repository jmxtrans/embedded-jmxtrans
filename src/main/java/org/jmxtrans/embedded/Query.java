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
import org.jmxtrans.embedded.util.Preconditions;
import org.jmxtrans.embedded.util.concurrent.DiscardingBlockingQueue;
import org.jmxtrans.embedded.util.jmx.JmxUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Describe a JMX query on which metrics are collected and hold the query and export business logic
 * ({@link #collectMetrics()} and {@link #exportCollectedMetrics()}).
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Jon Stevens
 */
public class Query implements QueryMBean {

    private static final AtomicInteger queryIdSequence = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Parent.
     */
    private EmbeddedJmxTrans embeddedJmxTrans;

    /**
     * Mainly used for monitoring.
     */
    private String id = "query-" + queryIdSequence.getAndIncrement();

    /**
     * ObjectName of the Query MBean(s) to monitor, can contain
     */
    @Nonnull
    private ObjectName objectName;

    @Nullable
    private String resultAlias;
    /**
     * JMX attributes to collect. As an array for {@link javax.management.MBeanServer#getAttributes(javax.management.ObjectName, String[])}
     */
    @Nonnull
    private final Map<String, QueryAttribute> attributesByName = new HashMap<String, QueryAttribute>();
    /**
     * Copy of {@link #attributesByName}'s {@link java.util.Map#entrySet()} for performance optimization
     */
    @Nonnull
    private String[] attributeNames = new String[0];

    /**
     * List of {@linkplain OutputWriter} declared at the {@linkplain Query} level.
     *
     * @see EmbeddedJmxTrans#getOutputWriters()
     * @see #getEffectiveOutputWriters()
     */
    @Nonnull
    private final OutputWriterSet outputWriters = new OutputWriterSet();

    /**
     * Store the metrics collected on this {@linkplain Query} (see {@link #collectMetrics()})
     * until they are exported to the target {@linkplain OutputWriter}s (see {@link #exportCollectedMetrics()}.
     */
    @Nonnull
    private BlockingQueue<QueryResult> queryResults = new DiscardingBlockingQueue<QueryResult>(200);

    @Nonnull
    private final AtomicInteger collectedMetricsCount = new AtomicInteger();

    @Nonnull
    private final AtomicLong collectionDurationInNanos = new AtomicLong();

    @Nonnull
    private final AtomicInteger collectionCount = new AtomicInteger();

    @Nonnull
    private final AtomicInteger exportedMetricsCount = new AtomicInteger();

    @Nonnull
    private final AtomicLong exportDurationInNanos = new AtomicLong();

    @Nonnull
    private final AtomicInteger exportCount = new AtomicInteger();

    /**
     * {@link ObjectName} of this {@link QueryMBean}
     */
    @Nullable
    private ObjectName queryMbeanObjectName;

    /**
     * Creates a {@linkplain Query} on the given <code>objectName</code>.
     *
     * @param objectName {@link ObjectName} to query, can contain wildcards ('*' or '?')
     */
    public Query(@Nonnull String objectName) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Exception parsing '" + objectName + "'", e);
        }
    }

    /**
     * Creates a {@linkplain Query} on the given <code>objectName</code>.
     *
     * @param objectName {@link ObjectName} to query, can contain wildcards ('*' or '?')
     */
    public Query(@Nonnull ObjectName objectName) {
        this.objectName = objectName;
    }


    /**
     * Collect the values for this query and store them as {@link QueryResult} in the {@linkplain Query#queryResults} queue
     */
    @Override
    public void collectMetrics() {
        long nanosBefore = System.nanoTime();
        /*
         * Optimisation tip: no need to skip 'mbeanServer.queryNames()' if the ObjectName is not a pattern
         * (i.e. not '*' or '?' wildcard) because the mbeanserver internally performs the check.
         * Seen on com.sun.jmx.interceptor.DefaultMBeanServerInterceptor
         */
        Set<ObjectName> matchingObjectNames = embeddedJmxTrans.getMbeanServer().queryNames(this.objectName, null);
        logger.trace("Query {} returned {}", objectName, matchingObjectNames);

        for (ObjectName matchingObjectName : matchingObjectNames) {
            if (Thread.interrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
            long epochInMillis = System.currentTimeMillis();
            try {
                AttributeList jmxAttributes = embeddedJmxTrans.getMbeanServer().getAttributes(matchingObjectName, this.attributeNames);
                logger.trace("Query {} returned {}", matchingObjectName, jmxAttributes);
                for (Attribute jmxAttribute : jmxAttributes.asList()) {
                    QueryAttribute queryAttribute = this.attributesByName.get(jmxAttribute.getName());
                    Object value = jmxAttribute.getValue();
                    int count = queryAttribute.collectMetrics(matchingObjectName, value, epochInMillis, this.queryResults);
                    collectedMetricsCount.addAndGet(count);
                }
            } catch (Exception e) {
                logger.warn("Exception processing query {}", this, e);
            }
        }
        collectionCount.incrementAndGet();
        long nanosAfter = System.nanoTime();
        collectionDurationInNanos.addAndGet(nanosAfter - nanosBefore);
    }

    /**
     * Export the collected metrics to the {@linkplain OutputWriter}s associated with this {@linkplain Query}
     * (see {@link #getEffectiveOutputWriters()}).
     *
     * Metrics are batched according to {@link EmbeddedJmxTrans#getExportBatchSize()}
     *
     * @return the number of exported {@linkplain QueryResult}
     */
    @Override
    public int exportCollectedMetrics() {
        if (queryResults.isEmpty()) {
            return 0;
        }

        int successfullyExportedMetricsCount = 0;
        long nanosBefore = System.nanoTime();

        int exportBatchSize = getEmbeddedJmxTrans().getExportBatchSize();
        List<QueryResult> availableQueryResults = new ArrayList<QueryResult>(exportBatchSize);

        int size;
        while ((size = queryResults.drainTo(availableQueryResults, exportBatchSize)) > 0) {
            if (Thread.interrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
            OutputWriterSet.tryToWriteQueryResultsBatchToAll(availableQueryResults, getEmbeddedJmxTrans().getOutputWriters(), getOutputWriters());
            successfullyExportedMetricsCount += size;
            exportedMetricsCount.addAndGet(size);
            availableQueryResults.clear();
        }
        exportDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
        exportCount.incrementAndGet();
        return successfullyExportedMetricsCount;
    }

    /**
     * Start all the {@linkplain OutputWriter}s attached to this {@linkplain Query}
     */
    @PostConstruct
    public void start() throws Exception {
        queryMbeanObjectName = JmxUtils2.registerObject(this, "org.jmxtrans.embedded:Type=Query,id=" + id, getEmbeddedJmxTrans().getMbeanServer());

        outputWriters.startAll();
    }

    /**
     * Stop all the {@linkplain OutputWriter}s attached to this {@linkplain Query}
     */
    @PreDestroy
    public void stop() throws Exception {
        JmxUtils2.unregisterObject(queryMbeanObjectName, embeddedJmxTrans.getMbeanServer());

        outputWriters.stopAll();
    }

    @Override
    @Nonnull
    public ObjectName getObjectName() {
        return objectName;
    }

    @Nonnull
    public Collection<QueryAttribute> getQueryAttributes() {
        return attributesByName.values();
    }

    /**
     * Add the given attribute to the list attributes of this query
     * and maintains the reverse relation (see {@link org.jmxtrans.embedded.QueryAttribute#getQuery()}).
     *
     * @param attribute attribute to add
     * @return this
     */
    @Nonnull
    public Query addAttribute(@Nonnull QueryAttribute attribute) {
        attribute.setQuery(this);
        attributesByName.put(attribute.getName(), attribute);
        attributeNames = attributesByName.keySet().toArray(new String[0]);

        return this;
    }

    /**
     * Create a basic {@link QueryAttribute}, add it to the list attributes of this query
     * and maintains the reverse relation (see {@link org.jmxtrans.embedded.QueryAttribute#getQuery()}).
     *
     * @param attributeName attribute to add
     * @return this
     */
    @Nonnull
    public Query addAttribute(@Nonnull String attributeName) {
        return addAttribute(new QueryAttribute(attributeName, null, null));
    }

    @Nonnull
    public BlockingQueue<QueryResult> getResults() {
        return queryResults;
    }

    /**
     * WARNING: {@linkplain #queryResults} queue should not be changed at runtime as the operation is not thread safe.
     */
    public void setResultsQueue(@Nonnull BlockingQueue<QueryResult> queryResultQueue) {
        this.queryResults = Preconditions.checkNotNull(queryResultQueue);
    }

    public void setResultAlias(@Nullable String resultAlias) {
        this.resultAlias = resultAlias;
    }

    public EmbeddedJmxTrans getEmbeddedJmxTrans() {
        return embeddedJmxTrans;
    }

    public void setEmbeddedJmxTrans(EmbeddedJmxTrans embeddedJmxTrans) {
        this.embeddedJmxTrans = embeddedJmxTrans;
    }

    /**
     * Return the <code>outputWriters</code> to which the collected metrics of this {@linkplain Query} are exported,
     * the <code>outputWriters</code> declared at query level or a the parent level.
     *
     * @see #getOutputWriters()
     * @see EmbeddedJmxTrans#getOutputWriters()
     */
    @Nonnull
    public List<OutputWriter> getEffectiveOutputWriters() {
        // Google Guava predicates would be nicer but we don't include guava to ease embeddability
        List<OutputWriter> result = new ArrayList<OutputWriter>(embeddedJmxTrans.getOutputWriters().size() + outputWriters.size());
        result.addAll(embeddedJmxTrans.getOutputWriters().findEnabled());
        result.addAll(this.getOutputWriters().findEnabled());
        return result;
    }

    @Nonnull
    public OutputWriterSet getOutputWriters() {
        return outputWriters;
    }

    @Override
    @Nullable
    public String getResultAlias() {
        return resultAlias;
    }

    @Override
    public String toString() {
        return "Query{" +
                "objectName=" + objectName +
                ", resultAlias='" + resultAlias + '\'' +
                ", outputWriters=" + outputWriters +
                ", attributes=" + attributesByName.values() +
                '}';
    }

    @Override
    public int getCollectedMetricsCount() {
        return collectedMetricsCount.get();
    }

    @Override
    public long getCollectionDurationInNanos() {
        return collectionDurationInNanos.get();
    }

    @Override
    public int getCollectionCount() {
        return collectionCount.get();
    }

    @Override
    public int getExportedMetricsCount() {
        return exportedMetricsCount.get();
    }

    @Override
    public long getExportDurationInNanos() {
        return exportDurationInNanos.get();
    }

    @Override
    public int getExportCount() {
        return exportCount.get();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the number of discarded elements in the {@link #queryResults} queue
     * or <code>-1</code> if the queue is not a {@link DiscardingBlockingQueue}.
     */
    @Override
    public int getDiscardedResultsCount() {
        if (queryResults instanceof DiscardingBlockingQueue) {
            DiscardingBlockingQueue discardingBlockingQueue = (DiscardingBlockingQueue) queryResults;
            return discardingBlockingQueue.getDiscardedElementCount();
        } else {
            return -1;
        }
    }
}
