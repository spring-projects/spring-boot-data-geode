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
import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.app.crm.model.Customer;

/**
 * Integration Tests for {@link EnableClusterAware} and {@link ClusterAvailableConfiguration.CloudFoundryClusterAvailableCondition}
 * specifically when the Spring Boot application is run in CloudFoundry.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.DataPolicy
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.2.0
 */
@ActiveProfiles("cloudfoundry-cluster-test")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = "spring.main.cloud-platform=CLOUD_FOUNDRY",
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class CloudFoundryClusterNotAvailableConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final Logger logger = LoggerFactory.getLogger(ClusterAwareConfiguration.class);

	@AfterClass
	public static void tearDown() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Autowired
	private Environment environment;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@Before
	public void assertCloudFoundryEnvironment() {

		assertThat(this.environment).isNotNull();
		assertThat(CloudPlatform.CLOUD_FOUNDRY.isActive(this.environment)).isTrue();
	}

	@Before
	public void assertLoggerIsNotInfoEnabled() {

		assertThat(logger).isNotNull();
		assertThat(logger.isInfoEnabled()).isFalse();
	}

	@Test
	public void clientRegionIsProxy() {

		assertThat(this.customers).isNotNull();
		assertThat(this.customers.getName()).isEqualTo("Customers");
		assertThat(this.customers.getAttributes()).isNotNull();
		assertThat(this.customers.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@Test
	public void springDataGemFireCacheClientRegionShortcutPropertyIsNotSet() {

		String propertyName = ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY;

		assertThat(this.environment.getProperty(propertyName, String.class, null)).isNull();
	}

	@EnableClusterAware
	@EnableGemFireMockObjects
	@Profile("cloudfoundry-cluster-test")
	@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
	@ClientCacheApplication(name = "CloudFoundryClusterNotAvailableConfigurationIntegrationTests")
	static class TestGeodeConfiguration { }

}
