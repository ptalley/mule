/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.el.v2;

import org.mule.runtime.api.metadata.TypedValue;

public interface BindingContextBuilder {

  /**
   * Will create a binding for the specified identifier and value.
   *
   * @param value the value to bind
   * @param identifier the keyword to use in the EL to access the {@code value}
   */
  BindingContextBuilder addBinding(String identifier, TypedValue value);

  BindingContext build();
}
