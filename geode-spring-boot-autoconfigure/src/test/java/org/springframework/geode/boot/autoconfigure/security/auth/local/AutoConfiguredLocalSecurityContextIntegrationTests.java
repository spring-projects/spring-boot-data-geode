/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.security.auth.local;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.support.GemfireBeanFactoryLocatorProxy;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration test testing the auto-configuration of Apache Geode/Pivotal GemFire Security
 * authentication/authorization in a local, non-managed context.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.PeerSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("security-local-client")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AutoConfiguredLocalSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredLocalSecurityContextIntegrationTests
		extends AbstractAutoConfiguredSecurityContextIntegrationTests {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		GemfireBeanFactoryLocatorProxy.clear();

		startGemFireServer(GemFireServerConfiguration.class,
			"-Dspring.profiles.active=security-local-server");
	}

	@AfterClass
	public static void cleanUpBeanFactoryLocatorReferences() {
		GemfireBeanFactoryLocatorProxy.clear();
	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	@Import(ClientServerIntegrationTestsConfiguration.class)
	static class GemFireClientConfiguration extends BaseGemFireClientConfiguration { }

	@SpringBootApplication
	@CacheServerApplication(name = "AutoConfiguredLocalSecurityContextIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration extends BaseGemFireServerConfiguration {

		public static void main(String[] args) {
			SpringApplication.run(GemFireServerConfiguration.class, args);
		}
	}
}
