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
package org.springframework.geode.security.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.geode.cache.GemFireCache;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.config.annotation.EnableSecurityManagerProxy;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link SecurityManagerProxy}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.security.SecurityManager
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings("unused")
public class SecurityManagerProxyIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@BeforeClass
	@AfterClass
	public static void cleanupSecurityManagerProxyInstance() {

		ObjectUtils.doOperationSafely(() -> {
			SecurityManagerProxy.getInstance().destroy();
			return true;
		}, true);
	}

	@Autowired
	private org.apache.geode.security.SecurityManager mockSecurityManager;

	@Test
	public void securityManagerProxyWasConfiguredWithMockSecurityManager() {
		assertThat(SecurityManagerProxy.getInstance().getSecurityManager()).isEqualTo(this.mockSecurityManager);
	}

	@EnableGemFireMockObjects
	@EnableSecurityManagerProxy
	@PeerCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
	static class TestConfiguration {

		@Bean
		org.apache.geode.security.SecurityManager mockSecurityManager(GemFireCache gemfireCache) {
			return mock(org.apache.geode.security.SecurityManager.class);
		}
	}
}
