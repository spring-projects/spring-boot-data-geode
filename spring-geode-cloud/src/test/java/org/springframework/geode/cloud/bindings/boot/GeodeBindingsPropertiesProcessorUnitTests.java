/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.geode.cloud.bindings.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.geode.cloud.bindings.Guards;

/**
 * Unit Tests for {@link GeodeBindingsPropertiesProcessor}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.cloud.bindings.boot.GeodeBindingsPropertiesProcessor
 * @since 1.4.1
 */
public class GeodeBindingsPropertiesProcessorUnitTests {

	@Test
	public void processMapsCloudPropertiesToFrameworkPropertiesCorrectly() {

		String propertyName = String.format(Guards.SPRING_CLOUD_BOOT_BINDINGS_TYPE_ENABLED_PROPERTY,
			GeodeBindingsPropertiesProcessor.TYPE);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true)
			.when(mockEnvironment).getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		Path mockPath= mock(Path.class);

		Map<String, String> secret = MapBuilder.<String, String>newMapBuilder()
			.put(Binding.TYPE, GeodeBindingsPropertiesProcessor.TYPE)
			.put("gemfire.security-username", "DarthVader")
			.put("gemfire.security-password", "s5!thL0rd")
			.put("gemfire.locators", "mustafar[61616]")
			.put("gemfire.http-service-bind-address", "10.100.101.69")
			.put("gemfire.http-service-port", "7070")
			.build();

		Binding testBinding = new Binding("gemfire-test-binding-name", mockPath, secret);

		Bindings testBindings = new Bindings(testBinding);

		GeodeBindingsPropertiesProcessor bindingsPropertiesProcessor = new GeodeBindingsPropertiesProcessor();

		Map<String, Object> properties = new HashMap<>();

		bindingsPropertiesProcessor.process(mockEnvironment, testBindings, properties);

		assertThat(properties)
			.containsEntry("spring.data.gemfire.security.username", secret.get("gemfire.security-username"))
			.containsEntry("spring.data.gemfire.security.password", secret.get("gemfire.security-password"))
			.containsEntry("spring.data.gemfire.pool.locators", secret.get("gemfire.locators"))
			.containsEntry("spring.data.gemfire.management.http.host", secret.get("gemfire.http-service-bind-address"))
			.containsEntry("spring.data.gemfire.management.http.port", secret.get("gemfire.http-service-port"));

		assertThat(Boolean.TRUE.equals(properties.get("spring.data.gemfire.management.require-https"))).isTrue();
		assertThat(Boolean.TRUE.equals(properties.get("spring.data.gemfire.management.use-http"))).isTrue();
		assertThat(Boolean.TRUE.equals(properties.get("spring.data.gemfire.security.ssl.use-default-context"))).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void processDoesNotMapPropertiesWhenGemFireTypeIsDisabled() {

		String propertyName = String.format(Guards.SPRING_CLOUD_BOOT_BINDINGS_TYPE_ENABLED_PROPERTY,
			GeodeBindingsPropertiesProcessor.TYPE);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(false)
			.when(mockEnvironment).getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		Path mockPath= mock(Path.class);

		Binding testBinding = new Binding("gemfire-test-binding-name", mockPath,
			Collections.singletonMap(Binding.TYPE, GeodeBindingsPropertiesProcessor.TYPE));

		Bindings testBindings = new Bindings(testBinding);

		GeodeBindingsPropertiesProcessor bindingsPropertiesProcessor = new GeodeBindingsPropertiesProcessor();

		Map<String, Object> properties = new HashMap<>();

		bindingsPropertiesProcessor.process(mockEnvironment, testBindings, properties);

		assertThat(properties).isEmpty();

		verify(mockEnvironment, times(1))
			.getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void processDoesNotMapPropertiesWhenGemFireTypeIsNotPresent() {

		String propertyName = String.format(Guards.SPRING_CLOUD_BOOT_BINDINGS_TYPE_ENABLED_PROPERTY,
			GeodeBindingsPropertiesProcessor.TYPE);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true)
			.when(mockEnvironment).getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		Path mockPath= mock(Path.class);

		Binding testBinding = new Binding("mock-test-binding-name", mockPath,
			Collections.singletonMap(Binding.TYPE, "mock"));

		Bindings testBindings = new Bindings(testBinding);

		GeodeBindingsPropertiesProcessor bindingsPropertiesProcessor = new GeodeBindingsPropertiesProcessor();

		Map<String, Object> properties = new HashMap<>();

		bindingsPropertiesProcessor.process(mockEnvironment, testBindings, properties);

		assertThat(properties).isEmpty();

		verify(mockEnvironment, times(1))
			.getProperty(eq(propertyName), eq(Boolean.class), eq(true));

		verifyNoMoreInteractions(mockEnvironment);
	}
}
