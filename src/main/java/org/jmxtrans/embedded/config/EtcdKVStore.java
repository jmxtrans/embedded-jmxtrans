/*
 * Copyright (c) 2016 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jmxtrans.embedded.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.jmxtrans.embedded.EmbeddedJmxTransException;
import org.jmxtrans.embedded.util.json.PlaceholderEnabledJsonNodeFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is an etcd based KVStore implementation. The connection to etcd is estabilished and closed
 * every time a key is read. This is by design since we don't need to read a lot of keys and we do
 * it at relativly long interval times
 */
public class EtcdKVStore implements KVStore {

  private static final String HTTP_ERR = "ERR";

  private final ObjectMapper mapper;
  {
    mapper = new ObjectMapper();
    mapper.setNodeFactory(new PlaceholderEnabledJsonNodeFactory());
  }

  public static class EtcdNode {
    private String key;
    private long createdIndex;
    private long modifiedIndex;
    private String value;

    // For TTL keys
    private String expiration;
    private Integer ttl;


    public EtcdNode() {
      super();
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public long getCreatedIndex() {
      return createdIndex;
    }

    public void setCreatedIndex(long createdIndex) {
      this.createdIndex = createdIndex;
    }

    public long getModifiedIndex() {
      return modifiedIndex;
    }

    public void setModifiedIndex(long modifiedIndex) {
      this.modifiedIndex = modifiedIndex;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getExpiration() {
      return expiration;
    }

    public void setExpiration(String expiration) {
      this.expiration = expiration;
    }

    public Integer getTtl() {
      return ttl;
    }

    public void setTtl(Integer ttl) {
      this.ttl = ttl;
    }

  }

  public static class EtcdResult {
    // General values
    private String action;
    private EtcdNode node;

    // For errors
    private Integer errorCode;
    private String message;
    private String cause;
    private int errorIndex;

    public EtcdResult() {
      super();
    }

    public String getAction() {
      return action;
    }

    public void setAction(String action) {
      this.action = action;
    }

    public EtcdNode getNode() {
      return node;
    }

    public void setNode(EtcdNode node) {
      this.node = node;
    }

    public Integer getErrorCode() {
      return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
      this.errorCode = errorCode;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getCause() {
      return cause;
    }

    public void setCause(String cause) {
      this.cause = cause;
    }

    public int getErrorIndex() {
      return errorIndex;
    }

    public void setErrorIndex(int errorIndex) {
      this.errorIndex = errorIndex;
    }

  }


  /**
   *
   */
  public EtcdKVStore() {
    super();
  }

  /**
   * Get a key value from etcd. Returns the key value and the etcd modification index as version
   *
   * @param KeyURI URI of the key in the form etcd://ipaddr:port/path for an etcd cluster you can
   *        use etcd://[ipaddr1:port1, ipaddr:port2,...]:/path
   * @return a KeyValue object
   * @throws EmbeddedJmxTransException
   *
   * @see org.jmxtrans.embedded.config.KVStore#getKeyValue(java.lang.String)
   */
  public KeyValue getKeyValue(String KeyURI) throws EmbeddedJmxTransException {

    String etcdURI = KeyURI.substring(0, KeyURI.indexOf("/", 7));
    String key = KeyURI.substring(KeyURI.indexOf("/", 7));
    try {

      return getFromEtcd(makeEtcdBaseUris(etcdURI), key);

    } catch (Throwable t) {
      throw new EmbeddedJmxTransException("Exception reading etcd key '" + KeyURI + "': " + t.getMessage(), t);
    }
  }

  private URL[] makeEtcdBaseUris(String etcdURI) throws EmbeddedJmxTransException {
    String serverList = null;
    try {
      if (etcdURI.indexOf("[") > 0) {
        serverList = etcdURI.substring(etcdURI.indexOf("[") + 1, etcdURI.indexOf("]"));
      } else {
        serverList = etcdURI.substring(7, etcdURI.indexOf("/"));
      }

      StringTokenizer st = new StringTokenizer(serverList, ",");
      URL[] result = new URL[st.countTokens()];
      int k = 0;
      while (st.hasMoreTokens()) {
        result[k] = new URL("http://" + st.nextToken().trim());
        k++;
      }

      return result;

    } catch (Exception e) {
      throw new EmbeddedJmxTransException("Exception buildind etcd server list from: '" + etcdURI + "': " + e.getMessage(), e);
    }

  }

  private KeyValue getFromEtcd(URL[] baseUris, String key) throws EmbeddedJmxTransException {

    String json = null;
    int k = -1;
    while (k < baseUris.length - 1) {
      k++;
      String httpResponse = httpGET(baseUris[k], key);
      if (httpResponse == null) {
        // key not found on etcd server; since it's a cluster no need to try another one
        return null;
      }
      if (!HTTP_ERR.equals(httpResponse)) {
        json = httpResponse;
        break;
      }

    }

    if (json == null) {
      // couldn't get the key from etcd
      return null;
    }

    EtcdResult res = null;
    try {
      res = mapper.readValue(json, EtcdResult.class);
    } catch (Exception e) {
      throw new EmbeddedJmxTransException("Exception parsing etcd response: '" + json + "': " + e.getMessage(), e);
    }

    if (res.errorCode == null) {
      return new KeyValue(res.node.value, Long.toString(res.node.modifiedIndex));
    } else if (res.errorCode == 100) {
      // key not found
      return null;
    } else {
      throw new EmbeddedJmxTransException("Etcd error reading etcd key '" + key + "': " + res.errorCode);
    }

  }

  private String httpGET(URL base, String key) {

    InputStream is = null;
    HttpURLConnection conn = null;
    String json = null;
    try {
      URL url = new URL(base + "/v2/keys/" + key);
      conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
      conn.setRequestMethod("GET");
      conn.setConnectTimeout(2000);
      conn.setReadTimeout(2000);

      conn.connect();
      int respCode = conn.getResponseCode();

      if (respCode == 404) {
        return null;
      } else if (respCode > 400) {
        return HTTP_ERR;
      }

      is = conn.getInputStream();
      String contentEncoding = conn.getContentEncoding() != null ? conn.getContentEncoding() : "UTF-8";
      json = IOUtils.toString(is, contentEncoding);
      System.out.println("JSON: " + json);
    } catch (MalformedURLException e) {
      json = HTTP_ERR;
      // nothing to do, try next server
    } catch (ProtocolException e) {
      // nothing to do, try next server
      json = HTTP_ERR;
    } catch (IOException e) {
      // nothing to do, try next server
      json = HTTP_ERR;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // nothing to do, try next server
        }
      }
      if (conn != null) {
        conn.disconnect();
      }
    }

    return json;
  }
}
