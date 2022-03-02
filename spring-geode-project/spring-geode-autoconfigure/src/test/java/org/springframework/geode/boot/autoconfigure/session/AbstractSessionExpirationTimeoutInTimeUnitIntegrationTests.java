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

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * Abstract base test class for {@link Session} {@literal} in the configured {@literal time unit},
 * for example: seconds, minutes, etc.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.session.Session
 * @see org.springframework.session.SessionRepository
 * @see org.springframework.session.data.gemfire.GemFireOperationsSessionRepository
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSessionExpirationTimeoutInTimeUnitIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private GemFireHttpSessionConfiguration configuration;

	@Autowired
	private SessionRepository<Session> repository;

	protected int getExpectedMaxInactiveIntervalInSeconds() {
		return GemFireHttpSessionConfiguration.DEFAULT_MAX_INACTIVE_INTERVAL_IN_SECONDS;
	}

	@Test
	public void sessionTimeoutConfigurationIsCorrect() {

		int expectedMaxInactiveIntervalInSeconds = getExpectedMaxInactiveIntervalInSeconds();

		assertThat(this.configuration).isNotNull();
		assertThat(this.configuration.getMaxInactiveIntervalInSeconds()).isEqualTo(expectedMaxInactiveIntervalInSeconds);
		assertThat(this.repository).isInstanceOf(GemFireOperationsSessionRepository.class);

		Session session = this.repository.createSession();

		assertThat(session).isNotNull();
		assertThat(session.getMaxInactiveInterval().getSeconds()).isEqualTo(expectedMaxInactiveIntervalInSeconds);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration { }

}
