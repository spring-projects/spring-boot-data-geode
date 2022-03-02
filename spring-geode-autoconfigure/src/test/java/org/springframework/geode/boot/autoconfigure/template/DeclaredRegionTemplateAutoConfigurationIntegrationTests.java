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
package org.springframework.geode.boot.autoconfigure.template;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Tests for {@link RegionTemplateAutoConfiguration} using explicitly declared {@link Region}
 * bean definitions in a Spring {@link ApplicationContext}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class DeclaredRegionTemplateAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	@Qualifier("exampleTemplate")
	private GemfireTemplate exampleTemplate;

	@Autowired
	@Qualifier("testRegionTemplate")
	private GemfireTemplate testRegionTemplate;

	@Autowired
	@Qualifier("usersTemplate")
	private GemfireTemplate usersTemplate;

	@Resource(name = "Example")
	private Region<Long, String> exampleRegion;

	@Resource(name = "TestRegion")
	@SuppressWarnings("rawtypes")
	private Region testRegion;

	@Resource(name = "Users")
	private Region<Long, User> users;

	@Test
	public void exampleRegionIsPresent() {

		assertThat(this.exampleRegion).isNotNull();
		assertThat(this.exampleRegion.getName()).isEqualTo("Example");
	}

	@Test
	public void exampleRegionTemplateIsPresent() {

		assertThat(this.exampleTemplate).isNotNull();
		assertThat(this.exampleTemplate.getRegion()).isEqualTo(this.exampleRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRegionIsPresent() {

		assertThat(this.testRegion).isNotNull();
		assertThat(this.testRegion.getName()).isEqualTo("TestRegion");
	}

	@Test
	public void testRegionTemplateIsPresent() {

		assertThat(this.testRegionTemplate).isNotNull();
		assertThat(this.testRegionTemplate.getRegion()).isEqualTo(this.testRegion);
	}

	@Test
	public void usersRegionIsPresent() {

		assertThat(this.users).isNotNull();
		assertThat(this.users.getName()).isEqualTo("Users");
	}

	@Test
	public void usersRegionTemplateIsPresent() {

		assertThat(this.usersTemplate).isNotNull();
		assertThat(this.usersTemplate.getRegion()).isEqualTo(this.users);
	}

	@Getter
	@ToString(of = "name")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "newUser")
	static class User {

		private Long id;

		@NonNull
		private final String name;

		User identifiedBy(Long id) {
			this.id = id;
			return this;
		}
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean("Example")
		ClientRegionFactoryBean<Long, String> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Long, String> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setShortcut(ClientRegionShortcut.LOCAL);

			return exampleRegion;
		}

		@Bean("TestBean")
		public Object testBean(@Qualifier("exampleTemplate") GemfireTemplate exampleTemplate) {
			return "TEST";
		}

		// NOTE: Raw type definition is deliberate to ensure the generic FactoryBean and Region signature
		// can be properly autowired/injected into the auto-configured GemfireTemplate for this Region!
		@Bean("TestRegion")
		@SuppressWarnings("rawtypes")
		public ClientRegionFactoryBean testRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean testRegion = new ClientRegionFactoryBean();

			testRegion.setCache(gemfireCache);
			testRegion.setShortcut(ClientRegionShortcut.PROXY);

			return testRegion;
		}

		@Bean("Users")
		public ClientRegionFactoryBean<Long, User> usersRegions(GemFireCache cache) {

			ClientRegionFactoryBean<Long, User> usersRegion = new ClientRegionFactoryBean<>();

			usersRegion.setCache(cache);
			usersRegion.setPersistent(false);

			return usersRegion;
		}
	}
}
