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
package org.springframework.geode.core.env;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.geode.core.env.support.CloudCacheService;
import org.springframework.geode.core.env.support.Service;
import org.springframework.geode.core.env.support.User;

/**
 * Unit tests for {@link VcapPropertySource}.
 *
 * @author John Blum
 * @see java.net.URL
 * @see java.util.Properties
 * @see java.util.function.Predicate
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.EnumerablePropertySource
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.MutablePropertySources
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.geode.core.env.VcapPropertySource
 * @see org.springframework.geode.core.env.support.CloudCacheService
 * @see org.springframework.geode.core.env.support.Service
 * @see org.springframework.geode.core.env.support.User
 * @since 1.0.0
 */
public class VcapPropertySourceUnitTests {

	@Test
	public void fromEnvironmentIsSuccessful() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		PropertySource mockVcapPropertySource = mock(EnumerablePropertySource.class);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		doReturn(mockVcapPropertySource).when(propertySources).get(eq("vcap"));
		when(mockVcapPropertySource.containsProperty(anyString())).thenReturn(true);
		when(mockVcapPropertySource.getName()).thenReturn("vcap");

		VcapPropertySource propertySource = VcapPropertySource.from(mockEnvironment);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isEqualTo(mockVcapPropertySource);

		verify(mockEnvironment, times(1)).getPropertySources();
		verify(propertySources, times(1)).get(eq("vcap"));
		verify(mockVcapPropertySource, times(1))
			.containsProperty(eq("vcap.application.name"));
		verify(mockVcapPropertySource, times(1))
			.containsProperty(eq("vcap.application.uris"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromNonConfigurableEnvironmentThrowsIllegalArgumentException() {

		Environment mockEnvironment = mock(Environment.class);

		try {
			VcapPropertySource.from(mockEnvironment);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected)
				.hasMessage("Environment was not configurable or does not contain an enumerable [vcap] PropertySource");

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockEnvironment);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromConfigurableEnvironmentWithNoVcapPropertySourceThrowsIllegalArgumentException() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		doReturn(null).when(propertySources).get(anyString());

		try {
			VcapPropertySource.from(mockEnvironment);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected)
				.hasMessage("Environment was not configurable or does not contain an enumerable [vcap] PropertySource");

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, times(1)).get(eq("vcap"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromConfigurableEnvironmentWithNonEnumerableVcapPropertySourceThrowsIllegalArgumentException() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		PropertySource mockPropertySource = mock(PropertySource.class);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		doReturn(mockPropertySource).when(propertySources).get(eq("vcap"));
		when(mockPropertySource.getName()).thenReturn("vcap");

		try {
			VcapPropertySource.from(mockEnvironment);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected)
				.hasMessage("An EnumerablePropertySource named [vcap] containing VCAP properties is required",
					mockEnvironment);

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, times(1)).get(eq("vcap"));
			verify(mockPropertySource, times(1)).getName();
			verify(mockPropertySource, times(1))
				.containsProperty(eq("vcap.application.name"));
			verifyNoMoreInteractions(mockPropertySource);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromConfigurableEnvironmentWithEnumerableVcapPropertySourceHavingNoRequiredPropertiesThrowsIllegalArgumentException() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = spy(new MutablePropertySources());

		PropertySource mockPropertySource = mock(EnumerablePropertySource.class);

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);
		doReturn(mockPropertySource).when(propertySources).get(eq("vcap"));
		when(mockPropertySource.containsProperty(eq("vcap.application.name"))).thenReturn(true);
		when(mockPropertySource.containsProperty(eq("vcap.application.uris"))).thenReturn(false);
		when(mockPropertySource.getName()).thenReturn("vcap");

		try {
			VcapPropertySource.from(mockEnvironment);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage(
				"An EnumerablePropertySource named [vcap] containing VCAP properties is required",
					mockEnvironment);

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, times(1)).get(eq("vcap"));
			verify(mockPropertySource, times(1)).getName();
			verify(mockPropertySource, times(1))
				.containsProperty(eq("vcap.application.name"));
			verify(mockPropertySource, times(1))
				.containsProperty(eq("vcap.application.uris"));
		}
	}

	@Test
	public void fromPropertiesIsSuccessful() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "TestApp");
		vcap.setProperty("vcap.application.uris", "test-app.boot-app.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isInstanceOf(PropertiesPropertySource.class);
		assertThat(propertySource.getProperty("vcap.application.name")).isEqualTo("TestApp");
		assertThat(propertySource.getProperty("vcap.application.uris")).isEqualTo("test-app.boot-app.apps.cloud.net");
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromPropertiesHavingNoRequiredProperties() {

		try {
			VcapPropertySource.from(new Properties());
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Properties are required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromNullPropertiesThrowsIllegalArgumentException() {

		try {
			VcapPropertySource.from((Properties) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Properties are required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void findAllVcapApplicationPropertiesIsSuccessful() {

		EnumerablePropertySource mockPropertySource = mock(EnumerablePropertySource.class);

		String[] propertyNames = {
			"vcap.services.jblum-pcc.credentials.locators",
			"vcap.services.jblum-pcc.credentials.users",
			"vcap.application.host",
			"vcap.application.name",
			"vcap.services.jblum-pcc.name",
			"vcap.application.port",
			"vcap.application.space_name",
			"vcap.services.jblum-pcc.plan",
			"vcap.application.uris",
			"vcap.services.jblum-pcc.tags"
		};

		when(mockPropertySource.containsProperty(anyString())).thenAnswer(invocation ->
			Arrays.asList(propertyNames).contains(invocation.<String>getArgument(0)));

		when(mockPropertySource.getName()).thenReturn("vcap");
		when(mockPropertySource.getPropertyNames()).thenReturn(propertyNames);

		VcapPropertySource propertySource = VcapPropertySource.from(mockPropertySource);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isEqualTo(mockPropertySource);

		Set<String> vcapApplicationProperties = propertySource.findAllVcapApplicationProperties();

		assertThat(vcapApplicationProperties).isNotNull();
		assertThat(vcapApplicationProperties).hasSize(5);
		assertThat(vcapApplicationProperties)
			.containsExactlyInAnyOrder("vcap.application.host", "vcap.application.name", "vcap.application.port",
				"vcap.application.space_name", "vcap.application.uris");

		verify(mockPropertySource, times(1)).getName();
		verify(mockPropertySource, times(1))
			.containsProperty(eq("vcap.application.name"));
		verify(mockPropertySource, times(1))
			.containsProperty(eq("vcap.application.uris"));
		verify(mockPropertySource, times(1)).getPropertyNames();
	}

	@Test
	public void findAllVcapServicesPropertiesIsSuccessful() {

		EnumerablePropertySource mockPropertySource = mock(EnumerablePropertySource.class);

		String[] propertyNames = {
			"vcap.services.jblum-pcc.credentials.locators",
			"vcap.services.jblum-pcc.credentials.users",
			"vcap.application.host",
			"vcap.application.name",
			"vcap.services.jblum-pcc.name",
			"vcap.application.port",
			"vcap.application.space_name",
			"vcap.services.jblum-pcc.plan",
			"vcap.application.uris",
			"vcap.services.jblum-pcc.tags"
		};

		when(mockPropertySource.containsProperty(anyString())).thenAnswer(invocation ->
			Arrays.asList(propertyNames).contains(invocation.<String>getArgument(0)));

		when(mockPropertySource.getName()).thenReturn("vcap");
		when(mockPropertySource.getPropertyNames()).thenReturn(propertyNames);

		VcapPropertySource propertySource = VcapPropertySource.from(mockPropertySource);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isEqualTo(mockPropertySource);

		Set<String> vcapServicesProperties = propertySource.findAllVcapServicesProperties();

		assertThat(vcapServicesProperties).isNotNull();
		assertThat(vcapServicesProperties).hasSize(5);
		assertThat(vcapServicesProperties)
			.containsExactlyInAnyOrder("vcap.services.jblum-pcc.credentials.locators",
				"vcap.services.jblum-pcc.credentials.users", "vcap.services.jblum-pcc.name",
					"vcap.services.jblum-pcc.plan", "vcap.services.jblum-pcc.tags");

		verify(mockPropertySource, times(1)).getName();
		verify(mockPropertySource, times(1))
			.containsProperty(eq("vcap.application.name"));
		verify(mockPropertySource, times(1))
			.containsProperty(eq("vcap.application.uris"));
		verify(mockPropertySource, times(1)).getPropertyNames();
	}

	@Test
	public void findTargetVcapServicePropertiesIsSuccessful() {

		EnumerablePropertySource mockPropertySource = mock(EnumerablePropertySource.class);

		String[] propertyNames = {
			"vcap.services.jblum-pcc.credentials.locators",
			"vcap.services.jblum-pcc.credentials.users",
			"vcap.application.host",
			"vcap.services.test-pcc.credentials.locators",
			"vcap.services.test-pcc.credentials.users",
			"vcap.application.name",
			"vcap.services.jblum-pcc.name",
			"vcap.services.test-pcc.name",
			"vcap.application.port",
			"vcap.services.jblum-pcc.plan",
			"vcap.application.space_name",
			"vcap.services.test-pcc.plan",
			"vcap.application.uris",
			"vcap.services.jblum-pcc.tags",
			"vcap.services.test-pcc.tags"
		};

		when(mockPropertySource.containsProperty(anyString())).thenAnswer(invocation ->
			Arrays.asList(propertyNames).contains(invocation.getArgument(0, String.class)));

		when(mockPropertySource.getName()).thenReturn("vcap");
		when(mockPropertySource.getPropertyNames()).thenReturn(propertyNames);

		VcapPropertySource propertySource = VcapPropertySource.from(mockPropertySource);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isEqualTo(mockPropertySource);

		Predicate<String> testPccServicePredicate = propertyName -> propertyName.contains("test-pcc");

		Set<String> testPccServicePropertyNames =
			propertySource.findTargetVcapServiceProperties(testPccServicePredicate);

		assertThat(testPccServicePropertyNames).isNotNull();

		assertThat(testPccServicePropertyNames)
			.describedAs("PCC Service Properties [%s]", testPccServicePropertyNames)
			.hasSize(5);

		assertThat(testPccServicePropertyNames)
			.containsExactlyInAnyOrder("vcap.services.test-pcc.credentials.locators",
				"vcap.services.test-pcc.credentials.users", "vcap.services.test-pcc.name",
					"vcap.services.test-pcc.plan", "vcap.services.test-pcc.tags");

		verify(mockPropertySource, times(1)).getName();
		verify(mockPropertySource, times(1)).containsProperty(eq("vcap.application.name"));
		verify(mockPropertySource, times(1)).containsProperty(eq("vcap.application.uris"));
		verify(mockPropertySource, times(1)).getPropertyNames();
	}

	@Test
	public void findFirstCloudCacheServiceReturnsOptionalOfCloudCacheService() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,cloudcache,database,gemfire,junk");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		Optional<CloudCacheService> cloudCacheService = propertySource.findFirstCloudCacheService();

		assertThat(cloudCacheService).isNotNull();
		assertThat(cloudCacheService.isPresent()).isTrue();
		assertThat(cloudCacheService.map(CloudCacheService::getName).orElse(null)).isEqualTo("test-pcc");
		assertThat(cloudCacheService.flatMap(CloudCacheService::getLocators).isPresent()).isFalse();
		assertThat(cloudCacheService.flatMap(CloudCacheService::getGfshUrl).isPresent()).isFalse();
	}

	@Test
	public void findFirstCloudCacheServiceReturnsEmptyOptional() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-postresql.name", "TestPostreSQLDatabase");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.test-postresql.tags", "pivotal, database, postresql");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstCloudCacheService().isPresent()).isFalse();
	}

	@Test
	public void requireFirstCloudCacheServiceReturnsCloudCacheService() throws Exception {

		URL gfshUrl = new URL("https://skullbox:7070/v1/gemfire");

		Properties vcap = new Properties();

		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.plan", "huge");
		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.credentials.locators", "sandbox[1234],toolbox,xbox[6789]");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.test-pcc.credentials.urls.gfsh", gfshUrl.toExternalForm());
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,cloudcache , database,  gemfire ");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		CloudCacheService cloudCacheService = propertySource.requireFirstCloudCacheService();

		assertThat(cloudCacheService).isNotNull();
		assertThat(cloudCacheService.getName()).isEqualTo("test-pcc");
		assertThat(cloudCacheService.getGfshUrl().orElse(null)).isEqualTo(gfshUrl);
		assertThat(cloudCacheService.isTlsEnabled()).isFalse();
		assertThat(cloudCacheService.getLocatorList()).containsExactly(
			CloudCacheService.Locator.newLocator("sandbox", 1234),
			CloudCacheService.Locator.newLocator("toolbox", 10334),
			CloudCacheService.Locator.newLocator("xbox", 6789)
		);
	}

	@Test(expected = IllegalStateException.class)
	public void requireFirstCloudCacheServiceWhenNotFoundThrowsIllegalStateException() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-postresql.name", "TestPostreSQLDatabase");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.test-postresql.tags", "pivotal, database, postresql");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		try {
			propertySource.requireFirstCloudCacheService();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Unable to resolve a CloudCache Service Instance");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void findFirstCloudCacheServiceNameReturnsOptionalOfServiceName() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.plan", "small");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,database,cloudcache,gemfire");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.jblum-pcc.name", "jblum-pcc");
		vcap.setProperty("vcap.services.jblum-pcc.plan", "medium");
		vcap.setProperty("vcap.services.jblum-pcc.tags", "cloudcache,database,gemfire,pivotal");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.mock-pcc.name", "mock-pcc");
		vcap.setProperty("vcap.services.mock-pcc.plan", "large");
		vcap.setProperty("vcap.services.mock-pcc.tags", "pivotal,cloudcache,database");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstCloudCacheServiceName().orElse(null)).isEqualTo("jblum-pcc");
	}

	@Test
	public void findFirstCloudCacheServiceNameReturnsEmptyOptional() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.a-pcc.name", "a-pcc");
		vcap.setProperty("vcap.services.a-pcc.plan", "large");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.services.b-pcc.name", "b-pcc");
		vcap.setProperty("vcap.services.b-pcc.plan", "small");
		vcap.setProperty("vcap.services.b-pcc.tags", "pivotal,database,gemfire");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstCloudCacheServiceName().isPresent()).isFalse();
	}

	@Test
	public void requireFirstCloudCacheServiceNameReturnsServiceName() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.a-pcc.name", "a-pcc");
		vcap.setProperty("vcap.services.a-pcc.plan", "large");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.services.b-pcc.name", "b-pcc");
		vcap.setProperty("vcap.services.b-pcc.plan", "small");
		vcap.setProperty("vcap.services.b-pcc.tags", "pivotal,database,gemfire");
		vcap.setProperty("vcap.services.c-pcc.name", "c-pcc");
		vcap.setProperty("vcap.services.c-pcc.plan", "medium");
		vcap.setProperty("vcap.services.c-pcc.tags", "pivotal,cloudcache,database,gemfire");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstCloudCacheServiceName().orElse(null)).isEqualTo("c-pcc");
	}

	@Test(expected = IllegalStateException.class)
	public void requireFirstCloudCacheServiceNameWithInvalidTagsThrowsIllegalStateException() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.c-pcc.name", "c-pcc");
		vcap.setProperty("vcap.services.c-pcc.plan", "small");
		vcap.setProperty("vcap.services.c-pcc.tags", "pivotal,database,gemfire");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.b-pcc.name", "b-pcc");
		vcap.setProperty("vcap.services.b-pcc.plan", "large");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		try {
			propertySource.requireFirstCloudCacheServiceName();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("No service with tags [cloudcache, gemfire] was found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void findUserByNameReturnsOptionalOfUser() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.jblum-pcc.name", "jblum-pcc");
		vcap.setProperty("vcap.services.jblum-pcc.tags", "pivotal,cloudcache,database,gemfire");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users", "admin, root, majorTom, jimbo, buster");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].username", "admin");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].roles", "cluster_admin");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].password", "p@55w0rd");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].username", "root");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].roles", "cluster_operator");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].password", "p@55w0rd");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].username", "majorTom");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].roles", "cluster_operator,ground_controller");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].password", "s3cUr3");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[3].username", "jimbo");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[3].roles", "cluster_fuck");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[3].password", "p@55!t");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[4].username", "buster");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[4].roles", "cluster_operator");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[4].password", "p@55!t");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		Service jblumPcc = Service.with("jblum-pcc");
		Service nonExistingService = Service.with("non-existing-service");

		Optional<User> user = propertySource.findUserByName(jblumPcc, "majorTom");

		assertThat(user).isNotNull();
		assertThat(user.map(User::getName).orElse(null)).isEqualTo("majorTom");
		assertThat(user.flatMap(User::getPassword).orElse(null)).isEqualTo("s3cUr3");

		user = propertySource.findUserByName(jblumPcc, "admin");

		assertThat(user).isNotNull();
		assertThat(user.map(User::getName).orElse(null)).isEqualTo("admin");
		assertThat(user.flatMap(User::getPassword).orElse(null)).isEqualTo("p@55w0rd");

		user = propertySource.findUserByName(jblumPcc, "nonExistingUser");

		assertThat(user).isNotNull();
		assertThat(user.isPresent()).isFalse();

		user = propertySource.findUserByName(nonExistingService, "root");

		assertThat(user).isNotNull();
		assertThat(user.isPresent()).isFalse();


		user = propertySource.findUserByName(jblumPcc, "buster");

		assertThat(user).isNotNull();
		assertThat(user.map(User::getName).orElse(null)).isEqualTo("buster");
		assertThat(user.flatMap(User::getPassword).orElse(null)).isEqualTo("p@55!t");
	}

	private void testFindUserByInvalidNameThrowsIllegalArgumentException(String targetUsername) {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		try {
			propertySource.findUserByName(Service.with("test-service"), targetUsername);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Target username [%s] is required", targetUsername);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void findUserByBlankName() {
		testFindUserByInvalidNameThrowsIllegalArgumentException("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void findUserByEmptyName() {
		testFindUserByInvalidNameThrowsIllegalArgumentException("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void findUserByNullName() {
		testFindUserByInvalidNameThrowsIllegalArgumentException(null);
	}

	@Test
	public void findFirstUserByRoleClusterOperatorReturnsOptionalOfUser() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,cloudcache , database,  gemfire ");
		vcap.setProperty("vcap.services.test-pcc.credentials.users", "jdoe");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].username", "jdoe");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].roles", "developer,poweruser,seaswab");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].password", "test");
		vcap.setProperty("vcap.services.a-pcc.name", "a-pcc");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.services.a-pcc.credentials.users", "admin, root");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].username", "admin");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].roles", "cluster_admin");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].password", "p@55w0rd");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].username", "root");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].roles", "cluster_operator");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].password", "p@55w0rd");
		vcap.setProperty("vcap.services.jblum-pcc.name", "jblum-pcc");
		vcap.setProperty("vcap.services.jblum-pcc.tags", "pivotal,gemfire,database");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users", "majorTom, jimbo, buster");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].username", "majorTom");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].roles", "cluster_operator,ground_controller");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].password", "s3cUr3");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].username", "jimbo");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].roles", "cluster_fuck");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].password", "p@55!t");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].username", "buster");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].roles", "cluster_operator");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].password", "p@55!t");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstUserByRoleClusterOperator(Service.with("test-pcc")).isPresent()).isFalse();
		assertThat(propertySource.findFirstUserByRoleClusterOperator(Service.with("non-existing-service")).isPresent())
			.isFalse();

		User root = propertySource.findFirstUserByRoleClusterOperator(Service.with("a-pcc")).orElse(null);

		assertThat(root).isNotNull();
		assertThat(root.getName()).isEqualTo("root");
		assertThat(root.getPassword().orElse(null)).isEqualTo("p@55w0rd");
		assertThat(root.getRole().map(User.Role::isClusterOperator).orElse(false)).isTrue();

		User majorTom = propertySource.findFirstUserByRoleClusterOperator(Service.with("jblum-pcc")).orElse(null);

		assertThat(majorTom).isNotNull();
		assertThat(majorTom.getName()).isEqualTo("majorTom");
		assertThat(majorTom.getPassword().orElse(null)).isEqualTo("s3cUr3");
		assertThat(majorTom.getRole().map(User.Role::isClusterOperator).orElse(false)).isTrue();
	}

	@Test
	public void cloudCacheServiceConfiguredWithTlsDisabled() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.test-cloudcache.name", "TestCloudCache");
		vcap.setProperty("vcap.services.test-cloudcache.tags", "pivotal, cloudcache, database, gemfire");
		vcap.setProperty("vcap.services.test-cloudcache.credentials.tls-enabled", "false");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		CloudCacheService testCloudCacheService = propertySource.requireFirstCloudCacheService();

		assertThat(testCloudCacheService).isNotNull();
		assertThat(testCloudCacheService.getName()).isEqualTo("test-cloudcache");
		assertThat(testCloudCacheService.isTlsEnabled()).isFalse();
	}

	@Test
	public void cloudCacheServiceConfiguredWithTlsEnabled() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.test-cloudcache.name", "TestCloudCache");
		vcap.setProperty("vcap.services.test-cloudcache.tags", "pivotal, cloudcache, database, gemfire");
		vcap.setProperty("vcap.services.test-cloudcache.credentials.tls-enabled", "true");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		CloudCacheService testCloudCacheService = propertySource.requireFirstCloudCacheService();

		assertThat(testCloudCacheService).isNotNull();
		assertThat(testCloudCacheService.getName()).isEqualTo("test-cloudcache");
		assertThat(testCloudCacheService.isTlsEnabled()).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withMockVcapServicePredicateConfiguresVcapServicePredicateReturnsThis() {

		Properties properties = new Properties();

		properties.setProperty("vcap.application.name", "withMockVcapServicePredicateConfiguresVcapServicePredicateReturnsThis");
		properties.setProperty("vcap.application.uris", "{}");

		Predicate<String> mockVcapServicePredicate = mock(Predicate.class);

		VcapPropertySource propertySource = VcapPropertySource.from(properties);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getVcapServicePredicate()).isNotEqualTo(mockVcapServicePredicate);
		assertThat(propertySource.withVcapServicePredicate(mockVcapServicePredicate)).isEqualTo(propertySource);
		assertThat(propertySource.getVcapServicePredicate()).isEqualTo(mockVcapServicePredicate);
	}

	@Test
	public void withNullVcapServicePredicateAllowsAndSetsNullReturnsThis() {

		Properties properties = new Properties();

		properties.setProperty("vcap.application.name", "withNullVcapServicePredicateAllowsAndSetsNullReturnsThis");
		properties.setProperty("vcap.application.uris", "{}");

		VcapPropertySource propertySource = VcapPropertySource.from(properties);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getVcapServicePredicate()).isNotNull();
		assertThat(propertySource.withVcapServicePredicate(null)).isEqualTo(propertySource);
		assertThat(propertySource.getVcapServicePredicate()).isNotNull();
	}

	private void testWithInvalidVcapServiceName(String serviceName) {

		Properties properties = new Properties();

		properties.setProperty("vcap.application.name", "testWithInvalidVcapServiceName");
		properties.setProperty("vcap.application.uris", "{}");

		VcapPropertySource propertySource = VcapPropertySource.from(properties);

		assertThat(propertySource).isNotNull();

		propertySource.withVcapServiceName(serviceName);

		fail("Expected VcapPropertySource.withServiceName(%s) to fail", serviceName);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withBlankVcapServiceNameThrowsIllegalArgumentException() {
		testWithInvalidVcapServiceName("    ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withEmptyVcapServiceNameThrowsIllegalArgumentException() {
		testWithInvalidVcapServiceName("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNullVcapServiceNameThrowsIllegalArgumentException() {
		testWithInvalidVcapServiceName(null);
	}

	@Test
	public void withValidVcapSeviceNameConfiguresVcapServicePredicateReturnsThis() {

		Properties properties = new Properties();

		properties.setProperty("vcap.application.name", "withValidVcapSeviceNameConfiguresVcapServicePredicateReturnsThis");
		properties.setProperty("vcap.application.uris", "{}");

		VcapPropertySource propertySource = VcapPropertySource.from(properties);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.withVcapServiceName("test-pcc")).isEqualTo(propertySource);

		Predicate<String> vcapServicePredicate = propertySource.getVcapServicePredicate();

		assertThat(vcapServicePredicate).isNotNull();
		assertThat(vcapServicePredicate.test("vcap.services.test-pcc.name")).isTrue();
		assertThat(vcapServicePredicate.test("vcap.services.test-pcc.tags")).isTrue();
		assertThat(vcapServicePredicate.test("vcap.application.test-pcc.name")).isFalse();
		assertThat(vcapServicePredicate.test("vcap.test-pcc.name")).isFalse();
		assertThat(vcapServicePredicate.test("test-pcc.name")).isFalse();
		assertThat(vcapServicePredicate.test("test-pcc")).isFalse();
		assertThat(vcapServicePredicate.test("vcap.services.junk-pcc.name")).isFalse();
	}
}
