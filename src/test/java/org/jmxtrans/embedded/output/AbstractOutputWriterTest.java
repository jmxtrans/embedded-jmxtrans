package org.jmxtrans.embedded.output;

import org.jmxtrans.embedded.QueryResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractOutputWriterTest {

    @Test
    public void test_toString_should_start_with_fully_qualified_classname() {
        AbstractOutputWriter abstractOutputWriter = new AbstractOutputWriter() {
            @Override
            public void write(Iterable<QueryResult> results) {

            }
        };
        String toStringValue = abstractOutputWriter.toString();
        String expectedStartString = AbstractOutputWriterTest.class.getName();
        assertTrue("toString() value should start with " + expectedStartString, toStringValue.startsWith(expectedStartString));
    }

    @Test
    public void test_toString_exact() {
        AbstractOutputWriter abstractOutputWriter = new AbstractOutputWriter() {
            @Override
            public void write(Iterable<QueryResult> results) {

            }
        };
        String toStringValue = abstractOutputWriter.toString();
        assertEquals("org.jmxtrans.embedded.output.AbstractOutputWriterTest$2{enabled=true, settings={}}", toStringValue);
    }

}
