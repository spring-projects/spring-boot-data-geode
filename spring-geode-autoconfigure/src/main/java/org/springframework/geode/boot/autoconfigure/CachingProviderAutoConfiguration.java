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
package org.springframework.geode.boot.autoconfigure;

import static org.springframework.data.gemfire.util.CollectionUtils.asSet;

import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.geode.cache.GemFireCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.cache.GemfireCacheManager;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for Spring's Cache Abstraction
 * using Apache Geode as the caching provider.
 *
 * @author John Blum
 * @see javax.annotation.PostConstruct
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers
 * @see org.springframework.boot.autoconfigure.cache.CacheProperties
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.data.gemfire.cache.GemfireCacheManager
 * @see org.springframework.data.gemfire.cache.config.EnableGemfireCaching
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @since 1.0.0
 */
@SpringBootConfiguration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@Conditional(CachingProviderAutoConfiguration.SpringCacheTypeCondition.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ GemfireCacheManager.class, GemFireCache.class })
@ConditionalOnMissingBean(CacheManager.class)
@EnableGemfireCaching
@SuppressWarnings("unused")
public class CachingProviderAutoConfiguration {

	protected static final Set<String> SPRING_CACHE_TYPES = asSet("gemfire", "geode");

	protected static final String SPRING_CACHE_TYPE_PROPERTY = "spring.cache.type";

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

		Assert.state(this.cacheManager != null, "GemfireCacheManager was not properly configured");

		return this.cacheManager;
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

	public static class SpringCacheTypeCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			String springCacheType = context.getEnvironment().getProperty(SPRING_CACHE_TYPE_PROPERTY);

			return !StringUtils.hasText(springCacheType) || SPRING_CACHE_TYPES.contains(springCacheType);
		}
	}
}
