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
package org.springframework.geode.boot.autoconfigure.security.ssl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.echo.config.EchoClientConfiguration;
import example.echo.config.EchoServerConfiguration;

/**
 * Integration Tests testing the auto-configuration of Apache Geode SSL.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSsl
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("ssl")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AutoConfiguredSslIntegrationTests.GemFireClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class AutoConfiguredSslIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String TRUSTED_KEYSTORE_FILENAME = "test-trusted.keystore";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class, "-Dspring.profiles.active=ssl");
	}

	@AfterClass
	public static void clearSpringProfilesActiveSystemProperty() {
		System.clearProperty("spring.profiles.active");
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

	@AfterClass
	public static void deleteKeyStoreFilesInCurrentWorkingDirectory() {

		Predicate<File> keystorePredicate = file -> file.getAbsolutePath().endsWith(".jks");

		keystorePredicate.or(file -> file.getAbsolutePath().endsWith(".keystore"));
		keystorePredicate.or(file -> file.getAbsolutePath().endsWith(".truststore"));

		File currentWorkingDirectory = new File(System.getProperty("user.dir"));

		if (currentWorkingDirectory.isDirectory()) {

			Arrays.stream(nullSafeArray(currentWorkingDirectory.listFiles(keystorePredicate::test), File.class))
				.filter(Objects::nonNull)
				.filter(File::isFile)
				.forEach(File::delete);
		}
	}

	@Autowired(required = false)
	private Environment environment;

	@Autowired
	private GemfireTemplate echoTemplate;

	@After
	public void removeGemFireSslPropertySourceFromEnvironment() {

		Optional.ofNullable(this.environment)
			.filter(ConfigurableEnvironment.class::isInstance)
			.map(ConfigurableEnvironment.class::cast)
			.map(ConfigurableEnvironment::getPropertySources)
			.ifPresent(propertySources -> propertySources.remove("gemfire-ssl"));
	}

	@Test
	public void clientServerCommunicationsSuccessful() {

		assertThat(this.echoTemplate).isNotNull();
		assertThat(this.echoTemplate.<String, String>get("Hello")).isEqualTo("Hello");
		assertThat(this.echoTemplate.<String, String>get("Test")).isEqualTo("Test");
		assertThat(this.echoTemplate.<String, String>get("Good-Bye")).isEqualTo("Good-Bye");
	}

	@SpringBootApplication
	@Import(EchoClientConfiguration.class)
	static class GemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration { }

	@SpringBootApplication
	@CacheServerApplication(name = "AutoConfiguredSslIntegrationTestsServer")
	@Import(EchoServerConfiguration.class)
	static class GemFireServerConfiguration {

		public static void main(String[] args) {
			new SpringApplicationBuilder(GemFireServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}
