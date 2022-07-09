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
package org.springframework.geode.cache.inline.database;

import java.util.function.Predicate;

import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;

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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.geode.cache.InlineCachingRegionConfigurer;
import org.springframework.geode.cache.inline.AbstractInlineCachingWithExternalDataSourceIntegrationTests;
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
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 * @see org.springframework.geode.cache.InlineCachingRegionConfigurer
 * @see org.springframework.geode.cache.inline.AbstractInlineCachingWithExternalDataSourceIntegrationTests
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("inline-caching-database")
@SuppressWarnings("unused")
public class InlineCachingWithDatabaseIntegrationTests
		extends AbstractInlineCachingWithExternalDataSourceIntegrationTests {

	@SpringBootApplication(exclude = { CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class })
	@ClientCacheApplication
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
