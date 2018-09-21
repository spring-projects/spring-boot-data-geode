/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;

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
		.andThen(withRegionExpirationDetails())
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
	protected void doHealthCheck(Health.Builder builder) throws Exception {

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

			Optional.ofNullable(region.getAttributes())
				.ifPresent(regionAttributes -> builder
					.withDetail(cacheRegionKey(regionName, "cloning-enabled"), toYesNoString(regionAttributes.getCloningEnabled()))
					.withDetail(cacheRegionKey(regionName, "data-policy"), regionAttributes.getDataPolicy())
					.withDetail(cacheRegionKey(regionName, "initial-capacity"), regionAttributes.getInitialCapacity())
					.withDetail(cacheRegionKey(regionName, "load-factor"), regionAttributes.getLoadFactor())
					.withDetail(cacheRegionKey(regionName, "key-constraint"), nullSafeClassName(regionAttributes.getKeyConstraint()))
					.withDetail(cacheRegionKey(regionName, "off-heap"), toYesNoString(regionAttributes.getOffHeap()))
					.withDetail(cacheRegionKey(regionName, "pool-name"), regionAttributes.getPoolName())
					.withDetail(cacheRegionKey(regionName, "scope"), regionAttributes.getScope())
					.withDetail(cacheRegionKey(regionName, "value-constraint"), nullSafeClassName(regionAttributes.getValueConstraint())));
		};

	}

	private BiConsumer<Region<?, ?>, Health.Builder> withPartitionRegionDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			Optional.of(region)
				.filter(it -> it.getAttributes() != null)
				.map(Region::getAttributes)
				.map(RegionAttributes::getPartitionAttributes)
				.ifPresent(partitionAttributes -> builder
					.withDetail(cachePartitionRegionKey(regionName, "collocated-with"), partitionAttributes.getColocatedWith())
					.withDetail(cachePartitionRegionKey(regionName, "local-max-memory"), partitionAttributes.getLocalMaxMemory())
					.withDetail(cachePartitionRegionKey(regionName, "redundant-copies"), partitionAttributes.getRedundantCopies())
					.withDetail(cachePartitionRegionKey(regionName, "total-max-memory"), partitionAttributes.getTotalMaxMemory())
					.withDetail(cachePartitionRegionKey(regionName, "total-number-of-buckets"), partitionAttributes.getTotalNumBuckets()));
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionEvictionPolicyDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			Optional.of(region)
				.filter(it -> it.getAttributes() != null)
				.map(Region::getAttributes)
				.map(RegionAttributes::getEvictionAttributes)
				.ifPresent(evictionAttributes -> {

					builder.withDetail(cacheRegionEvictionKey(regionName, "action"), evictionAttributes.getAction())
						.withDetail(cacheRegionEvictionKey(regionName, "algorithm"), evictionAttributes.getAlgorithm());

					// NOTE: Careful! Eviction Maximum does not apply when Algorithm is Heap LRU.
					Optional.ofNullable(evictionAttributes.getAlgorithm())
						.filter(it -> !it.isLRUHeap())
						.ifPresent(it -> builder
							.withDetail(cacheRegionEvictionKey(regionName,"maximum"), evictionAttributes.getMaximum()));
				});
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionExpirationDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			Optional.of(region)
				.filter(it -> it.getAttributes() != null)
				.map(Region::getAttributes)
				.map(RegionAttributes::getEntryTimeToLive)
				.ifPresent(expirationAttributes -> builder
					.withDetail(cacheRegionExpirationKey(regionName, "entry.ttl.action"), expirationAttributes.getAction())
					.withDetail(cacheRegionExpirationKey(regionName, "entry.ttl.timeout"), expirationAttributes.getTimeout()));

			Optional.of(region)
				.filter(it -> it.getAttributes() != null)
				.map(Region::getAttributes)
				.map(RegionAttributes::getEntryIdleTimeout)
				.ifPresent(expirationAttributes -> builder
					.withDetail(cacheRegionExpirationKey(regionName, "entry.tti.action"), expirationAttributes.getAction())
					.withDetail(cacheRegionExpirationKey(regionName, "entry.tti.timeout"), expirationAttributes.getTimeout()));
		};
	}

	private BiConsumer<Region<?, ?>, Health.Builder> withRegionStatisticsDetails() {

		return (region, builder) -> {

			String regionName = region.getName();

			Optional.of(region)
				.filter(it -> it.getAttributes() != null)
				.filter(it -> it.getAttributes().getStatisticsEnabled())
				.map(Region::getStatistics)
				.ifPresent(cacheStatistics -> builder
					.withDetail(cacheRegionStatisticsKey(regionName, "hit-count"), cacheStatistics.getHitCount())
					.withDetail(cacheRegionStatisticsKey(regionName, "hit-ratio"), cacheStatistics.getHitRatio())
					.withDetail(cacheRegionStatisticsKey(regionName, "last-accessed-time"), cacheStatistics.getLastAccessedTime())
					.withDetail(cacheRegionStatisticsKey(regionName, "last-modified-time"), cacheStatistics.getLastModifiedTime())
					.withDetail(cacheRegionStatisticsKey(regionName, "miss-count"), cacheStatistics.getMissCount()));
		};
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
}
