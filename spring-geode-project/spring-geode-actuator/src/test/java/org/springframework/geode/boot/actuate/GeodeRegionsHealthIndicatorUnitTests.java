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

import java.util.Arrays;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.CacheStatistics;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.EvictionAction;
import org.apache.geode.cache.EvictionAlgorithm;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.PartitionAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.Scope;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.tests.mock.CacheMockObjects;

/**
 * Unit tests for {@link GeodeRegionsHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.CacheStatistics
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.tests.mock.CacheMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeRegionsHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeRegionsHealthIndicatorUnitTests {

	@Mock
	private GemFireCache mockGemFireCache;

	private GeodeRegionsHealthIndicator regionsHealthIndicator;

	@Before
	public void setup() {
		this.regionsHealthIndicator = new GeodeRegionsHealthIndicator(this.mockGemFireCache);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void healthCheckCapturesDetails()  {

		Region<?, ?> mockRegionOne = CacheMockObjects.mockRegion("MockRegionOne", DataPolicy.PARTITION);

		when(mockRegionOne.getAttributes().getCloningEnabled()).thenReturn(true);
		when(mockRegionOne.getAttributes().getInitialCapacity()).thenReturn(101);
		when(mockRegionOne.getAttributes().getLoadFactor()).thenReturn(0.75f);
		when(mockRegionOne.getAttributes().getKeyConstraint()).thenReturn((Class) Long.class);
		when(mockRegionOne.getAttributes().getOffHeap()).thenReturn(true);
		when(mockRegionOne.getAttributes().getPoolName()).thenReturn("");
		when(mockRegionOne.getAttributes().getScope()).thenReturn(Scope.DISTRIBUTED_ACK);
		when(mockRegionOne.getAttributes().getStatisticsEnabled()).thenReturn(false);
		when(mockRegionOne.getAttributes().getValueConstraint()).thenReturn((Class) Currency.class);

		PartitionAttributes<?, ?> mockPartitionAttributes = mock(PartitionAttributes.class);

		when(mockPartitionAttributes.getColocatedWith()).thenReturn("CollocatedRegion");
		when(mockPartitionAttributes.getLocalMaxMemory()).thenReturn(10240);
		when(mockPartitionAttributes.getRedundantCopies()).thenReturn(2);
		//when(mockPartitionAttributes.getTotalMaxMemory()).thenReturn(4096000L);
		when(mockPartitionAttributes.getTotalNumBuckets()).thenReturn(226);
		when(mockRegionOne.getAttributes().getPartitionAttributes()).thenReturn(mockPartitionAttributes);

		EvictionAttributes mockEvictionAttributes = mock(EvictionAttributes.class);

		when(mockEvictionAttributes.getAction()).thenReturn(EvictionAction.LOCAL_DESTROY);
		when(mockEvictionAttributes.getAlgorithm()).thenReturn(EvictionAlgorithm.LRU_ENTRY);
		when(mockEvictionAttributes.getMaximum()).thenReturn(10000);
		when(mockRegionOne.getAttributes().getEvictionAttributes()).thenReturn(mockEvictionAttributes);

		Region<?, ?> mockRegionTwo = CacheMockObjects.mockRegion("MockRegionTwo", DataPolicy.EMPTY);

		when(mockRegionTwo.getAttributes().getCloningEnabled()).thenReturn(false);
		when(mockRegionTwo.getAttributes().getInitialCapacity()).thenReturn(0);
		when(mockRegionTwo.getAttributes().getLoadFactor()).thenReturn(0.0f);
		when(mockRegionTwo.getAttributes().getKeyConstraint()).thenReturn((Class) Integer.class);
		when(mockRegionTwo.getAttributes().getOffHeap()).thenReturn(false);
		when(mockRegionTwo.getAttributes().getPoolName()).thenReturn("TestPool");
		when(mockRegionTwo.getAttributes().getScope()).thenReturn(Scope.DISTRIBUTED_NO_ACK);
		when(mockRegionTwo.getAttributes().getStatisticsEnabled()).thenReturn(true);
		when(mockRegionTwo.getAttributes().getValueConstraint()).thenReturn((Class) String.class);

		ExpirationAttributes mockIdleTimeoutEntryExpirationAttributes =
			mock(ExpirationAttributes.class, "Entry-TTI");

		when(mockIdleTimeoutEntryExpirationAttributes.getAction()).thenReturn(ExpirationAction.INVALIDATE);
		when(mockIdleTimeoutEntryExpirationAttributes.getTimeout()).thenReturn(600);
		when(mockRegionTwo.getAttributes().getEntryIdleTimeout()).thenReturn(mockIdleTimeoutEntryExpirationAttributes);

		ExpirationAttributes mockTimeToLiveEntryExpirationAttributes =
			mock(ExpirationAttributes.class, "Entry-TTL");

		when(mockTimeToLiveEntryExpirationAttributes.getAction()).thenReturn(ExpirationAction.DESTROY);
		when(mockTimeToLiveEntryExpirationAttributes.getTimeout()).thenReturn(900);
		when(mockRegionTwo.getAttributes().getEntryTimeToLive()).thenReturn(mockTimeToLiveEntryExpirationAttributes);

		CacheStatistics mockCacheStatistics = mock(CacheStatistics.class);

		when(mockCacheStatistics.getHitCount()).thenReturn(202408L);
		when(mockCacheStatistics.getHitRatio()).thenReturn(0.82f);
		when(mockCacheStatistics.getLastAccessedTime()).thenReturn(1L);
		when(mockCacheStatistics.getLastModifiedTime()).thenReturn(2L);
		when(mockCacheStatistics.getMissCount()).thenReturn(767L);
		when(mockRegionTwo.getStatistics()).thenReturn(mockCacheStatistics);

		Set<Region<?, ?>> mockRegions = asSet(mockRegionOne, mockRegionTwo);

		when(this.mockGemFireCache.rootRegions()).thenReturn(mockRegions);

		Health.Builder builder = new Health.Builder();

		this.regionsHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.cache.regions", Arrays.asList("/MockRegionOne", "/MockRegionTwo"));
		assertThat(healthDetails).containsEntry("geode.cache.regions.count", (long) mockRegions.size());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.cloning-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.data-policy", DataPolicy.PARTITION.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.initial-capacity", 101);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.load-factor", 0.75f);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.key-constraint", Long.class.getName());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.off-heap", "Yes");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.eviction.action", EvictionAction.LOCAL_DESTROY.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.eviction.algorithm", EvictionAlgorithm.LRU_ENTRY.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.eviction.maximum", 10000);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.partition.collocated-with", "CollocatedRegion");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.partition.local-max-memory", 10240);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.partition.redundant-copies", 2);
		//assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.partition.total-max-memory", 4096000L);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.partition.total-number-of-buckets", 226);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.pool-name", "");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.scope", Scope.DISTRIBUTED_ACK.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.statistics-enabled", "No");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionOne.value-constraint", Currency.class.getName());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.cloning-enabled", "No");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.data-policy", DataPolicy.EMPTY.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.initial-capacity", 0);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.load-factor", 0.0f);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.key-constraint", Integer.class.getName());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.off-heap", "No");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.pool-name", "TestPool");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.scope", Scope.DISTRIBUTED_NO_ACK.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics-enabled", "Yes");
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.expiration.entry.tti.action", ExpirationAction.INVALIDATE.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.expiration.entry.tti.timeout", 600);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.expiration.entry.ttl.action", ExpirationAction.DESTROY.toString());
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.expiration.entry.ttl.timeout", 900);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics.hit-count", 202408L);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics.hit-ratio", 0.82f);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics.last-accessed-time", 1L);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics.last-modified-time", 2L);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.statistics.miss-count", 767L);
		assertThat(healthDetails).containsEntry("geode.cache.regions.MockRegionTwo.value-constraint", String.class.getName());

		verify(this.mockGemFireCache, times(1)).rootRegions();
	}

	@Test
	public void healthCheckFailsWhenGemFireCacheIsNotPresent() {

		GeodeRegionsHealthIndicator healthIndicator = new GeodeRegionsHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}
}
