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
package org.jmxtrans.embedded;

import org.jmxtrans.embedded.output.OutputWriter;
import org.junit.Assert;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class TestUtils {

    public static Map<String, QueryAttribute> indexQueryAttributesByAliasOrName(Iterable<QueryAttribute> queryAttributes) {
        Map<String, QueryAttribute> results = new HashMap<String, QueryAttribute>();
        for (QueryAttribute queryAttribute : queryAttributes) {
            String key = queryAttribute.getResultAlias() == null ? queryAttribute.getName() : queryAttribute.getResultAlias();
            results.put(key, queryAttribute);
        }

        return results;
    }

    public static Map<String, Query> indexQueriesByAliasOrName(Iterable<Query> queries) {
        Map<String, Query> results = new HashMap<String, Query>();
        for (Query query : queries) {
            String key = query.getResultAlias() == null ? query.getObjectName().toString() : query.getResultAlias();
            results.put(key, query);
        }
        return results;
    }

    public static Map<String, Query> indexQueriesByName(Iterable<Query> queries) {
        Map<String, Query> results = new HashMap<String, Query>();
        for (Query query : queries) {
            results.put(query.getObjectName().toString() , query);
        }
        return results;
    }

    public static Map<Class<? extends OutputWriter>, OutputWriter> indexOutputWritersByClass(Iterable<OutputWriter> outputWriters) {
        Map<Class<? extends OutputWriter>, OutputWriter> results = new HashMap<Class<? extends OutputWriter>, OutputWriter>();
        for (OutputWriter outputWriter : outputWriters) {
            results.put(outputWriter.getClass(), outputWriter);
        }
        return results;
    }

    @Nonnull
    public static Map<String, Object> loadSettingsFromClasspath(@Nonnull String filePath) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        Assert.assertNotNull("File " + filePath + " was not found in the classpath.", in);
        Properties config = new Properties();
        try {
            config.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Exception loading '" + filePath + "'", e);
        }

        return new HashMap<String, Object>((Map) config);
    }

    public static void generateJvmActivity() throws Exception {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {

            String msg = "";
            for (int j = 0; j < 100; j++) {
                int[] buffer = new int[2048];
                for (int bufferIdx = 0; bufferIdx < buffer.length; bufferIdx++) {
                    buffer[bufferIdx] = random.nextInt();
                }
                int total = 0;
                for (int aBuffer : buffer) {
                    total += aBuffer;
                }
                msg += total + " ";
                TimeUnit.MILLISECONDS.sleep(10);
            }
            System.out.println(msg);
        }
    }
}
