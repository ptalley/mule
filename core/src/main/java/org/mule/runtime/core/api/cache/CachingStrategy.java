/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.cache;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.processor.Processor;

/**
 * Defines a way to process a {@link Event} using a cache.
 */
public interface CachingStrategy {

  /**
   * Processes a {@link Event} using a caching schema. Uses a message processor to process the request when it is not found in the
   * cache or when it must be processed without using the cache.
   * <p/>
   * Different calls to this method using the same request does not implies that the same instance will be returned. Each
   * implementation could choose to create new instances every time.
   *
   * @param request the event to process
   * @param messageProcessor the message processor that will be executed when the response for the event is not in the cache.
   * @return a response for the request that could be obtained using the cache.
   * @throws MuleException
   */
  Event process(Event request, Processor messageProcessor) throws MuleException;
}
