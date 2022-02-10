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
package org.springframework.geode.boot.actuate.health.support;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.geode.cache.CacheStatistics;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.StatisticsDisabledException;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.internal.cache.BucketRegion;
import org.apache.geode.internal.cache.PartitionedRegion;
import org.apache.geode.internal.cache.PartitionedRegionDataStore;

import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.util.Assert;

/**
 * The {@link RegionStatisticsResolver} class is a utility class for resolving the {@link CacheStatistics}
 * for a {@link Region}, regardless of {@link Region} type, or more specifically {@link Region Region's}
 * {@link DataPolicy data management policy}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.CacheStatistics
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.partition.PartitionRegionHelper
 * @see org.apache.geode.internal.cache.BucketRegion
 * @see org.apache.geode.internal.cache.PartitionedRegion
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class RegionStatisticsResolver {

	public static CacheStatistics resolve(Region<?, ?> region) {

		return region != null
			? PartitionRegionHelper.isPartitionedRegion(region)
				? new PartitionRegionCacheStatistics(region)
				: region.getStatistics()
			: null;
	}

	protected static class PartitionRegionCacheStatistics implements CacheStatistics {

		private final PartitionedRegion partitionRegion;

		private float hitRatio = 0.0f;

		private long hitCount = 0L;
		private long lastAccessedTime = 0L;
		private long lastModifiedTime = 0L;
		private long missCount = 0L;

		protected PartitionRegionCacheStatistics(Region<?, ?> region) {

			Assert.isInstanceOf(PartitionedRegion.class, region, () ->
				String.format("Region [%1$s] must be of type [%2$s]", RegionUtils.toRegionPath(region),
					PartitionedRegion.class.getName()));

			this.partitionRegion = computeStatistics((PartitionedRegion) region);
		}

		protected PartitionedRegion computeStatistics(PartitionedRegion region) {

			float totalHitRatio = 0.0f;

			int totalCount = 0;

			long totalHitCount = 0L;
			long maxLastAccessedTime = 0L;
			long maxLastModifiedTime = 0L;
			long totalMissCount = 0L;

			Set<BucketRegion> bucketRegions = Optional.of(region)
				.map(PartitionedRegion::getDataStore)
				.map(PartitionedRegionDataStore::getAllLocalBucketRegions)
				.orElseGet(Collections::emptySet);

			for (BucketRegion bucket : bucketRegions) {

				CacheStatistics bucketStatistics = bucket.getStatistics();

				if (bucketStatistics != null) {

					totalCount++;

					totalHitCount += bucketStatistics.getHitCount();
					totalHitRatio += bucketStatistics.getHitRatio();
					maxLastAccessedTime = Math.max(maxLastAccessedTime, bucketStatistics.getLastAccessedTime());
					maxLastModifiedTime = Math.max(maxLastModifiedTime, bucketStatistics.getLastModifiedTime());
					totalMissCount += bucketStatistics.getMissCount();
				}
			}

			if (totalCount > 0) {
				this.hitCount = totalHitCount / totalCount;
				this.hitRatio = totalHitRatio / totalCount;
				this.lastAccessedTime = maxLastAccessedTime;
				this.lastModifiedTime = maxLastModifiedTime;
				this.missCount = totalMissCount / totalCount;
			}

			return region;
		}

		protected PartitionedRegion getPartitionRegion() {
			return this.partitionRegion;
		}

		@Override
		public long getHitCount() throws StatisticsDisabledException {
			return this.hitCount;
		}

		@Override
		public float getHitRatio() throws StatisticsDisabledException {
			return this.hitRatio;
		}

		@Override
		public long getLastAccessedTime() throws StatisticsDisabledException {
			return this.lastAccessedTime;
		}

		@Override
		public long getLastModifiedTime() {
			return this.lastModifiedTime;
		}

		@Override
		public long getMissCount() throws StatisticsDisabledException {
			return this.missCount;
		}

		@Override
		public void resetCounts() throws StatisticsDisabledException {

			this.hitCount = 0L;
			this.hitRatio = 0.0f;
			this.lastAccessedTime = 0L;
			this.lastModifiedTime = 0L;
			this.missCount = 0L;
		}
	}
}
