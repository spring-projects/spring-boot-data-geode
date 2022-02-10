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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.geode.config.annotation.LocatorsConfiguration.LOCATORS_PROPERTY;
import static org.springframework.geode.config.annotation.LocatorsConfiguration.REMOTE_LOCATORS_PROPERTY;

import java.net.InetAddress;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.Locator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.LocatorFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.LocatorApplication;
import org.springframework.data.gemfire.config.annotation.LocatorConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Integration Tests for {@link UseLocators} and {@link LocatorsConfiguration}.
 *
 * @author John Blum
 * @see java.net.InetAddress
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.apache.geode.distributed.Locator
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.LocatorFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.LocatorApplication
 * @see org.springframework.data.gemfire.config.annotation.LocatorConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LocatorsConfigurationIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

	@Before @After
	public void cleanup() {

		FileSystemUtils.deleteRecursive(FileSystemUtils.WORKING_DIRECTORY, file ->
			file != null && (file.getName().contains("locator") || file.getName().contains("ConfigDiskDir")));
	}

	@Test
	public void clientCacheLocatorPropertiesAreNotPresent() {

		ClientCache clientCache =
			newApplicationContext(ClientCacheTestConfiguration.class).getBean("gemfireCache", ClientCache.class);

		assertThat(clientCache).isNotNull();
		assertThat(clientCache.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = clientCache.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties).doesNotContainKeys(LOCATORS_PROPERTY, REMOTE_LOCATORS_PROPERTY);
	}

	@Test
	public void locatorLocatorPropertiesArePresent() {

		Locator locator = newApplicationContext(LocatorTestConfiguration.class).getBean(Locator.class);

		assertThat(locator).isNotNull();
		assertThat(locator.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = locator.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties).containsKeys(LOCATORS_PROPERTY, REMOTE_LOCATORS_PROPERTY);
		assertThat(gemfireProperties.getProperty(LOCATORS_PROPERTY)).containsIgnoringCase("skullbox[11235]");
		assertThat(gemfireProperties.getProperty(REMOTE_LOCATORS_PROPERTY)).containsIgnoringCase("remotehost[12480]");
	}

	@Test
	public void peerCacheLocatorPropertiesArePresent() {

		Cache peerCache =
			newApplicationContext(PeerCacheTestConfiguration.class).getBean("gemfireCache", Cache.class);

		assertThat(peerCache).isNotNull();
		assertThat(peerCache.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = peerCache.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties).containsKeys(LOCATORS_PROPERTY, REMOTE_LOCATORS_PROPERTY);
		assertThat(gemfireProperties.getProperty(LOCATORS_PROPERTY)).isEqualTo("mailbox[11235],skullbox[12480]");
		assertThat(gemfireProperties.getProperty(REMOTE_LOCATORS_PROPERTY)).isEqualTo("remotehost[10334]");
	}

	@EnableGemFireMockObjects
	@ClientCacheApplication(logLevel = "error")
	@UseLocators(remoteLocators = "remotehost[10334]")
	static class ClientCacheTestConfiguration { }

	@EnableGemFireMockObjects
	@LocatorApplication(logLevel = "error")
	@UseLocators(locators = "skullbox[11235]", remoteLocators = "remotehost[12480]")
	static class LocatorTestConfiguration {

		@Bean
		BeanPostProcessor locatorFactoryBeanPostProcessor() {

			return new BeanPostProcessor() {

				@Nullable @Override
				public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

					if (bean instanceof LocatorFactoryBean) {
						return new TestLocatorFactoryBean((LocatorFactoryBean) bean);
					}

					return bean;
				}
			};
		}

		@Bean
		LocatorConfigurer locatorUseClusterConfigurationConfigurer() {

			return (beanName, locatorFactoryBean) -> locatorFactoryBean.getGemFireProperties()
				.setProperty("use-cluster-configuration", Boolean.FALSE.toString());
		}
	}

	// TODO: replace with STDG when STDG is rebased on SD[G] Moore/2.2 and STDG includes dedicated mocking support
	//  for Apache Geode Locator creation using SDG's o.s.d.g.LocatorFactoryBean
	@EnableGemFireMockObjects
	@PeerCacheApplication(logLevel = "error")
	@UseLocators(locators = "mailbox[11235],skullbox[12480]", remoteLocators = "remotehost[10334]")
	static class PeerCacheTestConfiguration { }

	static class TestLocatorFactoryBean extends LocatorFactoryBean {

		private Locator mockLocator;

		private final LocatorFactoryBean locatorFactoryBean;

		public TestLocatorFactoryBean(LocatorFactoryBean locatorFactoryBean) {

			Assert.notNull(locatorFactoryBean, "LocatorFactoryBean is required");

			this.locatorFactoryBean = locatorFactoryBean;
		}

		@Override
		public void init() {

			DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

			InetAddress mockInetAddress = mock(InetAddress.class);

			this.mockLocator = mock(Locator.class);

			doReturn(mockInetAddress).when(this.mockLocator).getBindAddress();

			doReturn(mockDistributedSystem).when(this.mockLocator).getDistributedSystem();

			doReturn(getHostnameForClients().orElse("localhost"))
				.when(this.mockLocator).getHostnameForClients();

			doReturn(getPort()).when(this.mockLocator).getPort();

			doReturn(getGemFireProperties()).when(mockDistributedSystem).getProperties();

		}

		@Override
		public LocatorConfigurer getCompositeLocatorConfigurer() {
			return getLocatorFactoryBean().getCompositeLocatorConfigurer();
		}

		@Override
		public Locator getLocator() {
			return this.mockLocator;
		}

		protected LocatorFactoryBean getLocatorFactoryBean() {
			return this.locatorFactoryBean;
		}
	}
}
