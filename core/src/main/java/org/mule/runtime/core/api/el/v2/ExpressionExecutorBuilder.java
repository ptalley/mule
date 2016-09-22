/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.el.v2;

import org.mule.metadata.api.model.MetadataType;

public interface ExpressionExecutorBuilder
{

    /**
     * @param metadataType the type of the function or variable represented by {@code value}
     * @param value the value to bind
     * @param identifier the keyword to use in the EL to access the {@code value}
     * @return
     */
    ExpressionExecutorBuilder addBinding(MetadataType metadataType, Object value, String identifier);

    ExpressionExecutor buildExecutor();


}
