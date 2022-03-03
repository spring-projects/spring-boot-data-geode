/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package example.app.caching.near.client.service.support;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

/**
 * Abstract base class for implementing cacheable {@link Service services} along with additional functionality to
 * ascertain whether a cacheable operation led to a cache hit or a cache miss.
 *
 * @author John Blum
 * @since 1.1.0
 */
@SuppressWarnings("unused")
// tag::class[]
public abstract class AbstractCacheableService {

	protected static final int BOUNDED_MULTIPLIER = 3;

	protected static final long BASE_MILLISECONDS = 2000L;
	protected static final long ONE_SECOND_IN_MILLISECONDS = 1000L;

	protected final AtomicBoolean cacheMiss = new AtomicBoolean(false);

	protected final Random multiplier = new Random(System.currentTimeMillis());

	public boolean isCacheHit() {
		return !isCacheMiss();
	}

	public boolean isCacheMiss() {
		return this.cacheMiss.compareAndSet(true,false);
	}

	protected long delayInMilliseconds() {
		return BASE_MILLISECONDS + (ONE_SECOND_IN_MILLISECONDS * this.multiplier.nextInt(BOUNDED_MULTIPLIER));
	}

	protected boolean simulateLatency() {
		return simulateLatency(delayInMilliseconds());
	}

	protected boolean simulateLatency(long milliseconds) {

		try {
			Thread.sleep(milliseconds);
			return true;
		}
		catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
// end::class[]
