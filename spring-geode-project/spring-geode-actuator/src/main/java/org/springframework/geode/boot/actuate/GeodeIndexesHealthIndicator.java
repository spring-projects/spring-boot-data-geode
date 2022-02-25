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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.Index;
import org.apache.geode.cache.query.IndexStatistics;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link GeodeIndexesHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of Apache Geode {@link Region} OQL {@link Index Indexes}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.query.Index
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeIndexesHealthIndicator extends AbstractGeodeHealthIndicator {

	private final ApplicationContext applicationContext;

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeIndexesHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeIndexesHealthIndicator() {
		super("Indexes health check failed");
		this.applicationContext = null;
	}

	/**
	 * Constructs an instance of the {@link GeodeIndexesHealthIndicator} initialized with a reference to
	 * the {@link ApplicationContext} instance.
	 *
	 * @param applicationContext reference to the Spring {@link ApplicationContext}.
	 * @throws IllegalArgumentException if {@link ApplicationContext} is {@literal null}.
	 * @see org.springframework.context.ApplicationContext
	 */
	public GeodeIndexesHealthIndicator(ApplicationContext applicationContext) {

		super("Indexes health check enabled");

		Assert.notNull(applicationContext, "ApplicationContext is required");

		this.applicationContext = applicationContext;
	}

	/**
	 * Returns an {@link Optional} reference to the Spring {@link ApplicationContext}.
	 *
	 * @return an {@link Optional} reference to the Spring {@link ApplicationContext}.
	 * @see org.springframework.context.ApplicationContext
	 * @see java.util.Optional
	 */
	protected Optional<ApplicationContext> getApplicationContext() {
		return Optional.ofNullable(this.applicationContext);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getApplicationContext().isPresent()) {

			Map<String, Index> indexes = getApplicationContext()
				.map(it -> it.getBeansOfType(Index.class))
				.orElseGet(Collections::emptyMap);

			builder.withDetail("geode.index.count", indexes.size());

			indexes.values().stream()
				.filter(Objects::nonNull)
				.forEach(index -> {

					String indexName = index.getName();

					builder.withDetail(indexKey(indexName, "from-clause"), index.getFromClause())
						.withDetail(indexKey(indexName, "indexed-expression"), index.getIndexedExpression())
						.withDetail(indexKey(indexName, "projection-attributes"), index.getProjectionAttributes())
						.withDetail(indexKey(indexName, "region"), toRegionPath(index.getRegion()))
						.withDetail(indexKey(indexName, "type"), String.valueOf(index.getType()));

					IndexStatistics indexStatistics = index.getStatistics();

					if (indexStatistics != null) {

						builder.withDetail(indexStatisticsKey(indexName, "number-of-bucket-indexes"), indexStatistics.getNumberOfBucketIndexes())
							.withDetail(indexStatisticsKey(indexName, "number-of-keys"), indexStatistics.getNumberOfKeys())
							.withDetail(indexStatisticsKey(indexName, "number-of-map-index-keys"), indexStatistics.getNumberOfMapIndexKeys())
							.withDetail(indexStatisticsKey(indexName, "number-of-values"), indexStatistics.getNumberOfValues())
							.withDetail(indexStatisticsKey(indexName, "number-of-updates"), indexStatistics.getNumUpdates())
							.withDetail(indexStatisticsKey(indexName, "read-lock-count"), indexStatistics.getReadLockCount())
							.withDetail(indexStatisticsKey(indexName, "total-update-time"), indexStatistics.getTotalUpdateTime())
							.withDetail(indexStatisticsKey(indexName, "total-uses"), indexStatistics.getTotalUses());
					}
				});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String emptyIfUnset(String value) {
		return StringUtils.hasText(value) ? value : "";
	}

	private String indexKey(String indexName, String suffix) {
		return String.format("geode.index.%1$s.%2$s", indexName, suffix);
	}

	private String indexStatisticsKey(String indexName, String suffix) {
		return String.format("geode.index.%1$s.statistics.%2$s", indexName, suffix);
	}

	@SuppressWarnings("rawtypes")
	private String toRegionPath(Region region) {

		String regionPath = region != null ? region.getFullPath() : null;

		return emptyIfUnset(regionPath);
	}
}
