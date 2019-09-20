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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.util.StringUtils;

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
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
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

		@Override
		public synchronized boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			if (clusterAvailable.get() == null) {
				registerApplicationListener(context);
				doMatch(context);
			}

			return isMatch(context);
		}

		ConditionContext registerApplicationListener(ConditionContext conditionContext) {

			Optional.ofNullable(conditionContext)
				.map(ConditionContext::getResourceLoader)
				.filter(ConfigurableApplicationContext.class::isInstance)
				.map(ConfigurableApplicationContext.class::cast)
				.ifPresent(applicationContext ->
					applicationContext.addApplicationListener(clusterAwareConditionResetApplicationListener()));

			return conditionContext;
		}

		boolean isMatch(ConditionContext context) {

			Environment environment = context.getEnvironment();

			return isAvailable()
				|| environment.getProperty(SPRING_BOOT_DATA_GEMFIRE_CLUSTER_CONDITION_MATCH_PROPERTY,
					Boolean.class, false);
		}

		ConditionContext doMatch(ConditionContext conditionContext) {

			Environment environment = conditionContext.getEnvironment();

			ConnectionEndpointList connectionEndpoints =
				new ConnectionEndpointList(getDefaultConnectionEndpoints())
					.add(getConfiguredConnectionEndpoints(environment));

			int connectionCount = countConnections(connectionEndpoints);

			configureTopology(environment, connectionEndpoints, connectionCount);

			clusterAvailable.set(isMatch(connectionEndpoints, connectionCount));

			return conditionContext;
		}

		Logger getLogger() {
			return logger;
		}

		List<ConnectionEndpoint> getDefaultConnectionEndpoints() {

			return Arrays.asList(
				new ConnectionEndpoint(LOCALHOST, DEFAULT_CACHE_SERVER_PORT),
				new ConnectionEndpoint(LOCALHOST, DEFAULT_LOCATOR_PORT)
			);
		}

		List<ConnectionEndpoint> getConfiguredConnectionEndpoints(Environment environment) {

			List<ConnectionEndpoint> connectionEndpoints = new ArrayList<>();

			if (environment instanceof ConfigurableEnvironment) {

				ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

				MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

				if (propertySources != null) {

					Pattern pattern = Pattern.compile(MATCHING_PROPERTY_PATTERN);

					for (PropertySource propertySource : propertySources) {
						if (propertySource instanceof EnumerablePropertySource) {

							EnumerablePropertySource<?> enumerablePropertySource =
								(EnumerablePropertySource<?>) propertySource;

							String[] propertyNames = enumerablePropertySource.getPropertyNames();

							Arrays.stream(ArrayUtils.nullSafeArray(propertyNames, String.class))
								.filter(propertyName-> pattern.matcher(propertyName).find())
								.forEach(propertyName -> {

									String propertyValue = environment.getProperty(propertyName);

									if (StringUtils.hasText(propertyValue)) {

										int defaultPort = propertyName.contains("servers")
											? DEFAULT_CACHE_SERVER_PORT
											: DEFAULT_LOCATOR_PORT;

										String[] propertyValueArray = trim(propertyValue.split(","));

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

		// TODO: remove when DATAGEODE-228, a BUG in SDG, is fixed!
		private String[] trim(String[] array) {

			array = ArrayUtils.nullSafeArray(array, String.class);

			for (int index = 0; index < array.length; index++) {
				array[index] = StringUtils.trimAllWhitespace(array[index]);
			}

			return array;
		}

		int countConnections(ConnectionEndpointList connectionEndpoints) {

			int count = 0;

			for (ConnectionEndpoint connectionEndpoint : connectionEndpoints) {

				Socket socket = null;

				try {
					socket = connect(connectionEndpoint);
					count++;
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

		Socket connect(ConnectionEndpoint connectionEndpoint) throws IOException {

			SocketAddress socketAddress =
				new InetSocketAddress(connectionEndpoint.getHost(), connectionEndpoint.getPort());

			Socket socket = new Socket();

			socket.connect(socketAddress, DEFAULT_TIMEOUT_IN_MILLISECONDS);

			return socket;
		}

		boolean close(Socket socket) {

			return ObjectUtils.<Boolean>doOperationSafely(() -> {

				if (socket != null) {
					socket.close();
					return true;
				}

				return false;

			}, cause -> false);
		}

		void configureTopology(Environment environment, ConnectionEndpointList connectionEndpoints,
				int connectionCount) {

			if (connectionCount < 1) {
				if (!environment.containsProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY)) {
					System.setProperty(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY,
						LOCAL_CLIENT_REGION_SHORTCUT.name());
				}
			}
		}

		boolean isMatch(ConnectionEndpointList connectionEndpoints, int connectionCount) {
			return connectionCount > 0;
		}
	}
}
