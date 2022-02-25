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
package org.springframework.geode.boot.autoconfigure.security.auth.local;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing the auto-configuration of Apache Geode Security (authentication/authorization) in a local,
 * non-managed context.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.PeerSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("security-local-client")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AutoConfiguredLocalSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class AutoConfiguredLocalSecurityContextIntegrationTests
		extends AbstractAutoConfiguredSecurityContextIntegrationTests {

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class,
			"-Dspring.profiles.active=security-local-server");
	}

	@SpringBootApplication
	static class GemFireClientConfiguration extends BaseGemFireClientConfiguration { }

	@SpringBootApplication
	@CacheServerApplication(name = "AutoConfiguredLocalSecurityContextIntegrationTestsServer")
	static class GemFireServerConfiguration extends BaseGemFireServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(GemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
