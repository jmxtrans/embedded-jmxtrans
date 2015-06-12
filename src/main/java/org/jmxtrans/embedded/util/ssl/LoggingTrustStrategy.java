package org.jmxtrans.embedded.util.ssl;

import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class LoggingTrustStrategy implements TrustStrategy {

    private static final CertificateLoggingHelper certificateLoggingHelper = new CertificateLoggingHelper();

    private static final Logger logger = LoggerFactory.getLogger(LoggingTrustStrategy.class);

    private final TrustStrategy delegate;

    public LoggingTrustStrategy(TrustStrategy delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (!logger.isInfoEnabled()) {
            return delegate.isTrusted(chain, authType);
        }
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("isTrusted() : ");
        logBuilder.append("authType = ").append(authType);
        logBuilder.append(", chain.length = ").append(chain.length);
        if (chain.length > 0) {
            logBuilder.append(", ");
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                logBuilder.append("chain[").append(i).append("] = {");
                certificateLoggingHelper.appendCertificateDescription(logBuilder, cert);
                logBuilder.append("}");
                if (i + 1 < chain.length) {
                    logBuilder.append(", ");
                }
            }
        }

        // should always return false so that the TrustManager is queried
        // because we have no authority on whether or not the Certificate should be trusted without querying the underlying TrustManager
        // the result is returned by the delegate
        boolean trusted = false;
        try {
            trusted = delegate.isTrusted(chain, authType);
            return trusted;
        } finally {
            logBuilder.append(" : trustStrategy = ").append(delegate).append(", ");
            logBuilder.append(" : result : trusted = ").append(trusted);
            logger.info(logBuilder.toString());
        }
    }

}
