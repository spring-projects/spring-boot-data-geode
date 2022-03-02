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
package org.springframework.geode.boot.autoconfigure.cluster.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.config.annotation.ClusterConfigurationConfiguration;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for testing the {@link EnableClusterAware} annotation and expected configuration applied by SBDG
 * when the Apache Geode cluster is secure.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.config.annotation.ClusterConfigurationConfiguration
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.2.0
 */
@ActiveProfiles("cluster-configuration-with-secure-cluster")
@RunWith(SpringRunner.class)
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"VCAP_APPLICATION={ \"name\" : \"ClusterConfigurationWithClusterAwareWhenSecureClusterAvailableIntegrationTests\", \"uris\" : \"myapp.app.cloud.skullbox.com\"}",
		"VCAP_SERVICES={ \"test-pcc\" : [{ \"name\" : \"test-pcc\", \"tags\" : \"cloudcache,database,gemfire,pivotal\", \"credentials\" : { \"urls\" : { \"gfsh\" : \"https://myapp.app.cloud.skullbox.com/gemfire/v1\" }}}]}",
		"spring.boot.data.gemfire.cluster.condition.match=true"
	}
)
@SuppressWarnings("unused")
public class ClusterConfigurationWithClusterAwareWhenSecureClusterAvailableIntegrationTests
		extends IntegrationTestsSupport {

	@BeforeClass @AfterClass
	public static void resetClusterAwareCondition() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Autowired
	private Environment environment;

	@Autowired
	private ClusterConfigurationConfiguration configuration;

	@Test
	public void configurationStatesManagementRestApiRequiresHttps() {

		boolean configurationRequiresHttps =
			ObjectUtils.invoke(this.configuration, "resolveManagementRequireHttps");

		assertThat(configurationRequiresHttps).isTrue();
	}

	@Test
	public void environmentStatesManagementRestApiRequiresHttps() {

		boolean environmentRequiresHttps =
			this.environment.getProperty("spring.data.gemfire.management.require-https", Boolean.class);

		assertThat(environmentRequiresHttps).isTrue();
	}

	@SpringBootApplication
	@EnableClusterAware
	@EnableGemFireMockObjects
	@Profile("cluster-configuration-with-secure-cluster")
	static class TestConfiguration { }

}
