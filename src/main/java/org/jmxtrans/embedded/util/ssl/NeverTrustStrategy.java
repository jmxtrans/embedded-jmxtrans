package org.jmxtrans.embedded.util.ssl;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class NeverTrustStrategy implements TrustStrategy {
	@Override
	public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
		// should always return false so that the TrustManager is queried
		// because we have no authority on whether or not the Certificate should be trusted without querying the underlying TrustManager
		return false;
	}
}
