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

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ApacheShiroSecurityConfiguration;
import org.springframework.data.gemfire.config.annotation.GeodeIntegratedSecurityConfiguration;
import org.springframework.geode.config.annotation.EnableSecurityManager;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's Security functionality,
 * and specifically Authentication between a client and server using Spring Data Geode Security annotations.
 *
 * @author John Blum
 * @see org.apache.geode.security.SecurityManager
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ApacheShiroSecurityConfiguration
 * @see org.springframework.data.gemfire.config.annotation.GeodeIntegratedSecurityConfiguration
 * @see org.springframework.geode.config.annotation.EnableSecurityManager
 * @since 1.0.0
 */
@SpringBootConfiguration
@ConditionalOnBean(org.apache.geode.security.SecurityManager.class)
@ConditionalOnMissingBean({
	ClientCacheFactoryBean.class,
	ApacheShiroSecurityConfiguration.class,
	GeodeIntegratedSecurityConfiguration.class
})
@EnableSecurityManager
//@Import(HttpBasicAuthenticationSecurityConfiguration.class)
@SuppressWarnings("unused")
public class PeerSecurityAutoConfiguration {

}
