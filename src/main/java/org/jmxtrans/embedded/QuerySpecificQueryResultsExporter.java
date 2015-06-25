package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.plumbing.QueryResultSource;

public class QuerySpecificQueryResultsExporter extends QueryResultsExporter {

    private final Query query;
    
    private boolean exportToGlobalOutputWriters;

    public QuerySpecificQueryResultsExporter(Query query, boolean exportToGlobalOutputWriters) {
        super();
        this.query = query;
        this.exportToGlobalOutputWriters = exportToGlobalOutputWriters;
    }

    public void setExportToGlobalOutputWriters(boolean exportToGlobalOutputWriters) {
        this.exportToGlobalOutputWriters = exportToGlobalOutputWriters;
    }

    @Override
    protected OutputWriterSet[] getOutputWriterSets() {
        if (exportToGlobalOutputWriters) {
            OutputWriterSet[] results = new OutputWriterSet[2];
            results[0] = query.getEmbeddedJmxTrans().getOutputWriters();
            results[1] = query.getOutputWriters();
            return results;
        } else {
            OutputWriterSet[] results = new OutputWriterSet[1];
            results[0] = query.getOutputWriters();
            return results;
        }
    }

    @Override
    protected int getExportBatchSize() {
        return query.getEmbeddedJmxTrans().getExportBatchSize();
    }

    @Override
    protected QueryResultSource getQueryResultSource() {
        return query.getQueryResultSource();
    }

}
