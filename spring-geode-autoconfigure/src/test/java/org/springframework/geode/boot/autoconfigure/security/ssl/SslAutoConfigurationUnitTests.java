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
package org.springframework.geode.boot.autoconfigure.security.ssl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.geode.boot.autoconfigure.SslAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.SslAutoConfiguration.SslEnvironmentPostProcessor;

/**
 * Unit Tests for {@link SslAutoConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.geode.boot.autoconfigure.SslAutoConfiguration
 * @since 1.0.0
 */
public class SslAutoConfigurationUnitTests {

	@Test
	public void sslConfigurationIsDisabled() {

		SslEnvironmentPostProcessor environmentPostProcessor = new SslEnvironmentPostProcessor();

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.getProperty(eq(SslAutoConfiguration.SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true)))
				.thenReturn(false);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(SslAutoConfiguration.SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, never()).containsProperty(eq("spring.data.gemfire.security.ssl.keystore"));
	}

	@Test
	public void sslConfigurationIsEnabled() {

		SslEnvironmentPostProcessor environmentPostProcessor = spy(new SslEnvironmentPostProcessor());

		doNothing().when(environmentPostProcessor)
			.postProcessEnvironment(any(ConfigurableEnvironment.class), any(SpringApplication.class));

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty(eq("spring.boot.data.gemfire.security.ssl.keystore.name")))
			.thenReturn(true);

		when(mockEnvironment.getProperty("spring.boot.data.gemfire.security.ssl.keystore.name"))
			.thenReturn("non-existing-trusted.keystore");

		when(mockEnvironment.getProperty(eq(SslAutoConfiguration.SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(true)))
				.thenReturn(true);

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(SslAutoConfiguration.SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1))
			.containsProperty(eq("spring.data.gemfire.security.ssl.keystore"));

		verify(mockEnvironment, times(3))
			.containsProperty(eq("spring.boot.data.gemfire.security.ssl.keystore.name"));

		verify(mockEnvironment, times(3))
			.getProperty(eq("spring.boot.data.gemfire.security.ssl.keystore.name"));

		verify(environmentPostProcessor, never())
			.postProcessEnvironment(eq(mockEnvironment), any(SpringApplication.class));
	}

	@Test
	public void sslConfigurationIsEnabledWhenEnabledPropertyNotPresent() {

		SslEnvironmentPostProcessor environmentPostProcessor = new SslEnvironmentPostProcessor();

		ConfigurableEnvironment mockEnvironment = spy(new StandardEnvironment());

		environmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

		verify(mockEnvironment, times(1))
			.getProperty(eq(SslAutoConfiguration.SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(true));

		verify(mockEnvironment, times(1))
			.containsProperty(eq("spring.data.gemfire.security.ssl.keystore"));
	}
}
