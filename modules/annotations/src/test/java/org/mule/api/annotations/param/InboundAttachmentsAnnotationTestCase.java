/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.api.annotations.param;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.expression.RequiredValueException;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.StringDataSource;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.junit.Before;
import org.junit.Test;

public class InboundAttachmentsAnnotationTestCase extends FunctionalTestCase
{
    private MuleMessage muleMessage;

    public InboundAttachmentsAnnotationTestCase()
    {
        setDisposeManagerPerSuite(true);
    }

    @Override
    protected String getConfigResources()
    {
        return "inbound-attachments-annotation.xml";
    }

    @Before
    @Override
    public void doSetUp() throws Exception
    {
        muleMessage = new DefaultMuleMessage("test", muleContext);

        try
        {
            muleMessage.addAttachment("foo", new DataHandler(new StringDataSource("fooValue")));
            muleMessage.addAttachment("bar", new DataHandler(new StringDataSource("barValue")));
            muleMessage.addAttachment("baz", new DataHandler(new StringDataSource("bazValue")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testSingleAttachment() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachment", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue(message.getPayload() instanceof DataHandler);
        assertEquals("fooValue", ((DataHandler)message.getPayload()).getContent());
    }


    @Test
    public void testSingleAttachmentWithType() throws Exception
    {
        //These should really be in core, but the @Transformer annotation is not in core
        muleContext.getRegistry().registerObject("dataHandlerTransformers", new DataHandlerTransformer());

        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentWithType", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue(message.getPayload() instanceof String);
        assertEquals("fooValue", message.getPayload());
    }

    @Test
    public void testSingleAttachmentOptional() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertEquals("faz not set", message.getPayload());
    }

    @Test
    public void testSingleAttachmentWithTypeNoMatchingTransform() throws Exception
    {
        //TODO this test still works because
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentWithType", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue(message.getPayload() instanceof String);
        assertEquals("fooValue", message.getPayload());
    }

    @Test
    public void testMapAttachments() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachments", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map<String, DataHandler> result = (Map) message.getPayload();
        assertEquals(2, result.size());
        assertEquals("fooValue", result.get("foo").getContent());
        assertEquals("barValue", result.get("bar").getContent());
        assertNull(result.get("baz"));
    }

    @Test
    public void testMapAttachmentsMissing() throws Exception
    {
        muleMessage.removeAttachment("foo");
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachments", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertNotNull(message.getExceptionPayload());
        assertTrue(message.getExceptionPayload().getRootException() instanceof RequiredValueException);
    }

    @Test
    public void testMapSingleAttachment() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://singleAttachmentMap", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map<String,  DataHandler> result = (Map) message.getPayload();
        assertEquals(1, result.size());
        assertEquals("fooValue", result.get("foo").getContent());
        assertNull(result.get("bar"));
        assertNull(result.get("baz"));
    }

    @Test
    public void testMapAttachmentsOptional() throws Exception
    {
        muleMessage.removeAttachment("baz");

        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map<String, DataHandler> result = (Map) message.getPayload();
        assertEquals(2, result.size());
        assertEquals("fooValue", result.get("foo").getContent());
        assertEquals("barValue", result.get("bar").getContent());
        assertNull(result.get("baz"));
    }

    @Test
    public void testMapAttachmentsAllOptional() throws Exception
    {
        muleMessage.removeAttachment("foo");
        muleMessage.removeAttachment("bar");
        muleMessage.removeAttachment("baz");

        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsAllOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map result = (Map) message.getPayload();
        assertEquals(0, result.size());
    }

    @Test
    public void testMapAttachmentsUnmodifiable() throws Exception
    {

        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsUnmodifiable", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertNotNull("Exception should have been thrown", message.getExceptionPayload());
        assertTrue(message.getExceptionPayload().getRootException() instanceof UnsupportedOperationException);
    }

    @Test
    public void testMapAttachmentsAll() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsAll", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map<String, DataHandler> result = message.getPayload(Map.class);
        //Will include all Mule attachments too
        assertTrue(result.size() >= 3);
        assertEquals("fooValue", result.get("foo").getContent());
        assertEquals("barValue", result.get("bar").getContent());
        assertEquals("bazValue", result.get("baz").getContent());
    }

    @Test
    public void testMapAttachmentsWildcard() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsWildcard", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map result = message.getPayload(Map.class);
        //Will match on ba*
        assertEquals(2, result.size());
        assertNull(result.get("foo"));
        assertNotNull(result.get("bar"));
        assertNotNull(result.get("baz"));
    }

    @Test
    public void testMapAttachmentsMultiWildcard() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsMultiWildcard", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a Map", message.getPayload() instanceof Map);
        Map result = (Map) message.getPayload();
        //Will match on ba*, f*
        assertEquals(3, result.size());

        assertNotNull(result.get("foo"));
        assertNotNull(result.get("bar"));
        assertNotNull(result.get("baz"));
    }

    @Test
    public void testListAttachments() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsList", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        assertEquals(3, result.size());
        assertTrue(result.contains("fooValue"));
        assertTrue(result.contains("barValue"));
        assertTrue(result.contains("bazValue"));
    }

    @Test
    public void testListAttachmentsWithOptional() throws Exception
    {
        muleMessage.removeAttachment("baz");
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        assertEquals(2, result.size());
        assertTrue(result.contains("fooValue"));
        assertTrue(result.contains("barValue"));
    }

    @Test
    public void testListAttachmentsWithAllOptional() throws Exception
    {
        muleMessage.removeAttachment("foo");
        muleMessage.removeAttachment("bar");
        muleMessage.removeAttachment("baz");

        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListAllOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        assertEquals(0, result.size());
    }

    @Test
    public void testListAttachmentsWithMissing() throws Exception
    {
        muleMessage.removeAttachment("bar");
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListOptional", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertNotNull(message.getExceptionPayload());
        assertTrue(message.getExceptionPayload().getRootException() instanceof RequiredValueException);
    }

    @Test
    public void testSingleListAttachment() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://singleAttachmentList", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        assertEquals(1, result.size());
        assertTrue(result.contains("fooValue"));
    }

    @Test
    public void testListAttachmentsUnmodifiable() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListUnmodifiable", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertNotNull("Exception should have been thrown", message.getExceptionPayload());
        assertTrue(message.getExceptionPayload().getRootException() instanceof UnsupportedOperationException);
    }

    @Test
    public void testListAttachmentsAll() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListAll", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        //Will include all Mule attachments too
        assertTrue(result.size() >= 3);
        assertTrue(result.contains("fooValue"));
        assertTrue(result.contains("barValue"));
        assertTrue(result.contains("bazValue"));
    }

    @Test
    public void testListAttachmentsWilcard() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListWildcard", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        //Will match all attachments with ba*
        assertEquals(2, result.size());
        assertFalse(result.contains("fooValue"));
        assertTrue(result.contains("barValue"));
        assertTrue(result.contains("bazValue"));

    }

    @Test
    public void testListAttachmentsMultiWilcard() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage message = client.send("vm://attachmentsListMultiWildcard", muleMessage);
        assertNotNull("return message from MuleClient.send() should not be null", message);
        assertTrue("Message payload should be a List", message.getPayload() instanceof List);
        List result = (List) message.getPayload();
        //Will match all attachments with ba* and f*
        assertEquals(3, result.size());
        assertTrue(result.contains("fooValue"));
        assertTrue(result.contains("barValue"));
        assertTrue(result.contains("bazValue"));
    }
}
