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

package org.springframework.geode.core.env;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

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
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.geode.core.env.VcapPropertySource
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
		when(mockVcapPropertySource.getName()).thenReturn("vcap");
		when(mockVcapPropertySource.containsProperty(anyString())).thenReturn(true);

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
			verifyZeroInteractions(mockEnvironment);
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
				.hasMessage("A valid EnumerablePropertySource named [vcap] with VCAP properties is required",
					mockEnvironment);

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockEnvironment, times(1)).getPropertySources();
			verify(propertySources, times(1)).get(eq("vcap"));
			verify(mockPropertySource, times(1)).getName();
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
		when(mockPropertySource.getName()).thenReturn("vcap");
		when(mockPropertySource.containsProperty(eq("vcap.application.name"))).thenReturn(true);
		when(mockPropertySource.containsProperty(eq("vcap.application.uris"))).thenReturn(false);

		try {
			VcapPropertySource.from(mockEnvironment);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected)
				.hasMessage("A valid EnumerablePropertySource named [vcap] with VCAP properties is required",
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

		vcap.setProperty("vcap.application.name", "testApp");
		vcap.setProperty("vcap.application.uris", "boot-app.apps.cloud.net");


		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isInstanceOf(PropertiesPropertySource.class);
		assertThat(propertySource.getProperty("vcap.application.name")).isEqualTo("testApp");
		assertThat(propertySource.getProperty("vcap.application.uris")).isEqualTo("boot-app.apps.cloud.net");
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

		when(mockPropertySource.getName()).thenReturn("vcap");

		when(mockPropertySource.containsProperty(anyString())).thenAnswer(invocation ->
			Arrays.asList(propertyNames).contains(invocation.<String>getArgument(0)));

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

		when(mockPropertySource.getName()).thenReturn("vcap");

		when(mockPropertySource.containsProperty(anyString())).thenAnswer(invocation ->
			Arrays.asList(propertyNames).contains(invocation.<String>getArgument(0)));

		when(mockPropertySource.getPropertyNames()).thenReturn(propertyNames);

		VcapPropertySource propertySource = VcapPropertySource.from(mockPropertySource);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.getSource()).isEqualTo(mockPropertySource);

		Set<String> vcapApplicationProperties = propertySource.findAllVcapServicesProperties();

		assertThat(vcapApplicationProperties).isNotNull();
		assertThat(vcapApplicationProperties).hasSize(5);
		assertThat(vcapApplicationProperties)
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
	public void findFirstCloudCacheServiceNameReturnsServiceName() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.plan", "small");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,database,cloudcache,gemfire");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.jblum-pcc.name", "jblum-pcc");
		vcap.setProperty("vcap.services.jblum-pcc.plan", "small");
		vcap.setProperty("vcap.services.jblum-pcc.tags", "cloudcache,database,gemfire,pivotal");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.a-pcc.name", "a-pcc");
		vcap.setProperty("vcap.services.a-pcc.plan", "huge");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstCloudCacheServiceName()).isEqualTo("jblum-pcc");
	}

	@Test(expected = IllegalStateException.class)
	public void findFirstCloudCacheServiceNameWithInvalidTagsThrowsIllegalStateException() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.plan", "small");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,gemfire,database");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		try {
			propertySource.findFirstCloudCacheServiceName();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("No service with tags [cloudcache, gemfire] was found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void findFirstCloudCacheServiceNameWithNoTagsThrowsIllegalStateException() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.plan", "small");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();

		try {
			propertySource.findFirstCloudCacheServiceName();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("No service with tags [cloudcache, gemfire] was found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void findFirstCloudCacheServiceReturnsCloudCacheService() throws Exception {

		URL gfshUrl = new URL("http://skullbox:7070/v1/gemfire");

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

		CloudCacheService cloudCacheService = propertySource.findFirstCloudCacheService();

		assertThat(cloudCacheService).isNotNull();
		assertThat(cloudCacheService.getGfshUrl().orElse(null)).isEqualTo(gfshUrl);
		assertThat(cloudCacheService.getLocatorList()).containsExactly(
			CloudCacheService.Locator.newLocator("sandbox", 1234),
			CloudCacheService.Locator.newLocator("toolbox", 10334),
			CloudCacheService.Locator.newLocator("xbox", 6789)
		);
	}

	@Test
	public void findFirstUserByRoleClusterOperatorReturnsUser() {

		Properties vcap = new Properties();

		vcap.setProperty("vcap.application.name", "boot-example");
		vcap.setProperty("vcap.services.test-pcc.name", "test-pcc");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].username", "jdoe");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].roles", "developer,poweruser,seaswab");
		vcap.setProperty("vcap.services.test-pcc.credentials.users[0].password", "test");
		vcap.setProperty("vcap.services.test-pcc.tags", "pivotal,cloudcache , database,  gemfire ");
		vcap.setProperty("vcap.application.space_name", "outerspace");
		vcap.setProperty("vcap.services.a-pcc.name", "a-pcc");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].username", "admin");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].roles", "cluster_admin");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[0].password", "p@55w0rd");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].username", "root");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].roles", "cluster_operator");
		vcap.setProperty("vcap.services.a-pcc.credentials.users[1].password", "p@55w0rd");
		vcap.setProperty("vcap.services.a-pcc.tags", "pivotal,cloudcache,database");
		vcap.setProperty("vcap.application.uris", "boot-example.boot-apps.apps.cloud.net");
		vcap.setProperty("vcap.services.jblum-pcc.name", "jblum-pcc");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].username", "majorTom");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].roles", "cluster_operator,ground_contoller");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[0].password", "s3cUr3");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].username", "jimbo");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].roles", "cluster_fuck");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[1].password", "p@55!t");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].username", "buster");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].roles", "cluster_operator");
		vcap.setProperty("vcap.services.jblum-pcc.credentials.users[2].password", "p@55!t");
		vcap.setProperty("vcap.services.jblum-pcc.tags", "pivotal,gemfire,database");

		VcapPropertySource propertySource = VcapPropertySource.from(vcap);

		assertThat(propertySource).isNotNull();
		assertThat(propertySource.findFirstUserByRoleClusterOperator(Service.with("test-pcc")).isPresent()).isFalse();

		User root = propertySource.findFirstUserByRoleClusterOperator(Service.with("a-pcc")).orElse(null);

		assertThat(root).isNotNull();
		assertThat(root.getName()).isEqualTo("root");
		assertThat(root.getPassword().orElse(null)).isEqualTo("p@55w0rd");
		assertThat(root.getRole().orElse(null).isClusterOperator()).isTrue();

		User majorTom = propertySource.findFirstUserByRoleClusterOperator(Service.with("jblum-pcc")).orElse(null);

		assertThat(majorTom).isNotNull();
		assertThat(majorTom.getName()).isEqualTo("majorTom");
		assertThat(majorTom.getPassword().orElse(null)).isEqualTo("s3cUr3");
		assertThat(majorTom.getRole().orElse(null).isClusterOperator()).isTrue();
	}
}
