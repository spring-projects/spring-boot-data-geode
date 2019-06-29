/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.geode.boot.autoconfigure.security.auth.multi;

import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing the functionality and behavior of {@link ClientSecurityAutoConfiguration} when multiple
 * Pivotal Cloud Cache (PCC) services instances exists in a Pivotal CloudFoundry context.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableLogging
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.security.auth.AbstractAutoConfiguredSecurityContextIntegrationTests
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AutoConfiguredMultiCloudCacheServiceInstanceSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	properties = { "spring.boot.data.gemfire.cloud.cloudfoundry.service.cloudcache.name=test-pcc" },
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class AutoConfiguredMultiCloudCacheServiceInstanceSecurityContextIntegrationTests
		extends AbstractAutoConfiguredSecurityContextIntegrationTests {

	private static final String GEMFIRE_LOG_LEVEL = "error";
	private static final String VCAP_APPLICATION_PROPERTIES = "application-vcap-multi.properties";

	private static Properties vcapApplicationProperties = new Properties();

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		startGemFireServer(GemFireServerConfiguration.class, "-Dspring.profiles.active=security-multi");
		loadVcapApplicationProperties();
		unsetTestAutoConfiguredPoolServersPortSystemProperty();
	}

	public static void loadVcapApplicationProperties() throws IOException {

		vcapApplicationProperties.load(new ClassPathResource(VCAP_APPLICATION_PROPERTIES).getInputStream());

		vcapApplicationProperties.stringPropertyNames().forEach(property ->
			System.setProperty(property, vcapApplicationProperties.getProperty(property)));
	}

	public static void unsetTestAutoConfiguredPoolServersPortSystemProperty() {
		System.clearProperty(GEMFIRE_POOL_SERVERS_PROPERTY);
	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireClientConfiguration extends BaseGemFireClientConfiguration { }

	@SpringBootApplication
	@EnableLocator(port = 56421)
	@CacheServerApplication(name = "AutoConfiguredMultiCloudCacheServiceInstanceSecurityContextIntegrationTests",
		logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration extends BaseGemFireServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(GemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
