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
package org.springframework.geode.cache.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.internal.HttpService;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.internal.cache.CacheService;
import org.apache.geode.internal.net.SSLConfig;
import org.apache.geode.internal.net.SSLConfigurationFactory;
import org.apache.geode.internal.net.SSLUtil;
import org.apache.geode.internal.security.SecurableCommunicationChannel;
import org.apache.geode.management.internal.beans.CacheServiceMBeanBase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.geode.cache.service.support.JakartaEEMigrationService;
import org.springframework.geode.util.CacheUtils;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.SymlinkAllowedResourceAliasChecker;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.ClassMatcher;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * An Apache Geode {@link HttpService} implementation using Eclipse Jetty 11 HTTP server and Servlet container.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.internal.HttpService
 * @see org.apache.geode.distributed.internal.DistributionConfig
 * @see org.apache.geode.distributed.internal.InternalDistributedSystem
 * @see org.apache.geode.internal.cache.CacheService
 * @see org.apache.geode.internal.net.SSLConfig
 * @see org.apache.geode.internal.security.SecurableCommunicationChannel
 * @see org.eclipse.jetty.server.HttpConfiguration
 * @see org.eclipse.jetty.server.Server
 * @see org.eclipse.jetty.webapp.WebAppContext
 * @since 2.0.0
 */
public class Jetty11HttpService implements HttpService {

	private static final boolean JETTY_WEBAPP_PARENT_LOADER_PRIORITY = false;
	private static final boolean SKIP_SSL_VERIFICATION = false;

	private static final String APACHE_GEODE_ANY_SSL_CIPHERS = "any";
	private static final String APACHE_GEODE_CONFIGURATION_ATTRIBUTE_NAME = "apache.geode.cache.configuration";
	private static final String APACHE_GEODE_JETTY_THREAD_POOL_NAME = "ApacheGeode-EclipseJetty-ThreadPool";
	private static final String UNDERSCORE = "_";

	private static <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
		return map != null ? map : Collections.emptyMap();
	}

	private static String nullSafeString(String value, String defaultValue) {
		return StringUtils.isNotBlank(value) ? value : String.valueOf(defaultValue);
	}

	private static <T> T requireObject(T object, String message, Object... args) {

		if (object == null) {
			throw new IllegalArgumentException(String.format(message, args));
		}

		return object;
	}

	private static <T> Supplier<T> toSupplier(Supplier<T> lambda) {
		return lambda;
	}

	private final List<WebAppContext> webApplications = new CopyOnWriteArrayList<>();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private volatile Server server;

	/**
	 * @inheritDoc
	 */
	@Override
	public Class<? extends CacheService> getInterface() {
		return HttpService.class;
	}

	/**
	 * Return a reference to the configured SLF4J {@link Logger}.
	 *
	 * @return a reference to the configured SLF4J {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected Logger getLogger() {
		return this.logger;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public CacheServiceMBeanBase getMBean() {
		return null;
	}

	/**
	 * Gets a reference to the configured and initialized Eclipse Jetty HTTP server and Servlet container.
	 *
	 * @return a reference to the configured and initialized Eclipse Jetty HTTP server and Servlet container;
	 * may be {@literal null} if the {@link Server} has not yet been initialized.
	 * @see org.eclipse.jetty.server.Server
	 * @see #getOptionalServer()
	 * @see #init(Cache)
	 */
	protected Server getServer() {
		return this.server;
	}

	/**
	 * Gets an {@link Optional} reference to the configured and initialized Eclipse Jetty HTTP server
	 * and Servlet container.
	 *
	 * @return an {@link Optional} reference to the configured and initialized Eclipse Jetty HTTP server
	 * and Servlet container.
	 * @see org.eclipse.jetty.server.Server
	 * @see java.util.Optional
	 * @see #getServer()
	 */
	protected Optional<Server> getOptionalServer() {
		return Optional.ofNullable(getServer());
	}

	/**
	 * Gets a reference to the {@link List} of {@link WebAppContext Web applications} being run on
	 * this Jetty HTTP server.
	 *
	 * @return a reference to the {@link List} of {@link WebAppContext Web applications} being run on
	 * this Jetty HTTP server.
	 * @see org.eclipse.jetty.webapp.WebAppContext
	 * @see java.util.List
	 */
	protected List<WebAppContext> getWebApplications() {
		return Collections.unmodifiableList(this.webApplications);
	}

	/**
	 * Initializes the internal, embedded Apache Geode {@link HttpService} by creating an instance of
	 * the Eclipse Jetty 11 HTTP server and Servlet container.
	 *
	 * @param cache reference to the {@literal peer} {@link Cache} instance
	 * in which this embedded {@link HttpService} will be running.
	 * @return a boolean value indicating whether the Eclipse Jetty 11 based {@link HttpService} constructed,
	 * configured and initialized.
	 * @see org.apache.geode.cache.Cache
	 */
	@Override
	public boolean init(Cache cache) {

		return Optional.ofNullable(cache)
			.filter(CacheUtils::isPeerCache)
			.map(this::resolveDistributedSystem)
			.map(InternalDistributedSystem::getConfig)
			.filter(this::isHttpServiceEnabled)
			.map(this::initializeHttpServiceServer)
			.isPresent();
	}

	private InternalDistributedSystem resolveDistributedSystem(Cache cache) {

		return Optional.ofNullable(cache)
			.map(GemFireCache::getDistributedSystem)
			.filter(InternalDistributedSystem.class::isInstance)
			.map(InternalDistributedSystem.class::cast)
			.orElse(null);
	}

	private boolean isHttpServiceEnabled(DistributionConfig configuration) {

		int httpServicePort = configuration.getHttpServicePort();

		boolean httpServiceEnabled = httpServicePort > -1;

		if (!httpServiceEnabled) {
			logInfo("Apache Geode's embedded HttpService is disabled;"
				+ " {} is set to [{}]", DistributionConfig.HTTP_SERVICE_PORT_NAME, httpServicePort);
		}

		return httpServiceEnabled;
	}

	private Server initializeHttpServiceServer(DistributionConfig configuration) {

		Server server = new Server(newThreadPool(configuration));

		server.addConnector(newConnector(configuration, server));
		server.setAttribute(APACHE_GEODE_CONFIGURATION_ATTRIBUTE_NAME, configuration);
		server.setHandler(new HandlerCollection(true));

		logInfo("Initializing Apache Geode's embedded HTTP service with the Jetty {} Server...",
			toSupplier(Server::getVersion));

		this.server = server;

		return server;
	}

	@SuppressWarnings("unused")
	private ThreadPool newThreadPool(DistributionConfig configuration) {

		QueuedThreadPool threadPool = new QueuedThreadPool();

		threadPool.setName(APACHE_GEODE_JETTY_THREAD_POOL_NAME);

		return threadPool;
	}

	private Connector newConnector(DistributionConfig configuration, Server server) {

		String httpServiceBindAddress = configuration.getHttpServiceBindAddress();

		int httpServicePort = configuration.getHttpServicePort();

		logInfo("Apache Geode's embedded HTTP service will run on host [{}] and listen on port [{}]",
			httpServiceBindAddress, httpServicePort);

		ConnectionFactory[] connectionFactories =
			newConnectionFactories(configuration).toArray(new ConnectionFactory[0]);

		ServerConnector connector = new ServerConnector(server, connectionFactories);

		connector.setHost(httpServiceBindAddress);
		connector.setPort(httpServicePort);

		return connector;
	}

	private List<ConnectionFactory> newConnectionFactories(DistributionConfig configuration) {

		List<ConnectionFactory> connectionFactories = new ArrayList<>();

		HttpConnectionFactory httpConnectionFactory = newHttpConnectionFactory(configuration);

		newSslConnectionFactory(configuration, httpConnectionFactory)
			.ifPresent(connectionFactories::add);

		connectionFactories.add(httpConnectionFactory);

		return connectionFactories;
	}

	private HttpConnectionFactory newHttpConnectionFactory(DistributionConfig configuration) {

		HttpConfiguration httpConfiguration = new HttpConfiguration();

		httpConfiguration.setSecurePort(configuration.getHttpServicePort());

		return new HttpConnectionFactory(httpConfiguration);
	}

	private Optional<SslConnectionFactory> newSslConnectionFactory(DistributionConfig configuration,
			HttpConnectionFactory httpConnectionFactory) {

		SSLConfig sslConfiguration =
			SSLConfigurationFactory.getSSLConfigForComponent(configuration, SecurableCommunicationChannel.WEB);

		if (sslConfiguration.isEnabled()) {

			SslContextFactory.Server serverSslContextFactory = new SslContextFactory.Server();

			Optional.ofNullable(sslConfiguration.getAlias())
				.filter(StringUtils::isNotBlank)
				.ifPresent(serverSslContextFactory::setCertAlias);

			Optional.ofNullable(sslConfiguration.getCiphers())
				.filter(this::isSslCiphersConfigured)
				.ifPresent(ciphers -> {
					serverSslContextFactory.setExcludeCipherSuites();
					serverSslContextFactory.setIncludeCipherSuites(SSLUtil.readArray(ciphers));
				});

			serverSslContextFactory.setNeedClientAuth(sslConfiguration.isRequireAuth());
			serverSslContextFactory.setSslContext(SSLUtil.createAndConfigureSSLContext(sslConfiguration,
				SKIP_SSL_VERIFICATION));

			httpConnectionFactory.getHttpConfiguration().addCustomizer(new SecureRequestCustomizer());

			logDebug("SSL configuration [{}] for protocol [{}]",
				toSupplier(serverSslContextFactory::dump), toSupplier(httpConnectionFactory::getProtocol));

			return Optional.of(new SslConnectionFactory(serverSslContextFactory, httpConnectionFactory.getProtocol()));
		}

		return Optional.empty();
	}

	private boolean isSslCiphersConfigured(String sslCiphers) {
		return StringUtils.isNotBlank(sslCiphers)
			&& !APACHE_GEODE_ANY_SSL_CIPHERS.equalsIgnoreCase(sslCiphers.trim());
	}

	/**
	 * Adds Web applications to Apache Geode's embedded HTTP service (HTTP server) making them available for service.
	 *
	 * @param contextPath {@link String} containing the Web application context path
	 * in which to bind the Web application.
	 * @param warFilePath {@link Path} to the Java Web Application Archive (WAR) file.
	 * @param attributeNameValuePairs {@link Map} of Web application, {@link jakarta.servlet.ServletContext}
	 * attributes to set in the {@link WebAppContext}.
	 * @see org.eclipse.jetty.webapp.WebAppContext
	 * @see org.eclipse.jetty.server.Server
	 * @see #getOptionalServer()
	 */
	@Override
	public void addWebApplication(String contextPath, Path warFilePath, Map<String, Object> attributeNameValuePairs) {

		getOptionalServer().map(server -> {

			logInfo("Adding Web application from path [{}] using context [{}]"
				+ " to Apache Geode's embedded HTTP service", warFilePath, contextPath);

			Path resolveWarFilePath = JakartaEEMigrationService.INSTANCE.migrate(warFilePath);

			logInfo("Resolved WAR file path [{}]", resolveWarFilePath);

			WebAppContext webAppContext =
				getWebAppContextConfigurationFunction().apply(newWebAppContext(server, resolveWarFilePath, contextPath));

			webAppContext.setAttribute("org.eclipse.jetty.websocket.jakarta", false);

			nullSafeMap(attributeNameValuePairs)
				.forEach(webAppContext::setAttribute);

			((HandlerCollection) server.getHandler()).addHandler(webAppContext);

			startWebApplication(server, webAppContext);

			return true;
		})
		.orElseGet(() -> {

			logInfo("Unable to add Web application from path [{}] using context [{}]"
				+ " since the Apache Geode embedded HTTP service was not enabled", warFilePath, contextPath);

			return false;
		});
	}

	private WebAppContext newWebAppContext(Server server, Path warFilePath, String contextPath) {

		Resource webApp = new PathResource(requireObject(warFilePath,
			String.format("WAR file path of the Web application [%s] to add must not be null", contextPath)));

		WebAppContext webAppContext = new WebAppContext(webApp, contextPath);

		webAppContext.addAliasCheck(new SymlinkAllowedResourceAliasChecker(webAppContext));
		webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		webAppContext.setParentLoaderPriority(JETTY_WEBAPP_PARENT_LOADER_PRIORITY);
		webAppContext.setServer(server);

		return webAppContext;
	}

	private Function<WebAppContext, WebAppContext> getWebAppContextConfigurationFunction() {

		Function<WebAppContext, WebAppContext> function = this::configureWebApplicationClasspath;

		return function.andThen(this::configureWebApplicationTempDirectory);
	}

	private WebAppContext configureWebApplicationClasspath(WebAppContext webAppContext) {

		ClassMatcher classMatcher = webAppContext.getServerClassMatcher();

		classMatcher.include("com.fasterxml.jackson.");
		classMatcher.exclude("com.fasterxml.jackson.annotation.");

		File workingDirectory = new File(System.getProperty("user.dir")).getAbsoluteFile();

		webAppContext.setExtraClasspath(Collections.singletonList(new PathResource(workingDirectory)));

		return webAppContext;
	}

	private WebAppContext configureWebApplicationTempDirectory(WebAppContext webAppContext) {

		DistributionConfig configuration = requireObject((DistributionConfig)
			webAppContext.getServer().getAttribute(APACHE_GEODE_CONFIGURATION_ATTRIBUTE_NAME),
			"DistributionConfig was not stored in the Server Attributes");

		String contextPath = nullSafeString(webAppContext.getContextPath(), "defaultContext");

		contextPath = (contextPath.startsWith(File.separator) ? contextPath.substring(1) : contextPath)
			.replace(File.separator, UNDERSCORE);

		String hostPort = nullSafeString(configuration.getHttpServiceBindAddress(), "0.0.0.0")
			.concat(UNDERSCORE)
			.concat(String.valueOf(configuration.getHttpServicePort()));

		String uuid = UUID.randomUUID().toString().substring(0, 8);

		String[] tempDirectoryPathElements = {
			"temp",
			System.getProperty("user.name"),
			"geode",
			"services",
			"http",
			hostPort,
			contextPath,
			uuid
		};

		Path tempDirectoryPath = FileSystems.getDefault()
			.getPath(System.getProperty("user.dir"), tempDirectoryPathElements);

		File tempDirectory = tempDirectoryPath.toFile();

		tempDirectory.mkdirs();
		tempDirectory.deleteOnExit();
		webAppContext.setTempDirectory(tempDirectory);

		return webAppContext;
	}

	private WebAppContext startWebApplication(Server server, WebAppContext webApplication) {

		logInfo("Starting Web application in context [{}]...", webApplication.getContextPath());

		// Lazily start the Jetty HTTP server on first Web application start,
		// which will start all the added Web applications
		if (!server.isStarted()) {
			SafeServerWrapper.from(server).safeStart();
		}
		else {
			SafeWebApplicationWrapper.from(webApplication).safeStart();
		}

		this.webApplications.add(webApplication);

		return webApplication;
	}

	/**
	 * Stops Apache Geode's internal, embedded {@link HttpService}.
	 */
	@Override
	public void close() {

		logInfo("Closing Apache Geode's embedded HTTP service...");

		getWebApplications().stream()
			.map(SafeWebApplicationWrapper::from)
			.forEach(SafeWebApplicationWrapper::safeStop);

		getOptionalServer()
			.map(SafeServerWrapper::from)
			.ifPresent(SafeServerWrapper::safeStopAndDestroy);

		File tempDirectory = new File(System.getProperty("user.dir"), "temp");

		try {
			FileUtils.deleteDirectory(tempDirectory);
		}
		catch (IOException cause) {
			logWarn(cause, "Failed to delete the temp directory [{}]", tempDirectory);
		}
	}

	private void log(Predicate<Logger> loggerPredicate, Consumer<Logger> loggerConsumer) {

		Logger logger = getLogger();

		if (loggerPredicate.test(logger)) {
			loggerConsumer.accept(logger);
		}
	}

	private void logDebug(String message, Object... arguments) {
		log(Logger::isDebugEnabled, it -> it.debug(message, resolveArguments(arguments)));
	}

	private void logInfo(String message, Object... arguments) {
		log(Logger::isInfoEnabled, it -> it.info(message, resolveArguments(arguments)));
	}

	private void logWarn(Throwable cause, String message, Object... arguments) {
		log(Logger::isWarnEnabled, it ->
			it.warn(MessageFormatter.format(message, resolveArguments(arguments)).getMessage(), cause));
	}

	private Object[] resolveArguments(Object... arguments) {

		List<Object> resolvedArguments = new ArrayList<>(arguments.length);

		for (Object argument : arguments) {
			Object resolvedArgument = (argument instanceof Supplier<?> supplier) ? supplier.get() : argument;
			resolvedArguments.add(resolvedArgument);
		}

		return resolvedArguments.toArray();
	}

	@SuppressWarnings("unused")
	protected static class SafeServerWrapper extends Server {

		public static SafeServerWrapper from(Server server) {
			return new SafeServerWrapper(server);
		}

		private final Logger logger = LoggerFactory.getLogger(Jetty11HttpService.class);

		private final Server server;

		private SafeServerWrapper(Server server) {
			this.server = requireObject(server, "Server must not be null");
		}

		protected Logger getLogger() {
			return this.logger;
		}

		public void safeStart() {

			Server serverReference = this.server;

			try {
				serverReference.start();
			}
			catch (Exception cause) {
				throw new ServerException(String.format("Failed to start HTTP server [%s]",
					serverReference), cause);
			}
		}

		public void safeStop() {

			Server serverReference = this.server;

			try {
				serverReference.stop();
			}
			catch (Exception cause) {
				getLogger().warn("Failed to stop HTTP server [{}}]", serverReference);
				getLogger().debug("", cause);
			}
		}

		public void safeDestroy() {

			Server serverReference = this.server;

			try {
				serverReference.destroy();
			}
			catch (Throwable cause) {
				getLogger().warn("Failed to release system resources used by HTTP server [{}]", serverReference);
				getLogger().debug("", cause);
			}
		}

		public void safeStopAndDestroy() {
			safeStop();
			safeDestroy();
		}
	}

	protected static class SafeWebApplicationWrapper extends WebAppContext {

		protected static SafeWebApplicationWrapper from(WebAppContext webAppContext) {
			return new SafeWebApplicationWrapper(webAppContext);
		}

		private final Logger logger = LoggerFactory.getLogger(Jetty11HttpService.class);

		private final WebAppContext webAppContext;

		private SafeWebApplicationWrapper(WebAppContext webAppContext) {
			this.webAppContext = requireObject(webAppContext, "WebAppContext must not be null");
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public Logger getLogger() {
			return this.logger;
		}

		public void safeStart() {

			try {
				this.webAppContext.start();
				getLogger().info("Started Web application in context [{}]", this.webAppContext.getContextPath());
			}
			catch (Exception cause) {

				getLogger().error("Failed to start Web application in context [{}]",
					this.webAppContext.getContextPath(), cause);

				throw new WebApplicationException(String.format("Failed to start Web application in context [%s]",
					this.webAppContext.getContextPath()), cause);
			}
		}

		public void safeStop() {

			try {
				this.webAppContext.stop();
			}
			catch (Exception cause) {
				getLogger().warn("Failed to stop Web application in context [{}]", this.webAppContext.getContextPath());
				getLogger().debug("", cause);
			}
		}
	}

	@SuppressWarnings("unused")
	protected static class JettyException extends RuntimeException {

		protected JettyException() { }

		protected JettyException(String message) {
			super(message);
		}

		protected JettyException(Throwable cause) {
			super(cause);
		}

		protected JettyException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	@SuppressWarnings("unused")
	protected static class ServerException extends JettyException {

		public ServerException() { }

		public ServerException(String message) {
			super(message);
		}

		public ServerException(Throwable cause) {
			super(cause);
		}

		public ServerException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	@SuppressWarnings("unused")
	protected static class WebApplicationException extends JettyException {

		protected WebApplicationException() { }

		protected WebApplicationException(String message) {
			super(message);
		}

		protected WebApplicationException(Throwable cause) {
			super(cause);
		}

		protected WebApplicationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
