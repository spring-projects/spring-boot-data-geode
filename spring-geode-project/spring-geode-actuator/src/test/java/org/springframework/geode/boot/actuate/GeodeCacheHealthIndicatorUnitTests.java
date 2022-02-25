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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.geode.CancelCriterion;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.control.ResourceManager;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.CacheMockObjects;

/**
 * Unit tests for {@link GeodeCacheHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.CancelCriterion
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.control.ResourceManager
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.CacheMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeCacheHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeCacheHealthIndicatorUnitTests {

	@Mock
	private GemFireCache mockGemFireCache;

	private GeodeCacheHealthIndicator cacheHealthIndicator;

	@Before
	public void setup() {
		this.cacheHealthIndicator = new GeodeCacheHealthIndicator(this.mockGemFireCache);
	}

	@SuppressWarnings("all")
	private Set<DistributedMember> mockDistributedMembers(int size) {

		return IntStream.range(0, size)
			.mapToObj(it -> mock(DistributedMember.class))
			.collect(Collectors.toSet());
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		DistributedMember mockDistributedMember =
			CacheMockObjects.mockDistributedMember("TestMember", "TestGroup", "MockGroup");

		when(mockDistributedMember.getHost()).thenReturn("Skullbox");
		when(mockDistributedMember.getProcessId()).thenReturn(12345);

		DistributedSystem mockDistributedSystem = CacheMockObjects.mockDistributedSystem(mockDistributedMember);

		when(mockDistributedSystem.getAllOtherMembers()).thenAnswer(invocation -> mockDistributedMembers(8));
		when(mockDistributedSystem.isConnected()).thenReturn(true);
		when(mockDistributedSystem.isReconnecting()).thenReturn(false);

		ResourceManager mockResourceManager = CacheMockObjects.mockResourceManager(0.9f,
			0.95f, 0.85f, 0.9f);

		GemFireCache mockGemFireCache = CacheMockObjects.mockGemFireCache(this.mockGemFireCache,
			"MockGemFireCache", mockDistributedSystem, mockResourceManager);

		CancelCriterion mockCancelCriterion = mock(CancelCriterion.class);

		when(mockCancelCriterion.isCancelInProgress()).thenReturn(false);
		when(mockGemFireCache.getCancelCriterion()).thenReturn(mockCancelCriterion);
		when(mockGemFireCache.isClosed()).thenReturn(false);

		Health.Builder builder = new Health.Builder();

		this.cacheHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.cache.name", "MockGemFireCache");
		assertThat(healthDetails).containsEntry("geode.cache.closed", "No");
		assertThat(healthDetails).containsEntry("geode.cache.cancel-in-progress", "No");
		assertThat(healthDetails).containsKey("geode.distributed-member.id");
		assertThat(String.valueOf(healthDetails.get("geode.distributed-member.id"))).isNotEqualToIgnoringCase("null");
		assertThat(healthDetails).containsEntry("geode.distributed-member.name", "TestMember");
		assertThat(healthDetails).containsEntry("geode.distributed-member.groups", Arrays.asList("TestGroup", "MockGroup"));
		assertThat(healthDetails).containsEntry("geode.distributed-member.host", "Skullbox");
		assertThat(healthDetails).containsEntry("geode.distributed-member.process-id", 12345);
		assertThat(healthDetails).containsEntry("geode.distributed-system.member-count", 9);
		assertThat(healthDetails).containsEntry("geode.distributed-system.connection", "Connected");
		assertThat(healthDetails).containsEntry("geode.distributed-system.reconnecting", "No");
		//assertThat(healthDetails).containsKey("geode.distributed-member.properties-location");
		//assertThat(healthDetails).containsKey("geode.distributed-member.security-properties-location");
		assertThat(healthDetails).containsEntry("geode.resource-manager.critical-heap-percentage", 0.9f);
		assertThat(healthDetails).containsEntry("geode.resource-manager.critical-off-heap-percentage", 0.95f);
		assertThat(healthDetails).containsEntry("geode.resource-manager.eviction-heap-percentage", 0.85f);
		assertThat(healthDetails).containsEntry("geode.resource-manager.eviction-off-heap-percentage", 0.9f);

		verify(this.mockGemFireCache, times(1)).getCancelCriterion();
		verify(this.mockGemFireCache, times(2)).getDistributedSystem();
		verify(this.mockGemFireCache, times(1)).getResourceManager();
		verify(mockDistributedSystem, times(1)).getDistributedMember();
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPresent() throws Exception {

		GeodeCacheHealthIndicator healthIndicator = new GeodeCacheHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}
}
