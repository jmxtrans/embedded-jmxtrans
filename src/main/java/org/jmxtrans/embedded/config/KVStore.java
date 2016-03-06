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

import org.jmxtrans.embedded.EmbeddedJmxTransException;

/**
 * This interface represents a super simplified key value store from which you can only read keys.
 * You get back the key value and a version id
 */
public interface KVStore {

  /**
   * Retrieves the value of a key from the kv store. The version can be used to determine if a key
   * was changed since the last read
   *
   * @param KeyURI: uri of the key eg: etcd://127.0.0.1:123/level1/config for a cluster you can use
   *        etcd://[ipaddr1:port1, ipaddr:port2,...]:/path
   * @return a KeyValue object which hold the key value and the version id (modification index or
   *         trx id)
   * @throws EmbeddedJmxTransException
   */
  public KeyValue getKeyValue(String KeyURI) throws EmbeddedJmxTransException;

}
