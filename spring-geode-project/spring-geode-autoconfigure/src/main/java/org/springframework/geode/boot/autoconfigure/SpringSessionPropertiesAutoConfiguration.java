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
package org.springframework.geode.boot.autoconfigure;

import org.apache.geode.cache.GemFireCache;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.geode.boot.autoconfigure.configuration.SpringSessionProperties;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} class used to configure Spring Boot
 * {@link ConfigurationProperties} classes and beans from the Spring {@link Environment} containing Spring Session
 * configuration properties used to configure either Apache Geode to manage (HTTP) Session state.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.boot.context.properties.EnableConfigurationProperties
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.boot.autoconfigure.configuration.SpringSessionProperties
 * @see org.springframework.session.SessionRepository
 * @since 1.0.0
 */
@SpringBootConfiguration
@ConditionalOnBean({ GemFireCache.class, SessionRepository.class })
@ConditionalOnClass({ GemFireCache.class, CacheFactoryBean.class, GemFireHttpSessionConfiguration.class })
@EnableConfigurationProperties({ SpringSessionProperties.class })
@SuppressWarnings("unused")
public class SpringSessionPropertiesAutoConfiguration {

}
