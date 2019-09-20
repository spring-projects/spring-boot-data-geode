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
package org.springframework.geode.boot.autoconfigure.template;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for {@link RegionTemplateAutoConfiguration}.
 *
 * This Integration Test class tests that the {@link GemfireTemplate} is created regardless of whether
 * the {@literal Example} client {@link Region} bean is actually referenced (injected) into application code.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://github.com/spring-projects/spring-boot-data-geode/issues/55">Autowiring a GemfireTemplate into the application is not working in all cases</a>
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class DeclaredNonInjectedRegionTemplateAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ClientCache clientCache;

	@Autowired
	@Qualifier("exampleTemplate")
	private GemfireTemplate exampleTemplate;

	@Before
	public void setup() {

		assertThat(this.clientCache).isNotNull();
		assertThat(this.exampleTemplate).isNotNull();
	}

	@Test
	public void clientCacheContainsExampleRegion() {

		Region<?, ?> example = this.clientCache.getRegion(RegionUtils.toRegionPath("Example"));

		assertThat(example).isNotNull();
		assertThat(example.getName()).isEqualTo("Example");
	}

	@Test
	public void exampleRegionTemplateExists() {
		assertThat(this.exampleTemplate.getRegion())
			.isEqualTo(this.clientCache.getRegion(RegionUtils.toRegionPath("Example")));
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean("Example")
		public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setShortcut(ClientRegionShortcut.LOCAL);

			return exampleRegion;
		}

		@Bean("TestBean")
		public Object testBean(@Qualifier("exampleTemplate") GemfireTemplate exampleTemplate) {
			return "TEST";
		}
	}
}
