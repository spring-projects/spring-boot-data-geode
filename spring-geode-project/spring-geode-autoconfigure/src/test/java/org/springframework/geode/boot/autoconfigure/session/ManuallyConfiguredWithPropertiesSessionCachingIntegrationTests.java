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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests asserting the configuration of Spring Session using Spring Boot's
 * {@literal spring.session.store-type} configuration property set to {@literal none}
 * and assert that Apache Geode was not configured as the Session state management provider.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.session.SessionRepository
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.session.store-type=none", webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SuppressWarnings("unused")
public class ManuallyConfiguredWithPropertiesSessionCachingIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Before
	public void setup() {
		assertThat(this.applicationContext).isNotNull();
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void gemfireSessionRegionAndSessionRepositoryAreNotPresent() {

		String sessionsRegionName = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME;

		assertThat(this.applicationContext.containsBean("gemfireCache")).isTrue();
		assertThat(this.applicationContext.containsBean("sessionRepository")).isFalse();
		assertThat(this.applicationContext.containsBean(sessionsRegionName)).isFalse();
		assertThat(this.applicationContext.getBeansOfType(SessionRepository.class)).isEmpty();

		GemFireCache gemfireCache = this.applicationContext.getBean(GemFireCache.class);

		assertThat(gemfireCache).isNotNull();
		assertThat(gemfireCache.rootRegions()).isEmpty();

		this.applicationContext.getBean(SessionRepository.class);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration { }

}
