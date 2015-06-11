package org.jmxtrans.embedded.util.socket;

/**
 * just a configuring class SslSocketFactory properties (with a default constructor), that allows referring it's class name in the "graphite.socketFactoryClassName" property
 */
public class SelfSignedAcceptedSslSocketFactory extends SslSocketFactory {

    /**
     * default constructor is mandatory
     */
    public SelfSignedAcceptedSslSocketFactory() {
        super(true);
    }

}
