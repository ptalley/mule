/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import org.mule.extension.runtime.ExpirationPolicy;
import org.mule.time.TimeSupplier;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A immutable implementation of {@link ExpirationPolicy}
 *
 * @since 4.0
 */
public final class ImmutableExpirationPolicy implements ExpirationPolicy
{

    //TODO: When MULE-8869 is implemented, the TimeSupplier should be injected
    public static final ExpirationPolicy DEFAULT = new ImmutableExpirationPolicy(5, TimeUnit.MINUTES, TimeSupplier.INSTANCE);

    private final long maxIdleTime;
    private final TimeUnit timeUnit;
    private final Supplier<Long> timeSupplier;

    public ImmutableExpirationPolicy(long maxIdleTime, TimeUnit timeUnit, Supplier<Long> timeSupplier)
    {
        this.maxIdleTime = maxIdleTime;
        this.timeUnit = timeUnit;
        this.timeSupplier = timeSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExpired(long lastUsed, TimeUnit timeUnit)
    {
        long idleTimeMillis = timeSupplier.get() - timeUnit.toMillis(lastUsed);
        return idleTimeMillis > this.timeUnit.toMillis(maxIdleTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxIdleTime()
    {
        return maxIdleTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }
}