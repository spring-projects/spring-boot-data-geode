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

package org.springframework.geode.boot.autoconfigure;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.geode.cache.GemFireCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for Spring's Cache Abstraction
 * using Apache Geode as the caching provider.
 *
 * @author John Blum
 * @see javax.annotation.PostConstruct
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers
 * @see org.springframework.boot.autoconfigure.cache.CacheProperties
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.cache.GemfireCacheManager
 * @see org.springframework.data.gemfire.cache.config.EnableGemfireCaching
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ GemfireCacheManager.class, GemFireCache.class })
@ConditionalOnMissingBean(CacheManager.class)
@EnableGemfireCaching
@SuppressWarnings("all")
public class CachingProviderAutoConfiguration {

	private final CacheManagerCustomizers cacheManagerCustomizers;

	private final CacheProperties cacheProperties;

	@Autowired
	private GemfireCacheManager cacheManager;

	CachingProviderAutoConfiguration(
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
