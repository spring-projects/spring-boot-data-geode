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
package org.springframework.geode.boot.autoconfigure.support;

import java.util.Optional;
import java.util.Set;

import org.apache.geode.cache.client.Pool;

import org.apache.shiro.util.CollectionUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.PoolConfigurer;

/**
 * A Spring {@link Configuration} class used to enable subscription on the Apache Geode {@literal DEFAULT} {@link Pool}
 * as well as the SDG {@literal gemfirePool} {@link Pool}, only.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.Pool
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PoolConfigurer
 * @since 1.2.0
 */
@Configuration
@SuppressWarnings("unused")
public class EnableSubscriptionConfiguration {

	private static final String DEFAULT_POOL_NAME = "DEFAULT";
	private static final String GEMFIRE_POOL_NAME = "gemfirePool";

	private static final Set<String> POOL_NAMES = CollectionUtils.asSet(DEFAULT_POOL_NAME, GEMFIRE_POOL_NAME);

	@Bean
	public ClientCacheConfigurer enableSubscriptionClientCacheConfigurer() {
		return (beanName, clientCacheFactoryBean) -> clientCacheFactoryBean.setSubscriptionEnabled(true);
	}

	@Bean
	public PoolConfigurer enableSubscriptionPoolConfigurer() {

		return (beanName, poolFactoryBean) -> Optional.ofNullable(beanName)
			.filter(POOL_NAMES::contains)
			.ifPresent(poolName -> poolFactoryBean.setSubscriptionEnabled(true));
	}
}
