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

import org.apache.geode.cache.client.ClientCache;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's Continuous Query (CQ)
 * functionality in a {@link ClientCache} application.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.EnableContinuousQueries
 * @since 1.0.0
 */
@Configuration
@ConditionalOnBean(ClientCacheFactoryBean.class)
@ConditionalOnMissingBean(name = "continuousQueryBeanPostProcessor")
@EnableContinuousQueries
@SuppressWarnings("unused")
public class ContinuousQueryAutoConfiguration {

	@Bean
	public ClientCacheConfigurer enableSubscriptionClientCacheConfigurer() {
		return (beanName, clientCacheFactoryBean) -> clientCacheFactoryBean.setSubscriptionEnabled(true);
	}
}
