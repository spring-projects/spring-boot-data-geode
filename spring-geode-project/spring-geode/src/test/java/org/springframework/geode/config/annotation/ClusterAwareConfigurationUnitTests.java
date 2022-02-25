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
package org.springframework.geode.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.SocketFactory;

import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.support.ConnectionEndpointList;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration.PoolConnectionEndpoint;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration.SocketCreationException;
import org.springframework.lang.NonNull;
import org.springframework.mock.env.MockEnvironment;

import org.slf4j.Logger;

/**
 * Unit Tests for {@link EnableClusterAware} and {@link ClusterAwareConfiguration}.
 *
 * @author John Blum
 * @see java.net.InetSocketAddress
 * @see java.net.Socket
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.Spy
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.SocketFactory
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.MutablePropertySources
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.data.gemfire.support.ConnectionEndpoint
 * @see org.springframework.data.gemfire.support.ConnectionEndpointList
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.config.annotation.ClusterAwareConfiguration
 * @see org.springframework.geode.config.annotation.ClusterAwareConfiguration.PoolConnectionEndpoint
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @since 1.2.0
 */
public class ClusterAwareConfigurationUnitTests extends IntegrationTestsSupport {

	private ClusterAwareConfiguration.ClusterAwareCondition condition =
		spy(new ClusterAwareConfiguration.ClusterAwareCondition());

	private @NonNull InetSocketAddress newSocketAddress(@NonNull String host, int port) {
		return new InetSocketAddress(host, port);
	}

	@Before @After
	public void setupAndTearDown() {
		System.clearProperty(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Test
	public void matchesCallsIsMatchThenDoCachedMatchCorrectly() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		doReturn(false).when(this.condition).isMatch(eq(mockConditionContext));
		doReturn(true).when(this.condition).doCachedMatch(eq(mockConditionContext));
		doReturn(false).when(this.condition).isStrictMatch(eq(mockConditionContext), any());

		assertThat(this.condition.matches(mockConditionContext, null)).isTrue();

		InOrder order = inOrder(this.condition);

		order.verify(this.condition, times(1)).isMatch(eq(mockConditionContext));
		order.verify(this.condition, times(1)).doCachedMatch(eq(mockConditionContext));
		order.verify(this.condition, times(1)).isStrictMatch(eq(mockConditionContext), any());

		verifyNoInteractions(mockConditionContext);
	}

	@Test
	public void matchesCallsIsMatchReturningTrueWillNotCallDoCachedMatchCorrectly() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		doReturn(true).when(this.condition).isMatch(eq(mockConditionContext));
		doReturn(false).when(this.condition).isStrictMatch(eq(mockConditionContext), any());

		assertThat(this.condition.matches(mockConditionContext, null)).isTrue();

		InOrder order = inOrder(this.condition);

		order.verify(this.condition, times(1)).isMatch(eq(mockConditionContext));
		order.verify(this.condition, never()).doCachedMatch(any());
		order.verify(this.condition, times(1)).isStrictMatch(eq(mockConditionContext), any());

		verifyNoInteractions(mockConditionContext);
	}

	@Test
	public void isMatchQueriesEnvironmentReturnsTrue() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(mockEnvironment).when(mockConditionContext).getEnvironment();
		doReturn(true).when(mockEnvironment)
			.getProperty(eq(ClusterAwareConfiguration.SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY),
				eq(Boolean.class), eq(ClusterAwareConfiguration.DEFAULT_CLUSTER_AWARE_CONDITION_MATCH));

		assertThat(this.condition.isMatch(mockConditionContext)).isTrue();

		verify(mockConditionContext, times(1)).getEnvironment();
		verify(mockEnvironment, times(1))
			.getProperty(eq(ClusterAwareConfiguration.SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY),
				eq(Boolean.class), eq(ClusterAwareConfiguration.DEFAULT_CLUSTER_AWARE_CONDITION_MATCH));

		verifyNoMoreInteractions(mockConditionContext, mockEnvironment);
	}

	@Test
	public void isMatchIsNullSafe() {
		assertThat(this.condition.isMatch(null)).isFalse();
	}

	@Test
	public void doCachedMatchRegistersApplicationListenerCallsDoMatchAndCachesTheResultCorrectly() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		doReturn(mockConditionContext).when(this.condition).registerApplicationListener(eq(mockConditionContext));
		doReturn(true).when(this.condition).doMatch(eq(mockConditionContext));

		assertThat(this.condition.doCachedMatch(mockConditionContext)).isTrue();
		assertThat(this.condition.doCachedMatch(mockConditionContext)).isTrue();

		InOrder order = inOrder(this.condition);

		order.verify(this.condition, times(1)).registerApplicationListener(eq(mockConditionContext));
		order.verify(this.condition, times(1)).doMatch(eq(mockConditionContext));

		verifyNoInteractions(mockConditionContext);
	}

	@Test
	public void registersApplicationListenerCorrectly() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		doReturn(mockApplicationContext).when(mockConditionContext).getResourceLoader();

		assertThat(this.condition.registerApplicationListener(mockConditionContext)).isEqualTo(mockConditionContext);

		verify(mockConditionContext, times(1)).getResourceLoader();
		verify(mockApplicationContext, times(1))
			.addApplicationListener(isA(ApplicationListener.class));

		verifyNoMoreInteractions(mockConditionContext, mockApplicationContext);
	}

	@Test
	public void doMatchCallsGetConnectionEndpointsThenCountConnectionsThenConfigureTopologyAndIsMatchCorrectly() {

		ConditionContext mockConditionContext = mock(ConditionContext.class);

		ConnectionEndpointList mockConnectionEndpointList = mock(ConnectionEndpointList.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(mockEnvironment).when(mockConditionContext).getEnvironment();
		doReturn(mockConnectionEndpointList).when(this.condition).getConnectionEndpoints(eq(mockEnvironment));
		doReturn(1).when(this.condition).countConnections(eq(mockConnectionEndpointList));

		assertThat(this.condition.doMatch(mockConditionContext)).isTrue();

		InOrder order = inOrder(this.condition, mockConditionContext, mockConnectionEndpointList, mockEnvironment);

		order.verify(this.condition, times(1)).doMatch(eq(mockConditionContext));
		order.verify(mockConditionContext, times(1)).getEnvironment();
		order.verify(this.condition, times(1)).getConnectionEndpoints(eq(mockEnvironment));
		order.verify(this.condition, times(1)).countConnections(eq(mockConnectionEndpointList));
		order.verify(this.condition, times(1))
			.configureTopology(eq(mockEnvironment), eq(mockConnectionEndpointList), eq(1));
		order.verify(this.condition, times(1)).isMatch(eq(mockConnectionEndpointList), eq(1));

		verify(this.condition, atLeastOnce()).getLogger();

		verifyNoMoreInteractions(mockConditionContext);

		verifyNoInteractions(mockConnectionEndpointList, mockEnvironment);
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

	@Test
	public void getConnectionEndpointsCollectsDefaultsConfiguredAndPooledConnections() {

		ConnectionEndpoint defaultConnectionEndpoint = ConnectionEndpoint.parse("default[1234]");
		ConnectionEndpoint configuredConnectionEndpoint = ConnectionEndpoint.parse("configured[5678]");
		ConnectionEndpoint pooledConnectionEndpoint = ConnectionEndpoint.parse("pooled[9012]");

		Environment mockEnvironment = mock(Environment.class);

		doReturn(Collections.singletonList(defaultConnectionEndpoint))
			.when(this.condition).getDefaultConnectionEndpoints(eq(mockEnvironment));

		doReturn(Collections.singletonList(configuredConnectionEndpoint))
			.when(this.condition).getConfiguredConnectionEndpoints(eq(mockEnvironment));

		doReturn(Collections.singletonList(pooledConnectionEndpoint))
			.when(this.condition).getPooledConnectionEndpoints(eq(mockEnvironment));

		assertThat(this.condition.getConnectionEndpoints(mockEnvironment))
			.containsExactly(defaultConnectionEndpoint, configuredConnectionEndpoint, pooledConnectionEndpoint);

		verify(this.condition, times(1)).getDefaultConnectionEndpoints(eq(mockEnvironment));
		verify(this.condition, times(1)).getConfiguredConnectionEndpoints(eq(mockEnvironment));
		verify(this.condition, times(1)).getPooledConnectionEndpoints(eq(mockEnvironment));

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	public void getDefaultConnectionEndpointsIncludesDefaultLocatorAndDefaultServer() {

		Environment mockEnvironment = mock(Environment.class);

		assertThat(this.condition.getDefaultConnectionEndpoints(mockEnvironment).stream()
			.map(ConnectionEndpoint::toString)
			.collect(Collectors.toList()))
			.containsExactly("localhost[40404]", "localhost[10334]");

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	public void getConfiguredConnectionEndpointsIsCorrect() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		MutablePropertySources propertySources = new MutablePropertySources();

		Properties locatorProperties = new Properties();
		Properties serverProperties = new Properties();

		locatorProperties.setProperty("spring.data.gemfire.other-property", "junk");
		locatorProperties.setProperty("spring.data.gemfire.pool.locators", "boombox[1234], cardboardbox[5678], mailbox[9012]");
		locatorProperties.setProperty("spring.data.gemfire.pool.car.locators", "skullbox[11235]");
		locatorProperties.setProperty("spring.data.gemfire.pool.other-property", "junk");
		serverProperties.setProperty("spring.data.gemfire.other-property", "junk");
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
	public void getPooledConnectionEndpointsIsCorrect() {

		Pool mockPoolOne = mock(Pool.class);
		Pool mockPoolTwo = mock(Pool.class);

		doReturn(Arrays.asList(mockPoolOne, null, mockPoolTwo, null, null))
			.when(this.condition).getPoolsFromApacheGeode();

		List<InetSocketAddress> locatorSocketAddressesForPoolOne =
			Arrays.asList(null, newSocketAddress("boombox", 1234),
				newSocketAddress("skullbox", 56789), null);

		doReturn(locatorSocketAddressesForPoolOne).when(mockPoolOne).getLocators();

		List<InetSocketAddress> onlineLocatorsSocketAddressesForPoolOne =
			Arrays.asList(newSocketAddress("cardboardbox", 9012), null);

		doReturn(onlineLocatorsSocketAddressesForPoolOne).when(mockPoolOne).getOnlineLocators();

		List<InetSocketAddress> serverSocketAddressesForPoolOne =
			Arrays.asList(newSocketAddress("mailbox", 10334), newSocketAddress("pobox", 4321));

		doReturn(serverSocketAddressesForPoolOne).when(mockPoolOne).getServers();

		List<InetSocketAddress> locatorSocketAddressesForPoolTwo =
			Arrays.asList(null, newSocketAddress("mars", 1234), null, null);

		doReturn(locatorSocketAddressesForPoolTwo).when(mockPoolTwo).getLocators();
		doReturn(null).when(mockPoolTwo).getOnlineLocators();

		List<InetSocketAddress> serverSocketAddressesForPoolTwo =
			Arrays.asList(newSocketAddress("saturn", 1234), newSocketAddress("neptune", 56789));

		doReturn(serverSocketAddressesForPoolTwo).when(mockPoolTwo).getServers();

		Environment mockEnvironment = mock(Environment.class);

		List<ConnectionEndpoint> pooledConnectionEndpoints =
			this.condition.getPooledConnectionEndpoints(mockEnvironment);

		assertThat(pooledConnectionEndpoints).isNotNull();
		assertThat(pooledConnectionEndpoints).hasSize(8);
		assertThat(pooledConnectionEndpoints).containsExactlyInAnyOrder(
			new PoolConnectionEndpoint("boombox", 1234).with(mockPoolOne),
			new PoolConnectionEndpoint("skullbox", 56789).with(mockPoolOne),
			new PoolConnectionEndpoint("cardboardbox", 9012).with(mockPoolOne),
			new PoolConnectionEndpoint("mailbox", 10334).with(mockPoolOne),
			new PoolConnectionEndpoint("pobox", 4321).with(mockPoolOne),
			new PoolConnectionEndpoint("mars", 1234).with(mockPoolTwo),
			new PoolConnectionEndpoint("saturn", 1234).with(mockPoolTwo),
			new PoolConnectionEndpoint("neptune", 56789).with(mockPoolTwo)
		);

		verify(this.condition, times(1)).getPooledConnectionEndpoints(eq(mockEnvironment));
		verify(this.condition, times(1)).getPoolsFromApacheGeode();

		verifyNoMoreInteractions(this.condition);

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	public void getPoolsFromApacheGeodeIsCorrect() {

		Pool mockPoolOne = mock(Pool.class);
		Pool mockPoolTwo = mock(Pool.class);

		doReturn(Collections.singleton(mockPoolOne)).when(this.condition).getPoolsFromClientCache();
		doReturn(Collections.singletonList(mockPoolTwo)).when(this.condition).getPoolsFromPoolManager();

		assertThat(this.condition.getPoolsFromApacheGeode()).containsExactlyInAnyOrder(mockPoolOne, mockPoolTwo);

		verify(this.condition, times(1)).getPoolsFromApacheGeode();
		verify(this.condition, times(1)).getPoolsFromClientCache();
		verify(this.condition, times(1)).getPoolsFromPoolManager();

		verifyNoMoreInteractions(this.condition);
	}

	@Test
	public void countConnectionsIsCorrect() throws Exception {

		ConnectionEndpointList list = new ConnectionEndpointList(
			new ConnectionEndpoint("boombox", 1234),
			new ConnectionEndpoint("cardboardbox", 5678),
			new ConnectionEndpoint("mailbox", 9012),
			new ConnectionEndpoint("pobox", 40404),
			new ConnectionEndpoint("skullbox", 10334)
		);

		Logger mockLogger = mock(Logger.class);

		doReturn(mockLogger).when(this.condition).getLogger();

		doAnswer(invocation -> {

			ConnectionEndpoint connectionEndpoint = invocation.getArgument(0);

			if (Arrays.asList("mailbox", "pobox").contains(connectionEndpoint.getHost())) {
				Socket mockSocket = mock(Socket.class);
				doReturn(true).when(mockSocket).isConnected();
				return mockSocket;
			}
			else if (Arrays.asList("boombox", "skullbox").contains(connectionEndpoint.getHost())) {
				return mock(Socket.class);
			}
			else {
				throw new IOException("TEST");
			}

		}).when(this.condition).connect(any(ConnectionEndpoint.class));

		assertThat(this.condition.countConnections(list)).isEqualTo(2);
	}

	@Test
	public void isConnectedSocketReturnsTrue() {

		Socket mockSocket = mock(Socket.class);

		doReturn(true).when(mockSocket).isConnected();

		assertThat(this.condition.isConnected(mockSocket)).isTrue();

		verify(mockSocket, times(1)).isConnected();
		verifyNoMoreInteractions(mockSocket);
	}

	@Test
	public void isConnectedSocketReturnsFalse() {

		Socket mockSocket = mock(Socket.class);

		doReturn(false).when(mockSocket).isConnected();

		assertThat(this.condition.isConnected(mockSocket)).isFalse();

		verify(mockSocket, times(1)).isConnected();
		verifyNoMoreInteractions(mockSocket);
	}

	@Test
	public void isConnectedWithNullIsNullSafe() {
		assertThat(this.condition.isConnected(null)).isFalse();
	}

	@Test
	public void connectNonPooledSocket() throws IOException {

		ConnectionEndpoint mockConnectionEndpoint = mock(ConnectionEndpoint.class);

		InetSocketAddress mockSocketAddress = mock(InetSocketAddress.class);

		Socket mockSocket = mock(Socket.class);

		doReturn(mockSocketAddress).when(mockConnectionEndpoint).toInetSocketAddress();
		doReturn(mockSocket).when(this.condition).newSocket(isA(ConnectionEndpoint.class));

		assertThat(this.condition.connect(mockConnectionEndpoint)).isEqualTo(mockSocket);

		verify(mockConnectionEndpoint).toInetSocketAddress();
		verify(this.condition, times(1)).newSocket(eq(mockConnectionEndpoint));
		verify(mockSocket, times(1))
			.connect(eq(mockSocketAddress), eq(ClusterAwareConfiguration.DEFAULT_TIMEOUT_IN_MILLISECONDS));

		verifyNoMoreInteractions(mockConnectionEndpoint, mockSocket);

		verifyNoInteractions(mockSocketAddress);
	}

	@Test
	@SuppressWarnings("all")
	public void connectPooledSocket() throws IOException {

		ConnectionEndpoint mockConnectionEndpoint = mock(PoolConnectionEndpoint.class);

		InetSocketAddress mockSocketAddress = mock(InetSocketAddress.class);

		Socket mockSocket = mock(Socket.class);

		doReturn(mockSocketAddress).when(mockConnectionEndpoint).toInetSocketAddress();
		doReturn(mockSocket).when(this.condition).newSocket(isA(PoolConnectionEndpoint.class));
		//doNothing().when(mockSocket).connect(isA(InetSocketAddress.class), anyInt());

		assertThat(this.condition.connect(mockConnectionEndpoint)).isEqualTo(mockSocket);

		verify(mockConnectionEndpoint, times(1)).toInetSocketAddress();
		verify(this.condition, times(1)).newSocket(eq((PoolConnectionEndpoint) mockConnectionEndpoint));
		verify(mockSocket, times(1))
			.connect(isA(InetSocketAddress.class), eq(ClusterAwareConfiguration.DEFAULT_TIMEOUT_IN_MILLISECONDS));

		verifyNoMoreInteractions(mockConnectionEndpoint, mockSocket);

		verifyNoInteractions(mockSocketAddress);
	}

	@Test
	public void newSocketFromConnectionEndpoint() throws IOException {

		ConnectionEndpoint mockConnectionEndpoint = mock(ConnectionEndpoint.class);

		Socket socket = this.condition.newSocket(mockConnectionEndpoint);

		assertThat(socket).isNotNull();
		assertThat(socket.getKeepAlive()).isFalse();
		assertThat(socket.getReuseAddress()).isTrue();
		assertThat(socket.getSoLinger()).isEqualTo(-1);

		verifyNoInteractions(mockConnectionEndpoint);
	}

	@Test
	public void newSocketFromPoolConnectionEndpoint() throws IOException {

		Pool mockPool = mock(Pool.class);

		PoolConnectionEndpoint mockPoolConnectionEndpoint = mock(PoolConnectionEndpoint.class);

		Socket mockSocket = mock(Socket.class);

		SocketFactory mockSocketFactory = mock(SocketFactory.class);

		doReturn(Optional.of(mockPool)).when(mockPoolConnectionEndpoint).getPool();
		doReturn(mockSocketFactory).when(mockPool).getSocketFactory();
		doReturn(mockSocket).when(mockSocketFactory).createSocket();

		assertThat(this.condition.newSocket(mockPoolConnectionEndpoint)).isEqualTo(mockSocket);

		verify(mockPoolConnectionEndpoint, times(1)).getPool();
		verify(mockPool, times(1)).getSocketFactory();
		verify(mockSocketFactory, times(1)).createSocket();

		verifyNoMoreInteractions(mockPool, mockPoolConnectionEndpoint, mockSocketFactory);

		verifyNoInteractions(mockSocket);
	}
	@Test(expected = SocketCreationException.class)
	public void newSocketFromPoolConnectionEndpointHandlesIOException() throws IOException {

		Pool mockPool = mock(Pool.class);

		PoolConnectionEndpoint mockPoolConnectionEndpoint = mock(PoolConnectionEndpoint.class);

		SocketFactory mockSocketFactory = mock(SocketFactory.class);

		doReturn(Optional.of(mockPool)).when(mockPoolConnectionEndpoint).getPool();
		doReturn(mockSocketFactory).when(mockPool).getSocketFactory();
		doThrow(new IOException("TEST")).when(mockSocketFactory).createSocket();

		try {
			this.condition.newSocket(mockPoolConnectionEndpoint);
		}
		catch (SocketCreationException expected) {

			assertThat(expected).hasMessage("Failed to create Socket from PoolConnectionEndpoint [%s]",
				mockPoolConnectionEndpoint);

			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockPoolConnectionEndpoint, times(1)).getPool();
			verify(mockPool, times(1)).getSocketFactory();
			verify(mockSocketFactory, times(1)).createSocket();
			verify(this.condition, never()).newSocket(isA(ConnectionEndpoint.class));

			verifyNoMoreInteractions(mockPool, mockPoolConnectionEndpoint, mockSocketFactory);
		}
	}

	@Test
	public void newSocketFromPoolConnectionEndpointReturnsNonPooledSocket() throws IOException {

		Pool mockPool = mock(Pool.class);

		PoolConnectionEndpoint mockPoolConnectionEndpoint = mock(PoolConnectionEndpoint.class);

		Socket mockSocket = mock(Socket.class);

		SocketFactory mockSocketFactory = mock(SocketFactory.class);

		doReturn(Optional.of(mockPool)).when(mockPoolConnectionEndpoint).getPool();
		doReturn(mockSocketFactory).when(mockPool).getSocketFactory();
		doReturn(null).when(mockSocketFactory).createSocket();
		doReturn(mockSocket).when(this.condition).newSocket(ArgumentMatchers.<ConnectionEndpoint>any());

		assertThat(this.condition.newSocket(mockPoolConnectionEndpoint)).isEqualTo(mockSocket);

		verify(mockPoolConnectionEndpoint, times(1)).getPool();
		verify(mockPool, times(1)).getSocketFactory();
		verify(mockSocketFactory, times(1)).createSocket();
		verify(this.condition, times(1))
			.newSocket(eq((ConnectionEndpoint) mockPoolConnectionEndpoint));

		verifyNoMoreInteractions(mockPool, mockPoolConnectionEndpoint, mockSocketFactory);

		verifyNoInteractions(mockSocket);
	}

	@Test(expected = SocketCreationException.class)
	public void newSocketFromPoolConnectionEndpointReturningNonPooledSocketHandlesIOException() throws IOException {

		Pool mockPool = mock(Pool.class);

		PoolConnectionEndpoint mockPoolConnectionEndpoint = mock(PoolConnectionEndpoint.class);

		SocketFactory mockSocketFactory = mock(SocketFactory.class);

		doReturn(Optional.of(mockPool)).when(mockPoolConnectionEndpoint).getPool();
		doReturn(mockSocketFactory).when(mockPool).getSocketFactory();
		doReturn(null).when(mockSocketFactory).createSocket();
		doThrow(new IOException("TEST")).when(this.condition).newSocket(ArgumentMatchers.<ConnectionEndpoint>any());

		try {
			this.condition.newSocket(mockPoolConnectionEndpoint);
		}
		catch (SocketCreationException expected) {

			assertThat(expected).hasMessage("Failed to create Socket from PoolConnectionEndpoint [%s]",
				mockPoolConnectionEndpoint);

			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {

			verify(mockPoolConnectionEndpoint, times(1)).getPool();
			verify(mockPool, times(1)).getSocketFactory();
			verify(mockSocketFactory, times(1)).createSocket();
			verify(this.condition, times(1))
				.newSocket(eq((ConnectionEndpoint) mockPoolConnectionEndpoint));

			verifyNoMoreInteractions(mockPool, mockPoolConnectionEndpoint, mockSocketFactory);
		}
	}

	@Test
	public void closeSocketReturnsTrue() throws IOException {

		Socket mockSocket = mock(Socket.class);

		assertThat(this.condition.close(mockSocket)).isTrue();

		verify(mockSocket, times(1)).close();
		verifyNoMoreInteractions(mockSocket);
	}

	@Test
	public void closeSocketReturnsFalse() throws IOException {

		Socket mockSocket = mock(Socket.class);

		doThrow(new IOException("TEST")).when(mockSocket).close();

		assertThat(this.condition.close(mockSocket)).isFalse();

		verify(mockSocket, times(1)).close();
		verifyNoMoreInteractions(mockSocket);
	}

	@Test
	public void closeNullSocketIsNullSafe() {
		assertThat(this.condition.close(null)).isFalse();
	}

	@Test
	public void configureTopologySetsLocalWhenConnectionCountIsLessThanOneAndEnvironmentDoesNotContainTargetProperty() {

		assertThat(System.getProperties())
			.doesNotContainKey(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		MockEnvironment mockEnvironment = spy(new MockEnvironment());

		ConnectionEndpointList connectionEndpoints =
			new ConnectionEndpointList(new ConnectionEndpoint("localhost", 1234));

		this.condition.configureTopology(mockEnvironment, connectionEndpoints,0);

		assertThat(mockEnvironment.getProperty(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY))
			.isEqualTo("LOCAL");

		verify(mockEnvironment, times(1))
			.containsProperty(eq(ClusterAwareConfiguration.SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY));
		verify(mockEnvironment, times(1)).getPropertySources();
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
}
