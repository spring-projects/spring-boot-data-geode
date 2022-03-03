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
import org.apache.geode.cache.wan.GatewayReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.GatewayMockObjects;

/**
 * Unit tests for {@link GeodeGatewayReceiversHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.wan.GatewayReceiver
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.GatewayMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeGatewayReceiversHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeGatewayReceiversHealthIndicatorUnitTests {

	@Mock
	private Cache mockCache;

	private GeodeGatewayReceiversHealthIndicator gatewayReceiversHealthIndicator;

	@Before
	public void setup() {
		this.gatewayReceiversHealthIndicator = new GeodeGatewayReceiversHealthIndicator(this.mockCache);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		GatewayReceiver mockGatewayReceiverOne = GatewayMockObjects.mockGatewayReceiver("10.101.112.1",
			8192, "CardboardBox", "Mailbox", false, 15000,
			4096, true, null, 16384, 1024);

		GatewayReceiver mockGatewayReceiverTwo = GatewayMockObjects.mockGatewayReceiver("10.101.112.4",
			8192, "Skullbox", "PostOfficeBox", true, 5000,
			8192, false, null, 65536, 1024);

		Set<GatewayReceiver> mockGatewayReceivers =
			new TreeSet<>(Comparator.comparing(GatewayReceiver::getBindAddress));

		mockGatewayReceivers.addAll(asSet(mockGatewayReceiverOne, mockGatewayReceiverTwo));

		when(this.mockCache.getGatewayReceivers()).thenReturn(mockGatewayReceivers);

		Health.Builder builder = new Health.Builder();

		this.gatewayReceiversHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.count", mockGatewayReceivers.size());
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.bind-address", "10.101.112.1");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.end-port", 8192);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.host", "CardboardBox");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.max-time-between-pings", 15000);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.port", 4096);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.running", "Yes");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.socket-buffer-size", 16384);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.0.start-port", 1024);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.bind-address", "10.101.112.4");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.end-port", 8192);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.host", "Skullbox");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.max-time-between-pings",5000);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.port", 8192);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.running", "No");
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.socket-buffer-size", 65536);
		assertThat(healthDetails).containsEntry("geode.gateway-receiver.1.start-port", 1024);

		verify(this.mockCache, times(1)).getGatewayReceivers();
	}

	private void testHealthCheckFailsWhenGemFireCacheIsInvalid(GemFireCache gemfireCache) throws Exception {

		GeodeGatewayReceiversHealthIndicator healthIndicator = gemfireCache != null
			? new GeodeGatewayReceiversHealthIndicator(gemfireCache)
			: new GeodeGatewayReceiversHealthIndicator();

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
