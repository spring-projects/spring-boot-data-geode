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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.distributed.DistributedSystem;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.PoolMockObjects;
import org.springframework.data.gemfire.util.CacheUtils;

/**
 * Unit tests for {@link GeodePoolsHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.PoolMockObjects
 * @see org.springframework.geode.boot.actuate.GeodePoolsHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodePoolsHealthIndicatorUnitTests {

	@Mock
	private ClientCache mockClientCache;

	private GeodePoolsHealthIndicator poolsHealthIndicator;

	@Before
	public void setup() {
		this.poolsHealthIndicator = spy(new GeodePoolsHealthIndicator(mockDurableClient(this.mockClientCache)));
	}

	private ClientCache mockDurableClient(ClientCache mockClientCache) {

		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty(CacheUtils.DURABLE_CLIENT_ID_PROPERTY_NAME, "test-durable-client");

		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

		when(mockDistributedSystem.isConnected()).thenReturn(true);
		when(mockDistributedSystem.getProperties()).thenReturn(gemfireProperties);
		when(mockClientCache.getDistributedSystem()).thenReturn(mockDistributedSystem);

		return mockClientCache;
	}

	private InetSocketAddress testSocketAddress(String hostname, int port) {
		return new InetSocketAddress(hostname, port);
	}

	@Test
	public void healthCheckCapturesDetails() {

		List<InetSocketAddress> mockLocators =
			Arrays.asList(testSocketAddress("mailbox", 1234),
				testSocketAddress("skullbox", 6789));

		Pool mockPool = PoolMockObjects.mockPool("MockPool", false, 5000,
			60000L, 1000, mockLocators, 500, 50,
			true, mockLocators.subList(0, 1), 75, 15000L,
			true, null, 10000, 2, "TestGroup",
			Collections.emptyList(), 65536, 30000, 5000,
			10000, true, 5000,
			2, 8, false);

		when(this.poolsHealthIndicator.findAllPools()).thenReturn(Collections.singletonMap("MockPool", mockPool));

		Health.Builder builder = new Health.Builder();

		this.poolsHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.pool.count", 1);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.destroyed", "No");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.free-connection-timeout", 5000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.idle-timeout", 60000L);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.load-conditioning-interval", 1000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.locators", "mailbox:1234,skullbox:6789");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.max-connections", 500);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.min-connections", 50);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.multi-user-authentication", "Yes");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.online-locators", "mailbox:1234");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.pending-event-count", 75);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.ping-interval", 15000L);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.pr-single-hop-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.read-timeout", 10000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.retry-attempts", 2);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.server-group", "TestGroup");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.servers", "");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.socket-buffer-size", 65536);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.statistic-interval", 5000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.subscription-ack-interval", 10000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.subscription-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.subscription-message-tracking-timeout", 5000);
		assertThat(healthDetails).containsEntry("geode.pool.MockPool.subscription-redundancy", 2);
		//assertThat(healthDetails).containsEntry("geode.pool.MockPool.thread-local-connections", "No");

		verify(this.poolsHealthIndicator, times(1)).findAllPools();
	}

	public void testHealthCheckFailsWhenGemFireCacheIsInvalid(GemFireCache gemfireCache) {

		GeodePoolsHealthIndicator healthIndicator = gemfireCache != null
			? new GeodePoolsHealthIndicator(gemfireCache)
			: new GeodePoolsHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotClientCache() throws Exception {
		testHealthCheckFailsWhenGemFireCacheIsInvalid(mock(Cache.class));
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPresent() throws Exception {
		testHealthCheckFailsWhenGemFireCacheIsInvalid(null);
	}
}
