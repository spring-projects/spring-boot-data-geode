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
package org.springframework.geode.boot.autoconfigure.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests asserting and testing that Geode {@link Properties} declared in Spring Boot
 * {@literal application.properties} apply equally to Spring Boot configured and bootstrapped Apache Geode
 * {@literal peer} {@link Cache} applications.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see java.util.Properties
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("peer-application-gemfire-properties")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
	"spring.application.name=GemFirePropertiesFromEnvironmentApplyToPeerCacheApplicationIntegrationTests",
	"gemfire.conserve-sockets=false",
	"gemfire.distributed-system-id=123",
	"gemfire.enable-network-partition-detection=false",
	"gemfire.enable-time-statistics=true",
	"gemfire.enforce-unique-host=true",
	"gemfire.groups=TestGroup",
	"gemfire.member-timeout=30000"
})
@SuppressWarnings("unused")
public class GemFirePropertiesFromEnvironmentApplyToPeerCacheApplicationIntegrationTests
		extends IntegrationTestsSupport {

	@Autowired
	private Cache peerCache;

	@Test
	public void peerCacheConfigurationIsCorrect() {

		assertThat(this.peerCache).isNotNull();
		assertThat(this.peerCache.getName())
			.isEqualTo(GemFirePropertiesFromEnvironmentApplyToPeerCacheApplicationIntegrationTests.class.getSimpleName());
		assertThat(this.peerCache.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = this.peerCache.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties).containsKeys("conserve-sockets", "distributed-system-id",
			"enable-network-partition-detection", "enable-time-statistics", "enforce-unique-host",
			"groups", "member-timeout");
		assertThat(gemfireProperties.getProperty("conserve-sockets")).isEqualTo("false");
		assertThat(gemfireProperties.getProperty("distributed-system-id")).isEqualTo("123");
		assertThat(gemfireProperties.getProperty("enable-network-partition-detection")).isEqualTo("false");
		assertThat(gemfireProperties.getProperty("enable-time-statistics")).isEqualTo("true");
		assertThat(gemfireProperties.getProperty("enforce-unique-host")).isEqualTo("true");
		assertThat(gemfireProperties.getProperty("groups")).isEqualTo("TestGroup");
		assertThat(gemfireProperties.getProperty("member-timeout")).isEqualTo("30000");
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	@PeerCacheApplication
	@Profile("peer-application-gemfire-properties")
	static class TestGeodeConfiguration { }

}
