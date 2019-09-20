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

import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link RegionTemplateAutoConfiguration} using SDG's {@link EnableClusterDefinedRegions}
 * annotation to define {@link Region Regions} and associated Templates.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
	classes = ServerDefinedRegionTemplateAutoConfigurationIntegrationTests.GemFireClientConfiguration.class)
@SuppressWarnings("unused")
public class ServerDefinedRegionTemplateAutoConfigurationIntegrationTests
		extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "off";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class);
	}

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ClientCache clientCache;

	@Autowired
	private GemfireTemplate exampleServerRegionTemplate;

	@Test
	public void clientCacheContainsExampleServerRegion() {

		assertThat(this.clientCache).isNotNull();

		assertThat(CollectionUtils.nullSafeSet(this.clientCache.rootRegions()).stream()
			.map(Region::getName)
			.collect(Collectors.toSet())).containsExactly("ExampleServerRegion");
	}

	@Test
	public void exampleServerRegionExistsAsClientRegionBean() {

		Region<?, ?> exampleServerRegion = this.applicationContext.getBean("ExampleServerRegion", Region.class);

		assertThat(exampleServerRegion).isNotNull();
		assertThat(exampleServerRegion.getName()).isEqualTo("ExampleServerRegion");
		assertThat(exampleServerRegion.getAttributes()).isNotNull();
		assertThat(exampleServerRegion.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@Test
	public void exampleServerRegionTemplateIsPresent() {

		assertThat(this.exampleServerRegionTemplate).isNotNull();
		assertThat(this.exampleServerRegionTemplate.getRegion()).isNotNull();
		assertThat(this.exampleServerRegionTemplate.getRegion().getName()).isEqualTo("ExampleServerRegion");
	}

	@SpringBootApplication
	@EnableClusterDefinedRegions
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireClientConfiguration {

		@Bean("TestBean")
		Object testBean(@Qualifier("exampleServerRegionTemplate") GemfireTemplate exampleServerRegionTemplate) {
			return "TEST";
		}
	}

	@CacheServerApplication(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(GemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("ExampleServerRegion")
		LocalRegionFactoryBean<Object, Object> exampleServerRegion(GemFireCache gemfireCache) {

			LocalRegionFactoryBean<Object, Object> exampleServerRegion = new LocalRegionFactoryBean<>();

			exampleServerRegion.setCache(gemfireCache);
			exampleServerRegion.setClose(false);
			exampleServerRegion.setPersistent(false);

			return exampleServerRegion;
		}
	}
}
