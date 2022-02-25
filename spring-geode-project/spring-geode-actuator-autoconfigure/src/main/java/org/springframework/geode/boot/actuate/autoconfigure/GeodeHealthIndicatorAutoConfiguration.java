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
package org.springframework.geode.boot.actuate.autoconfigure;

import org.apache.geode.cache.GemFireCache;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.geode.boot.actuate.autoconfigure.config.BaseGeodeHealthIndicatorConfiguration;
import org.springframework.geode.boot.actuate.autoconfigure.config.ClientCacheHealthIndicatorConfiguration;
import org.springframework.geode.boot.actuate.autoconfigure.config.PeerCacheHealthIndicatorConfiguration;
import org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for Apache Geode
 * {@link HealthIndicator HealthIndicators}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator
 * @see org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.geode.boot.actuate.autoconfigure.config.BaseGeodeHealthIndicatorConfiguration
 * @see org.springframework.geode.boot.actuate.autoconfigure.config.ClientCacheHealthIndicatorConfiguration
 * @see org.springframework.geode.boot.actuate.autoconfigure.config.PeerCacheHealthIndicatorConfiguration
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass(CacheFactoryBean.class)
@ConditionalOnEnabledHealthIndicator("geode")
@Import({
	BaseGeodeHealthIndicatorConfiguration.class,
	ClientCacheHealthIndicatorConfiguration.class,
	PeerCacheHealthIndicatorConfiguration.class,
})
@SuppressWarnings("unused")
public class GeodeHealthIndicatorAutoConfiguration {

}
