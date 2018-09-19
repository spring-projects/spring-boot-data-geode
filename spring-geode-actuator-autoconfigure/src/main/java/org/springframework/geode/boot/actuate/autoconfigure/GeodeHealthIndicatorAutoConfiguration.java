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

package org.springframework.geode.boot.actuate.autoconfigure;

import org.apache.geode.cache.GemFireCache;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
 * The GeodeHealthIndicatorAutoConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@AutoConfigureBefore(HealthIndicatorAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ GemFireCache.class, CacheFactoryBean.class })
@ConditionalOnEnabledHealthIndicator("geode")
@Import({
	BaseGeodeHealthIndicatorConfiguration.class,
	ClientCacheHealthIndicatorConfiguration.class,
	PeerCacheHealthIndicatorConfiguration.class,
})
@SuppressWarnings("unused")
public class GeodeHealthIndicatorAutoConfiguration {

}
