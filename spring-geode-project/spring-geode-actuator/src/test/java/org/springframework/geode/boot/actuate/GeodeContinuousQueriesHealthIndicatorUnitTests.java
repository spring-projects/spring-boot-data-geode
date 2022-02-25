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

import java.util.Map;

import org.apache.geode.cache.query.CqQuery;
import org.apache.geode.cache.query.CqServiceStatistics;
import org.apache.geode.cache.query.CqState;
import org.apache.geode.cache.query.CqStatistics;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.QueryStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;

/**
 * Unit tests for {@link GeodeContinuousQueriesHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.query.CqQuery
 * @see org.apache.geode.cache.query.Query
 * @see org.apache.geode.cache.query.QueryService
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer
 * @see org.springframework.geode.boot.actuate.GeodeContinuousQueriesHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeContinuousQueriesHealthIndicatorUnitTests {

	private GeodeContinuousQueriesHealthIndicator continuousQueriesHealthIndicator;

	@Mock
	private QueryService mockQueryService;

	@Before
	public void setup() {

		ContinuousQueryListenerContainer container = new ContinuousQueryListenerContainer();

		container.setQueryService(this.mockQueryService);

		this.continuousQueriesHealthIndicator = new GeodeContinuousQueriesHealthIndicator(container);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		CqQuery mockContinuousQuery = mock(CqQuery.class, "MockContinuousQuery");

		when(mockContinuousQuery.getName()).thenReturn("MockContinuousQuery");
		when(mockContinuousQuery.getQueryString()).thenReturn("SELECT * FROM /Example WHERE status = 'RUNNING'");
		when(mockContinuousQuery.isClosed()).thenReturn(false);
		when(mockContinuousQuery.isDurable()).thenReturn(true);
		when(mockContinuousQuery.isRunning()).thenReturn(true);
		when(mockContinuousQuery.isStopped()).thenReturn(false);

		CqState mockContinuousQueryState = mock(CqState.class);

		when(mockContinuousQueryState.isClosing()).thenReturn(false);
		when(mockContinuousQuery.getState()).thenReturn(mockContinuousQueryState);

		CqStatistics mockContinuousQueryStatistics = mock(CqStatistics.class);

		when(mockContinuousQueryStatistics.numDeletes()).thenReturn(1024L);
		when(mockContinuousQueryStatistics.numEvents()).thenReturn(4096000L);
		when(mockContinuousQueryStatistics.numInserts()).thenReturn(8192L);
		when(mockContinuousQueryStatistics.numUpdates()).thenReturn(1638400L);
		when(mockContinuousQuery.getStatistics()).thenReturn(mockContinuousQueryStatistics);

		Query mockQuery = mock(Query.class);

		QueryStatistics mockQueryStatistics = mock(QueryStatistics.class);

		when(mockQueryStatistics.getNumExecutions()).thenReturn(1024L);
		when(mockQueryStatistics.getTotalExecutionTime()).thenReturn(123456789L);
		when(mockQuery.getStatistics()).thenReturn(mockQueryStatistics);
		when(mockContinuousQuery.getQuery()).thenReturn(mockQuery);

		CqQuery[] mockContinuousQueries = { mockContinuousQuery };

		when(this.mockQueryService.getCqs()).thenReturn(mockContinuousQueries);

		CqServiceStatistics mockContinuousQueryServiceStatistics = mock(CqServiceStatistics.class);

		when(mockContinuousQueryServiceStatistics.numCqsActive()).thenReturn(42L);
		when(mockContinuousQueryServiceStatistics.numCqsClosed()).thenReturn(8L);
		when(mockContinuousQueryServiceStatistics.numCqsCreated()).thenReturn(51L);
		when(mockContinuousQueryServiceStatistics.numCqsStopped()).thenReturn(16L);
		when(mockContinuousQueryServiceStatistics.numCqsOnClient()).thenReturn(64L);
		when(this.mockQueryService.getCqStatistics()).thenReturn(mockContinuousQueryServiceStatistics);

		Health.Builder builder = new Health.Builder();

		this.continuousQueriesHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.continuous-query.count", mockContinuousQueries.length);
		assertThat(healthDetails).containsEntry("geode.continuous-query.number-of-active", 42L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.number-of-closed", 8L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.number-of-created", 51L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.number-of-stopped", 16L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.number-on-client", 64L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.oql-query-string", "SELECT * FROM /Example WHERE status = 'RUNNING'");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.closed", "No");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.closing", "No");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.durable", "Yes");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.running", "Yes");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.stopped", "No");
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.query.number-of-executions", 1024L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.query.total-execution-time", 123456789L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.statistics.number-of-deletes", 1024L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.statistics.number-of-events", 4096000L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.statistics.number-of-inserts", 8192L);
		assertThat(healthDetails).containsEntry("geode.continuous-query.MockContinuousQuery.statistics.number-of-updates", 1638400L);

		verify(this.mockQueryService, times(1)).getCqs();
	}

	@Test
	public void healthCheckFailsWhenContinuousQueryListenerContainerIsNotPresent() throws Exception {

		GeodeContinuousQueriesHealthIndicator healthIndicator = new GeodeContinuousQueriesHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}
}
