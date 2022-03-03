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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.wan.GatewaySender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.AsyncEventQueueMockObjects;

/**
 * Unit tests for {@link GeodeAsyncEventQueuesHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.AsyncEventQueueMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeAsyncEventQueuesHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeAsyncEventQueuesHealthIndicatorUnitTests {

	@Mock
	private Cache mockCache;

	private GeodeAsyncEventQueuesHealthIndicator asyncEventQueuesHealthIndicator;

	@Before
	public void setup() {
		this.asyncEventQueuesHealthIndicator = new GeodeAsyncEventQueuesHealthIndicator(this.mockCache);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		Set<AsyncEventQueue> mockAsyncEventQueues = new HashSet<>();

		mockAsyncEventQueues.add(AsyncEventQueueMockObjects.mockAsyncEventQueue("aeqOne", true,
			250, 10000, "testDiskStoreOne", true, 16,
			true, 65536, GatewaySender.OrderPolicy.THREAD, true,
			true, true, 1024));

		mockAsyncEventQueues.add(AsyncEventQueueMockObjects.mockAsyncEventQueue("aeqTwo", false,
			100, 1000, "testDiskStoreTwo", false, 8,
			false, 32768, GatewaySender.OrderPolicy.KEY, false,
			true, false, 8192));

		when(this.mockCache.getAsyncEventQueues()).thenReturn(mockAsyncEventQueues);

		Health.Builder builder = new Health.Builder();

		this.asyncEventQueuesHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.async-event-queue.count", mockAsyncEventQueues.size());
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.batch-conflation-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.batch-size", 250);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.batch-time-interval", 10000);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.disk-store-name", "testDiskStoreOne");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.disk-synchronous", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.dispatcher-threads", 16);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.forward-expiration-destroy", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.max-queue-memory", 65536);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.order-policy", GatewaySender.OrderPolicy.THREAD);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.parallel", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.persistent", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.primary", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqOne.size", 1024);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.batch-conflation-enabled", "No");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.batch-size", 100);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.batch-time-interval", 1000);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.disk-store-name", "testDiskStoreTwo");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.disk-synchronous", "No");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.dispatcher-threads", 8);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.forward-expiration-destroy", "No");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.max-queue-memory", 32768);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.order-policy", GatewaySender.OrderPolicy.KEY);
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.parallel", "No");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.persistent", "Yes");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.primary", "No");
		assertThat(healthDetails).containsEntry("geode.async-event-queue.aeqTwo.size", 8192);

		verify(this.mockCache, times(1)).getAsyncEventQueues();
	}

	public void testHealthCheckFailsWhenGemFireCacheIsInvalid(GemFireCache gemfireCache) throws Exception {

		GeodeAsyncEventQueuesHealthIndicator healthIndicator = gemfireCache != null
			? new GeodeAsyncEventQueuesHealthIndicator(gemfireCache)
			: new GeodeAsyncEventQueuesHealthIndicator();

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
