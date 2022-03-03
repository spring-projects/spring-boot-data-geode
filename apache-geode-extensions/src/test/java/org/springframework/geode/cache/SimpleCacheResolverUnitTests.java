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

import org.junit.Test;

/**
 * Unit Tests for {@link SimpleCacheResolver}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.geode.cache.SimpleCacheResolver
 * @since 1.3.0
 */
public class SimpleCacheResolverUnitTests {

	@Test
	public void getInstanceReturnsASingleInstance() {

		SimpleCacheResolver cacheResolver = SimpleCacheResolver.getInstance();

		assertThat(cacheResolver).isNotNull();
		assertThat(cacheResolver).isSameAs(SimpleCacheResolver.getInstance());
	}

	@Test
	public void resolveWhenNoCacheIsPresentReturnsEmptyOptional() {
		assertThat(SimpleCacheResolver.getInstance().resolve().orElse(null)).isNull();
	}

	@Test
	public void resolveClientCacheWhenNoClientCacheIsPresentReturnsEmptyOptional() {
		assertThat(SimpleCacheResolver.getInstance().resolveClientCache().orElse(null)).isNull();
	}

	@Test
	public void resolvePeerCacheWhenNoPeerCacheIsPresentReturnsEmptyOptional() {
		assertThat(SimpleCacheResolver.getInstance().resolvePeerCache().orElse(null)).isNull();
	}

	@Test(expected = IllegalStateException.class)
	public void requireCacheWhenNoCacheIsPresentThrowsIllegalStateException() {

		try {
			SimpleCacheResolver.getInstance().require();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("GemFireCache not found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}
}
