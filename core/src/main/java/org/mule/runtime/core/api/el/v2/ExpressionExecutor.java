/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.el.v2;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

/**
 * Evaluates an expression considering a given context.
 *
 * @since 4.0
 */
public interface ExpressionExecutor {

  /**
   * Evaluates an expression according to a given {@link BindingContext}
   *
   * @param expression the EL expression
   * @param context the current binding execution context to consider
   * @return the result of the expression plus its type
   */
  TypedValue evaluate(String expression, BindingContext context);

  /**
   * Evaluates an expression according to a given {@link BindingContext} and the {@link DataType} of the expected result.
   *
   * @param expression the EL expression
   * @param expectedOutputType the expected output type so that the EL can do automatic conversion for the resulting value type.
   * @param context the current expression execution context to consider
   * @return the result of the expression plus its type
   */
  TypedValue evaluate(String expression, DataType expectedOutputType, BindingContext context);

  /**
   * Verifies whether an expression is valid or not.
   *
   * @param expression to be validated
   * @return a {@link ValidationResult} indicating whether the validation was successful or not
   */
  ValidationResult validate(String expression);

}
