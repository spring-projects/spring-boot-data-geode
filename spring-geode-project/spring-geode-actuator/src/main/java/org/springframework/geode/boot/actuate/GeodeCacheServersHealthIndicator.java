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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.server.ServerLoad;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.geode.boot.actuate.health.support.ActuatorServerLoadProbeWrapper;

/**
 * The {@link GeodeCacheServersHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of Apache Geode {@link CacheServer CacheServers}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @see org.springframework.geode.boot.actuate.health.support.ActuatorServerLoadProbeWrapper
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeCacheServersHealthIndicator extends AbstractGeodeHealthIndicator {

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeCacheServersHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeCacheServersHealthIndicator() {
		super("Cache Servers health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodeCacheServersHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodeCacheServersHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().filter(CacheUtils::isPeer).isPresent()) {

			AtomicInteger globalIndex = new AtomicInteger(0);

			List<CacheServer> cacheServers = getGemFireCache()
				.map(Cache.class::cast)
				.map(Cache::getCacheServers)
				.orElseGet(Collections::emptyList);

			builder.withDetail("geode.cache.server.count", cacheServers.size());

			cacheServers.stream()
				.filter(Objects::nonNull)
				.forEach(cacheServer -> {

					int cacheServerIndex = globalIndex.getAndIncrement();

					builder.withDetail(cacheServerKey(cacheServerIndex, "bind-address"), cacheServer.getBindAddress())
						.withDetail(cacheServerKey(cacheServerIndex, "hostname-for-clients"), cacheServer.getHostnameForClients())
						.withDetail(cacheServerKey(cacheServerIndex, "load-poll-interval"), cacheServer.getLoadPollInterval())
						.withDetail(cacheServerKey(cacheServerIndex, "max-connections"), cacheServer.getMaxConnections())
						.withDetail(cacheServerKey(cacheServerIndex, "max-message-count"), cacheServer.getMaximumMessageCount())
						.withDetail(cacheServerKey(cacheServerIndex, "max-threads"), cacheServer.getMaxThreads())
						.withDetail(cacheServerKey(cacheServerIndex, "max-time-between-pings"), cacheServer.getMaximumTimeBetweenPings())
						.withDetail(cacheServerKey(cacheServerIndex, "message-time-to-live"), cacheServer.getMessageTimeToLive())
						.withDetail(cacheServerKey(cacheServerIndex, "port"), cacheServer.getPort())
						.withDetail(cacheServerKey(cacheServerIndex, "running"), toYesNoString(cacheServer.isRunning()))
						.withDetail(cacheServerKey(cacheServerIndex, "socket-buffer-size"), cacheServer.getSocketBufferSize())
						.withDetail(cacheServerKey(cacheServerIndex, "tcp-no-delay"), toYesNoString(cacheServer.getTcpNoDelay()));

					Optional.ofNullable(cacheServer.getLoadProbe())
						.filter(ActuatorServerLoadProbeWrapper.class::isInstance)
						.map(ActuatorServerLoadProbeWrapper.class::cast)
						.flatMap(ActuatorServerLoadProbeWrapper::getCurrentServerMetrics)
						.ifPresent(serverMetrics -> {

							builder.withDetail(cacheServerMetricsKey(cacheServerIndex, "client-count"), serverMetrics.getClientCount())
								.withDetail(cacheServerMetricsKey(cacheServerIndex, "max-connection-count"), serverMetrics.getMaxConnections())
								.withDetail(cacheServerMetricsKey(cacheServerIndex, "open-connection-count"), serverMetrics.getConnectionCount())
								.withDetail(cacheServerMetricsKey(cacheServerIndex, "subscription-connection-count"), serverMetrics.getSubscriptionConnectionCount());

							ServerLoad serverLoad = cacheServer.getLoadProbe().getLoad(serverMetrics);

							if (serverLoad != null) {

								builder.withDetail(cacheServerLoadKey(cacheServerIndex, "connection-load"), serverLoad.getConnectionLoad())
									.withDetail(cacheServerLoadKey(cacheServerIndex, "load-per-connection"), serverLoad.getLoadPerConnection())
									.withDetail(cacheServerLoadKey(cacheServerIndex, "subscription-connection-load"), serverLoad.getSubscriptionConnectionLoad())
									.withDetail(cacheServerLoadKey(cacheServerIndex, "load-per-subscription-connection"), serverLoad.getLoadPerSubscriptionConnection());
							}
						});
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String cacheServerKey(int index, String suffix) {
		return String.format("geode.cache.server.%d.%s", index, suffix);
	}

	private String cacheServerLoadKey(int index, String suffix) {
		return String.format("geode.cache.server.%d.load.%s", index, suffix);
	}

	private String cacheServerMetricsKey(int index, String suffix) {
		return String.format("geode.cache.server.%d.metrics.%s", index, suffix);
	}
}
