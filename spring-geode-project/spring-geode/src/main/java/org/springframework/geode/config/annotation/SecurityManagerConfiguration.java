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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;

/**
 * Spring {@link Configuration} class used to configure a {@link org.apache.geode.security.SecurityManager},
 * thereby enabling Security (Auth) on this Apache Geode node.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @since 1.1.0
 */
@Configuration
@SuppressWarnings("unused")
public class SecurityManagerConfiguration {

	@Bean
	ClientCacheConfigurer clientSecurityManagerConfigurer(org.apache.geode.security.SecurityManager securityManager) {
		return (beanName, clientCacheFactoryBean) -> clientCacheFactoryBean.setSecurityManager(securityManager);
	}

	@Bean
	PeerCacheConfigurer peerSecurityManagerConfigurer(org.apache.geode.security.SecurityManager securityManager) {
		return (beanName, cacheFactoryBean) -> cacheFactoryBean.setSecurityManager(securityManager);
	}
}
