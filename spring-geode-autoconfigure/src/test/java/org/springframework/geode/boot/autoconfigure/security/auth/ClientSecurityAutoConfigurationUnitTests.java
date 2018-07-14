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

import org.junit.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.ClientSecurityAutoConfiguration.AutoConfiguredCloudSecurityEnvironmentPostProcessor;

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

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(true);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(true);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
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

		ConfigurableEnvironment environment = spy(new StandardEnvironment());

		doReturn(true).when(environment).containsProperty(eq("VCAP_SERVICES"));

		environmentPostProcessor.postProcessEnvironment(environment, null);

		verify(environment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(environment, times(1)).containsProperty(eq("VCAP_APPLICATION"));
		verify(environment, times(1)).containsProperty(eq("VCAP_SERVICES"));

		verify(environmentPostProcessor, times(1))
			.configureSecurityContext(eq(environment));
	}

	@Test
	public void clientSecurityIsDisabledWhenEnablePropertyIsFalseAndCloudFoundryIsActive() {

		AutoConfiguredCloudSecurityEnvironmentPostProcessor environmentPostProcessor =
			spy(new AutoConfiguredCloudSecurityEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor).configureSecurityContext(any(ConfigurableEnvironment.class));

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(false);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(true);
		when(mockEnvironment.containsProperty(eq("VCAP_SERVICES"))).thenReturn(true);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
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

		when(mockEnvironment.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true))).thenReturn(true);

		when(mockEnvironment.containsProperty(eq("VCAP_APPLICATION"))).thenReturn(false);
		when(mockEnvironment.containsProperty(eq("VCAP_SERVICES"))).thenReturn(false);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(ClientSecurityAutoConfiguration.SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_APPLICATION"));
		verify(mockEnvironment, times(1)).containsProperty(eq("VCAP_SERVICES"));
		verify(environmentPostProcessor, never()).configureSecurityContext(eq(mockEnvironment));
	}
}
