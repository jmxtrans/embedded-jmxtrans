package org.jmxtrans.embedded.util.net.ssl;

import org.jmxtrans.embedded.util.io.IoUtils2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class SslUtils {

    /**
     * Build an {@link SSLSocketFactory} with the given trust store and key store.
     *
     * @param keyStore           path to the given JKS key store. Can be a classpath resource ("classpath:com/example/keystore.jks") of file system related. Optional, if {code null}, then the JVM key store is used.
     * @param keyStorePassword   password to open the key store
     * @param trustStore         path to the given JKS trust store. Can be a classpath resource ("classpath:com/example/keystore.jks") of file system related. Optional, if {code null}, then the JVM key store is used.
     * @param trustStorePassword password to open the trust store
     * @return the {@link SSLSocketFactory}
     */
    @Nonnull
    public static SSLSocketFactory getSSLSocketFactory(@Nullable String keyStore, @Nullable String keyStorePassword, @Nullable String trustStore, @Nullable String trustStorePassword) {
        SSLContext sslContext = getSslContext(keyStore, keyStorePassword, trustStore, trustStorePassword);
        return sslContext.getSocketFactory();
    }

    /**
     * Build an {@link SSLContext} with the given trust store and key store.
     *
     * @param keyStore           path to the given JKS key store. Can be a classpath resource ("classpath:com/example/keystore.jks") of file system related. Optional, if {code null}, then the JVM key store is used.
     * @param keyStorePassword   password to open the key store
     * @param trustStore         path to the given JKS trust store. Can be a classpath resource ("classpath:com/example/truststore.jks") of file system related. Optional, if {code null}, then the JVM key store is used.
     * @param trustStorePassword password to open the trust store
     * @return the {@link SSLContext}
     */
    @Nonnull
    public static SSLContext getSslContext(@Nullable String keyStore, @Nullable String keyStorePassword, @Nullable String trustStore, @Nullable String trustStorePassword) {
        try {
            KeyManager[] keyManagers;
            if (keyStore == null) {
                keyManagers = null;
            } else {
                char[] password = keyStorePassword == null ? null : keyStorePassword.toCharArray();
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream in = IoUtils2.getInputStream(keyStore);
                try {
                    ks.load(in, password);
                } finally {
                    IoUtils2.closeQuietly(in);
                }
                keyManagerFactory.init(ks, password);
                keyManagers = keyManagerFactory.getKeyManagers();
            }

            TrustManager[] trustManagers;
            if (trustStore == null) {
                trustManagers = null;
            } else {
                char[] password = trustStorePassword == null ? null : trustStorePassword.toCharArray();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream in = IoUtils2.getInputStream(trustStore);
                try {
                    ks.load(in, password);
                } finally {
                    IoUtils2.closeQuietly(in);
                }
                trustManagerFactory.init(ks);
                trustManagers = trustManagerFactory.getTrustManagers();
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Exception loading SSLSocketFactory with " +
                    "keyStore=" + keyStore + ", keyStorePassword=" + (keyStorePassword == null ? "<null>" : "***") + "," +
                    "trustStore=" + trustStore + ", trustStorePassword=" + (trustStorePassword == null ? "<null>" : "***"), e);
        }
    }


    private SslUtils() {

    }
}
