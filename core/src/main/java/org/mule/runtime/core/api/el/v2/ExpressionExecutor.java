/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.el.v2;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.TypedValue;

public interface ExpressionExecutor
{

    /**
     * @param expression the EL expression
     * @return the result of the expression plus its type
     */
    TypedValue executeExpression(String expression);

    /**
     * @param expression the EL expression
     * @param expectedOutputType the expected output type so dataweave can do automatic conversion for the expected value type.
     * @return the result of the expression plus its type
     */
    TypedValue executeExpression(String expression, MetadataType expectedOutputType);

}
