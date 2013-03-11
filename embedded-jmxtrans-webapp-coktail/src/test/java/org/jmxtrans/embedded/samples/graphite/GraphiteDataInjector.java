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

import com.google.common.io.Closeables;
import com.google.common.io.Flushables;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.io.output.TeeOutputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.modules.cPickle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
    private int graphitePort = 2004;
    private String graphiteMetricPrefix = "servers.localhost";
    private String graphiteHost = "localhost";
    private int[] weeklyDistribution = new int[8];
    private int[] hourlyDistribution = new int[24];
    private RateLimiter rateLimiter = RateLimiter.create(1000);
    private int batchSize = 50;
    private boolean debug = false;
    private boolean generateDataPointsFile = true;

    public GraphiteDataInjector() {
        weeklyDistribution[DateTimeConstants.MONDAY] = 2;
        weeklyDistribution[DateTimeConstants.TUESDAY] = 2;
        weeklyDistribution[DateTimeConstants.WEDNESDAY] = 6;
        weeklyDistribution[DateTimeConstants.THURSDAY] = 8;
        weeklyDistribution[DateTimeConstants.FRIDAY] = 10;
        weeklyDistribution[DateTimeConstants.SATURDAY] = 10;
        weeklyDistribution[DateTimeConstants.SUNDAY] = 8;

        hourlyDistribution[0] = 8;
        hourlyDistribution[1] = 8;
        hourlyDistribution[2] = 5;
        hourlyDistribution[3] = 5;
        hourlyDistribution[4] = 3;
        hourlyDistribution[5] = 3;
        hourlyDistribution[6] = 3;
        hourlyDistribution[7] = 3;
        hourlyDistribution[8] = 3;
        hourlyDistribution[9] = 3;
        hourlyDistribution[10] = 3;
        hourlyDistribution[11] = 8;
        hourlyDistribution[12] = 8;
        hourlyDistribution[13] = 8;
        hourlyDistribution[14] = 8;
        hourlyDistribution[15] = 6;
        hourlyDistribution[16] = 6;
        hourlyDistribution[17] = 6;
        hourlyDistribution[18] = 8;
        hourlyDistribution[19] = 8;
        hourlyDistribution[20] = 10;
        hourlyDistribution[21] = 10;
        hourlyDistribution[22] = 10;
        hourlyDistribution[23] = 10;

    }

    public static void main(String[] args) throws Exception {
        GraphiteDataInjector graphiteDataInjector = new GraphiteDataInjector();

        boolean useHostedGraphite = true;
        if (useHostedGraphite) {
            // TODO DEFINE YOUR_HOSTED_GRAPHITE_KEY
            String hostedGraphiteKey = Resources.toString(Resources.getResource("hosted-graphite.credentials"), Charset.defaultCharset());
            graphiteDataInjector.graphiteMetricPrefix = hostedGraphiteKey + ".edu.servers.";
            graphiteDataInjector.graphiteHost = "carbon.hostedgraphite.com";
            graphiteDataInjector.setMaxGraphiteDataPointsPerSecond(100);
            graphiteDataInjector.batchSize = 50;
        } else {
            graphiteDataInjector.graphiteHost = "localhost";
            graphiteDataInjector.graphiteMetricPrefix = "edu.servers.";
            graphiteDataInjector.setMaxGraphiteDataPointsPerSecond(10000);
            graphiteDataInjector.batchSize = 200;
        }

        graphiteDataInjector.generateLoad();
    }

    public void generateLoad() throws Exception {
        System.out.println("Inject data on Graphite server '" + this.graphiteHost + "' with prefix '" + this.graphiteMetricPrefix + "'");

        TimeSeries rawIntegratedTimeSeries = new TimeSeries("sales.integrated.raw");
        TimeSeries rawTimeSeries = new TimeSeries("sales.raw");

        DateTime now = new DateTime();
        DateTime end = now.plusDays(1);

        DateTime date = now.minusDays(15);
        DateTime twoDaysAfterBegin = date.plusDays(2);
        double serverFairness = 1.05;


        int integratedValue = 0;

        MathContext mathContext = new MathContext(1, RoundingMode.CEILING);

        int randomFactor = 0;

        while (date.isBefore(end)) {
            if (rawIntegratedTimeSeries.getItemCount() % 120 == 0) {
                randomFactor = 10 + random.nextInt(2);
            }
            int weekGrowthFactor = 6 - (now.getWeekOfWeekyear() - date.getWeekOfWeekyear());
            int value =
                    new BigDecimal(randomFactor) // random factor
                            .multiply(new BigDecimal(10)) // go to cents of USD
                            .multiply(new BigDecimal(weekGrowthFactor))
                            .multiply(new BigDecimal(hourlyDistribution[date.getHourOfDay()]))
                            .multiply(new BigDecimal(weeklyDistribution[date.getDayOfWeek()]))
                            .divide(new BigDecimal(20), mathContext).intValue(); // split hourly value in minutes


            integratedValue += value;
            for (int i1 = 0; i1 < 3; i1++) {
                Minute period = new Minute(date.toDate());
                rawTimeSeries.add(period, value);
                rawIntegratedTimeSeries.add(period, integratedValue);
                date = date.plusMinutes(1);
            }
        }

        rawIntegratedTimeSeries = MovingAverage.createMovingAverage(rawIntegratedTimeSeries, rawIntegratedTimeSeries.getKey().toString(), 60 * 7, 0);
        rawTimeSeries = MovingAverage.createMovingAverage(rawTimeSeries, rawTimeSeries.getKey().toString(), 60 * 7, 0);

        // SALES - REVENUE

        TimeSeries salesRevenueInCentsCounter = new TimeSeries("sales.revenueInCentsCounter");
        TimeSeries salesRevenueInCentsCounterSrv1 = new TimeSeries("srv1.sales.revenueInCentsCounter");
        TimeSeries salesRevenueInCentsCounterSrv2 = new TimeSeries("srv2.sales.revenueInCentsCounter");
        int resetValue2ToZeroOffset = 0; // reset value 2 after 3 days of metrics
        for (int i = 0; i < rawIntegratedTimeSeries.getItemCount(); i++) {
            TimeSeriesDataItem dataItem = rawIntegratedTimeSeries.getDataItem(i);
            int value = dataItem.getValue().intValue();
            // value1 is 5% higher to value2 due to a 'weirdness' in the load balancing
            int value1 = Math.min((int) (value * serverFairness / 2), value);

            {
                // simulate srv2 restart
                DateTime currentDate = new DateTime(dataItem.getPeriod().getStart());
                boolean shouldResetValue2 = resetValue2ToZeroOffset == 0 && currentDate.getDayOfYear() == twoDaysAfterBegin.getDayOfYear();
                if (shouldResetValue2) {
                    resetValue2ToZeroOffset = value - value1;
                    System.out.println("reset value2 of " + resetValue2ToZeroOffset + " at " + currentDate);
                }
            }

            int value2 = value - value1 - resetValue2ToZeroOffset;
            salesRevenueInCentsCounter.add(dataItem.getPeriod(), value);
            salesRevenueInCentsCounterSrv1.add(dataItem.getPeriod(), value1);
            salesRevenueInCentsCounterSrv2.add(dataItem.getPeriod(), value2);
        }
        exportMetrics(
                salesRevenueInCentsCounter, salesRevenueInCentsCounterSrv1, salesRevenueInCentsCounterSrv2);


        // SALES - ITEMS
        TimeSeries salesItemsCounter = new TimeSeries("sales.itemsCounter");
        TimeSeries salesItemsCounterSrv1 = new TimeSeries("srv1.sales.itemsCounter");
        TimeSeries salesItemsCounterSrv2 = new TimeSeries("srv2.sales.itemsCounter");

        for (int i = 0; i < rawIntegratedTimeSeries.getItemCount(); i++) {
            RegularTimePeriod period = salesRevenueInCentsCounter.getDataItem(i).getPeriod();
            int ordersPriceInCents1 = salesRevenueInCentsCounterSrv1.getDataItem(i).getValue().intValue();
            int ordersPriceInCents2 = salesRevenueInCentsCounterSrv2.getDataItem(i).getValue().intValue();

            int value1 = ordersPriceInCents1 / 600;
            int value2 = ordersPriceInCents2 / 600;

            salesItemsCounter.add(period, value1 + value2);
            salesItemsCounterSrv1.add(period, value1);
            salesItemsCounterSrv2.add(period, value2);

        }

        exportMetrics(
                salesItemsCounter, salesItemsCounterSrv1, salesItemsCounterSrv2);

        // WEBSITE - VISITORS
        TimeSeries newVisitorsCounterSrv1 = new TimeSeries("srv1.website.visitors.newVisitorsCounter");
        TimeSeries newVisitorsCounterSrv2 = new TimeSeries("srv1.website.visitors.newVisitorsCounter");

        TimeSeries activeVisitorsGaugeSrv1 = new TimeSeries("srv1.website.visitors.activeGauge");
        TimeSeries activeVisitorsGaugeSrv2 = new TimeSeries("srv2.website.visitors.activeGauge");
        int integratedValue1 = 0;
        int integratedValue2 = 0;
        float activeVisitorsFactor = 1;
        for (int i = 0; i < rawTimeSeries.getItemCount(); i++) {

            TimeSeriesDataItem dataItem = rawTimeSeries.getDataItem(i);
            RegularTimePeriod period = dataItem.getPeriod();
            int value = dataItem.getValue().intValue() / 20;
            integratedValue += value;


            int value1 = Math.min((int) (value * serverFairness / 2), value);
            integratedValue1 += value1;

            int value2 = value - value1;
            integratedValue2 += value2;

            newVisitorsCounterSrv1.add(period, integratedValue1);
            newVisitorsCounterSrv2.add(period, integratedValue2);

            if (i % 120 == 0) {
                activeVisitorsFactor = (10 + random.nextInt(3)) / 10;
            }

            activeVisitorsGaugeSrv1.add(period, Math.floor(value1 * activeVisitorsFactor));
            activeVisitorsGaugeSrv2.add(period, Math.floor(value2 * activeVisitorsFactor));
        }

        exportMetrics(newVisitorsCounterSrv1, newVisitorsCounterSrv2, activeVisitorsGaugeSrv1, activeVisitorsGaugeSrv2);


    }

    public void exportMetrics(TimeSeries... timeSeries) throws IOException {
        for (TimeSeries ts : timeSeries) {
            exportMetrics(ts);
        }
    }

    public void exportMetrics(TimeSeries timeSeries) throws IOException {
        System.out.println("Export " + timeSeries.getKey());
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
