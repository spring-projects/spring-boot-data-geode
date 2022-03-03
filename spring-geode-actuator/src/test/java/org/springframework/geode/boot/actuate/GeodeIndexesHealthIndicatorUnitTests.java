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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.Index;
import org.apache.geode.cache.query.IndexStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.IndexType;
import org.springframework.data.gemfire.tests.mock.CacheMockObjects;
import org.springframework.data.gemfire.tests.mock.IndexMockObjects;

/**
 * Unit tests for {@link GeodeIndexesHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.query.Index
 * @see org.apache.geode.cache.query.IndexStatistics
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.data.gemfire.tests.mock.CacheMockObjects
 * @see org.springframework.data.gemfire.tests.mock.IndexMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeIndexesHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeIndexesHealthIndicatorUnitTests {

	@Mock
	private ApplicationContext applicationContext;

	private GeodeIndexesHealthIndicator indexesHealthIndicator;

	@Before
	public void setup() {
		this.indexesHealthIndicator = new GeodeIndexesHealthIndicator(this.applicationContext);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		Region mockRegion = CacheMockObjects.mockRegion("MockRegion", DataPolicy.PARTITION);

		IndexStatistics mockIndexStatistics = IndexMockObjects.mockIndexStatistics(226,
			100000, 6000, 1024000L, 51515L,
			512, 2048L, 4096L);

		Index mockIndex = IndexMockObjects.mockIndex("MockIndex", "/Example",
			"id", "one, two", mockRegion, mockIndexStatistics,
			IndexType.PRIMARY_KEY.getGemfireIndexType());

		Map<String, Index> mockIndexes = Collections.singletonMap("MockIndex", mockIndex);

		when(this.applicationContext.getBeansOfType(eq(Index.class))).thenReturn(mockIndexes);

		Health.Builder builder = new Health.Builder();

		this.indexesHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.index.count", mockIndexes.size());
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.from-clause", "/Example");
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.indexed-expression", "id");
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.projection-attributes", "one, two");
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.region", "/MockRegion");
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.type",
			IndexType.PRIMARY_KEY.getGemfireIndexType().toString());
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.number-of-bucket-indexes", 226);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.number-of-keys", 100000L);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.number-of-map-index-keys", 6000L);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.number-of-values", 1024000L);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.number-of-updates", 51515L);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.read-lock-count", 512);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.total-update-time", 2048L);
		assertThat(healthDetails).containsEntry("geode.index.MockIndex.statistics.total-uses", 4096L);

		verify(this.applicationContext, times(1)).getBeansOfType(eq(Index.class));
	}

	@Test
	public void healthCheckFailsWhenApplicationContextContainsIsNotPresent() throws Exception {

		GeodeIndexesHealthIndicator healthIndicator = new GeodeIndexesHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}
}
