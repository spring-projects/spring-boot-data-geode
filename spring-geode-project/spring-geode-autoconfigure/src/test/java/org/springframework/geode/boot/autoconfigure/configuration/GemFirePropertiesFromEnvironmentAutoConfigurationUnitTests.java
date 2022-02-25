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
package org.springframework.geode.boot.autoconfigure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.geode.boot.autoconfigure.EnvironmentSourcedGemFirePropertiesAutoConfiguration;
import org.springframework.lang.NonNull;

import org.slf4j.Logger;

/**
 * Unit Tests for {@link GemFirePropertiesFromEnvironmentAutoConfigurationUnitTests}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.geode.boot.autoconfigure.EnvironmentSourcedGemFirePropertiesAutoConfiguration
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GemFirePropertiesFromEnvironmentAutoConfigurationUnitTests {

	private final TestEnvironmentSourcedGemFirePropertiesAutoConfiguration configuration =
		spy(new TestEnvironmentSourcedGemFirePropertiesAutoConfiguration());

	@Test
	public void clientCacheGemFirePropertiesConfigurerCallsConfigureGemFireProperties() {

		ClientCacheFactoryBean mockClientCacheFactoryBean = mock(ClientCacheFactoryBean.class);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		doNothing().when(this.configuration)
			.configureGemFireProperties(any(ConfigurableEnvironment.class), any(CacheFactoryBean.class));

		ClientCacheConfigurer clientCacheConfigurer =
			this.configuration.clientCacheGemFirePropertiesConfigurer(mockEnvironment);

		assertThat(clientCacheConfigurer).isNotNull();

		clientCacheConfigurer.configure("MockCacheBeanName", mockClientCacheFactoryBean);

		verify(this.configuration, times(1))
			.configureGemFireProperties(eq(mockEnvironment), eq(mockClientCacheFactoryBean));
	}

	@Test
	public void peerCacheGemFirePropertiesConfigurerCallsConfigureGemFireProperties() {

		CacheFactoryBean mockPeerCacheFactoryBean = mock(CacheFactoryBean.class);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		doNothing().when(this.configuration)
			.configureGemFireProperties(any(ConfigurableEnvironment.class), any(CacheFactoryBean.class));

		PeerCacheConfigurer peerCacheConfigurer =
			this.configuration.peerCacheGemFirePropertiesConfigurer(mockEnvironment);

		assertThat(peerCacheConfigurer).isNotNull();

		peerCacheConfigurer.configure("MockCacheBeanName", mockPeerCacheFactoryBean);

		verify(this.configuration, times(1))
			.configureGemFireProperties(eq(mockEnvironment), eq(mockPeerCacheFactoryBean));
	}

	@Test
	public void configuresGemFirePropertiesCorrectlyAndSafely() {

		Map<String, Object> gemfirePropertiesOne = MapBuilder.<String, Object>newMapBuilder()
			.put("gemfire.cache-xml-file", "/path/to/cache.xml")
			.put("gemfire.name", "TestName")
			.put("gemfire.non-existing-property", "TEST")
			.put("geode.enforce-unique-host", "true")
			.put("enable-time-statistics", "true")
			.put("junk.test-property", "MOCK")
			.build();

		Map<String, Object> gemfirePropertiesTwo = MapBuilder.<String, Object>newMapBuilder()
			.put("gemfire.groups", "MockGroup,TestGroup")
			.put("gemfire.mcast-port", " ")
			.put("gemfire.remote-locators", "hellbox[666]")
			.put("mock-property", "MOCK")
			.put("  ", "BLANK")
			.put("", "EMPTY")
			.build();

		CacheFactoryBean mockCacheFactoryBean = mock(CacheFactoryBean.class);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		EnumerablePropertySource<?> emptyEnumerablePropertySource = mock(EnumerablePropertySource.class);

		Logger mockLogger = mock(Logger.class);

		MutablePropertySources propertySources = new MutablePropertySources();

		PropertySource<?> nonEnumerablePropertySource = mock(PropertySource.class);

		propertySources.addFirst(new MapPropertySource("gemfirePropertiesOne", gemfirePropertiesOne));
		propertySources.addLast(new MapPropertySource("gemfirePropertiesTwo", gemfirePropertiesTwo));
		propertySources.addLast(emptyEnumerablePropertySource);
		propertySources.addLast(nonEnumerablePropertySource);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);

		Properties actualGemFireProperties = new Properties();

		actualGemFireProperties.setProperty("remote-locators", "skullbox[12345]");

		doAnswer(invocation -> actualGemFireProperties).when(mockCacheFactoryBean).getProperties();
		doAnswer(invocation -> {

			Properties gemfireProperties = invocation.getArgument(0);

			actualGemFireProperties.putAll(gemfireProperties);

			return null;

		}).when(mockCacheFactoryBean).setProperties(any(Properties.class));

		doReturn(mockLogger).when(this.configuration).getLogger();

		doAnswer(invocation -> {

			String propertyName = invocation.getArgument(0);

			return gemfirePropertiesOne.getOrDefault(propertyName,
				gemfirePropertiesTwo.getOrDefault(propertyName, null));

		}).when(mockEnvironment).getProperty(anyString());

		this.configuration.configureGemFireProperties(mockEnvironment, mockCacheFactoryBean);

		assertThat(actualGemFireProperties).isNotNull();
		assertThat(actualGemFireProperties).hasSize(4);
		assertThat(actualGemFireProperties).containsKeys("cache-xml-file", "name", "groups");
		assertThat(actualGemFireProperties.getProperty("cache-xml-file")).isEqualTo("/path/to/cache.xml");
		assertThat(actualGemFireProperties.getProperty("name")).isEqualTo("TestName");
		assertThat(actualGemFireProperties.getProperty("groups")).isEqualTo("MockGroup,TestGroup");
		assertThat(actualGemFireProperties.getProperty("remote-locators")).isEqualTo("skullbox[12345]");

		verify(mockLogger, times(1))
			.warn(eq("[gemfire.non-existing-property] is not a valid Apache Geode property"));
		verify(mockLogger, times(1))
			.warn(eq("Apache Geode Property [{}] was not set"), eq("mcast-port"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void configureGemFirePropertiesWithNullCacheFactoryBean() {

		try {
			this.configuration.configureGemFireProperties(mock(ConfigurableEnvironment.class), null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CacheFactoryBean must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void configureGemFirePropertiesWithNullEnvironment() {

		try {
			this.configuration.configureGemFireProperties(null, mock(CacheFactoryBean.class));
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Environment must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	static class TestEnvironmentSourcedGemFirePropertiesAutoConfiguration extends EnvironmentSourcedGemFirePropertiesAutoConfiguration {

		@Override
		protected void configureGemFireProperties(@NonNull ConfigurableEnvironment environment,
				@NonNull CacheFactoryBean bean) {

			super.configureGemFireProperties(environment, bean);
		}

		@Override
		protected Logger getLogger() {
			return super.getLogger();
		}
	}
}
