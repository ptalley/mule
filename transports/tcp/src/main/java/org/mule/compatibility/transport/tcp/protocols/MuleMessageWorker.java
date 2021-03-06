/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.transport.tcp.protocols;

import static org.mule.runtime.core.message.DefaultEventBuilder.EventImplementation.getCurrentEvent;

import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.transformer.wire.WireFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for Mule message handling so that we can apply the same logic across all sub-protocols (default, EOF and length).
 */
class MuleMessageWorker {

  private final WireFormat wireFormat;

  MuleMessageWorker(WireFormat wireFormat) {
    this.wireFormat = wireFormat;
  }

  public byte[] doWrite() throws IOException {
    // TODO fix the api here so there is no need to use the RequestContext
    InternalMessage msg = getCurrentEvent().getMessage();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      wireFormat.write(baos, msg, msg.getPayload().getDataType().getMediaType().getCharset().orElse(null));
    } catch (MuleException e) {
      throw new IOException(e.getDetailedMessage());
    }
    return baos.toByteArray();
  }

  public Object doRead(Object message) throws IOException {
    if (message == null) {
      return null;
    }

    InputStream in;
    if (message instanceof byte[]) {
      in = new ByteArrayInputStream((byte[]) message);
    } else {
      in = (InputStream) message;
    }

    try {
      return wireFormat.read(in);
    } catch (MuleException e) {
      throw new IOException(e.getDetailedMessage());
    }

  }

}
