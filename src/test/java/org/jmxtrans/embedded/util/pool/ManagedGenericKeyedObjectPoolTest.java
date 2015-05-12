/*
 * Copyright (c) 2010-2013 the original author or authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.jmxtrans.embedded.util.pool;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;


import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class ManagedGenericKeyedObjectPoolTest {

    @Test
    public void testMbeanAttributeAccess() throws Exception {
        BaseKeyedPooledObjectFactory<String, String> factory = new BaseKeyedPooledObjectFactory<String, String>() {
            @Override
            public String create(String key) throws Exception {
                return key;
            }

            @Override
            public PooledObject<String> wrap(String value) {
                return new DefaultPooledObject<String>(value);
            }
        };
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setJmxNameBase("org.jmxtrans.embedded:type=GenericKeyedObjectPool,writer=MyWriter,name=");
        config.setJmxNamePrefix("my-host_1234");
        GenericKeyedObjectPool<String, String> objectPool = new GenericKeyedObjectPool<String, String>(factory, config);

        ObjectName objectName = new ObjectName("org.jmxtrans.embedded:type=GenericKeyedObjectPool,writer=MyWriter,name=my-host_1234");
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            Object numIdle = mbeanServer.getAttribute(objectName, "NumIdle");
            assertThat(numIdle, instanceOf(Number.class));

        } finally {
            mbeanServer.unregisterMBean(objectName);
        }
    }
}
