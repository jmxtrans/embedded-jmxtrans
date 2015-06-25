package org.jmxtrans.embedded.util.plumbing;

public class NullQueryResultSource implements QueryResultSource {

    @Override
    public int drainTo(QueryResultSink sink, int count) {
        // always returns zero
        // never writes to the sink 
        return 0;
    }

}
