/*
 * Copyright (c) 2010-2013 the original author or authors
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
    // TODO Auto-generated constructor stub
  }

  /**
   * Questo metodo
   *
   * @param KeyURI
   * @return
   * @throws EmbeddedJmxTransException
   *
   * @see org.jmxtrans.embedded.config.KVStore#getKeyValue(java.lang.String)
   */
  public KeyValue getKeyValue(String KeyURI) throws EmbeddedJmxTransException {

    String etcdURI = KeyURI.substring(0, KeyURI.indexOf("/", 7));
    String key = KeyURI.substring(KeyURI.indexOf("/", 7));
    EtcdClient etcd = null;
    try {
      etcd = new EtcdClient(URI.create(etcdURI));
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

}
