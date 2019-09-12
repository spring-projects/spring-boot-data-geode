/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.geode.config.annotation;

import static org.springframework.geode.config.annotation.ClusterAwareConfiguration.LOCAL_CLIENT_REGION_SHORTCUT;
import static org.springframework.geode.config.annotation.ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY;

import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.lang.Nullable;

/**
 * The {@link ClusterNotAvailableConfiguration} class is a Spring {@link Configuration @Configuration} class that
 * enables configuration when an Apache Geode or Pivotal GemFire cluster of servers is not available.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @since 1.2.0
 */
@Configuration
@Conditional(ClusterNotAvailableConfiguration.CusterNotAvailableCondition.class)
@SuppressWarnings("unused")
public class ClusterNotAvailableConfiguration {

	@Bean
	BeanPostProcessor localClientRegionBeanPostProcessor(Environment environment) {

		return new BeanPostProcessor() {

			@Nullable @Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof ClientRegionFactoryBean) {

					ClientRegionFactoryBean<?, ?> clientRegion = (ClientRegionFactoryBean<?, ?>) bean;

					configureAsLocalClientRegion(environment, clientRegion);
				}

				return bean;
			}
		};
	}

	@Bean
	@SuppressWarnings("unused")
	RegionConfigurer localClientRegionConfigurer(Environment environment) {

		return new RegionConfigurer() {

			@Override
			public void configure(String beanName, ClientRegionFactoryBean<?, ?> bean) {
				configureAsLocalClientRegion(environment, bean);
			}
		};
	}

	protected <K, V> ClientRegionFactoryBean configureAsLocalClientRegion(Environment environment,
		ClientRegionFactoryBean<K, V> clientRegion) {

		ClientRegionShortcut shortcut =
			environment.getProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
				ClientRegionShortcut.class, LOCAL_CLIENT_REGION_SHORTCUT);

		clientRegion.setPoolName(null);
		clientRegion.setShortcut(shortcut);

		return clientRegion;
	}

	public static final class CusterNotAvailableCondition extends ClusterAwareConfiguration.ClusterAwareCondition {

		@Override
		public synchronized boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return !super.matches(context, metadata);
		}
	}
}
