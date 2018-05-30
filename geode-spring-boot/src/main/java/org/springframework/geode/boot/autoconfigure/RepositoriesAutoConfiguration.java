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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.repository.config.GemfireRepositoryConfigurationExtension;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for Spring Data Geode
 * and Spring Data GemFire Repositories.
 *
 * Activates when there is a bean of type {@link Cache} or {@link ClientCache} configured in the Spring context,
 * the Spring Data Geode {@link GemfireRepository} type is on the classpath, and no other existing
 * {@link GemfireRepository GemfireRepositories} are configured.
 *
 * Once in effect, the auto-configuration is the equivalent of enabling Geode Repositories using the
 * {@link EnableGemfireRepositories} annotation.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @see org.springframework.data.gemfire.repository.config.EnableGemfireRepositories
 * @see org.springframework.data.gemfire.repository.config.GemfireRepositoryConfigurationExtension
 * @see org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.GemFireRepositoriesAutoConfigurationRegistrar
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass(GemfireRepository.class)
@ConditionalOnMissingBean({ GemfireRepositoryConfigurationExtension.class, GemfireRepositoryFactoryBean.class })
@ConditionalOnProperty(prefix = "spring.data.gemfire.repositories", name = "enabled", havingValue = "true",
	matchIfMissing = true)
@Import(GemFireRepositoriesAutoConfigurationRegistrar.class)
public class RepositoriesAutoConfiguration {

}
