package org.jmxtrans.embedded.util.plumbing;

import org.jmxtrans.embedded.QueryResult;

import java.util.Collection;
import java.util.Iterator;

public class QueryResultSinkToCollectionAdaptor implements Collection<QueryResult> {

    private final QueryResultSink sink;

    public QueryResultSinkToCollectionAdaptor(QueryResultSink sink) {
        this.sink = sink;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryResultSink[] toArray(Object[] objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(QueryResult o) {
        sink.accept(o);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection collection) {
        sink.accept(collection);
        return true;
    }

    @Override
    public boolean removeAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
