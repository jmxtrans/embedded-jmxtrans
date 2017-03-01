package org.jmxtrans.embedded.config;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jmxtrans.embedded.EmbeddedJmxTrans;
import org.jmxtrans.embedded.Query;
import org.jmxtrans.embedded.QueryResult;
import org.jmxtrans.embedded.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationResultNameStrategyTest
{
    static Map<String, Query> queriesByResultName;

    static EmbeddedJmxTrans embeddedJmxTrans;

    private static final String ALIAS_NAME = "%name%";

    @BeforeClass
    public static void beforeClass() throws Exception {
        ConfigurationParser configurationParser = new ConfigurationParser();
        embeddedJmxTrans = configurationParser.newEmbeddedJmxTrans("classpath:org/jmxtrans/embedded/jmxtrans-config-resultnamestrategy.json");
        queriesByResultName = TestUtils.indexQueriesByName(embeddedJmxTrans.getQueries());
    }

    @Test
    public void validateDefaultReplaceDotsInObjectName()
                    throws MalformedObjectNameException, InterruptedException
    {
        ObjectName objectName = new ObjectName("org:name=org.jmxtrans.embedded.default");
        Query query = queriesByResultName.get(objectName.toString());
        assertThat(query.getObjectName(), is(objectName));
        assertThat(query.getResultAlias(), is (ALIAS_NAME));
        BlockingQueue<QueryResult> queue = new ArrayBlockingQueue(1  );
        query.getQueryAttributes().iterator().next().collectMetrics(query.getObjectName(),1,0,  queue);

        QueryResult result = queue.poll( 1, TimeUnit.SECONDS );

        assertThat (result.getName(), is ("org_jmxtrans_embedded_default.Value"));
    }


    @Test
    public void validateReplaceDotsInObjectName()
                    throws MalformedObjectNameException, InterruptedException
    {
        ObjectName objectName = new ObjectName("org:name=org.jmxtrans.embedded.replace");
        Query query = queriesByResultName.get(objectName.toString());
        assertThat(query.getObjectName(), is(objectName));
        assertThat(query.getResultAlias(), is (ALIAS_NAME));
        BlockingQueue<QueryResult> queue = new ArrayBlockingQueue(1  );
        query.getQueryAttributes().iterator().next().collectMetrics(query.getObjectName(),1,0,  queue);

        QueryResult result = queue.poll( 1, TimeUnit.SECONDS );

        assertThat (result.getName(), is ("org_jmxtrans_embedded_replace.Value"));
    }



    @Test
    public void validateNoReplaceDotsInObjectName()
                    throws MalformedObjectNameException, InterruptedException
    {
        ObjectName objectName = new ObjectName("org:name=org.jmxtrans.embedded.noreplace");
        Query query = queriesByResultName.get(objectName.toString());
        assertThat(query.getObjectName(), is(objectName));
        assertThat(query.getResultAlias(), is (ALIAS_NAME));
        BlockingQueue<QueryResult> queue = new ArrayBlockingQueue(1  );
        query.getQueryAttributes().iterator().next().collectMetrics(query.getObjectName(),1,0,  queue);

        QueryResult result = queue.poll( 1, TimeUnit.SECONDS );

        assertThat (result.getName(), is ("org.jmxtrans.embedded.noreplace.Value"));
    }

}
