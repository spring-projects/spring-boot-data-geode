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
package org.springframework.geode.config.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.server.CacheServer;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.support.ConnectionEndpointList;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.cache.SimpleCacheResolver;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClusterAwareConfiguration} class is a Spring {@link Configuration @Configuration} class imported by
 * {@link EnableClusterAware} used to determine whether a Spring Boot application using Apache Geode should run
 * in {@literal local-only mode} or {@literal client/server}.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see java.net.InetSocketAddress
 * @see java.net.Socket
 * @see java.net.SocketAddress
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientRegionShortcut
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.EnumerablePropertySource
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.data.gemfire.support.ConnectionEndpoint
 * @see org.springframework.data.gemfire.support.ConnectionEndpointList
 * @see org.springframework.geode.cache.SimpleCacheResolver
 * @since 1.2.0
 */
@Configuration
@Import({ ClusterAvailableConfiguration.class, ClusterNotAvailableConfiguration.class })
public class ClusterAwareConfiguration extends AbstractAnnotationConfigSupport {

	static final boolean DEFAULT_CLUSTER_CONDITION_MATCH = false;

	static final int DEFAULT_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;
	static final int DEFAULT_LOCATOR_PORT = 10334;
	static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 500;

	static final ClientRegionShortcut LOCAL_CLIENT_REGION_SHORTCUT = ClientRegionShortcut.LOCAL;

	static final Logger logger = LoggerFactory.getLogger(ClusterAwareConfiguration.class);

	static final String LOCALHOST = "localhost";
	static final String MATCHING_PROPERTY_PATTERN = "spring\\.data\\.gemfire\\.pool\\..*locators|servers";

	static final String SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY =
		"spring.boot.data.gemfire.cluster.condition.match";

	static final String SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY =
		"spring.data.gemfire.cache.client.region.shortcut";

	private static final Function<ConditionContext, Boolean> configuredMatchFunction = conditionContext ->
		Optional.ofNullable(conditionContext)
			.map(ConditionContext::getEnvironment)
			.map(environment -> environment.getProperty(SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY,
				Boolean.class, DEFAULT_CLUSTER_CONDITION_MATCH))
			.orElse(DEFAULT_CLUSTER_CONDITION_MATCH);

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return EnableClusterAware.class;
	}

	@SuppressWarnings("unused")
	public static class ClusterAwareCondition implements Condition {

		private static final AtomicReference<Boolean> clusterAvailable = new AtomicReference<>(null);

		private static ApplicationListener<ContextClosedEvent> clusterAwareConditionResetOnContextClosedApplicationListener() {
			return contextClosedEvent-> reset();
		}

		public static boolean isAvailable() {
			return Boolean.TRUE.equals(clusterAvailable.get());
		}

		public static void reset() {
			clusterAvailable.set(null);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public synchronized boolean matches(@NonNull ConditionContext conditionContext,
				@NonNull AnnotatedTypeMetadata metadata) {

			return isMatch(conditionContext) || doCachedMatch(conditionContext);
		}

		boolean isMatch(@NonNull ConditionContext conditionContext) {
			return isAvailable() || configuredMatchFunction.apply(conditionContext);
		}

		/**
		 * Caches the result of the computed {@link #doMatch(ConditionContext)} operation.
		 *
		 * Subsequent calls returns the cached value of the computed (once) {@link #doMatch(ConditionContext)}
		 * operation.
		 *
		 * @param conditionContext Spring {@link ConditionContext} capturing the context in which the conditions
		 * are evaluated; must not be {@literal null}.
		 * @return a boolean value indicating whether the conditions match (i.e. {@literal true}).
		 * @see org.springframework.context.annotation.ConditionContext
		 * @see #registerApplicationListener(ConditionContext)
		 * @see #doMatch(ConditionContext)
		 */
		protected boolean doCachedMatch(@NonNull ConditionContext conditionContext) {

			Supplier<Boolean> evaluateConditionMatch = () -> {
				registerApplicationListener(conditionContext);
				return doMatch(conditionContext);
			};

			UnaryOperator<Boolean> clusterAvailableUpdateFunction = currentClusterAvailableState ->
				ObjectUtils.initialize(currentClusterAvailableState, evaluateConditionMatch);

			return clusterAvailable.updateAndGet(clusterAvailableUpdateFunction);
		}

		protected @NonNull ConditionContext registerApplicationListener(@NonNull ConditionContext conditionContext) {

			Optional.ofNullable(conditionContext)
				.map(ConditionContext::getResourceLoader)
				.filter(ConfigurableApplicationContext.class::isInstance)
				.map(ConfigurableApplicationContext.class::cast)
				.ifPresent(applicationContext -> applicationContext
					.addApplicationListener(clusterAwareConditionResetOnContextClosedApplicationListener()));

			return conditionContext;
		}

		/**
		 * Performs the actual conditional match to determine whether this Spring Boot for Apache Geode application
		 * can connect to an available Apache Geode cluster available in any environment (e.g. Standalone or Cloud).
		 *
		 * @param conditionContext Spring {@link ConditionContext} capturing the context in which the condition(s)
		 * are evaluated; must not be {@literal null}.
		 * @return the given {@link ConditionContext}.
		 * @see org.springframework.context.annotation.ConditionContext
		 * @see #getConnectionEndpoints(Environment)
		 * @see #countConnections(ConnectionEndpointList)
		 * @see #configureTopology(Environment, ConnectionEndpointList, int)
		 * @see #isMatch(ConnectionEndpointList, int)
		 */
		protected boolean doMatch(@NonNull ConditionContext conditionContext) {

			Environment environment = conditionContext.getEnvironment();

			ConnectionEndpointList connectionEndpoints = getConnectionEndpoints(environment);

			int connectionCount = countConnections(connectionEndpoints);

			configureTopology(environment, connectionEndpoints, connectionCount);

			return isMatch(connectionEndpoints, connectionCount);
		}

		boolean isMatch(@NonNull ConnectionEndpointList connectionEndpoints, int connectionCount) {
			return connectionCount > 0;
		}

		protected @NonNull Logger getLogger() {
			return logger;
		}

		protected ConnectionEndpointList getConnectionEndpoints(@NonNull Environment environment) {

			return new ConnectionEndpointList(getDefaultConnectionEndpoints(environment))
				.add(getConfiguredConnectionEndpoints(environment))
				.add(getPooledConnectionEndpoints(environment));
		}

		protected List<ConnectionEndpoint> getDefaultConnectionEndpoints(@NonNull Environment environment) {

			return Arrays.asList(
				new ConnectionEndpoint(LOCALHOST, DEFAULT_CACHE_SERVER_PORT),
				new ConnectionEndpoint(LOCALHOST, DEFAULT_LOCATOR_PORT)
			);
		}

		protected List<ConnectionEndpoint> getConfiguredConnectionEndpoints(@NonNull Environment environment) {

			List<ConnectionEndpoint> connectionEndpoints = new ArrayList<>();

			if (environment instanceof ConfigurableEnvironment) {

				ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

				MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

				if (propertySources != null) {

					Pattern pattern = Pattern.compile(MATCHING_PROPERTY_PATTERN);

					for (PropertySource<?> propertySource : propertySources) {
						if (propertySource instanceof EnumerablePropertySource) {

							EnumerablePropertySource<?> enumerablePropertySource =
								(EnumerablePropertySource<?>) propertySource;

							String[] propertyNames = enumerablePropertySource.getPropertyNames();

							Arrays.stream(ArrayUtils.nullSafeArray(propertyNames, String.class))
								.filter(StringUtils::hasText)
								.filter(propertyName-> pattern.matcher(propertyName).find())
								.forEach(propertyName -> {

									String propertyValue = environment.getProperty(propertyName);

									if (StringUtils.hasText(propertyValue)) {

										int defaultPort = propertyName.toLowerCase().contains("servers")
											? DEFAULT_CACHE_SERVER_PORT
											: DEFAULT_LOCATOR_PORT;

										String[] propertyValueArray = propertyValue.split(",");

										ConnectionEndpointList list =
											ConnectionEndpointList.parse(defaultPort, propertyValueArray);

										connectionEndpoints.addAll(list);
									}
								});
						}
					}
				}
			}

			return connectionEndpoints;
		}

		protected List<ConnectionEndpoint> getPooledConnectionEndpoints(@NonNull Environment environment) {

			List<ConnectionEndpoint> pooledConnectionEndpoints = new ArrayList<>();

			getPoolsFromApacheGeode().stream()
				.filter(Objects::nonNull)
				.map(ConnectionEndpointListBuilder::from)
				.forEach(pooledConnectionEndpoints::addAll);

			return pooledConnectionEndpoints;
		}

		protected Collection<Pool> getPoolsFromApacheGeode() {

			Set<Pool> pools = new HashSet<>();

			pools.addAll(getPoolsFromClientCache());
			pools.addAll(getPoolsFromPoolManager());

			return pools;
		}

		// Technically, should be registered with the PoolManager, but...
		Collection<Pool> getPoolsFromClientCache() {

			return SimpleCacheResolver.getInstance().resolveClientCache()
				.map(ClientCache::getDefaultPool)
				.map(Collections::singleton)
				.orElseGet(Collections::emptySet);
		}

		Collection<Pool> getPoolsFromPoolManager() {

			Map<String, Pool> namedPools = PoolManager.getAll();

			return CollectionUtils.nullSafeMap(namedPools).values().stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		}

		protected int countConnections(@NonNull ConnectionEndpointList connectionEndpoints) {

			int count = 0;

			for (ConnectionEndpoint connectionEndpoint : connectionEndpoints) {

				try (Socket socket = connect(connectionEndpoint)){

					count += isConnected(socket) ? 1 : 0;

					if (getLogger().isInfoEnabled()) {
						getLogger().info("Successfully connected to {}", connectionEndpoint);
					}
				}
				catch (IOException | SocketCreationException cause) {

					if (getLogger().isInfoEnabled()) {
						getLogger().info("Failed to connect to {}", connectionEndpoint);
					}

					if (getLogger().isDebugEnabled()) {
						getLogger().debug("Connection failed because:", cause);
					}
				}
			}

			return count;
		}

		protected boolean isConnected(@NonNull Socket socket) {
			return socket != null && socket.isConnected();
		}

		protected @NonNull Socket connect(@NonNull ConnectionEndpoint connectionEndpoint) throws IOException {

			SocketAddress socketAddress = connectionEndpoint.toInetSocketAddress();

			Socket socket = connectionEndpoint instanceof PoolConnectionEndpoint
				? newSocket((PoolConnectionEndpoint) connectionEndpoint)
				: newSocket(connectionEndpoint);

			socket.connect(socketAddress, DEFAULT_TIMEOUT_IN_MILLISECONDS);

			return socket;
		}

		protected @NonNull Socket newSocket(@NonNull ConnectionEndpoint connectionEndpoint) throws IOException {

			Socket socket = new Socket();

			socket.setKeepAlive(false);
			socket.setReuseAddress(true);
			socket.setSoLinger(false, 0);

			return socket;
		}

		protected @NonNull Socket newSocket(@NonNull PoolConnectionEndpoint poolConnectionEndpoint) {

			Function<Throwable, Socket> ioExceptionHandlingFunction = cause -> {

				String message = String.format("Failed to create Socket from PoolConnectionEndpoint [%s]",
					poolConnectionEndpoint);

				throw new SocketCreationException(message, cause);
			};

			return poolConnectionEndpoint.getPool()
				.map(Pool::getSocketFactory)
				.map(socketFactory -> ObjectUtils.<Socket>doOperationSafely(socketFactory::createSocket,
					ioExceptionHandlingFunction))
				.orElseGet(() -> ObjectUtils.<Socket>doOperationSafely(() ->
					newSocket((ConnectionEndpoint) poolConnectionEndpoint), ioExceptionHandlingFunction));
		}

		protected boolean close(@Nullable Socket socket) {

			return ObjectUtils.<Boolean>doOperationSafely(() -> {

				if (socket != null) {
					socket.close();
					return true;
				}

				return false;

			}, cause -> false);
		}

		protected void configureTopology(@NonNull Environment environment, @NonNull ConnectionEndpointList connectionEndpoints,
				int connectionCount) {

			if (connectionCount < 1) {
				if (!environment.containsProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY)) {
					System.setProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
						LOCAL_CLIENT_REGION_SHORTCUT.name());
				}

				if (getLogger().isInfoEnabled()) {
					getLogger().info("No cluster found; Spring Boot application is running in standalone [LOCAL] mode");
				}
			}
			else {
				if (getLogger().isInfoEnabled()) {
					getLogger().info("Cluster was found; Auto-configuration made [{}] successful connection(s);"
						+ " Spring Boot application is running in a client/server topology", connectionCount);
				}

				if (getLogger().isInfoEnabled()) {
					if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " inside a VMware Tanzu GemFire for VMs environment");
					}
					else if (CloudPlatform.KUBERNETES.isActive(environment)) {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " inside a VMware Tanzu GemFire for K8S environment");
					}
					else {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " using a standalone Apache Geode-based cluster");
					}
				}
			}
		}
	}

	protected static class ConnectionEndpointListBuilder {

		protected static @NonNull ConnectionEndpointList from(@NonNull Pool pool) {

			ConnectionEndpointList list = new ConnectionEndpointList();

			if (pool != null) {

				Set<InetSocketAddress> poolSocketAddresses = new HashSet<>();

				collect(poolSocketAddresses, pool.getLocators());
				collect(poolSocketAddresses, pool.getOnlineLocators());
				collect(poolSocketAddresses, pool.getServers());

				poolSocketAddresses.stream()
					.map(ConnectionEndpoint::from)
					.map(PoolConnectionEndpoint::from)
					.map(it -> it.with(pool))
					.forEach(list::add);
			}

			return list;
		}

		private static <T extends Collection<InetSocketAddress>> T collect(@NonNull T collection,
				@NonNull Collection<InetSocketAddress> socketAddressesToCollect) {

			CollectionUtils.nullSafeCollection(socketAddressesToCollect).stream()
				.filter(Objects::nonNull)
				.forEach(collection::add);

			return collection;
		}
	}

	protected static class PoolConnectionEndpoint extends ConnectionEndpoint {

		protected static PoolConnectionEndpoint from(@NonNull ConnectionEndpoint connectionEndpoint) {
			return new PoolConnectionEndpoint(connectionEndpoint.getHost(), connectionEndpoint.getPort());
		}

		private Pool pool;

		PoolConnectionEndpoint(@NonNull String host, int port) {
			super(host, port);
		}

		public Optional<Pool> getPool() {
			return Optional.ofNullable(this.pool);
		}

		public @NonNull PoolConnectionEndpoint with(@Nullable Pool pool) {
			this.pool = pool;
			return this;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean equals(Object obj) {

			if (obj == this) {
				return true;
			}

			if (!(obj instanceof PoolConnectionEndpoint)) {
				return false;
			}

			PoolConnectionEndpoint that = (PoolConnectionEndpoint) obj;

			return super.equals(that)
				&& this.getPool().equals(that.getPool());
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public int hashCode() {

			int hashValue = super.hashCode();

			hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getPool());

			return hashValue;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public String toString() {
			return String.format("ConnectionEndpoint [%1$s] from Pool [%2$s]",
				super.toString(), getPool().map(Pool::getName).orElse(""));
		}
	}

	@SuppressWarnings("unused")
	protected static class SocketCreationException extends RuntimeException {

		protected SocketCreationException() { }

		protected SocketCreationException(String message) {
			super(message);
		}

		protected SocketCreationException(Throwable cause) {
			super(cause);
		}

		protected SocketCreationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
