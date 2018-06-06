/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link EnableDurableClient} and {@link DurableClientConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.config.annotation.DurableClientConfiguration
 * @see org.springframework.geode.config.annotation.EnableDurableClient
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class DurableClientIdConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final AtomicReference<ConfigurableApplicationContext> applicationContextReference =
		new AtomicReference<>(null);

	private static final AtomicReference<ClientCache> clientCacheReference =
		new AtomicReference<>(null);

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private GemFireCache gemfireCache;

	@Autowired
	private ClientCacheFactoryBean clientCacheFactoryBean;

	@AfterClass
	public static void closeApplicationContext() {

		Optional.ofNullable(applicationContextReference.get()).ifPresent(ConfigurableApplicationContext::close);

		ClientCache clientCache = clientCacheReference.get();

		assertThat(clientCache).isNotNull();

		verify(clientCache, times(1)).close(eq(true));
	}

	@Test
	public void durableClientWasConfiguredSuccessfully() {

		assertThat(this.gemfireCache).isInstanceOf(ClientCache.class);
		assertThat(this.gemfireCache.getDistributedSystem()).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem().getProperties()).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem().getProperties().getProperty("durable-client-id"))
			.isEqualTo("abc123");
		assertThat(this.gemfireCache.getDistributedSystem().getProperties().getProperty("durable-client-timeout"))
			.isEqualTo("600");

		applicationContextReference.set(this.applicationContext);
		clientCacheReference.set((ClientCache) this.gemfireCache);
	}

	@Test
	public void setClientCacheFactoryBeanSetsKeepAliveOnClose() {

		assertThat(this.clientCacheFactoryBean).isNotNull();
		assertThat(this.clientCacheFactoryBean.isKeepAlive()).isTrue();
	}

	@Test
	public void setClientCacheFactoryBeanSetsReadyForEventOnContextRefreshedEvent() {

		assertThat(this.clientCacheFactoryBean).isNotNull();
		assertThat(this.clientCacheFactoryBean.isReadyForEvents()).isTrue();
	}

	@ClientCacheApplication
	@EnableGemFireMockObjects
	@EnableDurableClient(id = "abc123", timeout = 600)
	static class TestConfiguration { }

}
