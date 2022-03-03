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
package org.springframework.geode.cache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;

import org.springframework.geode.util.CacheUtils;

/**
 * The {@link SimpleCacheResolver} abstract class contains utility functions for resolving Apache Geode
 * {@link GemFireCache} instances, such as a {@link ClientCache} or a {@literal peer} {@link Cache}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.RegionService
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientCacheFactory
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class SimpleCacheResolver {

	private static final AtomicReference<SimpleCacheResolver> instance = new AtomicReference<>(null);

	/**
	 * Lazily constructs and gets an instance to the {@link SimpleCacheResolver}, as needed.
	 *
	 * @return an instance of the {@link SimpleCacheResolver}.
	 * @see #newSimpleCacheResolver()
	 */
	public static SimpleCacheResolver getInstance() {

		return instance.updateAndGet(cacheResolver -> cacheResolver != null
			? cacheResolver
			: newSimpleCacheResolver());
	}

	// TODO Consider resolving the SimpleCacheResolver instance using Java's ServiceProvider API.
	private static SimpleCacheResolver newSimpleCacheResolver() {
		return new SimpleCacheResolver() { };
	}

	/**
	 * 	The 1st {@code resolve():Optional<? extends GemFireCache>} method signature avoids the cast
	 * 	  and the @SuppressWarnings("unchecked") annotation, but puts the burden on the caller.
	 * 	The 2nd {@code resolve():Optional<T extends GemFireCache>} method signature requires a cast
	 * 	  and the @SuppressWarnings("unchecked") annotation, but avoids putting the burden on the caller.
	 */
	private static void testCallResolve() {
		Optional<ClientCache> clientCache = getInstance().resolve();
	}

	/**
	 * The resolution algorithm first tries to resolve an {@link Optional} {@link ClientCache} instance
	 * then a {@literal peer} {@link Cache} instance if a {@link ClientCache} is not present.
	 *
	 * If neither a {@link ClientCache} or {@literal peer} {@link Cache} is available, then {@link Optional#empty()}
	 * is returned.  No {@link Throwable Exception} is thrown.
	 *
	 * @param <T> {@link Class subclass} of {@link GemFireCache}.
	 * @return a {@link ClientCache} or then a {@literal peer} {@link Cache} instance if present.
	 * @see org.apache.geode.cache.client.ClientCache
	 * @see org.apache.geode.cache.Cache
	 * @see java.util.Optional
	 * @see #resolveClientCache()
	 * @see #resolvePeerCache()
	 */
	//public static Optional<? extends GemFireCache> resolve() {
	@SuppressWarnings("unchecked")
	public <T extends GemFireCache> Optional<T> resolve() {

		Optional<ClientCache> clientCache = resolveClientCache();

		return (Optional<T>) (clientCache.isPresent() ? clientCache : resolvePeerCache());
	}

	/**
	 * Attempts to resolve an {@link Optional} {@link ClientCache} instance.
	 *
	 * @return an {@link Optional} {@link ClientCache} instance.
	 * @see org.springframework.geode.util.CacheUtils#isClientCache(RegionService)
	 * @see org.apache.geode.cache.client.ClientCacheFactory#getAnyInstance()
	 * @see org.apache.geode.cache.client.ClientCache
	 * @see java.util.Optional
	 */
	public Optional<ClientCache> resolveClientCache() {

		try {
			return Optional.ofNullable(ClientCacheFactory.getAnyInstance())
				.filter(CacheUtils::isClientCache);
		}
		catch (Throwable ignore) {
			return Optional.empty();
		}
	}

	/**
	 * Attempts to resolve an {@link Optional} {@link Cache} instance.
	 *
	 * @return an {@link Optional} {@link Cache} instance.
	 * @see org.springframework.geode.util.CacheUtils#isPeerCache(RegionService)
	 * @see org.apache.geode.cache.CacheFactory#getAnyInstance()
	 * @see org.apache.geode.cache.Cache
	 * @see java.util.Optional
	 */
	public Optional<Cache> resolvePeerCache() {

		try {
			return Optional.ofNullable(CacheFactory.getAnyInstance())
				.filter(CacheUtils::isPeerCache);
		}
		catch (Throwable ignore) {
			return Optional.empty();
		}
	}

	/**
	 * Requires an instance of either a {@link ClientCache} or a {@literal peer} {@link Cache}.
	 *
	 * @param <T> {@link Class subclass} of {@link GemFireCache} to resolve.
	 * @return an instance of either a {@link ClientCache} or a {@literal peer} {@link Cache}.
	 * @throws IllegalStateException if a cache instance cannot be resolved.
	 * @see org.apache.geode.cache.client.ClientCache
	 * @see org.apache.geode.cache.Cache
	 * @see org.apache.geode.cache.GemFireCache
	 * @see #resolve()
	 */
	public <T extends GemFireCache> T require() {
		return this.<T>resolve()
			.orElseThrow(() -> new IllegalStateException("GemFireCache not found"));
	}
}
