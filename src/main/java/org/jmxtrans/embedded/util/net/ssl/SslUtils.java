/*
 * Copyright (c) 2010-2015 the original author or authors
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
package org.jmxtrans.embedded.util.net.ssl;

import org.jmxtrans.embedded.util.io.IoUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
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
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("NoSuchAlgorithmException loading SSLSocketFactory with " +
                    "keyStore=" + keyStore + ", keyStorePassword=" + (keyStorePassword == null ? "<null>" : "***") + "," +
                    "trustStore=" + trustStore + ", trustStorePassword=" + (trustStorePassword == null ? "<null>" : "***")+ ": " + e, e);
        } catch(FileNotFoundException e) {
            throw new IllegalStateException("FileNotFoundException loading SSLSocketFactory with " +
                    "keyStore=" + keyStore + ", keyStorePassword=" + (keyStorePassword == null ? "<null>" : "***") + "," +
                    "trustStore=" + trustStore + ", trustStorePassword=" + (trustStorePassword == null ? "<null>" : "***")+ ": " + e, e);
        } catch (Exception e) {
            throw new RuntimeException("Exception loading SSLSocketFactory with " +
                    "keyStore=" + keyStore + ", keyStorePassword=" + (keyStorePassword == null ? "<null>" : "***") + "," +
                    "trustStore=" + trustStore + ", trustStorePassword=" + (trustStorePassword == null ? "<null>" : "***") + ": " + e, e);
        }
    }

    /**
     * Build an {@link SSLSocketFactory} that trusts all the X509 certificates.
     *
     * @return the created {@link SSLSocketFactory}
     */
    @Nonnull
    public static SSLSocketFactory getTrustAllSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    private SslUtils() {

    }

    /**
     * Trust all {@link X509Certificate}. Not for production use.
     */
    public static class TrustAllX509TrustManager implements X509TrustManager {
        protected final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
            if (logger.isDebugEnabled()) {
                logger.debug("checkClientTrusted(authType=" + authType + ")");
                for (X509Certificate x509Certificate : x509Certificates) {
                    logger.debug(x509Certificate.toString());
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
            if (logger.isDebugEnabled()) {
                logger.debug("checkServerTrusted(authType=" + authType + ")");
                for (X509Certificate x509Certificate : x509Certificates) {
                    logger.debug(x509Certificate.toString());
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
