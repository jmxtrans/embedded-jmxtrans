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
package org.jmxtrans.embedded.samples.graphite;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.joda.time.DateTime;

import java.util.Random;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class BasicAppMetricsSimulator {

    private final Random random = new Random();

    public static void main(String[] args) throws Exception {
        GraphiteDataInjector graphiteDataInjector;
        boolean useHostedGraphite = false;
        if (useHostedGraphite) {
            graphiteDataInjector = GraphiteDataInjector.newHostedGraphiteDataInjector();
        } else {
            graphiteDataInjector = GraphiteDataInjector.newLocalhostGraphiteDataInjector();
        }


        new BasicAppMetricsSimulator().generateLoad(graphiteDataInjector);
    }

    public void generateLoad(GraphiteDataInjector graphiteDataInjector) throws Exception {

        int dataPointIntervalInSeconds = 30;
        int scaleFactor = 2;

        TimeSeries timeSeries = new TimeSeries("www.requestsCounter");

        DateTime now = new DateTime();
        DateTime end = now.plusDays(2);

        DateTime date = now.minusDays(1);

        int integratedValue = 0;

        while (date.isBefore(end)) {
            integratedValue += dataPointIntervalInSeconds * scaleFactor;
            timeSeries.add(new Second(date.toDate()), integratedValue);
            date = date.plusSeconds(dataPointIntervalInSeconds);
        }

        graphiteDataInjector.exportMetrics(timeSeries);


    }
}
