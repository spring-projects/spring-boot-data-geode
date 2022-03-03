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
package org.springframework.geode.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.crm.model.Customer;
import example.app.crm.service.CustomerService;

/**
 * Integration Tests asserting the functionality and behavior of {@link EnableClusterAware}
 * and {@link ClusterNotAvailableConfiguration} when the Apache Geode cluster of servers
 * is not available.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.config.annotation.ClusterNotAvailableConfiguration
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = ClusterNotAvailableConfigurationIntegrationTests.GeodeClientApplication.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class ClusterNotAvailableConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final String LOG_LEVEL = "error";

	@AfterClass
	public static void tearDown() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@Resource(name = "CustomersByName")
	private Region<String, Customer> customersByName;

	@Resource(name = "Example")
	private Region<Object, Object> example;

	@Before
	public void assertRegionConfiguration() {

		assertRegion(this.example, "Example", DataPolicy.NORMAL, null);
		assertRegion(this.customers, "Customers", DataPolicy.NORMAL, null);
		assertRegion(this.customersByName, "CustomersByName", DataPolicy.NORMAL, null);
	}

	private void assertRegion(Region<?, ?> region, String name, DataPolicy dataPolicy, String poolName) {

		assertThat(region).isNotNull();
		assertThat(region.getName()).isEqualTo(name);
		assertThat(region.getAttributes()).isNotNull();
		assertThat(region.getAttributes().getDataPolicy()).isEqualTo(dataPolicy);
		assertThat(region.getAttributes().getPoolName()).isEqualTo(poolName);
	}

	@Test
	public void customersRegionDataAccessOperationsWork() {

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		assertThat(this.customers.put(jonDoe.getId(), jonDoe)).isNull();
		assertThat(this.customers.get(jonDoe.getId())).isEqualTo(jonDoe);
	}

	@Test
	public void customersByNameRegionDataAccessOperationsWork() {

		Customer jonDoe = Customer.newCustomer(2L, "Jane Doe");

		assertThat(this.customersByName.put(jonDoe.getName(), jonDoe)).isNull();
		assertThat(this.customersByName.get(jonDoe.getName())).isEqualTo(jonDoe);
	}

	@Test
	public void exampleRegionDataAccessOperationsWork() {

		assertThat(this.example.put(1, "TEST")).isNull();
		assertThat(this.example.get(1)).isEqualTo("TEST");
	}

	@EnableClusterAware
	@EnableCachingDefinedRegions
	@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
	@ClientCacheApplication(name = "ClusterNotAvailableConfigurationIntegrationTests", logLevel = LOG_LEVEL)
	static class GeodeClientApplication {

		@Bean("Example")
		ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache cache) {

			ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(cache);
			exampleRegion.setShortcut(ClientRegionShortcut.PROXY);

			return exampleRegion;
		}

		@Bean
		CustomerService customerService() {
			return new CustomerService();
		}
	}
}
