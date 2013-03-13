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

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.Flushables;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.io.output.TeeOutputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.modules.cPickle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class GraphiteDataInjector {

    private final Random random = new Random();
    public int graphitePort = 2004;
    public String graphiteMetricPrefix = "servers.localhost";
    public String graphiteHost = "localhost";
    public int batchSize = 50;
    public boolean debug = false;
    public boolean generateDataPointsFile = true;
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    public GraphiteDataInjector() {

    }

    public static GraphiteDataInjector newHostedGraphiteDataInjector() {
        GraphiteDataInjector graphiteDataInjector = new GraphiteDataInjector();
        // TODO DEFINE YOUR_HOSTED_GRAPHITE_KEY
        String hostedGraphiteKey = null;
        try {
            hostedGraphiteKey = Resources.toString(Resources.getResource("hosted-graphite.credentials"), Charset.defaultCharset());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        graphiteDataInjector.graphiteMetricPrefix = hostedGraphiteKey + ".edu.servers.";
        graphiteDataInjector.graphiteHost = "carbon.hostedgraphite.com";
        graphiteDataInjector.setMaxGraphiteDataPointsPerSecond(100);
        graphiteDataInjector.batchSize = 50;
        return graphiteDataInjector;
    }

    public static GraphiteDataInjector newLocalhostGraphiteDataInjector() {
        GraphiteDataInjector graphiteDataInjector = new GraphiteDataInjector();
        graphiteDataInjector.graphiteHost = "localhost";
        graphiteDataInjector.graphiteMetricPrefix = "edu.servers.";
        graphiteDataInjector.setMaxGraphiteDataPointsPerSecond(10000);
        graphiteDataInjector.batchSize = 200;
        return graphiteDataInjector;
    }

    public void exportMetrics(TimeSeries... timeSeries) throws IOException {
        for (TimeSeries ts : timeSeries) {
            exportMetrics(ts);
        }
    }

    public void exportMetrics(TimeSeries timeSeries) throws IOException {
        System.out.println("Export '" + timeSeries.getKey() + "' to " + graphiteHost + " with prefix '" + graphiteMetricPrefix + "'");
        Socket socket = new Socket(graphiteHost, graphitePort);
        OutputStream outputStream = socket.getOutputStream();

        if (generateDataPointsFile) {
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Purchase", "date", "Amount", new TimeSeriesCollection(timeSeries), PlotOrientation.VERTICAL, true, true, false);
            // chart.getXYPlot().setRenderer(new XYSplineRenderer(60));

            File file = new File("/tmp/" + timeSeries.getKey() + ".png");
            ChartUtilities.saveChartAsPNG(file, chart, 1200, 800);
            System.out.println("Exported " + file.getAbsolutePath());

            String pickleFileName = "/tmp/" + timeSeries.getKey().toString() + ".pickle";
            System.out.println("Generate " + pickleFileName);
            outputStream = new TeeOutputStream(
                    outputStream,
                    new FileOutputStream(pickleFileName));
        }

        PyList list = new PyList();

        for (int i = 0; i < timeSeries.getItemCount(); i++) {
            if (debug)
                System.out.println(new DateTime(timeSeries.getDataItem(i).getPeriod().getStart()) + "\t" + timeSeries.getDataItem(i).getValue().intValue());
            String metricName = graphiteMetricPrefix + timeSeries.getKey().toString();
            int time = (int) TimeUnit.SECONDS.convert(timeSeries.getDataItem(i).getPeriod().getStart().getTime(), TimeUnit.MILLISECONDS);
            int value = timeSeries.getDataItem(i).getValue().intValue();

            list.add(new PyTuple(new PyString(metricName), new PyTuple(new PyInteger(time), new PyInteger(value))));

            if (list.size() >= batchSize) {
                System.out.print("-");
                rateLimiter.acquire(list.size());
                sendDataPoints(outputStream, list);
            }
        }

        // send last data points
        if (!list.isEmpty()) {
            rateLimiter.acquire(list.size());
            sendDataPoints(outputStream, list);
        }


        Flushables.flushQuietly(outputStream);
        Closeables.close(outputStream, true);
        try {
            socket.close();
        } catch (Exception e) {
            // swallow exception
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("Exported " + timeSeries.getKey() + ": " + timeSeries.getItemCount() + " items");
    }

    protected void sendDataPoints(OutputStream outputStream, PyList list) {
        try {
            PyString payload = cPickle.dumps(list);
            list.clear();

            byte[] header = ByteBuffer.allocate(4).putInt(payload.__len__()).array();

            outputStream.write(header);
            outputStream.write(payload.toBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMaxGraphiteDataPointsPerSecond(int maxGraphiteDataPointsPerSecond) {
        this.rateLimiter = RateLimiter.create(maxGraphiteDataPointsPerSecond);
    }

}
