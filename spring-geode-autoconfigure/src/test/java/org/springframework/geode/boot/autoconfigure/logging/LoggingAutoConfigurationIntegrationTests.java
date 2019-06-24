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
package org.springframework.geode.boot.autoconfigure.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.distributed.internal.DistributionConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests asserting the configuration and behavior of Apache Geode & Pivotal GemFire logging
 * when configured with Spring Boot auto-configuration.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.distributed.internal.DistributionConfig
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = {
		"spring.data.gemfire.logging.level=fine",
		"spring.data.gemfire.logging.log-disk-space-limit=4096",
		"spring.data.gemfire.logging.log-file=/path/to/gemfire.log",
		"spring.data.gemfire.logging.log-file-size-limit=512"
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class LoggingAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private GemFireCache gemfireCache;

	@Test
	public void loggingConfigurationWasApplied() {

		assertThat(this.gemfireCache).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem()).isNotNull();

		Properties distributedSystemProperties = this.gemfireCache.getDistributedSystem().getProperties();

		assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_DISK_SPACE_LIMIT_NAME)).isEqualTo("4096");
		assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_FILE_NAME)).isEqualTo("/path/to/gemfire.log");
		assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_FILE_SIZE_LIMIT_NAME)).isEqualTo("512");
		assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_LEVEL_NAME)).isEqualTo("fine");
	}

	@EnableGemFireMockObjects
	@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class)
	static class TestConfiguration { }

}
