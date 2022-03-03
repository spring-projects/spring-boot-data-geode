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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.security.support.SecurityManagerProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link EnableSecurityManager} and {@link SecurityManagerConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.security.SecurityManager
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.security.support.SecurityManagerProxy
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class SecurityManagerConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	public CacheFactoryBean cacheFactoryBean;

	@Autowired @SuppressWarnings("unused")
	private org.apache.geode.security.SecurityManager securityManager;

	@Test
	public void securityManagerBeanIsConfigured() {
		assertThat(this.cacheFactoryBean.getSecurityManager()).isEqualTo(this.securityManager);
	}

	@Test(expected = IllegalStateException.class)
	public void securityManagerProxyIsNotUsed() {

		try {
			SecurityManagerProxy.getInstance();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("SecurityManagerProxy was not configured");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@ClientCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
	@EnableGemFireMockObjects
	@EnableSecurityManager
	static class TestConfiguration {

		@Bean @SuppressWarnings("unused")
		org.apache.geode.security.SecurityManager mockSecurityManager() {
			return mock(org.apache.geode.security.SecurityManager.class);
		}
	}
}
