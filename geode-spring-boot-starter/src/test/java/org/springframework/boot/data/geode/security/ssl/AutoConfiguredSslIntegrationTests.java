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

package org.springframework.boot.data.geode.security.ssl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.geode.cache.EchoCacheLoader;

/**
 * Integration tests testing the auto-configuration of Apache Geode/Pivotal GemFire SSL.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSsl
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("ssl")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
	classes = AutoConfiguredSslIntegrationTests.GemFireClientConfiguration.class)
@SuppressWarnings("unused")
public class AutoConfiguredSslIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";
	private static final String TRUSTED_KEYSTORE_FILENAME = "test-trusted.keystore";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class, "-Dspring.profiles.active=ssl");
	}

	@AfterClass
	public static void clearSslSystemProperties() {

		List<String> sslSystemProperties = System.getProperties().keySet().stream()
			.map(String::valueOf)
			.map(String::toLowerCase)
			.filter(property -> property.contains("ssl"))
			.collect(Collectors.toList());

		//System.err.printf("SSL System Properties [%s]%n", sslSystemProperties);

		sslSystemProperties.forEach(System::clearProperty);
	}

	@javax.annotation.Resource(name = "Echo")
	private Region<String, String> echo;

	@Test
	public void clientServerCommunicationsSuccessful() {

		assertThat(this.echo).isNotNull();
		assertThat(this.echo.get("Hello")).isEqualTo("Hello");
		assertThat(this.echo.get("Test")).isEqualTo("Test");
		assertThat(this.echo.get("Good-Bye")).isEqualTo("Good-Bye");
	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration {

		@Bean("Echo")
		public ClientRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setClose(false);
			echoRegion.setShortcut(ClientRegionShortcut.PROXY);

			return echoRegion;
		}
	}

	@SpringBootApplication
	@CacheServerApplication(name = "AutoConfiguredSslIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration {

		public static void main(String[] args) {
			SpringApplication.run(GemFireServerConfiguration.class, args);
		}

		@Bean("Echo")
		public PartitionedRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<String, String> echoRegion = new PartitionedRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setCacheLoader(EchoCacheLoader.INSTANCE);
			echoRegion.setClose(false);
			echoRegion.setPersistent(false);

			return echoRegion;
		}
	}
}
