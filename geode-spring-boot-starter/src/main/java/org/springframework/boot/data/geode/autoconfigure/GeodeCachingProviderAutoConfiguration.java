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

package org.springframework.boot.data.geode.autoconfigure;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.geode.cache.GemFireCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.cache.GemfireCacheManager;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;

/**
 * Cache auto-configuration for Spring's Cache Abstraction, using Apache Geode as the caching provider.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.cache.GemfireCacheManager
 * @since 1.5.0
 */
@Configuration
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ GemfireCacheManager.class, GemFireCache.class })
@ConditionalOnMissingBean(CacheManager.class)
@AutoConfigureAfter(GeodeClientCacheAutoConfiguration.class)
@EnableGemfireCaching
@SuppressWarnings("all")
class GeodeCachingProviderAutoConfiguration {

	private final CacheManagerCustomizers cacheManagerCustomizers;

	private final CacheProperties cacheProperties;

	@Autowired
	private GemfireCacheManager cacheManager;

	GeodeCachingProviderAutoConfiguration(
			@Autowired(required = false) CacheProperties cacheProperties,
			@Autowired(required = false) CacheManagerCustomizers cacheManagerCustomizers) {

		this.cacheProperties = cacheProperties;
		this.cacheManagerCustomizers = cacheManagerCustomizers;
	}

	GemfireCacheManager getCacheManager() {
		return Optional.ofNullable(this.cacheManager)
			.orElseThrow(() -> newIllegalStateException("GemfireCacheManager was not properly configured"));
	}

	Optional<CacheManagerCustomizers> getCacheManagerCustomizers() {
		return Optional.ofNullable(this.cacheManagerCustomizers);
	}

	Optional<CacheProperties> getCacheProperties() {
		return Optional.ofNullable(this.cacheProperties);
	}

	@PostConstruct
	public void onGeodeCachingInitialization() {
		getCacheManagerCustomizers()
			.ifPresent(cacheManagerCustomizers -> cacheManagerCustomizers.customize(getCacheManager()));
	}
}
