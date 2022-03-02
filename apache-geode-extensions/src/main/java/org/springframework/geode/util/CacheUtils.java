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
package org.springframework.geode.util;

import static org.springframework.geode.util.GeodeAssertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.internal.cache.GemFireCacheImpl;

/**
 * Abstract utility class for working with Apache Geode cache instances, such as {@link ClientCache}
 * and {@literal peer} {@link Cache} instances.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.DataPolicy
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionAttributes
 * @see org.apache.geode.cache.RegionService
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @since 1.3.0
 */
public abstract class CacheUtils {

	/**
	 * Collects all {@link Object values} from the given {@link Region}.
	 *
	 * This method is capable of pulling {@link Object values} from either {@literal client}
	 * or {@literal peer} {@link Region Regions}.
	 *
	 * @param <T> {@link Class type} of the {@link Region} {@link Object values}.
	 * @param region {@link Region} from which to collect the {@link Object values}.
	 * @return a {@link Collection} of all {@link Object values} from the given {@link Region}.
	 * @throws IllegalArgumentException if {@link Region} is {@literal null}.
	 * @see org.apache.geode.cache.Region
	 * @see java.util.Collection
	 */
	// TODO Add Predicate-based Filtering
	public static <T> Collection<T> collectValues(Region<?, T> region) {

		assertThat(region).isNotNull();

		return isClientRegion(region)
			? clientRegionValues(region)
			: localRegionValues(region);
	}

	/**
	 * Collects values from the given {@literal client} {@link Region}.
	 *
	 * @param <T> {@link Class type} of the {@link Region Region's} values.
	 * @param region {@link Region} from which to collect values.
	 * @return a {@link Collection} of the {@literal client} {@link Region Region's} values.
	 * @see org.apache.geode.cache.Region
	 * @see #clientRegionValuesFromServer(Region)
	 * @see #localRegionValues(Region)
	 * @see #isProxyRegion(Region)
	 */
	private static <T> Collection<T> clientRegionValues(Region<?, T> region) {

		return isProxyRegion(region)
			? clientRegionValuesFromServer(region)
			: localRegionValues(region);
	}

	/**
	 * Collections all values from the {@literal client} {@link Region} by pulling the values down from the server.
	 *
	 * @param <T> {@link Class type} of the {@link Region Region's} values.
	 * @param region {@link Region} from which to collect values.
	 * @return a {@link Collection} containing the values from the {@literal client} {@link Region} on the server.
	 * @see org.apache.geode.cache.Region#keySetOnServer()
	 * @see #getAll(Region, Set)
	 */
	private static <T> Collection<T> clientRegionValuesFromServer(Region<?, T> region) {

		Set<?> keys = nullSafeSet(region.keySetOnServer());

		return !keys.isEmpty() ? getAll(region, keys) : Collections.emptySet();
	}

	/**
	 * Gets all values from the given {@link Region} mapped to the specified {@link Set keys}.
	 *
	 * @param <T> {@link Class type} of the {@link Region Region's} values.
	 * @param region {@link Region} from which to get all values.
	 * @param keys {@link Set} of keys targeting the values to retrieve.
	 * @return a {@link Collection} of the {@link Region Region's} values.
	 * @see org.apache.geode.cache.Region#getAll(Collection)
	 */
	private static <T> Collection<T> getAll(Region<?, T> region, Set<?> keys) {
		return nullSafeMap(region.getAll(keys)).values();
		// Fallback procedure if region.getAll(keys) is buggered
		//return keys.stream().map(region::get).collect(Collectors.toSet());
	}

	/**
	 * Collects values from the given {@link Region}, locally.
	 *
	 * @param <T> {@link Class type} of the {@link Region Region's} values.
	 * @param region {@link Region} from which to collect values.
	 * @return a {@link Collection} of the {@link Region Region's} values.
	 * @see org.apache.geode.cache.Region#values()
	 */
	private static <T> Collection<T> localRegionValues(Region<?, T> region) {
		return region.values();
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
		return map != null ? map :Collections.emptyMap();
	}

	private static <T> Set<T> nullSafeSet(Set<T> set) {
		return set != null ? set : Collections.emptySet();
	}

	/**
	 * Null-safe method to determine whether the given {@link RegionService} is an instance of {@link ClientCache}.
	 *
	 * The problem is, {@link GemFireCacheImpl} implements both the (peer) {@link Cache}
	 * and {@link ClientCache} interfaces. #sigh
	 *
	 * @param regionService {@link RegionService} to evaluate.
	 * @return a boolean value indicating whether the {@link RegionService} an instance of {@link ClientCache}.
	 * @see org.apache.geode.cache.client.ClientCache
	 * @see org.apache.geode.cache.RegionService
	 */
	public static boolean isClientCache(RegionService regionService) {

		boolean result = regionService instanceof ClientCache;

		if (regionService instanceof GemFireCacheImpl) {
			result &= ((GemFireCacheImpl) regionService).isClient();
		}

		return result;
	}

	/**
	 * Null-safe method to determine whether the given {@link Region} is a {@literal client} {@link Region}
	 * in a {@link ClientCache}.
	 *
	 * @param region {@link Region} to evaluate.
	 * @return a boolean value indicating whether the given {@link Region} is a {@literal client} {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #isClientCache(RegionService)
	 */
	public static boolean isClientRegion(Region<?, ?> region) {

		return region != null
			&& (isClientCache(region.getRegionService()) || isRegionWithPool(region));
	}

	/**
	 * Null-safe method to determine whether the given {@link RegionService} is an instance of
	 * a {@literal peer} {@link Cache}.
	 *
	 * The problem is, {@link GemFireCacheImpl} implements both the (peer) {@link Cache}
	 * and {@link ClientCache} interfaces. #sigh
	 *
	 * @param regionService {@link RegionService} to evaluate.
	 * @return a boolean value indicating whether the {@link RegionService} is an instance of
	 * a {@literal peer} {@link Cache}.
	 * @see org.apache.geode.cache.RegionService
	 * @see org.apache.geode.cache.Cache
	 */
	public static boolean isPeerCache(RegionService regionService) {

		boolean result = regionService instanceof Cache;

		if (regionService instanceof GemFireCacheImpl) {
			result &= !((GemFireCacheImpl) regionService).isClient();
		}

		return result;
	}

	/**
	 * Null-safe method to determine whether the given {@link Region} is a {@literal peer} {@link Region}
	 * in a {@literal peer} {@link Cache}.
	 *
	 * @param region {@link Region} to evaluate.
	 * @return a boolean value indicating whether the given {@link Region} is a {@literal peer} {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #isPeerCache(RegionService)
	 */
	public static boolean isPeerRegion(Region<?, ?> region) {
		return region != null && !isClientRegion(region);
	}

	/**
	 * Null-safe method to determine whether the given {@link Region} is a [client] {@literal PROXY} {@link Region}.
	 *
	 * The {@link Region} is a {@literal PROXY} if the {@link DataPolicy} is {@link DataPolicy#EMPTY}
	 * or the {@link Region} has a configured {@link Pool}.
	 *
	 * @param region {@link Region} to evaluate.
	 * @return a boolean value to determine whether the {@link Region} is a [client] {@literal PROXY} {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #isRegionWithPool(Region)
	 */
	public static boolean isProxyRegion(Region<?, ?> region) {

		return region != null
			&& region.getAttributes() != null
			&& (DataPolicy.EMPTY.equals(region.getAttributes().getDataPolicy())
			|| isRegionWithPool(region));
	}

	/**
	 * Null-safe method to determine whether the given {@link Region} was configured with a {@link Pool}.
	 *
	 * A {@link Region} configured with a {@link Pool} (by {@link String name} specified in the {@link RegionAttributes})
	 * is a strong indicator that the {@link Region} is a {@literal client} {@link Region}.
	 *
	 * @param region {@link Region} to evaluate.
	 * @return a boolean to determine whether the given {@link Region} was configured with a {@link Pool}.
	 * @see org.apache.geode.cache.client.Pool
	 * @see org.apache.geode.cache.Region
	 */
	public static boolean isRegionWithPool(Region<?, ?> region) {

		return Optional.ofNullable(region)
			.map(Region::getAttributes)
			.map(RegionAttributes::getPoolName)
			.filter(CacheUtils::hasText)
			.isPresent();
	}
}
