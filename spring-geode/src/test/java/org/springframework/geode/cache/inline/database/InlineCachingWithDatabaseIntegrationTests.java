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
package org.springframework.geode.cache.inline.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.geode.cache.InlineCachingRegionConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.crm.model.Customer;
import example.app.crm.repo.CustomerRepository;

/**
 * Spring Boot Integration Tests testing the Inline Caching support using a RDBMS (HSQLDB Database).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 * @see org.springframework.geode.cache.InlineCachingRegionConfigurer
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("inline-caching-database")
@SuppressWarnings("unused")
public class InlineCachingWithDatabaseIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "off";

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private GemfireTemplate customersTemplate;

	@Before
	public void setup() {

		assertThat(this.customerRepository).isNotNull();
		assertThat(this.customerRepository.count()).isEqualTo(1);
		assertThat(this.customerRepository.existsById(16L)).isTrue();

		Customer pieDoe = this.customerRepository.findByName("Pie Doe");

		assertThat(pieDoe).isNotNull();
		assertThat(pieDoe.getId()).isEqualTo(16L);
		assertThat(pieDoe.getName()).isEqualTo("Pie Doe");
	}

	@Test
	public void cacheLoadsFromDatabase() {

		assertThat(this.customersTemplate.containsKey(16L)).isFalse();

		Customer pieDoe = this.customersTemplate.get(16L);

		assertThat(pieDoe).isNotNull();
		assertThat(pieDoe.getId()).isEqualTo(16L);
		assertThat(pieDoe.getName()).isEqualTo("Pie Doe");

		assertThat(this.customersTemplate.containsKey(16L)).isTrue();
	}

	@Test
	public void cacheWritesToDatabase() {

		Customer jonDoe = Customer.newCustomer(2L, "Jon Doe");

		assertThat(this.customerRepository.existsById(jonDoe.getId())).isFalse();
		assertThat(this.customerRepository.findByName(jonDoe.getName())).isNull();
		assertThat(this.customersTemplate.containsKey(jonDoe.getId())).isFalse();

		this.customersTemplate.put(jonDoe.getId(), jonDoe);

		assertThat(this.customersTemplate.containsKey(jonDoe.getId())).isTrue();
		assertThat(this.customerRepository.existsById(jonDoe.getId())).isTrue();

		Customer jonDoeLoaded = this.customerRepository.findByName(jonDoe.getName());

		assertThat(jonDoeLoaded).isNotNull();
		assertThat(jonDoeLoaded).isEqualTo(jonDoe);
	}

	@SpringBootApplication(exclude = { CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class })
	@ClientCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
	@EntityScan(basePackageClasses = Customer.class)
	@EnableEntityDefinedRegions(basePackageClasses = Customer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
	@EnableJpaRepositories(basePackageClasses = CustomerRepository.class)
	static class TestGeodeClientConfiguration {

		@Bean
		@DependsOn("Customers")
		GemfireTemplate customersTemplate(GemFireCache gemfireCache) {
			return new GemfireTemplate(gemfireCache.getRegion("/Customers"));
		}

		@Bean
		InlineCachingRegionConfigurer<Customer, Long> inlineCachingForCustomersRegionConfigurer(
				CustomerRepository customerRepository) {

			return new InlineCachingRegionConfigurer<>(customerRepository, Predicate.isEqual("Customers"));
		}
	}
}
