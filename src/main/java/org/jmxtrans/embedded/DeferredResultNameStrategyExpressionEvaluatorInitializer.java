package org.jmxtrans.embedded;

import org.jmxtrans.embedded.util.StringUtils2;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

class DeferredResultNameStrategyExpressionEvaluatorInitializer {

    Map<String, Callable<String>> createInitialExpressionEvaluatorsMap() {
        // Using Callable Objects in order to defer the actual call to the moment if is actually needed
        // in case we do not ever need to use an expression, the Callable is never called

        // This is particularly important for the InetAddress.getLocalHost() method invocation
        // because this method can throw an Exception
        // in case the network is not fully/properly working at the time of the execution

        // by using a Callable, the network is not accessed inside the ResultNameStrategy constructor
        // so the constructor does not have to handle any Exception

        // To ensure consistency and performance,
        // actual Callable Objects are enclosed inside a CachingCallable
        // that takes care of caching the result on first access

        final Callable<InetAddress> localHostCallable = new CachingCallable<InetAddress>(new Callable<InetAddress>() {
            @Override
            public InetAddress call() throws Exception {
                return InetAddress.getLocalHost();
            }
        });
        final Callable<String> hostnameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localHostCallable.call().getHostName();
            }
        });
        final Callable<String> reversedHostNameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return StringUtils2.reverseTokens(hostnameCallable.call(), ".");
            }
        });
        final Callable<String> canonicalHostNameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localHostCallable.call().getCanonicalHostName();
            }
        });
        final Callable<String> reversedCanonicalHostNameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return StringUtils2.reverseTokens(canonicalHostNameCallable.call(), ".");
            }
        });
        final Callable<String> hostAddressCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return localHostCallable.call().getHostAddress();
            }
        });

        final Callable<String> escapedHostNameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return hostnameCallable.call().replaceAll("\\.", "_");
            }
        });
        final Callable<String> escapedCanonicalHostNameCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return canonicalHostNameCallable.call().replaceAll("\\.", "_");
            }
        });
        final Callable<String> escapedHostAddressCallable = new CachingCallable<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return hostAddressCallable.call().replaceAll("\\.", "_");
            }
        });

        Map<String, Callable<String>> resultMap = new HashMap<String, Callable<String>>();
        //
        resultMap.put("hostname", hostnameCallable);
        resultMap.put("reversed_hostname", reversedHostNameCallable);
        resultMap.put("escaped_hostname", escapedHostNameCallable);
        resultMap.put("canonical_hostname", canonicalHostNameCallable);
        resultMap.put("reversed_canonical_hostname", reversedCanonicalHostNameCallable);
        resultMap.put("escaped_canonical_hostname", escapedCanonicalHostNameCallable);
        resultMap.put("hostaddress", hostAddressCallable);
        resultMap.put("escaped_hostaddress", escapedHostAddressCallable);
        //
        return resultMap;
    }

    private static class CachedValueReference<T> {

        private final T value;

        public CachedValueReference(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

    }

    private static class CachingCallable<T> implements Callable<T> {

        private final Callable<T> delegate;

        // using a container Object in order to be able to cache null values
        private CachedValueReference<T> cachedValueReference;

        public CachingCallable(Callable<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T call() throws Exception {
            CachedValueReference<T> valueRef = cachedValueReference;
            if (valueRef == null) {
                valueRef = new CachedValueReference<T>(delegate.call());
                cachedValueReference = valueRef;
            }
            return valueRef.getValue();
        }
    }

}
