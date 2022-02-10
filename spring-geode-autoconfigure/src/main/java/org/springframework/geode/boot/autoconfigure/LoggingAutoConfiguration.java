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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableLogging;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-Configuration} for Apache Geode logging.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnableLogging
 * @since 1.1.0
 */
@SpringBootConfiguration
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass(CacheFactoryBean.class)
@ConditionalOnMissingBean(name = {
	"org.springframework.data.gemfire.config.annotation.LoggingConfiguration.ClientGemFirePropertiesConfigurer",
	"org.springframework.data.gemfire.config.annotation.LoggingConfiguration.LocatorGemFirePropertiesConfigurer",
	"org.springframework.data.gemfire.config.annotation.LoggingConfiguration.PeerGemFirePropertiesConfigurer",
})
@EnableLogging
@SuppressWarnings("unused")
// TODO Find a more reliable way to refer to the LoggingConfiguration Configurer beans defined above other than by name!
public class LoggingAutoConfiguration {

}
