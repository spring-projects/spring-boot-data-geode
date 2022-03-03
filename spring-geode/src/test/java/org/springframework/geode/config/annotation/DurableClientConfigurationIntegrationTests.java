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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.apache.geode.cache.client.ClientCache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;

/**
 * Integration tests for {@link EnableDurableClient} and {@link DurableClientConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.config.annotation.DurableClientConfiguration
 * @see org.springframework.geode.config.annotation.EnableDurableClient
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DurableClientConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static ConfigurableApplicationContext applicationContext;

	private static ClientCache clientCache;

	private static ClientCacheFactoryBean clientCacheFactoryBean;

	@BeforeClass
	public static void newApplicationContext() {

		applicationContext = new AnnotationConfigApplicationContext(TestConfiguration.class);

		clientCache = applicationContext.getBean("gemfireCache", ClientCache.class);

		clientCacheFactoryBean = applicationContext.getBean("&gemfireCache", ClientCacheFactoryBean.class);
	}

	@AfterClass
	public static void closeApplicationContext() {

		Optional.ofNullable(applicationContext)
			.ifPresent(ConfigurableApplicationContext::close);

		assertThat(clientCache).isNotNull();

		verify(clientCache, times(1)).close(eq(true));
	}

	@Test
	public void durableClientWasConfiguredSuccessfully() {

		assertThat(clientCache).isInstanceOf(ClientCache.class);
		assertThat(clientCache.getDistributedSystem()).isNotNull();
		assertThat(clientCache.getDistributedSystem().getProperties()).isNotNull();
		assertThat(clientCache.getDistributedSystem().getProperties().getProperty("durable-client-id")).isEqualTo("abc123");
		assertThat(clientCache.getDistributedSystem().getProperties().getProperty("durable-client-timeout")).isEqualTo("600");
	}

	@Test
	public void setClientCacheFactoryBeanSetsKeepAliveOnClose() {

		assertThat(clientCacheFactoryBean).isNotNull();
		assertThat(clientCacheFactoryBean.isKeepAlive()).isTrue();
	}

	@Test
	public void setClientCacheFactoryBeanSetsReadyForEventOnContextRefreshedEvent() {

		assertThat(clientCacheFactoryBean).isNotNull();
		assertThat(clientCacheFactoryBean.isReadyForEvents()).isTrue();
	}

	@ClientCacheApplication
	@EnableGemFireMockObjects
	@EnableDurableClient(id = "abc123", timeout = 600)
	static class TestConfiguration { }

}
