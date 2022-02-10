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

import static org.springframework.data.gemfire.util.CollectionUtils.nullSafeList;
import static org.springframework.data.gemfire.util.CollectionUtils.nullSafeMap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.util.StringUtils;

/**
 * The {@link GeodePoolsHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of the configured Apache Geode client {@link Pool Pools}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodePoolsHealthIndicator extends AbstractGeodeHealthIndicator {

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodePoolsHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodePoolsHealthIndicator() {
		super("Pools health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodePoolsHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodePoolsHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().filter(CacheUtils::isClient).isPresent()) {

			Map<String, Pool> pools = nullSafeMap(findAllPools());

			builder.withDetail("geode.pool.count", pools.size());

			pools.values().stream()
				.filter(Objects::nonNull)
				.forEach(pool -> {

					String poolName = pool.getName();

					builder.withDetail(poolKey(poolName, "destroyed"), toYesNoString(pool.isDestroyed()))
						.withDetail(poolKey(poolName, "free-connection-timeout"), pool.getFreeConnectionTimeout())
						.withDetail(poolKey(poolName, "idle-timeout"), pool.getIdleTimeout())
						.withDetail(poolKey(poolName, "load-conditioning-interval"), pool.getLoadConditioningInterval())
						.withDetail(poolKey(poolName, "locators"), toCommaDelimitedHostAndPortsString(pool.getLocators()))
						.withDetail(poolKey(poolName, "max-connections"), pool.getMaxConnections())
						.withDetail(poolKey(poolName, "min-connections"), pool.getMinConnections())
						.withDetail(poolKey(poolName, "multi-user-authentication"), toYesNoString(pool.getMultiuserAuthentication()))
						.withDetail(poolKey(poolName, "online-locators"), toCommaDelimitedHostAndPortsString(pool.getOnlineLocators()))
						.withDetail(poolKey(poolName, "ping-interval"), pool.getPingInterval())
						.withDetail(poolKey(poolName, "pr-single-hop-enabled"), toYesNoString(pool.getPRSingleHopEnabled()))
						.withDetail(poolKey(poolName, "read-timeout"), pool.getReadTimeout())
						.withDetail(poolKey(poolName, "retry-attempts"), pool.getRetryAttempts())
						.withDetail(poolKey(poolName, "server-group"), pool.getServerGroup())
						.withDetail(poolKey(poolName, "servers"), toCommaDelimitedHostAndPortsString(pool.getServers()))
						.withDetail(poolKey(poolName, "socket-buffer-size"), pool.getSocketBufferSize())
						.withDetail(poolKey(poolName, "statistic-interval"), pool.getStatisticInterval())
						.withDetail(poolKey(poolName, "subscription-ack-interval"), pool.getSubscriptionAckInterval())
						.withDetail(poolKey(poolName, "subscription-enabled"), toYesNoString(pool.getSubscriptionEnabled()))
						.withDetail(poolKey(poolName, "subscription-message-tracking-timeout"), pool.getSubscriptionMessageTrackingTimeout())
						.withDetail(poolKey(poolName, "subscription-redundancy"), pool.getSubscriptionRedundancy());
						//.withDetail(poolKey(poolName, "thread-local-connections"), toYesNoString(pool.getThreadLocalConnections()));

					getGemFireCache()
						.map(ClientCache.class::cast)
						.filter(CacheUtils::isDurable)
						.ifPresent(it -> builder.withDetail(poolKey(poolName, "pending-event-count"), pool.getPendingEventCount()));
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	Map<String, Pool> findAllPools() {
		return PoolManager.getAll();
	}

	private String poolKey(String poolName, String suffix) {
		return String.format("geode.pool.%1$s.%2$s", poolName, suffix);
	}

	private String toCommaDelimitedHostAndPortsString(List<InetSocketAddress> socketAddresses) {

		return StringUtils.collectionToCommaDelimitedString(nullSafeList(socketAddresses).stream()
			.filter(Objects::nonNull)
			.map(socketAddress -> String.format("%1$s:%2$d", socketAddress.getHostName(), socketAddress.getPort()))
			.collect(Collectors.toList()));
	}
}
