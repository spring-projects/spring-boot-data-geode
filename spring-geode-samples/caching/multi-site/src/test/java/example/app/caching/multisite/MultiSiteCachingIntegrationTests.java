/*
 * Copyright 2020 the original author or authors.
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
package example.app.caching.multisite;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;

import example.app.caching.multisite.client.BootGeodeMultiSiteCachingClientApplication;
import example.app.caching.multisite.client.model.Customer;
import example.app.caching.multisite.client.service.CustomerService;
import example.app.caching.multisite.client.util.ThreadUtils;
import example.app.caching.multisite.server.BootGeodeMultiSiteCachingServerApplication;

/**
 * Integration Tests testing the Multi-Site (WAN) Caching Example.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see java.util.Properties
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @see example.app.caching.multisite.client.BootGeodeMultiSiteCachingClientApplication
 * @see example.app.caching.multisite.client.model.Customer
 * @see example.app.caching.multisite.client.service.CustomerService
 * @see example.app.caching.multisite.server.BootGeodeMultiSiteCachingServerApplication
 * @since 1.3.0
 */
public class MultiSiteCachingIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static int locatorPortClusterOne;
	private static int locatorPortClusterTwo;

	private static ProcessWrapper geodeClusterOne;
	private static ProcessWrapper geodeClusterTwo;

	private static final String HOSTNAME = "localhost";

	@BeforeClass
	public static void startGeodeClusters() throws IOException {

		locatorPortClusterOne = findAndReserveAvailablePort();
		locatorPortClusterTwo = findAndReserveAvailablePort();

		geodeClusterOne = run(BootGeodeMultiSiteCachingServerApplication.class,
			"-Dspring.profiles.active=server-site1",
			String.format("-Dspring.data.gemfire.locator.port=%d", locatorPortClusterOne),
			"-Dspring.data.gemfire.manager.start=false",
			String.format("-Dgemfire.remote-locators=%s[%d]", HOSTNAME, locatorPortClusterTwo));

		waitForServerToStart("localhost", locatorPortClusterOne);

		geodeClusterTwo = run(BootGeodeMultiSiteCachingServerApplication.class,
			"-Dspring.profiles.active=server-site2",
			String.format("-Dspring.data.gemfire.locator.port=%d", locatorPortClusterTwo),
			"-Dspring.data.gemfire.manager.start=false",
			String.format("-Dgemfire.remote-locators=%s[%d]", HOSTNAME, locatorPortClusterOne));

		waitForServerToStart("localhost", locatorPortClusterTwo);
	}

	@AfterClass
	public static void stopGeodeClusters() {
		stop(geodeClusterOne);
		stop(geodeClusterTwo);
	}

	private ConfigurableApplicationContext newApplicationContext(int locatorPort, String... springProfiles) {

		Properties configuration = new Properties();

		configuration.setProperty("spring.data.gemfire.pool.locators",
			String.format("%s[%d]", HOSTNAME, locatorPort));

		System.setProperty("spring.data.gemfire.pool.locators",
			configuration.getProperty("spring.data.gemfire.pool.locators"));

		SpringApplication springApplication =
			new SpringApplicationBuilder(BootGeodeMultiSiteCachingClientApplication.class)
				.headless(true)
				.profiles(springProfiles)
				//.properties(configuration)
				.registerShutdownHook(true)
				.build();

		return springApplication.run();
	}

	private void close(ConfigurableApplicationContext applicationContext) {

		Optional.ofNullable(applicationContext)
			.ifPresent(it -> {
				it.close();
				ThreadUtils.waitFor(Duration.ofSeconds(5), () -> !(it.isActive() || it.isRunning()));
			});
	}

	@Test
	public void clientOfClusterTwoResultsInCacheHit() {

		ConfigurableApplicationContext applicationContext = null;

		try {

			applicationContext = newApplicationContext(locatorPortClusterOne, "client-site1");

			assertThat(applicationContext).isNotNull();
			assertThat(applicationContext.isActive()).isTrue();

			CustomerService customerService = applicationContext.getBean(CustomerService.class);

			customerService.setSleepInSeconds(2L);

			Customer jonDoe = customerService.findBy("Jon Doe");

			assertThat(jonDoe).isNotNull();
			assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
			assertThat(customerService.isCacheMiss()).isTrue();
			assertThat(customerService.findBy(jonDoe.getName())).isEqualTo(jonDoe);
			assertThat(customerService.isCacheMiss()).isFalse();

			close(applicationContext);

			applicationContext = newApplicationContext(locatorPortClusterTwo, "client-site2");

			assertThat(applicationContext).isNotNull();
			assertThat(applicationContext.isActive()).isTrue();

			customerService = applicationContext.getBean(CustomerService.class);
			customerService.setSleepInSeconds(2L);

			Customer cachedJonDoe = customerService.findBy(jonDoe.getName());

			assertThat(cachedJonDoe).isNotNull();
			assertThat(cachedJonDoe).isNotSameAs(jonDoe);
			assertThat(cachedJonDoe).isEqualTo(jonDoe);
			assertThat(customerService.isCacheMiss()).isFalse();
		}
		finally {
			close(applicationContext);
		}
	}
}
