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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.events.AbstractSessionEvent;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests to assert the property configuration of the Session state management provider
 * with respect to Session expiration timeout.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.session.Session
 * @see org.springframework.session.SessionRepository
 * @see org.springframework.session.data.gemfire.GemFireOperationsSessionRepository
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.session.events.AbstractSessionEvent
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = SessionExpirationIntegrationTests.TestConfiguration.class,
	properties = {
		"spring.session.data.gemfire.cache.client.region.shortcut=LOCAL",
		"spring.session.timeout=1s",
	},
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@SuppressWarnings("unused")
public class SessionExpirationIntegrationTests extends IntegrationTestsSupport {

	@Resource(name = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME)
	private Region<Object, Session> sessionRegion;

	@Autowired
	private SessionEventListener sessionEventListener;

	@Autowired
	private SessionRepository<Session> sessionRepository;

	@Before
	public void setup() {

		assertThat(this.sessionEventListener).isNotNull();
		assertThat(this.sessionRegion).isNotNull();
		assertThat(this.sessionRegion).isEmpty();
		assertThat(this.sessionRegion.getName()).isEqualTo(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME);
		assertThat(this.sessionRepository).isInstanceOf(GemFireOperationsSessionRepository.class);
	}

	@Test
	public void sessionExpirationIsCorrect() {

		Session session = this.sessionRepository.createSession();

		assertThat(session).isNotNull();
		assertThat(session.getId()).isNotBlank();
		assertThat(session.isExpired()).isFalse();
		assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(1));

		this.sessionRepository.save(session);

		Session savedSession = this.sessionRepository.findById(session.getId());

		assertThat(savedSession).isEqualTo(session);

		// Clear any existing Session event
		this.sessionEventListener.getSessionEvent();

		AbstractSessionEvent sessionEvent =
			this.sessionEventListener.waitForSessionEvent(TimeUnit.SECONDS.toMillis(2));

		assertThat(sessionEvent).isNotNull();
		assertThat(sessionEvent.getSessionId()).isEqualTo(savedSession.getId());
		assertThat(this.sessionRepository.findById(savedSession.getId())).isNull();
	}

	static class SessionEventListener implements ApplicationListener<AbstractSessionEvent> {

		private volatile AbstractSessionEvent sessionEvent;

		@SuppressWarnings("unchecked")
		public <T extends AbstractSessionEvent> T getSessionEvent() {

			T sessionEvent = (T) this.sessionEvent;

			this.sessionEvent = null;

			return sessionEvent;
		}

		public void onApplicationEvent(AbstractSessionEvent event) {
			this.sessionEvent = event;
		}

		public <T extends AbstractSessionEvent> T waitForSessionEvent(long duration) {

			waitOn(() -> SessionEventListener.this.sessionEvent != null, duration);

			return getSessionEvent();
		}
	}

	@SpringBootApplication
	static class TestConfiguration {

		@Bean
		SessionEventListener sessionEventListener() {
			return new SessionEventListener();
		}
	}
}
