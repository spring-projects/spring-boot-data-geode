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
import org.apache.geode.cache.wan.GatewaySender;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.util.StringUtils;

/**
 * The {@link GeodeGatewaySendersHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of Apache Geode {@link GatewaySender GatewaySenders}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeGatewaySendersHealthIndicator extends AbstractGeodeHealthIndicator {

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeGatewaySendersHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeGatewaySendersHealthIndicator() {
		super("Gateway Senders health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodeGatewaySendersHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodeGatewaySendersHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().filter(CacheUtils::isPeer).isPresent()) {

			Set<GatewaySender> gatewaySenders = getGemFireCache()
				.map(Cache.class::cast)
				.map(Cache::getGatewaySenders)
				.orElseGet(Collections::emptySet);

			builder.withDetail("geode.gateway-sender.count", gatewaySenders.size());

			gatewaySenders.stream()
				.filter(Objects::nonNull)
				.forEach(gatewaySender -> {

					String gatewaySenderId = gatewaySender.getId();

					builder.withDetail(gatewaySendersKey(gatewaySenderId, "alert-threshold"), gatewaySender.getAlertThreshold())
						.withDetail(gatewaySendersKey(gatewaySenderId, "batch-conflation-enabled"), toYesNoString(gatewaySender.isBatchConflationEnabled()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "batch-size"), gatewaySender.getBatchSize())
						.withDetail(gatewaySendersKey(gatewaySenderId, "batch-time-interval"), gatewaySender.getBatchTimeInterval())
						.withDetail(gatewaySendersKey(gatewaySenderId, "disk-store-name"), emptyIfUnset(gatewaySender.getDiskStoreName()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "disk-synchronous"), toYesNoString(gatewaySender.isDiskSynchronous()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "dispatcher-threads"), gatewaySender.getDispatcherThreads())
						.withDetail(gatewaySendersKey(gatewaySenderId, "max-queue-memory"), gatewaySender.getMaximumQueueMemory())
						.withDetail(gatewaySendersKey(gatewaySenderId, "max-parallelism-for-replicated-region"), gatewaySender.getMaxParallelismForReplicatedRegion())
						.withDetail(gatewaySendersKey(gatewaySenderId, "order-policy"), gatewaySender.getOrderPolicy())
						.withDetail(gatewaySendersKey(gatewaySenderId, "parallel"), toYesNoString(gatewaySender.isParallel()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "paused"), toYesNoString(gatewaySender.isPaused()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "persistent"), toYesNoString(gatewaySender.isPersistenceEnabled()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "remote-distributed-system-id"), gatewaySender.getRemoteDSId())
						.withDetail(gatewaySendersKey(gatewaySenderId, "running"), toYesNoString(gatewaySender.isRunning()))
						.withDetail(gatewaySendersKey(gatewaySenderId, "socket-buffer-size"), gatewaySender.getSocketBufferSize())
						.withDetail(gatewaySendersKey(gatewaySenderId, "socket-read-timeout"), gatewaySender.getSocketReadTimeout());
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String emptyIfUnset(String value) {
		return StringUtils.hasText(value) ? value : "";
	}

	private String gatewaySendersKey(String id, String suffix) {
		return String.format("geode.gateway-sender.%1$s.%2$s", id, suffix);
	}
}
