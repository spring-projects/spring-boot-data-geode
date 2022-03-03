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
package example.app.geode.caching.session;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.hamcrest.RegexMatcher;
import org.springframework.http.MediaType;
import org.springframework.security.AuthenticationException;
import org.springframework.session.web.servlet.http.SpringSessionSubstitutingSpyRequestPostProcessor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Smoke Tests testing the persistent access of a (HTTP) Session stored and managed by Apache Geode using Mock Objects
 * with Spring Session auto-configured with Spring Boot.
 *
 * @author John Blum
 * @see javax.servlet.http.HttpSession
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.springframework.test.web.servlet.MockMvc
 * @see org.springframework.web.bind.annotation.RestController
 * @since 1.4.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = {
		MockPersistentSessionAccessSmokeTests.TestSessionGeodeConfiguration.class,
		MockPersistentSessionAccessSmokeTests.TestUserAccessController.class
	},
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class MockPersistentSessionAccessSmokeTests extends IntegrationTestsSupport {

	@Autowired
	private MockMvc mvc;

	@Test
	public void persistentSessionAccessIsSuccessful() throws Exception {

		String username = "jonDoe";

		this.mvc.perform(get("/users/{username}", username)
			.param("password", "p@5$w0rd")
			.with(SpringSessionSubstitutingSpyRequestPostProcessor.create()))
			.andExpect(status().isOk())
			.andExpect(request().sessionAttribute(username, User.newUser(username)))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(content().string(RegexMatcher.from("\\{\"id\":\".*\",\"name\":\"jonDoe\"\\}")));
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestSessionGeodeConfiguration { }

	@RestController
	static class TestUserAccessController {

		static final Map<String, String> userAccessControlMap = MapBuilder.<String, String>newMapBuilder()
			.put("jonDoe", "p@5$w0rd")
			.put("janeDoe", "s3cr3t!")
			.build();

		@GetMapping("/users/{username}")
		public User login(HttpSession session, @PathVariable String username,
				@RequestParam(required = false) String password) {

			return Optional.ofNullable(password)
				.filter(StringUtils::hasText)
				.filter(it -> String.valueOf(userAccessControlMap.get(username)).equals(password))
				.map(it -> User.newUser(username).identifiedBy(UUID.randomUUID().toString()))
				.map(user -> {
					session.setAttribute(user.getName(), user);
					return user;
				})
				.orElseThrow(() -> new AuthenticationException(String.format("User [%s] is not authorized", username)));
		}
	}

	@Getter
	@ToString(of = "name")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "newUser")
	static class User {

		private String id;

		@NonNull
		private final String name;

		public User identifiedBy(String id) {
			this.id = id;
			return this;
		}
	}
}
