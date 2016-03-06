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

/**
 * This class is a value retrieved from a key value store. Associated with the value comes the
 * version. The version is a unique id of the value of the key it can be used to determine if a key
 * has been changed. Etcd and Consul use an "index", a monotone incrementing number, Zookeeper uses
 * transaction ids
 */
public class KeyValue {

  private final String value;
  private final String version;

  /**
   * Constructor
   */
  public KeyValue(String val, String idx) {
    value = val;
    version = idx;
  }

  /**
   * The value of the key
   *
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   * The version of the key
   *
   * @return
   */
  public String getVersion() {
    return version;
  }


}
