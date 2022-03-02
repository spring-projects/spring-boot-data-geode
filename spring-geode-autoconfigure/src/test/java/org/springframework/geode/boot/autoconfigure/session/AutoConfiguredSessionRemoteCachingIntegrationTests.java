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

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
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
 * Integration Tests for Spring Session, Remote Caching (e.g. client/server).
 *
 * @author John Blum
 * @see java.net.URI
 * @see javax.servlet.http.HttpSession
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.boot.web.server.LocalServerPort
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.SpringSessionAutoConfiguration
 * @see org.springframework.http.RequestEntity
 * @see org.springframework.http.ResponseEntity
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession
 * @see org.springframework.session.web.http.HeaderHttpSessionIdResolver
 * @see org.springframework.session.web.http.HttpSessionIdResolver
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.springframework.web.bind.annotation.RestController
 * @see org.springframework.web.client.RestTemplate
 * @since 1.0.0
 */
@ActiveProfiles("session-remote")
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes =  {
		AutoConfiguredSessionRemoteCachingIntegrationTests.SessionGemFireClientConfiguration.class,
		AutoConfiguredSessionRemoteCachingIntegrationTests.TestWebApplication.class
	},
	properties = "spring.session.data.gemfire.cache.client.region.shortcut=PROXY",
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@SuppressWarnings("unused")
public class AutoConfiguredSessionRemoteCachingIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final AtomicReference<String> sessionId = new AtomicReference<>(null);

	private static final String HTTP_HEADER_AUTHENTICATION_INFO = "Authentication-Info";

	@BeforeClass
	public static void setupGemFireServer() throws IOException {
		startGemFireServer(SessionGemFireServerConfiguration.class);
	}

	@LocalServerPort
	private int httpServerPort;

	@Autowired
	private ClientCache clientCache;

	@Resource(name = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME)
	private Region<Object, Object> sessionsRegion;

	private String url;

	@Before
	public void assertClientCachePoolSubscriptionEnabled() {

		assertThat(this.clientCache).isNotNull();
		assertThat(this.clientCache.getDefaultPool()).isNotNull();
		assertThat(this.clientCache.getDefaultPool().getSubscriptionEnabled()).isTrue();
	}

	@Before
	public void assertSessionsRegionConfiguration() {

		assertThat(this.sessionsRegion).isNotNull();
		assertThat(this.sessionsRegion.getName())
			.isEqualTo(GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME);
		assertThat(this.sessionsRegion.getAttributes()).isNotNull();
		assertThat(this.sessionsRegion.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
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
		assertThat(this.sessionsRegion.keySetOnServer()).containsExactlyInAnyOrder(sessionId.get());

		RequestEntity<Void> request = RequestEntity
			.get(URI.create(url.concat("/getter?name=MyKey")))
			.header(HTTP_HEADER_AUTHENTICATION_INFO, httpHeaderWithSessionId)
			.build();

		response = restTemplate.exchange(request, String.class);

		assertThat(response.getBody()).isEqualTo("TEST");
	}

	@SpringBootApplication
	static class SessionGemFireClientConfiguration {

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

	@CacheServerApplication(name = "AutoConfiguredSessionRemoteCachingIntegrationTests")
	@EnableGemFireHttpSession
	static class SessionGemFireServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(SessionGemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
