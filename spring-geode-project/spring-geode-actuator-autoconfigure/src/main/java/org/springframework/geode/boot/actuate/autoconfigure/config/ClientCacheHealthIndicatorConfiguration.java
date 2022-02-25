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
package org.springframework.geode.boot.actuate.autoconfigure.config;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;
import org.springframework.data.gemfire.util.CacheUtils;
import org.springframework.geode.boot.actuate.GeodeContinuousQueriesHealthIndicator;
import org.springframework.geode.boot.actuate.GeodePoolsHealthIndicator;

/**
 * Spring {@link Configuration} class declaring Spring beans for Apache Geode {@link ClientCache}
 * {@link HealthIndicator HealthIndicators}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.geode.boot.actuate.GeodeContinuousQueriesHealthIndicator
 * @see org.springframework.geode.boot.actuate.GeodePoolsHealthIndicator
 * @since 1.0.0
 */
@Configuration
@Conditional(ClientCacheHealthIndicatorConfiguration.ClientCacheCondition.class)
@SuppressWarnings("unused")
public class ClientCacheHealthIndicatorConfiguration {

	@Bean("GeodeContinuousQueryHealthIndicator")
	GeodeContinuousQueriesHealthIndicator continuousQueriesHealthIndicator(
			@Autowired(required = false) ContinuousQueryListenerContainer continuousQueryListenerContainer) {

		return new GeodeContinuousQueriesHealthIndicator(continuousQueryListenerContainer);
	}

	@Bean("GeodePoolsHealthIndicator")
	GeodePoolsHealthIndicator poolsHealthIndicator(GemFireCache gemfireCache) {
		return new GeodePoolsHealthIndicator(gemfireCache);
	}

	public static final class ClientCacheCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			Cache peerCache = CacheUtils.getCache();

			ClientCache clientCache = CacheUtils.getClientCache();

			return clientCache != null || peerCache == null;
		}
	}
}
