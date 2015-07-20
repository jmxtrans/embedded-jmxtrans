package org.jmxtrans.embedded;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

public class HttpProxyUtils {
    
    public static HttpProxyUtils instance = new HttpProxyUtils();
    
    public static HttpProxyUtils getInstance() {
        return instance;
    }

    public Proxy guessProxy() throws MalformedURLException {
        String httpsProxyUrlProperty = System.getenv("https_proxy");
        if (httpsProxyUrlProperty == null || httpsProxyUrlProperty.isEmpty()) {
            return null;
        }
        URL proxyUrl = new URL(httpsProxyUrlProperty);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
    }

}
