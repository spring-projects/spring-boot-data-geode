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
package example.app.caching.multisite;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.InterestResultPolicy;
import org.apache.geode.cache.util.CacheListenerAdapter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.data.gemfire.client.RegexInterest;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;

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
	private static final String TEST_CUSTOMER_NAME = "Jon Doe";

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
		return newApplicationContext(locatorPort, Collections.emptySet(), springProfiles);
	}

	private ConfigurableApplicationContext newApplicationContext(int locatorPort, Set<Class<?>> sources,
			String... springProfiles) {

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
				.sources(CollectionUtils.nullSafeSet(sources).toArray(new Class[0]))
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
			assertThat(applicationContext.isRunning()).isTrue();

			CustomerService customerService = applicationContext.getBean(CustomerService.class);

			customerService.setSleepInSeconds(2L);

			Customer jonDoe = customerService.findBy("Jon Doe");

			assertThat(jonDoe).isNotNull();
			assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
			assertThat(customerService.isCacheMiss()).isTrue();
			assertThat(customerService.findBy(jonDoe.getName())).isEqualTo(jonDoe);
			assertThat(customerService.isCacheMiss()).isFalse();

			close(applicationContext);

			applicationContext = newApplicationContext(locatorPortClusterTwo,
				Collections.singleton(TestGeodeClientConfiguration.class), "client-site2");

			assertThat(applicationContext).isNotNull();
			assertThat(applicationContext.isActive()).isTrue();
			assertThat(applicationContext.isRunning()).isTrue();

			CustomersByNameCacheListener customersByNameCacheListener =
				applicationContext.getBean("customersByNameCacheListener", CustomersByNameCacheListener.class);

			assertThat(customersByNameCacheListener).isNotNull();

			ThreadUtils.waitFor(Duration.ofSeconds(10), 500L, customersByNameCacheListener::isEntryEventArrived);

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

	@SuppressWarnings("unused")
	static class CustomersByNameCacheListener extends CacheListenerAdapter<String, Customer> {

		private boolean entryEventArrived = false;

		@Override
		public synchronized void afterCreate(EntryEvent<String, Customer> event) {

			this.entryEventArrived |= Optional.ofNullable(event)
				//.map(this::log)
				.map(EntryEvent::getKey)
				.filter(TEST_CUSTOMER_NAME::equals)
				.isPresent();
		}

		synchronized boolean isEntryEventArrived() {
			return this.entryEventArrived;
		}

		@NonNull EntryEvent<String, Customer> log(@NonNull EntryEvent<String, Customer> entryEvent) {

			System.err.printf("EntryEvent with key [%s] and value [%s] arrived for Region [%s]%n",
				entryEvent.getKey(), entryEvent.getNewValue(), entryEvent.getRegion().getName());

			return entryEvent;
		}
	}

	@Configuration
	@SuppressWarnings("unused")
	static class TestGeodeClientConfiguration {

		@Bean
		ClientCacheConfigurer clientCacheSubscriptionsEnabledConfigurer() {
			return (beanName, bean) -> bean.setSubscriptionEnabled(true);
		}

		@Bean
		@SuppressWarnings({ "rawtypes", "unchecked" })
		RegionConfigurer customersByNameCacheListenerConfigurer(@Qualifier("customersByNameCacheListener")
				CacheListener<String, Customer> customersByNameCacheListener) {

			return new RegionConfigurer() {

				@Override
				public void configure(String beanName, ClientRegionFactoryBean<?, ?> bean) {

					if ("CustomersByName".equals(beanName)) {
						bean.setCacheListeners(ArrayUtils.<CacheListener>asArray(customersByNameCacheListener));
						bean.setInterests(ArrayUtils.<Interest>asArray(new RegexInterest(".*",
							InterestResultPolicy.KEYS, false, false)));
					}
				}
			};
		}

		@Bean
		CacheListener<String, Customer> customersByNameCacheListener() {
			return new CustomersByNameCacheListener();
		}
	}
}
