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
import java.net.URI;
import java.util.StringTokenizer;

import org.jmxtrans.embedded.EmbeddedJmxTransException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

/**
 * This is an etcd based KVStore implementation. The connection to etcd is estabilished and closed
 * every time a key is read. This is by design since we don't need to read a lot of keys and we do
 * it at relativly long interval times
 */
public class EtcdKVStore implements KVStore {

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
    EtcdClient etcd = null;
    try {
      etcd = new EtcdClient(makeEtcdUris(etcdURI));
      EtcdResponsePromise<EtcdKeysResponse> conf = etcd.get(key).send();
      EtcdKeysResponse resp = conf.get();
      String keyVal = resp.node.value;

      return new KeyValue(keyVal, Long.toString(resp.node.modifiedIndex));

    } catch (EtcdException e) {
      if (e.errorCode == 100) {
        return null;
      } else {
        throw new EmbeddedJmxTransException("Exception reading etcd key '" + KeyURI + "': " + e.getMessage(), e);
      }

    } catch (Throwable t) {
      throw new EmbeddedJmxTransException("Exception reading etcd key '" + KeyURI + "': " + t.getMessage(), t);
    } finally {
      try {
        if (etcd != null) {
          etcd.close();
        }
      } catch (IOException e) {
        // Nothing to do closing
      }
    }
  }

  private URI[] makeEtcdUris(String etcdURI) throws EmbeddedJmxTransException {
    String serverList = null;
    try {
      if (etcdURI.indexOf("[") > 0) {
        serverList = etcdURI.substring(etcdURI.indexOf("[") + 1, etcdURI.indexOf("]"));
      } else {
        serverList = etcdURI.substring(7, etcdURI.indexOf("/"));
      }

      StringTokenizer st = new StringTokenizer(serverList, ",");
      URI[] result = new URI[st.countTokens()];
      int k = 0;
      while (st.hasMoreTokens()) {
        result[k] = URI.create("etcd://" + st.nextToken().trim());
        k++;
      }

      return result;

    } catch (Exception e) {
      throw new EmbeddedJmxTransException("Exception buildind etcd server list from: '" + etcdURI + "': " + e.getMessage(), e);
    }

  }
}
