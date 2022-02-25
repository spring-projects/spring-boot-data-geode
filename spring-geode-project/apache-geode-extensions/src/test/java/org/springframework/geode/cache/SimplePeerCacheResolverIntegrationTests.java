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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;

/**
 * Integration Tests for {@link SimpleCacheResolver} using an Apache Geode {@literal peer} {@link Cache}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.springframework.geode.cache.SimpleCacheResolver
 * @since 1.3.0
 */
public class SimplePeerCacheResolverIntegrationTests {

	private static Cache peerCache;

	@BeforeClass
	public static void createPeerCache() {
		peerCache = new CacheFactory().create();
		assertThat(peerCache).isNotNull();
	}

	@AfterClass
	public static void destroyPeerCache() {
		Optional.ofNullable(peerCache).ifPresent(Cache::close);
	}

	@Test
	public void resolveReturnsPeerCache() {
		assertThat(SimpleCacheResolver.getInstance().resolve().orElse(null)).isSameAs(peerCache);
	}

	@Test
	public void resolveClientCacheReturnsEmptyOptional() {
		assertThat(SimpleCacheResolver.getInstance().resolveClientCache().orElse(null)).isNull();
	}

	@Test
	public void resolvePeerCacheReturnsPeerCache() {
		assertThat(SimpleCacheResolver.getInstance().resolvePeerCache().orElse(null)).isSameAs(peerCache);
	}

	@Test
	public void requireReturnsPeerCache() {
		assertThat(SimpleCacheResolver.getInstance().<Cache>require()).isSameAs(peerCache);
	}
}
