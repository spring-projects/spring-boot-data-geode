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
import java.util.concurrent.atomic.AtomicBoolean;
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

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
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
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.EnumerablePropertySource
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.data.gemfire.support.ConnectionEndpoint
 * @see org.springframework.data.gemfire.support.ConnectionEndpointList
 * @see org.springframework.geode.cache.SimpleCacheResolver
 * @since 1.2.0
 */
@Configuration
@Import({ ClusterAvailableConfiguration.class, ClusterNotAvailableConfiguration.class })
public class ClusterAwareConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	static final boolean DEFAULT_CLUSTER_AWARE_CONDITION_MATCH = false;
	static final boolean DEFAULT_CLUSTER_AWARE_CONDITION_STRICT_MATCH = false;

	static final int DEFAULT_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;
	static final int DEFAULT_LOCATOR_PORT = 10334;
	static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 500;

	static final ClientRegionShortcut LOCAL_CLIENT_REGION_SHORTCUT = ClientRegionShortcut.LOCAL;

	static final String LOCALHOST = "localhost";
	static final String MATCHING_PROPERTY_PATTERN = "spring\\.data\\.gemfire\\.pool\\..*locators|servers";
	static final String STRICT_MATCH_ATTRIBUTE_NAME = "strictMatch";

	static final String CLUSTER_AWARE_CONFIGURATION_PROPERTY_SOURCE_NAME =
		ClusterAwareConfiguration.class.getSimpleName().concat("PropertySource");

	static final String SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY =
		"spring.boot.data.gemfire.cluster.condition.match";

	static final String SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_STRICT_PROPERTY =
		"spring.boot.data.gemfire.cluster.condition.match.strict";

	static final String SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY =
		"spring.data.gemfire.cache.client.region.shortcut";

	private static final AtomicBoolean strictMatchConfiguration =
		new AtomicBoolean(DEFAULT_CLUSTER_AWARE_CONDITION_STRICT_MATCH);

	private static final Function<ConditionContext, Boolean> configuredMatchFunction = conditionContext ->
		Optional.ofNullable(conditionContext)
			.map(ConditionContext::getEnvironment)
			.map(environment -> environment.getProperty(SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY,
				Boolean.class, DEFAULT_CLUSTER_AWARE_CONDITION_MATCH))
			.orElse(DEFAULT_CLUSTER_AWARE_CONDITION_MATCH);

	private static final Logger logger = LoggerFactory.getLogger(ClusterAwareConfiguration.class);

	/**
	 * @inheritDoc
	 */
	@Override
	protected @NonNull Class<? extends Annotation> getAnnotationType() {
		return EnableClusterAware.class;
	}

	protected boolean isStrictMatchConfigured(@NonNull AnnotationAttributes enableClusterAwareAttributes) {
		return enableClusterAwareAttributes != null
			&& Boolean.TRUE.equals(enableClusterAwareAttributes.getBoolean(STRICT_MATCH_ATTRIBUTE_NAME));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes enableClusterAwareAttributes = getAnnotationAttributes(importMetadata);

			strictMatchConfiguration.set(isStrictMatchConfigured(enableClusterAwareAttributes));
		}
	}

	@SuppressWarnings("unused")
	public static class ClusterAwareCondition implements Condition {

		private static final AtomicReference<Boolean> clusterAvailable = new AtomicReference<>(null);

		protected static final String RUNTIME_ENVIRONMENT_NAME = "Apache Geode-based Cluster on Bare Metal";

		private static @NonNull ApplicationListener<ContextClosedEvent> clusterAwareConditionResetOnContextClosedApplicationListener() {
			return contextClosedEvent-> reset();
		}

		/**
		 * Determines whether an Apache Geode-based Cluster is available in the runtime environment.
		 *
		 * @return a boolean value indicating whether an Apache Geode-based Cluster is available in
		 * the runtime environment.
		 * @see #wasClusterAvailabilityEvaluated()
		 */
		public static boolean isAvailable() {
			return Boolean.TRUE.equals(clusterAvailable.get());
		}

		/**
		 * Resets the state of this {@link Condition} to reevaluate whether an Apache Geode-based Cluster
		 * is available in the runtime environment.
		 *
		 * @see #set(Boolean)
		 */
		public static void reset() {
			set(null);
		}

		/**
		 * Sets the state of the {@code clusterAvailable} variable.
		 *
		 * @param available state to set the {@code clusterAvailable} variable to.
		 * @see #reset()
		 */
		protected static void set(@Nullable Boolean available) {
			clusterAvailable.set(available);
		}

		/**
		 * Determines whether the {@link Condition} that determines whether an Apache Geode-based Cluster is available
		 * in the runtime environment has been evaluated.
		 *
		 * @return a boolean value indicating whether the {@link Condition} that determines
		 * whether an Apache Geode-based Cluster is available in the runtime environment has been evaluated.
		 * @see #isAvailable()
		 */
		public static boolean wasClusterAvailabilityEvaluated() {
			return clusterAvailable.get() != null;
		}

		/**
		 * Returns a {@link String} containing a description of the runtime environment.
		 *
		 * @return a {@link String} containing a description of the runtime environment.
		 */
		protected String getRuntimeEnvironmentName() {
			return RUNTIME_ENVIRONMENT_NAME;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public synchronized boolean matches(@NonNull ConditionContext conditionContext,
				@NonNull AnnotatedTypeMetadata typeMetadata) {

			boolean matches = isMatch(conditionContext) || doCachedMatch(conditionContext);
			boolean strictMatch = isStrictMatch(conditionContext, typeMetadata);

			failOnStrictMatchAndNoMatches(strictMatch, matches);

			return matches;
		}

		boolean isMatch(@NonNull ConditionContext conditionContext) {
			return isAvailable() || configuredMatchFunction.apply(conditionContext);
		}

		protected boolean isStrictMatch(@NonNull ConditionContext conditionContext,
				@NonNull AnnotatedTypeMetadata typeMetadata) {

			Environment environment = conditionContext.getEnvironment();

			Function<ConfigurableListableBeanFactory, Boolean> isStrictMatchEnabledFunction = beanFactory -> {

				boolean strictMatchEnabled = strictMatchConfiguration.get();

				if (!strictMatchEnabled) {

					String annotationName = EnableClusterAware.class.getName();

					strictMatchEnabled = beanFactory != null
						&& Arrays.stream(ArrayUtils.nullSafeArray(beanFactory.getBeanDefinitionNames(), String.class))
							.map(beanFactory::getBeanDefinition)
							.filter(AnnotatedBeanDefinition.class::isInstance)
							.map(AnnotatedBeanDefinition.class::cast)
							.map(AnnotatedBeanDefinition::getMetadata)
							.filter(annotationMetadata -> annotationMetadata.hasAnnotation(annotationName))
							.findFirst()
							.map(annotationMetadata -> annotationMetadata.getAnnotationAttributes(annotationName))
							.map(AnnotationAttributes::fromMap)
							.map(annotationAttributes -> annotationAttributes.getBoolean(STRICT_MATCH_ATTRIBUTE_NAME))
							.orElse(DEFAULT_CLUSTER_AWARE_CONDITION_STRICT_MATCH);
				}

				return strictMatchEnabled;
			};

			return environment.getProperty(SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_STRICT_PROPERTY,
				Boolean.class, isStrictMatchEnabledFunction.apply(conditionContext.getBeanFactory()));
		}

		protected boolean isStrictMatchAndNoMatches(boolean strictMatch, boolean matches) {
			return strictMatch && !matches;
		}

		protected void failOnStrictMatchAndNoMatches(boolean strictMatch, boolean matches) {

			if (isStrictMatchAndNoMatches(strictMatch, matches)) {

				String message =
					String.format("Failed to find available cluster in [%1$s] when strictMatch was [%2$s]",
						getRuntimeEnvironmentName(), strictMatch);

				throw new ClusterNotAvailableException(message);
			}
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

			Supplier<Boolean> evaluateConditionMatches = () -> {
				registerApplicationListener(conditionContext);
				return doMatch(conditionContext);
			};

			UnaryOperator<Boolean> clusterAvailableUpdateFunction = currentClusterAvailable ->
				ObjectUtils.initialize(currentClusterAvailable, evaluateConditionMatches);

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
		 * @see #doCachedMatch(ConditionContext)
		 * @see #getConnectionEndpoints(Environment)
		 * @see #countConnections(ConnectionEndpointList)
		 * @see #configureTopology(Environment, ConnectionEndpointList, int)
		 * @see #logRuntimeEnvironment(Logger, int)
		 * @see #isMatch(ConnectionEndpointList, int)
		 */
		protected boolean doMatch(@NonNull ConditionContext conditionContext) {

			Environment environment = conditionContext.getEnvironment();

			ConnectionEndpointList connectionEndpoints = getConnectionEndpoints(environment);

			int connectionCount = countConnections(connectionEndpoints);

			configureTopology(environment, connectionEndpoints, connectionCount);
			logRuntimeEnvironment(getLogger(), connectionCount);

			return isMatch(connectionEndpoints, connectionCount);
		}

		boolean isMatch(@NonNull ConnectionEndpointList connectionEndpoints, int connectionCount) {
			return isConnected(connectionCount);
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

		protected boolean isConnected(int connectionCount) {
			return connectionCount > 0;
		}

		protected boolean isNotConnected(int connectionCount) {
			return !isConnected(connectionCount);
		}

		private void configureEnvironment(@NonNull Environment environment) {

			if (environment != null) {
				if (!environment.containsProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY)) {
					if (environment instanceof ConfigurableEnvironment) {

						MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();

						propertySources.addFirst(new MapPropertySource(CLUSTER_AWARE_CONFIGURATION_PROPERTY_SOURCE_NAME,
							Collections.singletonMap(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
								LOCAL_CLIENT_REGION_SHORTCUT.name())));
					}
					else {
						System.setProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
							LOCAL_CLIENT_REGION_SHORTCUT.name());
					}
				}
			}
		}

		protected void configureTopology(@NonNull Environment environment,
				@NonNull ConnectionEndpointList connectionEndpoints, int connectionCount) {

			if (isNotConnected(connectionCount)) {
				configureEnvironment(environment);
			}
		}

		protected void logConnectedRuntimeEnvironment(@NonNull Logger logger) {

			if (logger.isInfoEnabled()) {
				logger.info("Spring Boot application is running in a client/server topology"
					+ " using a standalone Apache Geode-based cluster");
			}
		}

		protected void logConnectedRuntimeEnvironment(@NonNull Logger logger, int connectionCount) {

			if (logger.isInfoEnabled()) {
				logger.info("Cluster was found; Auto-configuration made [{}] successful connection(s)",
					connectionCount);
			}

			logConnectedRuntimeEnvironment(logger);
		}

		protected void logRuntimeEnvironment(@NonNull Logger logger, int connectionCount) {

			if (isConnected(connectionCount)) {
				logConnectedRuntimeEnvironment(logger, connectionCount);
			}
			else {
				logUnconnectedRuntimeEnvironment(logger);
			}
		}

		protected void logUnconnectedRuntimeEnvironment(@NonNull Logger logger) {

			if (logger.isInfoEnabled()) {
				logger.info("No cluster was found; Spring Boot application will run in standalone [LOCAL] mode"
					+ " unless strictMode is false and the application is running in a Cloud-managed Environment");
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
