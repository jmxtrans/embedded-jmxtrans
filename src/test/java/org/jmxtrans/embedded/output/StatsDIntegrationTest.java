package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.EmbeddedJmxTrans;
import org.jmxtrans.embedded.config.ConfigurationParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
@Ignore
public class StatsDIntegrationTest {
    public static void main(String[] args) throws Exception {
        StatsDIntegrationTest test = new StatsDIntegrationTest();
        test.test();
    }

    @Test
    public void test() throws Exception {
        List<String> configurationFiles = Arrays.asList(
                "classpath:org/jmxtrans/embedded/config/jvm-sun-hotspot.json",
                "classpath:org/jmxtrans/embedded/config/jmxtrans-internals.json",
                "classpath:org/jmxtrans/embedded/output/statsd-writer.json"
        );

        ConfigurationParser configurationParser = new ConfigurationParser();
        EmbeddedJmxTrans embeddedJmxTrans = configurationParser.newEmbeddedJmxTrans(configurationFiles);
        embeddedJmxTrans.setQueryIntervalInSeconds(5);
        embeddedJmxTrans.setExportIntervalInSeconds(10);
        embeddedJmxTrans.start();
        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
        embeddedJmxTrans.stop();
        System.out.println("bye");
    }
}
