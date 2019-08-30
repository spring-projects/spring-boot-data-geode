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
package org.springframework.geode.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.support.ConnectionEndpointList;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;

/**
 * Unit Tests for {@link EnableClusterAware} and {@link ClusterAwareConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.Spy
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.data.gemfire.support.ConnectionEndpoint
 * @see org.springframework.data.gemfire.support.ConnectionEndpointList
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.config.annotation.ClusterAwareConfiguration
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @since 1.2.0
 */
public class ClusterAwareConfigurationUnitTests extends IntegrationTestsSupport {

	private ClusterAwareConfiguration.ClusterAwareCondition condition =
		spy(new ClusterAwareConfiguration.ClusterAwareCondition());

	@Before @After
	public void setupAndTearDown() {
		System.clearProperty(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);
	}

	@Test
	public void getDefaultConnectionEndpointsIncludesDefaultLocatorAndDefaultServer() {

		assertThat(this.condition.getDefaultConnectionEndpoints().stream()
			.map(ConnectionEndpoint::toString)
			.collect(Collectors.toList()))
			.containsExactly("localhost[40404]", "localhost[10334]");
	}

	@Test
	public void getConfiguredConnectionEndpointsIsCorrect() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = new MutablePropertySources();

		Properties locatorProperties = new Properties();
		Properties serverProperties = new Properties();

		locatorProperties.setProperty("spring.data.gemfire.pool.locators", "boombox[1234], cardboardbox[5678], mailbox[9012]");
		locatorProperties.setProperty("spring.data.gemfire.pool.car.locators", "skullbox[11235]");
		locatorProperties.setProperty("spring.data.gemfire.pool.other-property", "junk");
		serverProperties.setProperty("spring.data.gemfire.pool.servers", "mars[41414]");
		serverProperties.setProperty("spring.data.gemfire.pool.swimming.servers", "jupiter[42424], saturn[43434]");
		serverProperties.setProperty("spring.data.gemfire.pool.other-property", "junk");

		propertySources.addLast(new PropertiesPropertySource("LocatorPoolProperties", locatorProperties));
		propertySources.addFirst(new PropertiesPropertySource("ServerPoolProperties", serverProperties));

		when(mockEnvironment.getPropertySources()).thenReturn(propertySources);

		when(mockEnvironment.getProperty(anyString())).thenAnswer(invocation -> {

			String propertyName = invocation.getArgument(0);

			return locatorProperties.getProperty(propertyName, serverProperties.getProperty(propertyName));
		});

		List<ConnectionEndpoint> connectionEndpoints = this.condition.getConfiguredConnectionEndpoints(mockEnvironment);

		List<String> connectionEndpointStrings = connectionEndpoints.stream()
			.map(ConnectionEndpoint::toString)
			.collect(Collectors.toList());

		assertThat(connectionEndpoints).isNotNull();
		assertThat(connectionEndpoints).hasSize(7);
		assertThat(connectionEndpointStrings)
			.containsExactlyInAnyOrder("mars[41414]", "jupiter[42424]", "saturn[43434]", "boombox[1234]",
				"cardboardbox[5678]", "mailbox[9012]", "skullbox[11235]");

		verify(mockEnvironment, times(1)).getPropertySources();
		verify(mockEnvironment, times(4)).getProperty(anyString());
	}

	@Test
	public void countConnectionsIsCorrect() throws Exception {

		ConnectionEndpointList list = new ConnectionEndpointList(
			new ConnectionEndpoint("boombox", 1234),
			new ConnectionEndpoint("cardboardbox", 5678),
			new ConnectionEndpoint("mailbox", 9012),
			new ConnectionEndpoint("skullbox", 10334)
		);

		Logger mockLogger = mock(Logger.class);

		doReturn(mockLogger).when(this.condition).getLogger();

		doAnswer(invocation -> {

			ConnectionEndpoint connectionEndpoint = invocation.getArgument(0);

			if (Arrays.asList("boombox", "skullbox").contains(connectionEndpoint.getHost())) {
				return mock(Socket.class);
			}
			else {
				throw new IOException("TEST");
			}

		}).when(this.condition).connect(any(ConnectionEndpoint.class));

		assertThat(this.condition.countConnections(list)).isEqualTo(2);
	}

	@Test
	public void configureTopologySetsLocalWhenConnectionCountIsLessThanOneAndEnvironmentDoesNotContainTargetProperty() {

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		ConnectionEndpointList connectionEndpoints =
			new ConnectionEndpointList(new ConnectionEndpoint("localhost", 1234));

		this.condition.configureTopology(mockEnvironment, connectionEndpoints,0);

		assertThat(System.getProperty(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY))
			.isEqualTo("LOCAL");

		verify(mockEnvironment, times(1))
			.containsProperty(eq(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY));
	}

	@Test
	public void configureTopologySetsClientServerWhenConnectionCountIsGreaterThanEqualToOne() {

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		this.condition.configureTopology(mockEnvironment, null,1);

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		verify(mockEnvironment, never())
			.containsProperty(eq(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY));
	}

	@Test
	public void configureTopologySetsClientServerWhenEnvironmentContainsTargetProperty() {

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		when(mockEnvironment.containsProperty(eq(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY)))
			.thenReturn(true);

		this.condition.configureTopology(mockEnvironment, null,0);

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		verify(mockEnvironment, times(1))
			.containsProperty(eq(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY));
	}

	@Test
	public void isMatchReturnsTrue() {
		assertThat(this.condition.isMatch(null, 1)).isTrue();
	}

	@Test
	public void isNotMatchReturnsFalse() {

		ConnectionEndpointList connectionEndpoints =
			ConnectionEndpointList.from(new ConnectionEndpoint("localhost", 1234));

		assertThat(this.condition.isMatch(connectionEndpoints, 0)).isFalse();
	}
}
