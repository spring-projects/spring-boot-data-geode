/*
 * Copyright 2018 the original author or authors.
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
import static org.springframework.geode.config.annotation.LocatorsConfiguration.LOCATORS_PROPERTY;
import static org.springframework.geode.config.annotation.LocatorsConfiguration.REMOTE_LOCATORS_PROPERTY;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;

/**
 * Integration tests for {@link UseLocators} and {@link LocatorsConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LocatorsConfigurationIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

	@Test
	public void clientCacheLocatorPropertiesAreNotPresent() {

		ClientCache clientCache =
			newApplicationContext(ClientCacheTestConfiguration.class).getBean("gemfireCache", ClientCache.class);

		assertThat(clientCache).isNotNull();
		assertThat(clientCache.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = clientCache.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties.containsKey(LOCATORS_PROPERTY)).isFalse();
		assertThat(gemfireProperties.containsKey(REMOTE_LOCATORS_PROPERTY)).isFalse();
	}

	@Test
	public void peerCacheLocatorPropertiesArePresent() {

		Cache peerCache =
			newApplicationContext(PeerCacheTestConfiguration.class).getBean("gemfireCache", Cache.class);

		assertThat(peerCache).isNotNull();
		assertThat(peerCache.getDistributedSystem()).isNotNull();

		Properties gemfireProperties = peerCache.getDistributedSystem().getProperties();

		assertThat(gemfireProperties).isNotNull();
		assertThat(gemfireProperties.containsKey(LOCATORS_PROPERTY)).isTrue();
		assertThat(gemfireProperties.getProperty(LOCATORS_PROPERTY)).isEqualTo("mailbox[11235],skullbox[12480]");
		assertThat(gemfireProperties.containsKey(REMOTE_LOCATORS_PROPERTY)).isTrue();
		assertThat(gemfireProperties.getProperty(REMOTE_LOCATORS_PROPERTY)).isEqualTo("remotehost[10334]");
	}

	@Configuration
	@ClientCacheApplication
	@EnableGemFireMockObjects
	@UseLocators(remoteLocators = "remotehost[10334]")
	static class ClientCacheTestConfiguration { }

	@Configuration
	@PeerCacheApplication
	@EnableGemFireMockObjects
	@UseLocators(locators = "mailbox[11235],skullbox[12480]", remoteLocators = "remotehost[10334]")
	static class PeerCacheTestConfiguration { }

}
