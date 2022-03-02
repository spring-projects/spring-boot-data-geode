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

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Tests for Spring Boot for Apache Geode auto-configuration of Spring Session using Apache Geode
 * as the provider with Mock Objects.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.boot.autoconfigure.SpringSessionAutoConfiguration
 * @since 1.4.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SuppressWarnings("unused")
public class AutoConfiguredSessionCachingUsingRepositoryWithMocksIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private SessionRepository<Session> sessionRepository;

	@Before
	public void setup() {
		assertThat(this.sessionRepository).isNotNull();
	}

	@Test
	public void persistentSessionAccessIsSuccessful() {

		Instant beforeCreationTime = Instant.now();

		Session session = this.sessionRepository.createSession();

		User jonDoe = User.newUser("jonDoe").identifiedBy(2L);

		assertThat(session).isNotNull();
		assertThat(session.getId()).isNotBlank();
		assertThat(session.getCreationTime()).isAfterOrEqualTo(beforeCreationTime);
		assertThat(session.isExpired()).isFalse();

		session.setAttribute(jonDoe.getName(), jonDoe);

		assertThat(session.<User>getAttribute(jonDoe.getName())).isEqualTo(jonDoe);

		this.sessionRepository.save(session);

		Instant beforeLastAccessTime = Instant.now();

		Session loadedSession = this.sessionRepository.findById(session.getId());

		assertThat(loadedSession).isNotNull();
		assertThat(loadedSession.getId()).isEqualTo(session.getId());
		assertThat(loadedSession.getCreationTime()).isEqualTo(session.getCreationTime());
		assertThat(loadedSession.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessTime);
		assertThat(loadedSession.isExpired()).isFalse();
		assertThat(loadedSession.<User>getAttribute(jonDoe.getName())).isEqualTo(jonDoe);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration { }

	@Getter
	@ToString(of = "name")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "newUser")
	static class User {

		private Long id;

		@NonNull
		private final String name;

		public User identifiedBy(Long id) {
			this.id = id;
			return this;
		}
	}
}
