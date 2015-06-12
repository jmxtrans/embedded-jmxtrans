package org.jmxtrans.embedded.util.socket;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.jmxtrans.embedded.util.ssl.LoggingTrustStrategy;
import org.jmxtrans.embedded.util.ssl.NeverTrustStrategy;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.SecureRandom;

public class ApacheHCSslSocketFactory extends AbstractSslSocketFactory {

	private final boolean allowSelfSignedCertificates;

	/**
	 * default constructor is mandatory
	 */
	public ApacheHCSslSocketFactory() {
		this(false);
	}

	public ApacheHCSslSocketFactory(boolean allowSelfSignedCertificates) {
		super();
		this.allowSelfSignedCertificates = allowSelfSignedCertificates;
	}

	@Override
	protected SSLContext buildSSLContextWithExceptions() throws Exception {
		final KeyStore defaultTrustStore;
		defaultTrustStore = null;

		final SSLContextBuilder builder = new SSLContextBuilder();

		TrustStrategy trustStrategy = allowSelfSignedCertificates ? new TrustSelfSignedStrategy() : new NeverTrustStrategy();
		TrustStrategy loggingTrustStrategy = new LoggingTrustStrategy(trustStrategy);
		builder.loadTrustMaterial(defaultTrustStore, loggingTrustStrategy);

		builder.setSecureRandom(new SecureRandom());
		//builder.useTLS();
		return builder.build();
	}

}
