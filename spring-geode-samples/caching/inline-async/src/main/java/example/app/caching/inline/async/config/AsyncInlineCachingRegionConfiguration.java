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
package example.app.caching.inline.async.config;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer;

import example.app.caching.inline.async.client.model.Golfer;

/**
 * Spring {@link Configuration} class used to configure an Apache Geode cache {@link Region}
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.ReplicatedRegionFactoryBean
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @since 1.4.0
 */
@Configuration
@SuppressWarnings("unused")
public class AsyncInlineCachingRegionConfiguration {

	protected static final String GOLFERS_REGION_NAME = "Golfers";

	@Bean(GOLFERS_REGION_NAME)
	public ReplicatedRegionFactoryBean<Object, Object> golfersRegion(GemFireCache gemfireCache,
			AsyncInlineCachingRegionConfigurer<Golfer, String> asyncInlineCachingRegionConfigurer) {

		ReplicatedRegionFactoryBean<Object, Object> golfersRegion = new ReplicatedRegionFactoryBean<>();

		golfersRegion.setCache(gemfireCache);
		golfersRegion.setPersistent(false);
		golfersRegion.setRegionConfigurers(asyncInlineCachingRegionConfigurer);

		return golfersRegion;
	}

	@Bean
	@DependsOn(GOLFERS_REGION_NAME)
	public GemfireTemplate golfersTemplate(GemFireCache gemfireCache) {
		return new GemfireTemplate(gemfireCache.getRegion(GOLFERS_REGION_NAME));
	}
}
