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
package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class GraphiteWriterIntegrationTest {

    GraphiteWriter graphiteWriter;

    @Before
    public void before() {
        graphiteWriter = new GraphiteWriter();
        Map<String, Object> settings = new HashMap<String, Object>();
        settings.put(AbstractOutputWriter.SETTING_HOST, "localhost");
        settings.put(AbstractOutputWriter.SETTING_PORT, 2003);

        graphiteWriter.setSettings(settings);
        graphiteWriter.start();
    }

    @After
    public void after() throws Exception {
        graphiteWriter.stop();
    }

    @Test
    public void testWithOneResult() throws Exception {
        QueryResult queryResult = new QueryResult("testwithoneresult.singleresult", 10, System.currentTimeMillis());
        graphiteWriter.write(Collections.singleton(queryResult));
    }

    @Test
    public void testWithTwoResult() throws Exception {

        QueryResult queryResult1 = new QueryResult("testwithtworesult.first", 10, System.currentTimeMillis());
        QueryResult queryResult2 = new QueryResult("testwithtworesult.second", 20, System.currentTimeMillis());
        List<QueryResult> results = Arrays.asList(queryResult1, queryResult2);

        graphiteWriter.write(results);
    }
}
