package org.jmxtrans.embedded.util.socket;

import javax.net.ssl.SSLContext;

public class DefaultSslSocketFactory extends AbstractSslSocketFactory {

	/**
	 * default constructor is mandatory
	 */
	public DefaultSslSocketFactory() {
	}

	@Override
	protected SSLContext buildSSLContextWithExceptions() throws Exception {
		return SSLContext.getDefault();
	}


}
