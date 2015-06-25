package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.plumbing.QueryResultSource;

public class GlobalQueryResultsExporter extends QueryResultsExporter {

    private final EmbeddedJmxTrans embeddedJmxTrans;

    public GlobalQueryResultsExporter(EmbeddedJmxTrans embeddedJmxTrans) {
        super();
        this.embeddedJmxTrans = embeddedJmxTrans;
    }

    @Override
    protected OutputWriterSet[] getOutputWriterSets() {
        OutputWriterSet[] results = new OutputWriterSet[1];
        results[0] = embeddedJmxTrans.getOutputWriters();
        return results;
    }

    @Override
    protected int getExportBatchSize() {
        return embeddedJmxTrans.getExportBatchSize();
    }

    @Override
    protected QueryResultSource getQueryResultSource() {
        return embeddedJmxTrans.getGlobalQueryResultSource();
    }
}
