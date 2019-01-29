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

package org.springframework.geode.boot.autoconfigure.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration.AutoConfiguredCloudSecurityEnvironmentPostProcessor;
import org.springframework.mock.env.MockEnvironment;

/**
 * Unit Tests for {@link ClientSecurityAutoConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
}
