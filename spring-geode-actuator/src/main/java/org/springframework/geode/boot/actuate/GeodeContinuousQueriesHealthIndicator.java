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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.geode.cache.query.CqQuery;
import org.apache.geode.cache.query.CqState;
import org.apache.geode.cache.query.CqStatistics;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.QueryStatistics;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;

/**
 * The {@link GeodeContinuousQueriesHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details
 * about the health of the registered Apache Geode {@link CqQuery Continuous Queries}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.query.CqQuery
 * @see org.apache.geode.cache.query.Query
 * @see org.apache.geode.cache.query.QueryService
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeContinuousQueriesHealthIndicator extends AbstractGeodeHealthIndicator {

	private final ContinuousQueryListenerContainer continuousQueryListenerContainer;

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeContinuousQueriesHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeContinuousQueriesHealthIndicator() {
		super("Continuous Queries health check failed");
		this.continuousQueryListenerContainer = null;
	}

	/**
	 * Constructs an instance of the {@link GeodeContinuousQueriesHealthIndicator} initialized with a reference to
	 * the {@link ContinuousQueryListenerContainer}.
	 *
	 * @param continuousQueryListenerContainer reference to the SDG {@link ContinuousQueryListenerContainer}.
	 * @see org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer
	 */
	public GeodeContinuousQueriesHealthIndicator(ContinuousQueryListenerContainer continuousQueryListenerContainer) {

		super("Continuous Queries health check enabled");

		this.continuousQueryListenerContainer = continuousQueryListenerContainer;
	}

	/**
	 * Returns an {@link Optional} reference to the configured {@link ContinuousQueryListenerContainer}.
	 *
	 * @return an {@link Optional} reference to the configured {@link ContinuousQueryListenerContainer}.
	 * @see org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer
	 * @see java.util.Optional
	 */
	protected Optional<ContinuousQueryListenerContainer> getContinuousQueryListenerContainer() {
		return Optional.ofNullable(this.continuousQueryListenerContainer);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getContinuousQueryListenerContainer().isPresent()) {

			Optional<QueryService> queryService = getContinuousQueryListenerContainer()
				.map(ContinuousQueryListenerContainer::getQueryService);

			List<CqQuery> continuousQueries = queryService
				.map(QueryService::getCqs)
				.map(Arrays::asList)
				.orElseGet(Collections::emptyList);

			builder.withDetail("geode.continuous-query.count", continuousQueries.size());

			queryService
				.map(QueryService::getCqStatistics)
				.ifPresent(cqServiceStatistics ->

					builder.withDetail("geode.continuous-query.number-of-active", cqServiceStatistics.numCqsActive())
						.withDetail("geode.continuous-query.number-of-closed", cqServiceStatistics.numCqsClosed())
						.withDetail("geode.continuous-query.number-of-created", cqServiceStatistics.numCqsCreated())
						.withDetail("geode.continuous-query.number-of-stopped", cqServiceStatistics.numCqsStopped())
						.withDetail("geode.continuous-query.number-on-client", cqServiceStatistics.numCqsOnClient())
				);

			continuousQueries.stream()
				.filter(Objects::nonNull)
				.forEach(continuousQuery -> {

					String continuousQueryName = continuousQuery.getName();

					builder.withDetail(continuousQueryKey(continuousQueryName,"oql-query-string"), continuousQuery.getQueryString())
						.withDetail(continuousQueryKey(continuousQueryName, "closed"), toYesNoString(continuousQuery.isClosed()))
						.withDetail(continuousQueryKey(continuousQueryName, "closing"), toYesNoString(continuousQuery.getState()))
						.withDetail(continuousQueryKey(continuousQueryName, "durable"), toYesNoString(continuousQuery.isDurable()))
						.withDetail(continuousQueryKey(continuousQueryName, "running"), toYesNoString(continuousQuery.isRunning()))
						.withDetail(continuousQueryKey(continuousQueryName, "stopped"), toYesNoString(continuousQuery.isStopped()));

					Query query = continuousQuery.getQuery();

					if (query != null) {

						QueryStatistics queryStatistics = query.getStatistics();

						if (queryStatistics != null) {
							builder.withDetail(continuousQueryQueryKey(continuousQueryName, "number-of-executions"), queryStatistics.getNumExecutions())
								.withDetail(continuousQueryQueryKey(continuousQueryName, "total-execution-time"), queryStatistics.getTotalExecutionTime());
						}
					}

					CqStatistics continuousQueryStatistics = continuousQuery.getStatistics();

					if (continuousQueryStatistics != null) {

						builder.withDetail(continuousQueryStatisticsKey(continuousQueryName, "number-of-deletes"), continuousQueryStatistics.numDeletes())
							.withDetail(continuousQueryStatisticsKey(continuousQueryName, "number-of-events"), continuousQueryStatistics.numEvents())
							.withDetail(continuousQueryStatisticsKey(continuousQueryName, "number-of-inserts"), continuousQueryStatistics.numInserts())
							.withDetail(continuousQueryStatisticsKey(continuousQueryName, "number-of-updates"), continuousQueryStatistics.numUpdates());
					}
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String continuousQueryKey(String continuousQueryName, String suffix) {
		return String.format("geode.continuous-query.%1$s.%2$s", continuousQueryName, suffix);
	}

	private String continuousQueryQueryKey(String continuousQueryName, String suffix) {
		return String.format("geode.continuous-query.%1$s.query.%2$s", continuousQueryName, suffix);
	}

	private String continuousQueryStatisticsKey(String continuousQueryName, String suffix) {
		return String.format("geode.continuous-query.%1$s.statistics.%2$s", continuousQueryName, suffix);
	}

	private String toYesNoString(CqState continuousQueryState) {
		return continuousQueryState != null ? toYesNoString(continuousQueryState.isClosing()) : UNKNOWN;
	}
}
