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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.apache.geode.cache.client.ClientRegionShortcut;
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
 * @see java.net.Socket
 * @see java.net.SocketAddress
 * @see org.apache.geode.cache.client.ClientRegionShortcut
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
 * @since 1.2.0
 */
@Configuration
@Import({ ClusterAvailableConfiguration.class, ClusterNotAvailableConfiguration.class })
public class ClusterAwareConfiguration extends AbstractAnnotationConfigSupport {

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

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return EnableClusterAware.class;
	}

	@SuppressWarnings("unused")
	public static class ClusterAwareCondition implements Condition {

		private static final AtomicReference<Boolean> clusterAvailable = new AtomicReference<>(null);

		private static ApplicationListener<ContextClosedEvent> clusterAwareConditionResetApplicationListener() {
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
		public synchronized boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {

			if (clusterAvailable.get() == null) {
				registerApplicationListener(context);
				doMatch(context);
			}

			return isMatch(context);
		}

		@NonNull ConditionContext registerApplicationListener(@NonNull ConditionContext conditionContext) {

			Optional.ofNullable(conditionContext)
				.map(ConditionContext::getResourceLoader)
				.filter(ConfigurableApplicationContext.class::isInstance)
				.map(ConfigurableApplicationContext.class::cast)
				.ifPresent(applicationContext ->
					applicationContext.addApplicationListener(clusterAwareConditionResetApplicationListener()));

			return conditionContext;
		}

		boolean isMatch(@NonNull ConditionContext context) {

			Environment environment = context.getEnvironment();

			return isAvailable()
				|| Boolean.TRUE.equals(environment.getProperty(SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY,
					Boolean.class, false));
		}

		/**
		 * Performs the actual conditional match to determine whether this Spring Boot for Apache Geode application
		 * can connect to an available Apache Geode cluster available in any environment (e.g. Standalone or Cloud).
		 *
		 * @param conditionContext Spring {@link ConditionContext} which captures the context in which the condition(s)
		 * are evaluated; must not be {@literal null}.
		 * @return the given {@link ConditionContext}.
		 * @see org.springframework.context.annotation.ConditionContext
		 */
		@NonNull ConditionContext doMatch(@NonNull ConditionContext conditionContext) {

			Environment environment = conditionContext.getEnvironment();

			ConnectionEndpointList connectionEndpoints =
				new ConnectionEndpointList(getDefaultConnectionEndpoints())
					.add(getConfiguredConnectionEndpoints(environment));

			int connectionCount = countConnections(connectionEndpoints);

			configureTopology(environment, connectionEndpoints, connectionCount);

			clusterAvailable.set(isMatch(connectionEndpoints, connectionCount));

			return conditionContext;
		}

		@NonNull Logger getLogger() {
			return logger;
		}

		List<ConnectionEndpoint> getDefaultConnectionEndpoints() {

			return Arrays.asList(
				new ConnectionEndpoint(LOCALHOST, DEFAULT_CACHE_SERVER_PORT),
				new ConnectionEndpoint(LOCALHOST, DEFAULT_LOCATOR_PORT)
			);
		}

		List<ConnectionEndpoint> getConfiguredConnectionEndpoints(@NonNull Environment environment) {

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

		int countConnections(@NonNull ConnectionEndpointList connectionEndpoints) {

			int count = 0;

			for (ConnectionEndpoint connectionEndpoint : connectionEndpoints) {

				Socket socket = null;

				try {

					socket = connect(connectionEndpoint);

					count++;

					if (getLogger().isInfoEnabled()) {
						getLogger().info("Successfully connected to {}", connectionEndpoint);
					}
				}
				catch (IOException cause) {

					if (getLogger().isInfoEnabled()) {
						getLogger().info("Failed to connect to {}", connectionEndpoint);
					}

					if (getLogger().isDebugEnabled()) {
						getLogger().debug("Connection failure caused by:", cause);
					}
				}
				finally {
					close(socket);
				}
			}

			return count;
		}

		@NonNull Socket connect(@NonNull ConnectionEndpoint connectionEndpoint) throws IOException {

			SocketAddress socketAddress =
				new InetSocketAddress(connectionEndpoint.getHost(), connectionEndpoint.getPort());

			Socket socket = new Socket();

			socket.connect(socketAddress, DEFAULT_TIMEOUT_IN_MILLISECONDS);

			return socket;
		}

		boolean close(@Nullable Socket socket) {

			return ObjectUtils.<Boolean>doOperationSafely(() -> {

				if (socket != null) {
					socket.close();
					return true;
				}

				return false;

			}, cause -> false);
		}

		void configureTopology(@NonNull Environment environment, @NonNull ConnectionEndpointList connectionEndpoints,
				int connectionCount) {

			if (connectionCount < 1) {
				if (!environment.containsProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY)) {
					System.setProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
						LOCAL_CLIENT_REGION_SHORTCUT.name());
				}

				if (getLogger().isInfoEnabled()) {
					getLogger().info("No cluster found; Spring Boot application will run in standalone (LOCAL) mode");
				}
			}
			else {
				if (getLogger().isInfoEnabled()) {
					getLogger().info("Cluster was found; Auto-configuration made [{}] successful connection(s);"
						+ " Spring Boot application will run in a client/server topology", connectionCount);
				}

				if (getLogger().isInfoEnabled()) {
					if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " connected to VMware Tanzu GemFire for VMs");
					}
					else if (CloudPlatform.KUBERNETES.isActive(environment)) {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " connected to VMware Tanzu GemFire for K8S");
					}
					else {
						getLogger().info("Spring Boot application is running in a client/server topology,"
							+ " connected to a standalone Apache Geode-based cluster");
					}
				}
			}
		}

		boolean isMatch(@NonNull ConnectionEndpointList connectionEndpoints, int connectionCount) {
			return connectionCount > 0;
		}
	}
}
