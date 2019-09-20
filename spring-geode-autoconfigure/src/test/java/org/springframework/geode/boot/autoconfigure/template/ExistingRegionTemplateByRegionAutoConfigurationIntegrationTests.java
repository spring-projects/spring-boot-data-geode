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

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link RegionTemplateAutoConfiguration} using explicitly declared {@link Region}
 * bean definition and existing {@link GemfireTemplate} {@link Region} in a Spring {@link ApplicationContext}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class ExistingRegionTemplateByRegionAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private GemfireTemplate testTemplate;

	@Resource(name = "Example")
	private Region<Object, Object> example;

	@Test
	public void exampleTemplateIsPresentButIsNotGemfireTemplate() {

		assertThat(this.applicationContext.containsBean("exampleTemplate")).isTrue();
		assertThat(this.applicationContext.getBean("exampleTemplate")).isNotInstanceOf(GemfireTemplate.class);
	}

	@Test
	public void onlyOneBeanOfTypeGemfireTemplateExists() {
		assertThat(this.applicationContext.getBeanNamesForType(GemfireTemplate.class)).hasSize(1);
	}

	@Test
	public void testTemplateIsPresent() {

		assertThat(this.testTemplate).isNotNull();
		assertThat(this.testTemplate.getRegion()).isEqualTo(this.example);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean("Example")
		public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Object, Object> clientRegion = new ClientRegionFactoryBean<>();

			clientRegion.setCache(gemfireCache);
			clientRegion.setClose(false);
			clientRegion.setShortcut(ClientRegionShortcut.LOCAL);

			return clientRegion;
		}

		@Bean("TestTemplate")
		@DependsOn("Example")
		GemfireTemplate testTemplate(GemFireCache gemfireCache) {
			return new GemfireTemplate(gemfireCache.getRegion("/Example"));
		}
	}
}
