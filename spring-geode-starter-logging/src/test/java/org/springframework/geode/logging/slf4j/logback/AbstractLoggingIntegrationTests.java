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
package org.springframework.geode.logging.slf4j.logback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.AfterClass;
import org.junit.Before;

import org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.logging.slf4j.logback.support.LogbackSupport;
import org.springframework.util.StringUtils;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;

/**
 * Abstract base class for testing the spring-geode-starter-logging and spring-geode-starter modules.
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @see ch.qos.logback.classic.Logger
 * @see ch.qos.logback.classic.LoggerContext
 * @see ch.qos.logback.classic.util.ContextInitializer
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class AbstractLoggingIntegrationTests {

	private static final boolean STATUS_DEBUG_ENABLED = false;

	private static final Class<ch.qos.logback.classic.Logger> LOGBACK_LOGGER_TYPE =
		ch.qos.logback.classic.Logger.class;

	private static final Function<? super ch.qos.logback.classic.Logger, Level> LOGBACK_LOGGER_LEVEL =
		ch.qos.logback.classic.Logger::getLevel;

	protected static final String APACHE_GEODE_LOGGER_NAME = "org.apache.geode";
	protected static final String CONSOLE_APPENDER_NAME = "CONSOLE";
	protected static final String DELEGATE_APPENDER_NAME = "delegate";
	protected static final String SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY = "spring.boot.data.gemfire.log.level";

	private static TestAppender testAppender;

	private static ch.qos.logback.classic.Logger assertLogbackLogger(org.slf4j.Logger slf4jLogger,
			String loggerName, Level logLevel) {

		assertThat(slf4jLogger).isInstanceOf(LOGBACK_LOGGER_TYPE);
		assertThat(slf4jLogger.getName()).isEqualTo(loggerName);
		assertThat(slf4jLogger)
			.asInstanceOf(InstanceOfAssertFactories.type(LOGBACK_LOGGER_TYPE))
			.extracting(LOGBACK_LOGGER_LEVEL)
			.isEqualTo(logLevel);

		return LOGBACK_LOGGER_TYPE.cast(slf4jLogger);
	}

	private static LoggerContext assertLogbackLoggerConfiguration(LoggerContext loggerContext,
			String... loggerNames) {

		List<String> configuredLoggerNames = CollectionUtils.nullSafeList(loggerContext.getLoggerList()).stream()
			.filter(Objects::nonNull)
			.map(Logger::getName)
			.filter(StringUtils::hasText)
			.collect(Collectors.toList());

		assertThat(configuredLoggerNames).contains(loggerNames);

		return loggerContext;
	}

	private static ch.qos.logback.classic.Logger assertLogbackLoggerAppenderConfiguration(
			ch.qos.logback.classic.Logger logbackLogger, String... appenderNames) {

		Spliterator<Appender<?>> configuredTestLoggerAppenders =
			Spliterators.spliteratorUnknownSize(logbackLogger.iteratorForAppenders(), Spliterator.NONNULL);

		List<String> configuredTestLoggerAppenderNames =
			StreamSupport.stream(configuredTestLoggerAppenders, false)
				.filter(Objects::nonNull)
				.map(Appender::getName)
				.filter(StringUtils::hasText)
				.collect(Collectors.toList());

		assertThat(configuredTestLoggerAppenderNames).contains(appenderNames);

		return logbackLogger;
	}

	private static void log(String message, Object... args) {
		System.err.printf(message, args);
		System.err.flush();
	}

	private static ch.qos.logback.classic.Logger logConfiguredAppenders(ch.qos.logback.classic.Logger logger) {

		log("Configured Appenders: ");

		Spliterator<Appender<?>> appenders =
			Spliterators.spliteratorUnknownSize(logger.iteratorForAppenders(), Spliterator.NONNULL);

		log("%s%n", Arrays.toString(StreamSupport.stream(appenders, false)
			.filter(Objects::nonNull)
			.map(Appender::getName)
			.filter(StringUtils::hasText)
			.toArray(String[]::new)));

		return logger;
	}

	private static LoggerContext logConfiguredLoggers(LoggerContext loggerContext) {

		log("Configured Loggers: ");

		log("%s%n", Arrays.toString(loggerContext.getLoggerList().stream()
			.filter(Objects::nonNull)
			.map(Logger::getName)
			.filter(StringUtils::hasText)
			.toArray(String[]::new)));

		return loggerContext;
	}

	protected static void setupLogback(Level testLogLevel) {

		configureLogging(testLogLevel);
		configureRootLoggerDelegatingAppender();
	}

	@SuppressWarnings("all")
	private static void configureLogging(Level testLogLevel) {

		try {

			System.setProperty(SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY, testLogLevel.toString());

			LogbackSupport.resetLogback();

			LoggerContext loggerContext = LogbackSupport.requireLoggerContext();

			initializeLogback(loggerContext)
				.getStatusManager().add(LoggingStatusListener.create().debug(STATUS_DEBUG_ENABLED));

			logConfiguredLoggers(loggerContext);
			assertLogbackLoggerConfiguration(loggerContext,
				Logger.ROOT_LOGGER_NAME, APACHE_GEODE_LOGGER_NAME, "com.gemstone.gemfire", "org.jgroups");

			ch.qos.logback.classic.Logger logbackOrgApacheGeodeLogger = assertLogbackLogger(loggerContext
				.getLogger(APACHE_GEODE_LOGGER_NAME), APACHE_GEODE_LOGGER_NAME, testLogLevel);

			logConfiguredAppenders(logbackOrgApacheGeodeLogger);
			assertLogbackLoggerAppenderConfiguration(logbackOrgApacheGeodeLogger,
				CONSOLE_APPENDER_NAME, DELEGATE_APPENDER_NAME);
		}
		catch (Exception cause) {
			throw newIllegalStateException("Failed to configure and initialize SLF4J/Logback", cause);
		}
	}

	private static LoggerContext initializeLogback(LoggerContext loggerContext) throws JoranException {

		new ContextInitializer(loggerContext).autoConfig();

		return loggerContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void configureRootLoggerDelegatingAppender() {

		ch.qos.logback.classic.Logger logbackRootLogger =
			assertLogbackLogger(LogbackSupport.requireLogbackRootLogger(), Logger.ROOT_LOGGER_NAME, Level.ERROR);

		assertThat(logbackRootLogger.getAppender(CONSOLE_APPENDER_NAME)).isNull();

		Appender<?> delegateAppender = logbackRootLogger.getAppender(DELEGATE_APPENDER_NAME);

		assertThat(delegateAppender).isNotNull();
		assertThat(delegateAppender.getName()).isEqualTo(DELEGATE_APPENDER_NAME);

		TestAppender testAppender = initializeTestAppender();

		((DelegatingAppender) delegateAppender).setAppender(testAppender);

		assertThat(delegateAppender)
			.asInstanceOf(InstanceOfAssertFactories.type(DelegatingAppender.class))
			.extracting(DelegatingAppender::getAppender)
			.isSameAs(testAppender);
	}

	private static TestAppender initializeTestAppender() {

		testAppender = new TestAppender();
		testAppender.start();

		return testAppender;
	}

	@AfterClass
	public static void cleanupTestContext() {

		System.clearProperty(SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY);
		Optional.ofNullable(testAppender).ifPresent(TestAppender::clear);
	}

	private Logger apacheGeodeLogger;

	@Before
	public void initializeApacheGeodeLoggerReference() {
		this.apacheGeodeLogger = LoggerFactory.getLogger(APACHE_GEODE_LOGGER_NAME);
	}

	protected TestAppender getTestAppender() {

		assertThat(testAppender)
			.describedAs("TestAppender could not be resolved")
			.isNotNull();

		return testAppender;
	}

	protected void assertApacheGeodeLoggerLogLevel(Level logLevel) {

		Optional.ofNullable(this.apacheGeodeLogger)
			.filter(LOGBACK_LOGGER_TYPE::isInstance)
			.map(LOGBACK_LOGGER_TYPE::cast)
			.map(logger -> {

				assertThat(logger.getName()).isEqualTo(APACHE_GEODE_LOGGER_NAME);
				assertThat(logger.getLevel()).isEqualTo(logLevel);

				return logger;
			})
			.orElseThrow(() -> newIllegalStateException("'%s' Logger not found", APACHE_GEODE_LOGGER_NAME));
	}

	protected void assertLogMessages(String... logMessages) {

		logMessages();

		Arrays.stream(ArrayUtils.nullSafeArray(logMessages, String.class)).forEach(logMessage ->
			assertThat(getTestAppender().lastLogMessage()).isEqualTo(logMessage));

		assertThat(getTestAppender().lastLogMessage()).isNull();
	}

	protected void logMessages() {

		assertThat(this.apacheGeodeLogger).isNotNull();

		this.apacheGeodeLogger.trace("TRACE TEST");
		this.apacheGeodeLogger.debug("DEBUG TEST");
		this.apacheGeodeLogger.info("INFO TEST");
		this.apacheGeodeLogger.warn("WARN TEST");
		this.apacheGeodeLogger.error("ERROR TEST");
	}

	protected static class LoggingStatusListener implements StatusListener {

		protected static LoggingStatusListener create() {
			return new LoggingStatusListener();
		}

		private volatile boolean debug = false;

		protected boolean isDebugging() {
			return this.debug;
		}

		@Override
		public void addStatusEvent(Status status) {

			if (isDebugging()) {
				logStatus(status);
			}
		}

		protected LoggingStatusListener debug(boolean debug) {
			this.debug = debug;
			return this;
		}

		private void logStatus(Status status) {

			log("[STATUS] %s: %s%n", status.getOrigin(), status.getMessage());

			if (status.hasChildren()) {
				for (Status child : CollectionUtils.iterable(status.iterator())) {
					logStatus(child);
				}
			}
		}
	}
}
