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
package org.springframework.geode.boot.autoconfigure.cache.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnablePool;
import org.springframework.data.gemfire.config.annotation.PoolConfigurer;
import org.springframework.data.gemfire.tests.integration.SpringBootApplicationIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.lang.NonNull;

import org.opentest4j.AssertionFailedError;

/**
 * Integration Tests testing the custom configuration of the {@literal DEFAULT} {@link Pool}
 * as well as a named {@link Pool} when using Spring Boot {@link EnableAutoConfiguration auto-configuration}
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.EnablePool
 * @see org.springframework.data.gemfire.config.annotation.PoolConfigurer
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.SpringBootApplicationIntegrationTestsSupport
 * @since 1.1.0
 */
@SuppressWarnings("unused")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientCachePoolCustomizationsIntegrationTests extends SpringBootApplicationIntegrationTestsSupport {

	private static final String DEFAULT_POOL_NAME = "DEFAULT";
	private static final String SPRING_DATA_GEMFIRE_PROPERTY_PREFIX = "spring.data.gemfire.";

	@BeforeClass @AfterClass
	public static void testSuiteSetupAndTearDown() {

		PoolManager.close(false);

		assertThat(PoolManager.find(DEFAULT_POOL_NAME)).isNull();
	}

	@Before @After
	public void testCaseSetupAndTearDown() {

		System.getProperties().stringPropertyNames().stream()
			.filter(propertyName -> propertyName.startsWith(SPRING_DATA_GEMFIRE_PROPERTY_PREFIX))
			.forEach(System::clearProperty);
	}

	private void testNamedPoolWasCustomized(@NonNull ConfigurableApplicationContext applicationContext,
			@NonNull String poolName, boolean poolBeanPresent) {

		ClientCache clientCache = applicationContext.getBean(ClientCache.class);

		assertThat(clientCache).isNotNull();

		// NOTE: The named Pool bean must be gotten from the Spring BeanFactory before querying the PoolManager
		// since the named Pool bean construction and initialization is lazy.
		Pool namedPoolBean = null;

		if (poolBeanPresent) {

			namedPoolBean = applicationContext.getBean(poolName, Pool.class);

			assertThat(namedPoolBean).isNotNull();
			assertThat(namedPoolBean.getName()).isEqualTo(poolName);
			assertThat(namedPoolBean.getServerGroup()).isEqualTo("TestServerGroup");
			assertThat(namedPoolBean.getThreadLocalConnections()).isTrue();
		}

		Pool namedPool = PoolManager.find(poolName);

		assertThat(namedPool).isNotNull();
		assertThat(namedPool.getName()).isEqualTo(poolName);
		assertThat(namedPool.getServerGroup()).isEqualTo("TestServerGroup");
		assertThat(namedPool.getThreadLocalConnections()).isTrue();

		if (DEFAULT_POOL_NAME.equals(poolName)) {
			assertThat(namedPool).isSameAs(clientCache.getDefaultPool());
		}

		if (poolBeanPresent) {
			assertThat(namedPoolBean).isNotNull();
			assertThat(namedPool).isSameAs(namedPoolBean);
		}
	}

	private void testNamedPoolWasNotCustomized(@NonNull ConfigurableApplicationContext applicationContext,
			@NonNull String poolName, boolean poolBeanPresent) {

		try {
			testNamedPoolWasCustomized(applicationContext, poolName, poolBeanPresent);
			fail("Pool [%s] should not have been modified", poolName);
		}
		catch (AssertionFailedError ignore) { }
	}

	@Test
	public void usingClientCacheApplicationAnnotationModifiesDefaultPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithClientCacheApplicationConfiguration.class, WithEnableTestPoolConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, DEFAULT_POOL_NAME, false);
		testNamedPoolWasNotCustomized(applicationContext, "TestPool", true);
	}

	@Test
	public void usingClientCacheConfigurerModifiesDefaultPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithClientCacheConfigurerConfiguration.class, WithEnableTestPoolConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, DEFAULT_POOL_NAME,false);
		testNamedPoolWasNotCustomized(applicationContext, "TestPool", true);
	}

	@Test
	public void usingClientCacheDefaultPoolPropertiesConfigurationModifiesDefaultPool() {

		System.setProperty("spring.data.gemfire.pool.default.server-group", "TestServerGroup");
		System.setProperty("spring.data.gemfire.pool.default.thread-local-connections", Boolean.TRUE.toString());

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, DEFAULT_POOL_NAME,false);
		testNamedPoolWasNotCustomized(applicationContext, "TestPool", true);
	}

	@Test
	public void usingClientCachePoolPropertiesConfigurationModifiesAllPools() {

		System.setProperty("spring.data.gemfire.pool.server-group", "TestServerGroup");
		System.setProperty("spring.data.gemfire.pool.thread-local-connections", Boolean.TRUE.toString());

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, DEFAULT_POOL_NAME,false);
		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
	}

	@Test
	public void usingEnablePoolAnnotationForTestPoolModifiesTestPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolAttributesConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME, false);
	}

	@Test
	@Ignore
	// TODO: Understand why the Spring container cannot resolve the allPoolsConfigurer bean.
	public void usingPoolConfigurerForTestPoolModifiesTestPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolConfiguration.class, WithPoolConfigurerConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME, false);
	}

	@Test
	public void usingPoolConfigurerBeanForTestPoolModifiesTestPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolConfigurerConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME, false);
	}

	@Test
	public void usingPoolConfigurerImportForTestPoolModifiesTestPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolImportConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME, false);
	}

	@Test
	public void usingPoolPropertiesForTestPoolModifiesTestPool() {

		System.setProperty("spring.data.gemfire.pool.TestPool.server-group", "TestServerGroup");
		System.setProperty("spring.data.gemfire.pool.TestPool.thread-local-connections", Boolean.TRUE.toString());

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableTestPoolConfiguration.class);

		testNamedPoolWasCustomized(applicationContext, "TestPool", true);
		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME, false);
	}

	@Test
	public void usingEnablePoolAnnotationForDefaultPoolDoesNotModifyDefaultPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableDefaultPoolAttributesConfiguration.class);

		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME,true);
	}

	@Test
	public void usingEnablePoolAnnotationForDefaultPoolAndPoolConfigurerDoesNotModifyDefaultPool() {

		ConfigurableApplicationContext applicationContext =
			newApplicationContext(WithAutoConfiguredClientCacheConfiguration.class,
				WithEnableDefaultPoolConfiguration.class, WithPoolConfigurerConfiguration.class);

		testNamedPoolWasNotCustomized(applicationContext, DEFAULT_POOL_NAME,true);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableGemFireMockObjects(destroyOnEvents = ContextClosedEvent.class)
	static class WithAutoConfiguredClientCacheConfiguration { }

	@ClientCacheApplication(serverGroup = "TestServerGroup", threadLocalConnections = true)
	static class WithClientCacheApplicationConfiguration { }

	@Configuration
	static class WithClientCacheConfigurerConfiguration {

		@Bean
		ClientCacheConfigurer clientCacheDefaultPoolConfigurer() {

			return (beanName, clientCacheFactoryBean) -> {

				clientCacheFactoryBean.setServerGroup("TestServerGroup");
				clientCacheFactoryBean.setThreadLocalConnections(true);
			};
		}
	}

	@Configuration
	@EnablePool(name = "DEFAULT")
	static class WithEnableDefaultPoolConfiguration { }

	@Configuration
	@EnablePool(name = "DEFAULT", serverGroup = "TestServerGroup", threadLocalConnections = true)
	static class WithEnableDefaultPoolAttributesConfiguration { }

	@Configuration
	@EnablePool(name = "TestPool", servers = @EnablePool.Server)
	static class WithEnableTestPoolConfiguration { }

	@Configuration
	@EnablePool(name = "TestPool", servers = @EnablePool.Server, serverGroup = "TestServerGroup",
		threadLocalConnections = true)
	static class WithEnableTestPoolAttributesConfiguration { }

	@Configuration
	@EnablePool(name = "TestPool", servers = @EnablePool.Server)
	static class WithEnableTestPoolConfigurerConfiguration {

		@Bean
		PoolConfigurer allPoolsConfigurer() {

			return (beanName, poolFactoryBean) -> {

				poolFactoryBean.setServerGroup("TestServerGroup");
				poolFactoryBean.setThreadLocalConnections(true);
			};
		}
	}

	@Configuration
	@EnablePool(name = "TestPool", servers = @EnablePool.Server)
	@Import(WithPoolConfigurerConfiguration.class)
	static class WithEnableTestPoolImportConfiguration { }

	@Configuration
	static class WithPoolConfigurerConfiguration {

		@Bean
		PoolConfigurer allPoolsConfigurer() {

			return (beanName, poolFactoryBean) -> {

				poolFactoryBean.setServerGroup("TestServerGroup");
				poolFactoryBean.setThreadLocalConnections(true);
			};
		}
	}
}
