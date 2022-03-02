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
import static org.springframework.data.gemfire.util.CollectionUtils.asSet;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.wan.GatewaySender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.GatewayMockObjects;

/**
 * Unit tests for {@link GeodeGatewaySendersHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.GatewayMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeGatewaySendersHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeGatewaySendersHealthIndicatorUnitTests {

	@Mock
	private Cache mockCache;

	private GeodeGatewaySendersHealthIndicator gatewaySendersHealthIndicator;

	@Before
	public void setup() {
		this.gatewaySendersHealthIndicator = new GeodeGatewaySendersHealthIndicator(this.mockCache);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		GatewaySender mockGatewaySenderOne = GatewayMockObjects.mockGatewaySender("MockGatewaySenderOne",
			100, true, 250, 30000, "TestDiskStore",
			true, 8, 16384, 24,
			GatewaySender.OrderPolicy.THREAD, true, true, 123,
			true, 32768, 15000);

		GatewaySender mockGatewaySenderTwo = GatewayMockObjects.mockGatewaySender("MockGatewaySenderTwo",
			99, false, 500, 20000, null,
			false, 16, 8192, 32,
			GatewaySender.OrderPolicy.KEY, false, false, 789,
			false, 65536, 20000);

		Set<GatewaySender> mockGatewaySenders =
			new TreeSet<>(Comparator.comparing(GatewaySender::getId));

		mockGatewaySenders.addAll(asSet(mockGatewaySenderOne, mockGatewaySenderTwo));

		when(this.mockCache.getGatewaySenders()).thenReturn(mockGatewaySenders);

		Health.Builder builder = new Health.Builder();

		this.gatewaySendersHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.gateway-sender.count", mockGatewaySenders.size());
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.alert-threshold", 100);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.batch-conflation-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.batch-size", 250);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.batch-time-interval", 30000);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.disk-store-name", "TestDiskStore");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.disk-synchronous", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.dispatcher-threads", 8);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.max-queue-memory", 16384);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.max-parallelism-for-replicated-region", 24);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.order-policy", GatewaySender.OrderPolicy.THREAD);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.parallel", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.persistent", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.remote-distributed-system-id", 123);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.running", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.socket-buffer-size", 32768);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderOne.socket-read-timeout", 15000);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.alert-threshold", 99);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.batch-conflation-enabled", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.batch-size", 500);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.batch-time-interval", 20000);
		assertThat(healthDetails).containsKey("geode.gateway-sender.MockGatewaySenderTwo.disk-store-name");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.disk-synchronous", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.dispatcher-threads", 16);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.max-queue-memory", 8192);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.max-parallelism-for-replicated-region", 32);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.order-policy", GatewaySender.OrderPolicy.KEY);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.parallel", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.persistent", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.remote-distributed-system-id", 789);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.running", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.socket-buffer-size", 65536);
		assertThat(healthDetails).containsEntry("geode.gateway-sender.MockGatewaySenderTwo.socket-read-timeout", 20000);

		verify(this.mockCache, times(1)).getGatewaySenders();
	}

	private void testHealthCheckFailsWithInvalidGemFireCache(GemFireCache gemfireCache) throws Exception {

		GeodeGatewaySendersHealthIndicator healthIndicator = gemfireCache != null
			? new GeodeGatewaySendersHealthIndicator(gemfireCache)
			: new GeodeGatewaySendersHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
		assertThat(health.getDetails()).isEmpty();
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPeerCache() throws Exception {
		testHealthCheckFailsWithInvalidGemFireCache(mock(ClientCache.class));
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPresent() throws Exception {
		testHealthCheckFailsWithInvalidGemFireCache(null);
	}
}
