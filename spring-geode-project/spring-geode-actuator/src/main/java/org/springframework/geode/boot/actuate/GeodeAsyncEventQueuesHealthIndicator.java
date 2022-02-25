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
package org.springframework.geode.boot.actuate;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;

/**
 * The {@link GeodeAsyncEventQueuesHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details
 * about the health of Apache Geode {@link AsyncEventQueue AsyncEventQueues}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeAsyncEventQueuesHealthIndicator extends AbstractGeodeHealthIndicator {

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeAsyncEventQueuesHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeAsyncEventQueuesHealthIndicator() {
		super("Async Event Queues health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodeAsyncEventQueuesHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodeAsyncEventQueuesHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().filter(CacheUtils::isPeer).isPresent()) {

			Set<AsyncEventQueue> asyncEventQueues = getGemFireCache()
				.map(Cache.class::cast)
				.map(Cache::getAsyncEventQueues)
				.orElseGet(Collections::emptySet);

			builder.withDetail("geode.async-event-queue.count", asyncEventQueues.size());

			asyncEventQueues.stream()
				.filter(Objects::nonNull)
				.forEach(asyncEventQueue -> {

					String asyncEventQueueId = asyncEventQueue.getId();

					builder.withDetail(asyncEventQueueKey(asyncEventQueueId, "batch-conflation-enabled"), toYesNoString(asyncEventQueue.isBatchConflationEnabled()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "batch-size"), asyncEventQueue.getBatchSize())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "batch-time-interval"), asyncEventQueue.getBatchTimeInterval())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "disk-store-name"), asyncEventQueue.getDiskStoreName())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "disk-synchronous"), toYesNoString(asyncEventQueue.isDiskSynchronous()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "dispatcher-threads"), asyncEventQueue.getDispatcherThreads())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "forward-expiration-destroy"), toYesNoString(asyncEventQueue.isForwardExpirationDestroy()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "max-queue-memory"), asyncEventQueue.getMaximumQueueMemory())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "order-policy"), asyncEventQueue.getOrderPolicy())
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "parallel"), toYesNoString(asyncEventQueue.isParallel()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "persistent"), toYesNoString(asyncEventQueue.isPersistent()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "primary"), toYesNoString(asyncEventQueue.isPrimary()))
						.withDetail(asyncEventQueueKey(asyncEventQueueId, "size"), asyncEventQueue.size());
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String asyncEventQueueKey(String id, String suffix) {
		return String.format("geode.async-event-queue.%1$s.%2$s", id, suffix);
	}
}
