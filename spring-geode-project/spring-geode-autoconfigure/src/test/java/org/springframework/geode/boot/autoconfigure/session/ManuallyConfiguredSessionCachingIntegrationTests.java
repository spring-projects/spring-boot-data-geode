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
package org.springframework.geode.boot.autoconfigure.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for auto-configuration of Spring Session using a custom Spring Session {@link Session} state
 * management provider, asserting that Apache Geode is not configured.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.session.Session
 * @see org.springframework.session.SessionRepository
 * @see org.springframework.session.config.annotation.web.http.EnableSpringHttpSession
 * @see org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration
 * @see org.springframework.session.data.gemfire.GemFireOperationsSessionRepository
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SuppressWarnings("unused")
public class ManuallyConfiguredSessionCachingIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void setup() {

		assertThat(this.applicationContext).isNotNull();
		assertThat(this.applicationContext.getBean(SpringHttpSessionConfiguration.class)).isNotNull();
	}

	@Test
	public void gemfireSessionRegionAndSessionRepositoryAreNotPresent() {

		assertThat(this.applicationContext.containsBean("gemfireCache")).isTrue();
		assertThat(this.applicationContext.containsBean("sessionRepository")).isTrue();
		assertThat(this.applicationContext.containsBean(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME))
			.isFalse();

		GemFireCache gemfireCache = this.applicationContext.getBean(GemFireCache.class);

		assertThat(gemfireCache).isNotNull();
		assertThat(gemfireCache.rootRegions()).isEmpty();

		SessionRepository<?> sessionRepository = this.applicationContext.getBean(SessionRepository.class);

		assertThat(sessionRepository).isNotNull();
		assertThat(sessionRepository).isNotInstanceOf(GemFireOperationsSessionRepository.class);
		assertThat(sessionRepository.getClass().getName().toLowerCase()).doesNotContain("redis");
	}

	@SpringBootApplication
	@EnableSpringHttpSession
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean
		SessionRepository<?> sessionRepository() {
			return mock(SessionRepository.class);
		}
	}
}
