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
package org.springframework.geode.boot.autoconfigure.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Test;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration.AutoConfiguredCloudSecurityEnvironmentPostProcessor;
import org.springframework.mock.env.MockEnvironment;

/**
 * Unit Tests for {@link ClientSecurityAutoConfiguration}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.MutablePropertySources
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
 * @see org.springframework.mock.env.MockEnvironment
 * @since 1.0.0
 */
public class ClientSecurityAutoConfigurationUnitTests {

	@Test
	public void clientSecurityIsEnabledWhenEnablePropertyIsTrueAndCloudFoundryIsActive() {

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor).configureSecurityContext(any(ConfigurableEnvironment.class));

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(true);

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(true);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_APPLICATION"));

		verify(mockEnvironment, never()).containsProperty(eq("VCAP_SERVICES"));

		verify(environmentPostProcessor, times(1))
			.configureSecurityContext(eq(mockEnvironment));
	}

	@Test
	public void clientSecurityIsEnabledWhenEnablePropertyIsUnsetAndCloudFoundryIsActive() {

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor).configureSecurityContext(any(ConfigurableEnvironment.class));

		ConfigurableEnvironment mockEnvironment = spy(new MockEnvironment());

		doReturn(true).when(mockEnvironment).containsProperty(eq("VCAP_SERVICES"));

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_APPLICATION"));
		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_SERVICES"));

		verify(environmentPostProcessor, times(1)).configureSecurityContext(eq(mockEnvironment));
	}

	@Test
	public void clientSecurityIsDisabledWhenEnablePropertyIsFalseAndCloudFoundryIsActive() {

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor).configureSecurityContext(any(ConfigurableEnvironment.class));

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(true);
		when(mockEnvironment.containsProperty(eq("VCAP_SERVICES"))).thenReturn(true);

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(false);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, never()).containsProperty(eq("VCAP_APPLICATION"));
		verify(mockEnvironment, never()).containsProperty(eq("VCAP_SERVICES"));
		verify(environmentPostProcessor, never()).configureSecurityContext(eq(mockEnvironment));
	}

	@Test
	public void clientSecurityIsDisabledWhenEnablePropertyIsTrueAndCloudFoundryIsInactive() {

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor).configureSecurityContext(any(ConfigurableEnvironment.class));

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(false);
		when(mockEnvironment.containsProperty(eq("VCAP_SERVICES"))).thenReturn(false);

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(true);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_APPLICATION"));
		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_SERVICES"));
		verify(environmentPostProcessor, never()).configureSecurityContext(eq(mockEnvironment));
	}

	@Test
	public void configuresSecurityContext() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		Properties vcapProperties = new Properties();

		vcapProperties.setProperty("vcap.application.name", "TestApp");
		vcapProperties.setProperty("vcap.application.uris", "test-app.apps.cloud.skullbox.com");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.locators", "boombox[10334],skullbox[10334]");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.urls.gfsh", "https://cloud.skullbox.com:8080/gemfire/v1");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.urls.pulse", "https://cloud.skullbox.com:8080/pulse");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].username", "Abuser");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].password", "p@55w0rd");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].roles", "cluster_developer");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].username", "Master");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].password", "p@$$w0rd");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].roles", "cluster_operator");
		vcapProperties.setProperty("vcap.services.test-pcc.tags", "gemfire,cloudcache,test,geode");

		PropertySource vcapPropertySource = new PropertiesPropertySource("vcap", vcapProperties);

		MutablePropertySources propertySources = new MutablePropertySources();

		propertySources.addFirst(vcapPropertySource);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		environmentPostProcessor.configureSecurityContext(mockEnvironment);

		verify(mockEnvironment, times(2)).getPropertySources();

		assertThat(propertySources.contains("boot.data.gemfire.cloudcache")).isTrue();

		PropertySource propertySource = propertySources.get("boot.data.gemfire.cloudcache");

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getName()).isEqualTo("boot.data.gemfire.cloudcache");
		assertThat(propertySource.getProperty("spring.data.gemfire.security.username")).isEqualTo("Master");
		assertThat(propertySource.getProperty("spring.data.gemfire.security.password")).isEqualTo("p@$$w0rd");
		assertThat(propertySource.getProperty("spring.data.gemfire.pool.locators"))
			.isEqualTo("boombox[10334],skullbox[10334]");
		assertThat(propertySource.getProperty("spring.data.gemfire.management.use-http")).isEqualTo("true");
		assertThat(propertySource.getProperty("spring.data.gemfire.management.http.host")).isEqualTo("cloud.skullbox.com");
		assertThat(propertySource.getProperty("spring.data.gemfire.management.http.port")).isEqualTo("8080");
	}

	@Test
	public void configuresSecurityContextWithLocatorsOnly() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty("spring.data.gemfire.security.username")).thenReturn(true);
		when(mockEnvironment.containsProperty("spring.data.gemfire.security.password")).thenReturn(true);

		Properties vcapProperties = new Properties();

		vcapProperties.setProperty("vcap.application.name", "TestApp");
		vcapProperties.setProperty("vcap.application.uris", "test-app.apps.cloud.skullbox.com");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.locators", "boombox[10334],skullbox[10334]");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.urls.pulse", "https://cloud.skullbox.com:8080/pulse");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].username", "Abuser");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].password", "p@55w0rd");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[0].roles", "cluster_developer");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].username", "Master");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].password", "p@$$w0rd");
		vcapProperties.setProperty("vcap.services.test-pcc.credentials.users[1].roles", "cluster_operator");
		vcapProperties.setProperty("vcap.services.test-pcc.tags", "gemfire,cloudcache,test");

		PropertySource vcapPropertySource = new PropertiesPropertySource("vcap", vcapProperties);

		MutablePropertySources propertySources = new MutablePropertySources();

		propertySources.addFirst(vcapPropertySource);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		environmentPostProcessor.configureSecurityContext(mockEnvironment);

		verify(mockEnvironment, times(2)).getPropertySources();

		assertThat(propertySources.contains("boot.data.gemfire.cloudcache")).isTrue();

		PropertySource propertySource = propertySources.get("boot.data.gemfire.cloudcache");

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getName()).isEqualTo("boot.data.gemfire.cloudcache");
		assertThat(propertySource.containsProperty("spring.data.gemfire.security.username")).isFalse();
		assertThat(propertySource.containsProperty("spring.data.gemfire.security.password")).isFalse();
		assertThat(propertySource.getProperty("spring.data.gemfire.pool.locators"))
			.isEqualTo("boombox[10334],skullbox[10334]");
		assertThat(propertySource.containsProperty("spring.data.gemfire.management.use-http")).isFalse();
		assertThat(propertySource.containsProperty("spring.data.gemfire.management.http.host")).isFalse();
		assertThat(propertySource.containsProperty("spring.data.gemfire.management.http.port")).isFalse();
	}

	@Test
	public void configureSecurityContextWhenNoCloudCacheServiceInstanceIsFoundLogsWarning() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "TestApp");
		vcap.setProperty("vcap.application.uris", "test-app.apps.cloud.skullbox.com");

		PropertySource vcapPropertySource = new PropertiesPropertySource("vcap", vcap);

		propertySources.addFirst(vcapPropertySource);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		when(mockEnvironment.getProperty(anyString())).thenReturn(null);

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			new AutoConfiguredCloudSecurityEnvironmentPostProcessor();

		try {

			environmentPostProcessor.configureSecurityContext(mockEnvironment);

			TestAppender testAppender = TestAppender.getInstance();

			assertThat(testAppender).isNotNull();
			assertThat(testAppender.lastLogMessage()).isEqualTo("No CloudCache Service Instance was found");
		}
		finally {

			verify(mockEnvironment, times(1))
				.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_CACHE_SERVICE_INSTANCE_NAME_PROPERTY));
			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, never()).addLast(any(PropertySource.class));
		}
	}

	@Test(expected = IllegalStateException.class)
	public void configureSecurityContextWhenTargetCloudCacheServiceInstanceIsNotFoundThrowsIllegalStateException() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "TestApp");
		vcap.setProperty("vcap.application.uris", "test-app.apps.cloud.skullbox.com");

		PropertySource vcapPropertySource = new PropertiesPropertySource("vcap", vcap);

		propertySources.addFirst(vcapPropertySource);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		when(mockEnvironment.getProperty(ClientSecurityAutoConfiguration.CLOUD_CACHE_SERVICE_INSTANCE_NAME_PROPERTY))
			.thenReturn("test-pcc");

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			new AutoConfiguredCloudSecurityEnvironmentPostProcessor();

		try {
			environmentPostProcessor.configureSecurityContext(mockEnvironment);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("No CloudCache Service Instance with name [test-pcc] was found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockEnvironment, times(1))
				.getProperty(eq(ClientSecurityAutoConfiguration.CLOUD_CACHE_SERVICE_INSTANCE_NAME_PROPERTY));
			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, never()).addLast(any(PropertySource.class));
		}
	}
}
