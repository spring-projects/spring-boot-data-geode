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
package org.springframework.geode.boot.autoconfigure.security.auth.hybrid;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ClientCache;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing the functionality and behavior of {@link ClientSecurityAutoConfiguration} when a
 * Spring Boot app is deployed (pushed) to CloudFoundry, however, the app has not be bound to a Pivotal Cloud Cache
 * (PCC) service instance.
 *
 * This Use Case is common when users want to deploy their Spring Boot, Apache Geode {@link ClientCache} apps to
 * CloudFoundry however, want to connect those apps to an external Apache Geode cluster.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.PeerSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AutoConfiguredHybridSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	properties = {
		"spring.data.gemfire.pool.locators=localhost[${test.security.hybrid.gemfire.pool.locators.port:54441}]",
		"spring.data.gemfire.security.username=phantom",
		"spring.data.gemfire.security.password=s3cr3t"
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class AutoConfiguredHybridSecurityContextIntegrationTests
		extends AbstractAutoConfiguredSecurityContextIntegrationTests {

	private static final String LOCATOR_PORT_PLACEHOLDER_REGEX = "%LOCATOR_PORT%";
	private static final String VCAP_APPLICATION_PROPERTIES = "application-vcap-hybrid.properties";

	private static final Properties vcapApplicationProperties = new Properties();

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		int locatorPort = findAvailablePort();

		startGemFireServer(GemFireServerConfiguration.class,
			String.format("-Dspring.data.gemfire.locator.port=%d", locatorPort),
			"-Dspring.profiles.active=security-hybrid");

		loadVcapApplicationProperties(locatorPort);

		setTestSecuritySystemProperties(locatorPort);

		unsetTestAutoConfiguredPoolServersPortSystemProperty();
	}

	private static void loadVcapApplicationProperties(int locatorPort) throws IOException {

		vcapApplicationProperties.load(new ClassPathResource(VCAP_APPLICATION_PROPERTIES).getInputStream());

		vcapApplicationProperties.stringPropertyNames().forEach(propertyName -> {

			String propertyValue = String.valueOf(vcapApplicationProperties.getProperty(propertyName))
				.replaceAll(LOCATOR_PORT_PLACEHOLDER_REGEX, String.valueOf(locatorPort));

			System.setProperty(propertyName, propertyValue);
		});
	}

	private static void setTestSecuritySystemProperties(int locatorPort) {
		System.setProperty("test.security.hybrid.gemfire.pool.locators.port", String.valueOf(locatorPort));
	}

	private static void unsetTestAutoConfiguredPoolServersPortSystemProperty() {
		System.clearProperty(GEMFIRE_POOL_SERVERS_PROPERTY);
	}

	@AfterClass
	public static void cleanUpUsedResources() {

		vcapApplicationProperties.stringPropertyNames().forEach(System::clearProperty);
		System.clearProperty("test.security.hybrid.gemfire.pool.locators.port");
	}

	@SpringBootApplication
	static class GemFireClientConfiguration extends BaseGemFireClientConfiguration { }

	@SpringBootApplication
	@EnableLocator
	@CacheServerApplication(name = "AutoConfiguredHybridSecurityContextIntegrationTestsServer")
	static class GemFireServerConfiguration extends BaseGemFireServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(GemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
