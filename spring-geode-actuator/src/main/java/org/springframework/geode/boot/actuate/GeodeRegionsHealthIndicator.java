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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.geode.cache.EvictionAlgorithm;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.PartitionAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.internal.cache.LocalDataSet;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.geode.boot.actuate.health.support.RegionStatisticsResolver;
import org.springframework.util.StringUtils;

/**
 * The {@link GeodeRegionsHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of the {@link GemFireCache} {@link Region Regions}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeRegionsHealthIndicator extends AbstractGeodeHealthIndicator {

	private final BiConsumer<Region<?, ?>, Health.Builder> gemfireRegionHealthIndicatorConsumers = withRegionDetails()
		.andThen(withPartitionRegionDetails())
		.andThen(withRegionEvictionPolicyDetails())
		.andThen(withRegionExpirationPolicyDetails())
		.andThen(withRegionStatisticsDetails());

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeRegionsHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeRegionsHealthIndicator() {
		super("Regions health check failed");
	}

	/**
	 * Constructs an instance of the {@link GeodeRegionsHealthIndicator} initialized with a reference to
	 * the {@link GemFireCache} instance.
	 *
	 * @param gemfireCache reference to the {@link GemFireCache} instance used to collect health information.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 */
	public GeodeRegionsHealthIndicator(GemFireCache gemfireCache) {
		super(gemfireCache);
	}

	/**
	 * Returns the collection of {@link BiConsumer} objects that applies health details about the {@link GemFireCache}
	 * {@link Region Regions} to the {@link Health} object.
	 *
	 * @return the collection of {@link BiConsumer} objects that applies health details about the {@link GemFireCache}
	 * {@link Region Regions} to the {@link Health} object.
	 * @see org.springframework.boot.actuate.health.Health
	 * @see org.apache.geode.cache.Region
	 * @see java.util.function.BiConsumer
	 */
	protected BiConsumer<Region<?, ?>, Health.Builder> getGemfireRegionHealthIndicatorConsumers() {
		return this.gemfireRegionHealthIndicatorConsumers;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getGemFireCache().isPresent()) {

			Set<Region<?, ?>> rootRegions = getGemFireCache()
				.map(GemFireCache::rootRegions)
				.orElseGet(Collections::emptySet);

			builder.withDetail("geode.cache.regions", rootRegions.stream()
				.filter(Objects::nonNull)
				.map(Region::getFullPath)
				.sorted()
				.collect(Collectors.toList()));

			builder.withDetail("geode.cache.regions.count", rootRegions.stream().filter(Objects::nonNull).count());

			rootRegions.stream()
				.filter(Objects::nonNull)
				.forEach(region -> getGemfireRegionHealthIndicatorConsumers().accept(region, builder));

			builder.up();

			return;
		}

		builder.unknown();
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			builder.withDetail(cacheRegionKey(regionName, "full-path"), region.getFullPath());

			if (isRegionAttributesPresent(region)) {

				RegionAttributes<?, ?> regionAttributes = region.getAttributes();

				builder.withDetail(cacheRegionKey(regionName, "cloning-enabled"), toYesNoString(regionAttributes.getCloningEnabled()))
					.withDetail(cacheRegionKey(regionName, "data-policy"), String.valueOf(regionAttributes.getDataPolicy()))
					.withDetail(cacheRegionKey(regionName, "initial-capacity"), regionAttributes.getInitialCapacity())
					.withDetail(cacheRegionKey(regionName, "load-factor"), regionAttributes.getLoadFactor())
					.withDetail(cacheRegionKey(regionName, "key-constraint"), nullSafeClassName(regionAttributes.getKeyConstraint()))
					.withDetail(cacheRegionKey(regionName, "off-heap"), toYesNoString(regionAttributes.getOffHeap()))
					.withDetail(cacheRegionKey(regionName, "pool-name"), emptyIfUnset(regionAttributes.getPoolName()))
					.withDetail(cacheRegionKey(regionName, "scope"), String.valueOf(regionAttributes.getScope()))
					.withDetail(cacheRegionKey(regionName, "statistics-enabled"), toYesNoString(regionAttributes.getStatisticsEnabled()))
					.withDetail(cacheRegionKey(regionName, "value-constraint"), nullSafeClassName(regionAttributes.getValueConstraint()));			}
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withPartitionRegionDetails() {

		return (region, builder) -> {

			if (isRegionAttributesPresent(region)) {

				PartitionAttributes<?, ?> partitionAttributes = region.getAttributes().getPartitionAttributes();

				if (partitionAttributes != null) {

					String regionName = region.getName();

					builder.withDetail(cachePartitionRegionKey(regionName, "collocated-with"), emptyIfUnset(partitionAttributes.getColocatedWith()))
						.withDetail(cachePartitionRegionKey(regionName, "local-max-memory"), partitionAttributes.getLocalMaxMemory())
						.withDetail(cachePartitionRegionKey(regionName, "redundant-copies"), partitionAttributes.getRedundantCopies())
						//.withDetail(cachePartitionRegionKey(regionName, "total-max-memory"), partitionAttributes.getTotalMaxMemory())
						.withDetail(cachePartitionRegionKey(regionName, "total-number-of-buckets"), partitionAttributes.getTotalNumBuckets());
				}
			}
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionEvictionPolicyDetails() {

		return (region, builder) -> {

			if (isRegionAttributesPresent(region)) {

				EvictionAttributes evictionAttributes = region.getAttributes().getEvictionAttributes();

				if (evictionAttributes != null) {

					String regionName = region.getName();

					builder.withDetail(cacheRegionEvictionKey(regionName, "action"), String.valueOf(evictionAttributes.getAction()))
						.withDetail(cacheRegionEvictionKey(regionName, "algorithm"), String.valueOf(evictionAttributes.getAlgorithm()));

					EvictionAlgorithm evictionAlgorithm = evictionAttributes.getAlgorithm();

					// NOTE: Eviction Maximum does not apply when Eviction Algorithm is Heap LRU.
					if (evictionAlgorithm != null && !evictionAlgorithm.isLRUHeap()) {
						builder.withDetail(cacheRegionEvictionKey(regionName,"maximum"),
							evictionAttributes.getMaximum());
					}
				}
			}
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionExpirationPolicyDetails() {

		return (region, builder) -> {

			if (isRegionAttributesPresent(region)) {

				String regionName = region.getName();

				RegionAttributes<?, ?> regionAttributes = region.getAttributes();

				ExpirationAttributes entryTimeToLive = regionAttributes.getEntryTimeToLive();

				if (entryTimeToLive != null) {
					builder.withDetail(cacheRegionExpirationKey(regionName, "entry.ttl.action"), String.valueOf(entryTimeToLive.getAction()))
						.withDetail(cacheRegionExpirationKey(regionName, "entry.ttl.timeout"), entryTimeToLive.getTimeout());
				}

				ExpirationAttributes entryIdleTimeout = regionAttributes.getEntryIdleTimeout();

				if (entryIdleTimeout != null) {
					builder.withDetail(cacheRegionExpirationKey(regionName, "entry.tti.action"), String.valueOf(entryIdleTimeout.getAction()))
						.withDetail(cacheRegionExpirationKey(regionName, "entry.tti.timeout"), entryIdleTimeout.getTimeout());
				}
			}
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionStatisticsDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			Optional.of(region)
				.filter(this::isNotLocalDataSet)
				.filter(this::isStatisticsEnabled)
				.map(RegionStatisticsResolver::resolve)
				.ifPresent(cacheStatistics -> builder
					.withDetail(cacheRegionStatisticsKey(regionName, "cache-statistics-type"), nullSafeClassName(cacheStatistics.getClass()))
					.withDetail(cacheRegionStatisticsKey(regionName, "hit-count"), cacheStatistics.getHitCount())
					.withDetail(cacheRegionStatisticsKey(regionName, "hit-ratio"), cacheStatistics.getHitRatio())
					.withDetail(cacheRegionStatisticsKey(regionName, "last-accessed-time"), cacheStatistics.getLastAccessedTime())
					.withDetail(cacheRegionStatisticsKey(regionName, "last-modified-time"), cacheStatistics.getLastModifiedTime())
					.withDetail(cacheRegionStatisticsKey(regionName, "miss-count"), cacheStatistics.getMissCount()));
		};
	}

	private boolean isLocalDataSet(Region<?, ?> region) {
		return region instanceof LocalDataSet;
	}

	private boolean isNotLocalDataSet(Region<?, ?> region) {
		return !isLocalDataSet(region);
	}

	private boolean isRegionAttributesPresent(Region<?, ?> region) {
		return region != null && region.getAttributes() != null;
	}

	private boolean isStatisticsEnabled(Region<?, ?> region) {
		return isRegionAttributesPresent(region) && region.getAttributes().getStatisticsEnabled();
	}

	private String cachePartitionRegionKey(String regionName, String suffix) {
		return cacheRegionKey(regionName, String.format("partition.%s", suffix));
	}

	private String cacheRegionKey(String regionName, String suffix) {
		return String.format("geode.cache.regions.%1$s.%2$s", regionName, suffix);
	}

	private String cacheRegionEvictionKey(String regionName, String suffix) {
		return cacheRegionKey(regionName, String.format("eviction.%s", suffix));
	}

	private String cacheRegionExpirationKey(String regionName, String suffix) {
		return cacheRegionKey(regionName, String.format("expiration.%s", suffix));
	}

	private String cacheRegionStatisticsKey(String regionName, String suffix) {
		return cacheRegionKey(regionName, String.format("statistics.%s", suffix));
	}

	private String emptyIfUnset(String value) {
		return StringUtils.hasText(value) ? value : "";
	}
}
