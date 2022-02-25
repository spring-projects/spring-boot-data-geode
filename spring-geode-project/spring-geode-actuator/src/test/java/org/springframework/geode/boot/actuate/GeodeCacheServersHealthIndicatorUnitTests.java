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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.server.ServerLoadProbe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.CacheServerMockObjects;

/**
 * Unit tests for {@link GeodeCacheServersHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.apache.geode.cache.server.ServerLoadProbe
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.CacheServerMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeCacheServersHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeCacheServersHealthIndicatorUnitTests {

	@Mock
	private Cache mockCache;

	private GeodeCacheServersHealthIndicator cacheServersHealthIndicator;

	@Before
	public void setup() {
		this.cacheServersHealthIndicator = new GeodeCacheServersHealthIndicator(this.mockCache);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		List<CacheServer> mockCacheServers = new ArrayList<>();

		ServerLoadProbe mockServerLoadProbe = mock(ServerLoadProbe.class);

		mockCacheServers.add(CacheServerMockObjects.mockCacheServer("10.11.111.1", null,
			"Mailbox", 15000L, mockServerLoadProbe, 100, 500,
			8, 20000, 30000, 41414, true, 16384,
			true));

		mockCacheServers.add(CacheServerMockObjects.mockCacheServer("10.12.120.2", null,
			"Skullbox", 10000L, mockServerLoadProbe, 250, 50,
			16, 5000, 15000, 42424, false, 8192,
			false));

		when(this.mockCache.getCacheServers()).thenReturn(mockCacheServers);

		Health.Builder builder = new Health.Builder();

		this.cacheServersHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.cache.server.count", 2);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.bind-address", "10.11.111.1");
		assertThat(healthDetails).containsEntry("geode.cache.server.0.hostname-for-clients", "Mailbox");
		assertThat(healthDetails).containsEntry("geode.cache.server.0.load-poll-interval", 15000L);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.max-connections", 100);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.max-message-count", 500);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.max-threads", 8);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.max-time-between-pings", 20000);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.message-time-to-live", 30000);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.port", 41414);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.running", "Yes");
		assertThat(healthDetails).containsEntry("geode.cache.server.0.socket-buffer-size", 16384);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.tcp-no-delay", "Yes");
		assertThat(healthDetails).doesNotContainKeys("geode.cache.server.0.client-subscription-config",
			"geode.cache.server.0.metrics.client-count", "geode.cache.server.0.load.connection-load");
		assertThat(healthDetails).containsEntry("geode.cache.server.1.bind-address", "10.12.120.2");
		assertThat(healthDetails).containsEntry("geode.cache.server.1.hostname-for-clients", "Skullbox");
		assertThat(healthDetails).containsEntry("geode.cache.server.1.load-poll-interval", 10000L);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.max-connections", 250);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.max-message-count", 50);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.max-threads", 16);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.max-time-between-pings", 5000);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.message-time-to-live", 15000);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.port", 42424);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.running", "No");
		assertThat(healthDetails).containsEntry("geode.cache.server.1.socket-buffer-size", 8192);
		assertThat(healthDetails).containsEntry("geode.cache.server.1.tcp-no-delay", "No");
		assertThat(healthDetails).doesNotContainKeys("geode.cache.server.1.client-subscription-config",
			"geode.cache.server.1.metrics.client-count", "geode.cache.server.1.load.connection-load");

		verify(this.mockCache, times(1)).getCacheServers();
	}

	private void testHealthCheckFailsWhenGemFireCacheIsInvalid(GemFireCache gemfireCache) throws Exception {

		GeodeCacheServersHealthIndicator healthIndicator = gemfireCache != null
			? new GeodeCacheServersHealthIndicator(gemfireCache)
			: new GeodeCacheServersHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPeerCache() throws Exception {
		testHealthCheckFailsWhenGemFireCacheIsInvalid(mock(ClientCache.class));
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPresent() throws Exception {
		testHealthCheckFailsWhenGemFireCacheIsInvalid(null);
	}
}
