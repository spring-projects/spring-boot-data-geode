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

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Integration Tests for local HTTP Session state caching using SBDG with SSDG along with the {@link EnableClusterAware}
 * to appropriately identify and configure a local Apache Geode cache topology.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.client.RestTemplate
 * @since 1.2.2
 */
@ActiveProfiles("session-local")
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = {
		AutoConfiguredSessionLocalCachingIntegrationTests.TestConfiguration.class,
		AutoConfiguredSessionLocalCachingIntegrationTests.TestWebApplication.class
	},
	properties = {
		"spring.session.data.gemfire.session.region.name=Sessions"
	},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@SuppressWarnings("unused")
public class AutoConfiguredSessionLocalCachingIntegrationTests extends IntegrationTestsSupport {

	private static final AtomicReference<String> sessionId = new AtomicReference<>(null);

	private static final String HTTP_HEADER_AUTHENTICATION_INFO = "Authentication-Info";

	@BeforeClass @AfterClass
	public static void resetClusterAwareCondition() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@LocalServerPort
	@SuppressWarnings("unused")
	private int httpServerPort;

	@SuppressWarnings("unused")
	@Resource(name = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME)
	private Region<Object, Object> sessionsRegion;

	private String url;

	@Before
	public void assertSessionsRegionConfiguration() {

		assertThat(this.sessionsRegion).isNotNull();
		assertThat(this.sessionsRegion.getName()).isEqualTo("Sessions");
		assertThat(this.sessionsRegion.getAttributes()).isNotNull();
		assertThat(this.sessionsRegion.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.NORMAL);
		assertThat(this.sessionsRegion.getAttributes().getPoolName()).isNull();
	}

	@Before
	public void configureWebApplicationUrl() {
		this.url = String.format("http://localhost:%d/session/attribute", this.httpServerPort);
	}

	@Test
	public void setAndGetSessionAttributeIsCorrect() {

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> response =
			restTemplate.getForEntity(url.concat("/setter?name=MyKey&value=TEST"), String.class);

		assertThat(response.getBody()).isEqualTo("SUCCESS");

		//System.err.printf("HTTP RESPONSE HEADERS [%s]%n", response.getHeaders());

		String httpHeaderWithSessionId =
			StringUtils.collectionToCommaDelimitedString(response.getHeaders().get(HTTP_HEADER_AUTHENTICATION_INFO));

		assertThat(httpHeaderWithSessionId).contains(sessionId.get());
		assertThat(this.sessionsRegion.keySet()).containsExactlyInAnyOrder(sessionId.get());

		RequestEntity<Void> request = RequestEntity
			.get(URI.create(url.concat("/getter?name=MyKey")))
			.header(HTTP_HEADER_AUTHENTICATION_INFO, httpHeaderWithSessionId)
			.build();

		response = restTemplate.exchange(request, String.class);

		assertThat(response.getBody()).isEqualTo("TEST");
	}

	@SpringBootApplication
	@EnableClusterAware
	static class TestConfiguration {

		@Bean
		HttpSessionIdResolver headerHttpSessionIdResolver() {
			return HeaderHttpSessionIdResolver.authenticationInfo();
		}
	}

	@RestController
	@RequestMapping("/session")
	static class TestWebApplication {

		@GetMapping("/attribute/setter")
		public String setSessionAttribute(HttpSession session,
				@RequestParam("name") String name, @RequestParam("value") String value) {

			assertThat(session).isNotNull();
			assertThat(session.getClass().getPackage().getName()).startsWith("org.springframework.session");

			//System.out.printf("SESSION.getId() is [%s] as TYPE [%s] and SESSION.setAttribute(%s, %s)%n",
			//	session.getId(), session.getClass().getName(), name, value);

			session.setAttribute(name, value);
			sessionId.set(session.getId());

			return "SUCCESS";
		}

		@GetMapping("/attribute/getter")
		public String getSessionAttribute(HttpSession session, @RequestParam("name") String name) {

			assertThat(session).isNotNull();
			assertThat(session.getClass().getPackage().getName()).startsWith("org.springframework.session");
			assertThat(session.getId()).isEqualTo(sessionId.get());

			//System.out.printf("SESSION.getId() is [%s] as TYPE [%s] and SESSION.getAttribute(%s)%n",
			//	session.getId(), session.getClass().getName(), name);

			return String.valueOf(session.getAttribute(name));
		}
	}
}
