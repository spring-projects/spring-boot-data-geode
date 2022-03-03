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
package example.app.crm.support;

import java.util.Arrays;
import java.util.Optional;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.internal.net.SSLConfig;
import org.apache.geode.internal.net.SSLConfigurationFactory;
import org.apache.geode.internal.security.SecurableCommunicationChannel;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug {@link Configuration} for Apache Geode based Spring Boot Tests.
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @since 1.2.0
 */
@Configuration
@Profile("debug")
@SuppressWarnings("unused")
public class GemFireDebugConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(GemFireDebugConfiguration.class);

	@Bean
	public BeanPostProcessor gemfireCacheSslConfigurationBeanPostProcessor() {

		return new BeanPostProcessor() {

			@Nullable @Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				Optional.ofNullable(bean)
					.filter(GemFireCache.class::isInstance)
					.map(GemFireCache.class::cast)
					.map(GemFireCache::getDistributedSystem)
					.filter(InternalDistributedSystem.class::isInstance)
					.map(InternalDistributedSystem.class::cast)
					.map(InternalDistributedSystem::getConfig)
					.ifPresent(distributionConfig -> {

						SecurableCommunicationChannel[] securableCommunicationChannels =
							ArrayUtils.nullSafeArray(distributionConfig.getSecurableCommunicationChannels(),
								SecurableCommunicationChannel.class);

						logger.error("SECURABLE COMMUNICATION CHANNELS {}",
							Arrays.toString(securableCommunicationChannels));

						Arrays.stream(securableCommunicationChannels).forEach(securableCommunicationChannel -> {

							SSLConfig sslConfig = SSLConfigurationFactory
								.getSSLConfigForComponent(distributionConfig, securableCommunicationChannel);

							logger.error("{} SSL CONFIGURATION [{}]", securableCommunicationChannel.name(), sslConfig);
						});
					});

				return bean;
			}
		};
	}
}
