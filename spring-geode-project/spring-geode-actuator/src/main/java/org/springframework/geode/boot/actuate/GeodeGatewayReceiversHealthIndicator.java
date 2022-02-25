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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.wan.GatewayReceiver;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;

/**
 * The {@link GeodeGatewayReceiversHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details
 * about the health of Apache Geode {@link GatewayReceiver GatewayReceivers}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.wan.GatewayReceiver
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeGatewayReceiversHealthIndicator extends AbstractGeodeHealthIndicator {

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeGatewayReceiversHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeGatewayReceiversHealthIndicator() {
		super("Gateway Receivers health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodeGatewayReceiversHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodeGatewayReceiversHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().filter(CacheUtils::isPeer).isPresent()) {

			AtomicInteger globalIndex = new AtomicInteger(0);

			Set<GatewayReceiver> gatewayReceivers = getGemFireCache()
				.map(Cache.class::cast)
				.map(Cache::getGatewayReceivers)
				.orElseGet(Collections::emptySet);

			builder.withDetail("geode.gateway-receiver.count", gatewayReceivers.size());

			gatewayReceivers.stream()
				.filter(Objects::nonNull)
				.forEach(gatewayReceiver -> {

					int index = globalIndex.getAndIncrement();

					builder.withDetail(gatewayReceiverKey(index, "bind-address"), gatewayReceiver.getBindAddress())
						.withDetail(gatewayReceiverKey(index, "end-port"), gatewayReceiver.getEndPort())
						.withDetail(gatewayReceiverKey(index, "host"), gatewayReceiver.getHost())
						.withDetail(gatewayReceiverKey(index, "max-time-between-pings"), gatewayReceiver.getMaximumTimeBetweenPings())
						.withDetail(gatewayReceiverKey(index, "port"), gatewayReceiver.getPort())
						.withDetail(gatewayReceiverKey(index, "running"), toYesNoString(gatewayReceiver.isRunning()))
						.withDetail(gatewayReceiverKey(index, "socket-buffer-size"), gatewayReceiver.getSocketBufferSize())
						.withDetail(gatewayReceiverKey(index, "start-port"), gatewayReceiver.getStartPort());
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String gatewayReceiverKey(int index, String suffix) {
		return String.format("geode.gateway-receiver.%d.%s", index, suffix);
	}
}
