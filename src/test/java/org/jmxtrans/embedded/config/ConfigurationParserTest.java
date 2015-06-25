package org.jmxtrans.embedded.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.jmxtrans.embedded.EmbeddedJmxTrans;
import org.jmxtrans.embedded.Query;
import org.jmxtrans.embedded.output.OutputWriterSet;
import org.jmxtrans.embedded.util.plumbing.QueryResultSource;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ThrowsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConfigurationParserTest {

    private final ThrowsException defaultMockAnswer = new ThrowsException(new UnsupportedOperationException());

    @Test(expected = JsonMappingException.class)
    public void should_fail_on_empty_config_resource() throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        parser.mergeEmbeddedJmxTransConfiguration(new ByteArrayInputStream(new byte[0]), embeddedJmxTrans);
    }

    @Test
    public void should_parse_one_query_without_any_outputwriter() throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doNothing().when(embeddedJmxTrans).addQuery(any(Query.class), any(QueryResultSource.class));

        InputStream resourceInputStream = null;
        try {
            resourceInputStream = getClass().getResourceAsStream("one_query_without_any_outputwriter.json");
            parser.mergeEmbeddedJmxTransConfiguration(resourceInputStream, embeddedJmxTrans);
        } finally {
            IOUtils.closeQuietly(resourceInputStream);
        }
    }

    @Test
    public void should_parse_one_query_with_one_specific_outputwriter() throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doNothing().when(embeddedJmxTrans).addQuery(any(Query.class), any(QueryResultSource.class));

        InputStream resourceInputStream = null;
        try {
            resourceInputStream = getClass().getResourceAsStream("one_query_with_one_specific_outputwriter.json");
            parser.mergeEmbeddedJmxTransConfiguration(resourceInputStream, embeddedJmxTrans);
        } finally {
            IOUtils.closeQuietly(resourceInputStream);
        }
    }

    @Test
    public void should_parse_one_query_with_one_global_outputwriter() throws IOException {
        ConfigurationParser parser = new ConfigurationParser();

        OutputWriterSet globalOutputWriterSet = mock(OutputWriterSet.class, defaultMockAnswer);
        doNothing().when(globalOutputWriterSet).addAll(anyCollection());

        EmbeddedJmxTrans embeddedJmxTrans = mock(EmbeddedJmxTrans.class, defaultMockAnswer);
        doNothing().when(embeddedJmxTrans).addQuery(any(Query.class), any(QueryResultSource.class));
        doReturn(globalOutputWriterSet).when(embeddedJmxTrans).getOutputWriters();

        InputStream resourceInputStream = null;
        try {
            resourceInputStream = getClass().getResourceAsStream("one_query_with_one_global_outputwriter.json");
            parser.mergeEmbeddedJmxTransConfiguration(resourceInputStream, embeddedJmxTrans);
        } finally {
            IOUtils.closeQuietly(resourceInputStream);
        }
    }


}
