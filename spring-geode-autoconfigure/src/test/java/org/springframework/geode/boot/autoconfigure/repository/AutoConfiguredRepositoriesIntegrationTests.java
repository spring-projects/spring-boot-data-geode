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

package org.springframework.geode.boot.autoconfigure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.geode.boot.autoconfigure.repository.model.Customer;
import org.springframework.geode.boot.autoconfigure.repository.service.CustomerService;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests testing the auto-configuration of Spring Data Repositories backed by either Apache Geode
 * or Pivotal GemFire.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.RepositoriesAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredRepositoriesIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	private CustomerService customerService;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@Test
	public void customerServiceWasConfiguredCorrectly() {

		assertThat(this.customerService).isNotNull();
		assertThat(this.customerService.getCustomerRepository()).isNotNull();
	}

	@Test
	public void customersRegionWasConfiguredCorrectly() {

		assertThat(this.customers).isNotNull();
		assertThat(this.customers.getName()).isEqualTo("Customers");
		assertThat(this.customers.getFullPath()).isEqualTo(RegionUtils.toRegionPath("Customers"));
		assertThat(this.customers).isEmpty();
	}

	@Test
	public void repositoryWasAutoConfiguredCorrectly() {

		Customer jonDoe = Customer.newCustomer("Jon Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.isNew()).isTrue();

		jonDoe = this.customerService.save(jonDoe);

		assertThat(jonDoe.isNew()).isFalse();
		assertThat(this.customers.get(jonDoe.getId())).isEqualTo(jonDoe);
		assertThat(this.customerService.findBy(jonDoe.getName()).orElse(null)).isEqualTo(jonDoe);

	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	@EnableEntityDefinedRegions(basePackageClasses = Customer.class,
		clientRegionShortcut = ClientRegionShortcut.LOCAL)
	static class TestConfiguration { }

}
