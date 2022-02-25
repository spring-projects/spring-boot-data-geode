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
package org.springframework.geode.config.annotation;

import static org.springframework.geode.config.annotation.ClusterAwareConfiguration.LOCAL_CLIENT_REGION_SHORTCUT;
import static org.springframework.geode.config.annotation.ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY;

import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.config.annotation.support.CacheTypeAwareRegionFactoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link ClusterNotAvailableConfiguration} class is a Spring {@link Configuration} class that enables configuration
 * when an Apache Geode cluster of servers is not available.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.boot.autoconfigure.condition.AllNestedConditions
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @see org.springframework.data.gemfire.config.annotation.support.CacheTypeAwareRegionFactoryBean
 * @see org.springframework.geode.config.annotation.ClusterAwareConfiguration
 * @since 1.2.0
 */
@Configuration
@Conditional(ClusterNotAvailableConfiguration.AllClusterNotAvailableConditions.class)
@SuppressWarnings("unused")
public class ClusterNotAvailableConfiguration {

	@Bean
	BeanPostProcessor localClientRegionBeanPostProcessor(@NonNull Environment environment) {

		return new BeanPostProcessor() {

			@Nullable @Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (isClientRegion(bean)) {
					configureAsLocalClientRegion(environment, bean);
				}

				return bean;
			}
		};
	}

	@Bean
	@SuppressWarnings("unused")
	RegionConfigurer localClientRegionConfigurer(@NonNull Environment environment) {

		return new RegionConfigurer() {

			@Override
			public void configure(String beanName, ClientRegionFactoryBean<?, ?> bean) {
				configureAsLocalClientRegion(environment, bean);
			}
		};
	}

	protected boolean isClientRegion(@Nullable Object bean) {
		return bean instanceof CacheTypeAwareRegionFactoryBean || bean instanceof ClientRegionFactoryBean;
	}

	protected @NonNull Object configureAsLocalClientRegion(@NonNull Environment environment,
			@NonNull Object clientRegion) {

		return clientRegion instanceof ClientRegionFactoryBean
			? configureAsLocalClientRegion(environment, (ClientRegionFactoryBean<?, ?>) clientRegion)
			: configureAsLocalClientRegion(environment, (CacheTypeAwareRegionFactoryBean<?, ?>) clientRegion);
	}

	protected @NonNull <K, V> CacheTypeAwareRegionFactoryBean<K, V> configureAsLocalClientRegion(
			@NonNull Environment environment, @NonNull CacheTypeAwareRegionFactoryBean<K, V> clientRegion) {

		ClientRegionShortcut shortcut =
			environment.getProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
				ClientRegionShortcut.class, LOCAL_CLIENT_REGION_SHORTCUT);

		clientRegion.setClientRegionShortcut(shortcut);
		clientRegion.setPoolName(GemfireUtils.DEFAULT_POOL_NAME);

		return clientRegion;
	}

	protected @NonNull <K, V> ClientRegionFactoryBean<K, V> configureAsLocalClientRegion(
			@NonNull Environment environment, @NonNull ClientRegionFactoryBean<K, V> clientRegion) {

		ClientRegionShortcut shortcut =
			environment.getProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
				ClientRegionShortcut.class, LOCAL_CLIENT_REGION_SHORTCUT);

		clientRegion.setPoolName(null);
		clientRegion.setShortcut(shortcut);

		return clientRegion;
	}

	public static final class AllClusterNotAvailableConditions extends AllNestedConditions {

		public AllClusterNotAvailableConditions() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@Conditional(ClusterNotAvailableCondition.class)
		static class IsClusterNotAvailableCondition { }

		@Conditional(NotCloudFoundryEnvironmentCondition.class)
		static class IsNotCloudFoundryEnvironmentCondition { }

		@Conditional(NotKubernetesEnvironmentCondition.class)
		static class IsNotKubernetesEnvironmentCondition { }

	}

	public static final class ClusterNotAvailableCondition extends ClusterAwareConfiguration.ClusterAwareCondition {

		@Override
		public synchronized boolean matches(@NonNull ConditionContext conditionContext,
				@NonNull AnnotatedTypeMetadata typeMetadata) {

			return !super.matches(conditionContext, typeMetadata);
		}
	}

	public static final class NotCloudFoundryEnvironmentCondition implements Condition {

		@Override
		public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
			return !CloudPlatform.CLOUD_FOUNDRY.isActive(context.getEnvironment());
		}
	}

	public static final class NotKubernetesEnvironmentCondition implements Condition {

		@Override
		public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
			return !CloudPlatform.KUBERNETES.isActive(context.getEnvironment());
		}
	}
}
