package org.jmxtrans.embedded.util.plumbing;

public interface QueryResultSource {

    int drainTo(QueryResultSink sink, int count);

}
