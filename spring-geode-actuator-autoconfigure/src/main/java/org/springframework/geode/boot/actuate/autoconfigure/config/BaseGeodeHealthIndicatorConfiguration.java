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

package org.springframework.geode.boot.actuate.autoconfigure.config;

import org.apache.geode.cache.GemFireCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.geode.boot.actuate.GeodeCacheHealthIndicator;
import org.springframework.geode.boot.actuate.GeodeDiskStoresHealthIndicator;
import org.springframework.geode.boot.actuate.GeodeIndexesHealthIndicator;
import org.springframework.geode.boot.actuate.GeodeRegionsHealthIndicator;

/**
 * The BaseGeodeHealthIndicatorConfiguration class...
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.geode.boot.actuate.GeodeCacheHealthIndicator
 * @see org.springframework.geode.boot.actuate.GeodeDiskStoresHealthIndicator
 * @see org.springframework.geode.boot.actuate.GeodeIndexesHealthIndicator
 * @see org.springframework.geode.boot.actuate.GeodeRegionsHealthIndicator
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class BaseGeodeHealthIndicatorConfiguration {

	@Bean("GeodeCacheHealthIndicator")
	GeodeCacheHealthIndicator cacheHealthIndicator(GemFireCache gemfireCache) {
		return new GeodeCacheHealthIndicator(gemfireCache);
	}

	@Bean("GeodeDiskStoresHealthIndicator")
	GeodeDiskStoresHealthIndicator diskStoresHealthIndicator(ApplicationContext applicationContext) {
		return new GeodeDiskStoresHealthIndicator(applicationContext);
	}

	@Bean("GeodeIndexesHealthIndicator")
	GeodeIndexesHealthIndicator indexesHealthIndicator(ApplicationContext applicationContext) {
		return new GeodeIndexesHealthIndicator(applicationContext);
	}

	@Bean("GeodeRegionsHealthIndicator")
	GeodeRegionsHealthIndicator regionsHealthIndicator(GemFireCache gemfireCache) {
		return new GeodeRegionsHealthIndicator(gemfireCache);
	}
}
