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
package org.springframework.geode.boot.autoconfigure.caching;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.CachingProviderAutoConfiguration;
import org.springframework.geode.util.GeodeAssertions;
import org.springframework.test.context.junit4.SpringRunner;

import example.test.service.TestCacheableService;

/**
 * Unit Tests for {@link CachingProviderAutoConfiguration} using Mock Apache Geode {@link Object Objects}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.CachingProviderAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.2.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredCachingUnitTests extends IntegrationTestsSupport {

	@Autowired
	private ClientCache clientCache;

	@Resource(name = "RandomNumbers")
	private Region<String, Number> randomNumbers;

	@Autowired
	private TestCacheableService cacheableService;

	@Before
	public void setup() {

		assertThat(this.clientCache).isNotNull();
		assertThat(this.randomNumbers).isNotNull();

		GeodeAssertions.assertThat(this.clientCache).isNotInstanceOfGemFireCacheImpl();
		GeodeAssertions.assertThat(this.randomNumbers).isNotInstanceOfAbstractRegion();

		assertThat(this.cacheableService).isNotNull();
		assertThat(this.cacheableService.isCacheMiss()).isFalse();
	}

	@Test
	public void cachingIsConfiguredAndWorkingCorrectly() {

		Number randomNumber = this.cacheableService.getRandomNumber("A");

		assertThat(randomNumber).isNotNull();
		assertThat(this.cacheableService.isCacheMiss()).isTrue();

		Number sameRandomNumber = this.cacheableService.getRandomNumber("A");

		assertThat(sameRandomNumber).isEqualTo(randomNumber);
		assertThat(this.cacheableService.isCacheMiss()).isFalse();

		Number anotherRandomNumber = this.cacheableService.getRandomNumber("B");

		assertThat(anotherRandomNumber).isNotNull();
		assertThat(anotherRandomNumber).isNotEqualTo(randomNumber);
		assertThat(this.cacheableService.isCacheMiss()).isTrue();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableCachingDefinedRegions
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean
		TestCacheableService testCacheableService() {
			return new TestCacheableService();
		}
	}
}
