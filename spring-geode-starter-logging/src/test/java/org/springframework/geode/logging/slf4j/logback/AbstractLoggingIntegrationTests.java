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

import java.util.Optional;

import org.junit.After;
import org.junit.Before;

import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;

/**
 * Abstract base class for testing the spring-geode-starter-logging and spring-gemfire-starter modules.
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @see ch.qos.logback.classic.Level
 * @see ch.qos.logback.classic.LoggerContext
 * @see ch.qos.logback.classic.util.ContextInitializer
 * @see ch.qos.logback.core.Appender
 * @since 1.3.0
 */
public abstract class AbstractLoggingIntegrationTests extends IntegrationTestsSupport {

	protected static final String APACHE_GEODE_LOGGER_NAME = "org.apache.geode";
	protected static final String SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY = "spring.boot.data.gemfire.log.level";

	private Logger apacheGeodeLogger = LoggerFactory.getLogger(APACHE_GEODE_LOGGER_NAME);

	private TestAppender testAppender;

	protected TestAppender getTestAppender() {

		assertThat(this.testAppender).describedAs("TestAppender could not be resolved").isNotNull();

		return this.testAppender;
	}

	protected Level getTestLogLevel() {
		return Level.INFO;
	}

	public void assertApacheGeodeLoggerLogLevel(Level logLevel) {

		Optional.ofNullable(this.apacheGeodeLogger)
			.filter(ch.qos.logback.classic.Logger.class::isInstance)
			.map(ch.qos.logback.classic.Logger.class::cast)
			.map(logger -> {
				assertThat(logger.getLevel()).isEqualTo(logLevel);
				return logger;
			})
			.orElseThrow(() -> newIllegalStateException("'org.apache.geode' Logger not found"));
	}

	@Before
	public void setup() {

		configureLogging();
		configureRootLoggerDelegatingAppender();
		logMessages();
	}

	private void configureLogging() {

		System.setProperty(SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY, getTestLogLevel().toString());

		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

		assertThat(loggerFactory).isInstanceOf(LoggerContext.class);

		LoggerContext loggerContext = (LoggerContext) loggerFactory;

		try {
			new ContextInitializer(loggerContext).autoConfig();
		}
		catch (Exception cause) {
			throw newIllegalStateException("Failed to configure and initialize SLF4J/Logback logging context", cause);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void configureRootLoggerDelegatingAppender() {

		Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		assertThat(rootLogger).isInstanceOf(ch.qos.logback.classic.Logger.class);

		ch.qos.logback.classic.Logger logbackRootLogger = (ch.qos.logback.classic.Logger) rootLogger;

		Appender<?> delegateAppender = logbackRootLogger.getAppender("delegate");

		assertThat(delegateAppender).isNotNull();
		assertThat(delegateAppender.getName()).isEqualTo("delegate");

		this.testAppender = new TestAppender();
		this.testAppender.start();

		((DelegatingAppender) delegateAppender).setAppender(this.testAppender);

		assertThat(((DelegatingAppender) delegateAppender).getAppender()).isSameAs(this.testAppender);
	}

	public void logMessages() {

		assertThat(this.apacheGeodeLogger).isNotNull();

		this.apacheGeodeLogger.debug("DEBUG TEST");
		this.apacheGeodeLogger.info("INFO TEST");
		this.apacheGeodeLogger.error("ERROR TEST");
	}

	@After
	public void tearDown() {

		Optional.ofNullable(this.testAppender).ifPresent(it -> {
			it.clear();
			it.stop();
		});

		System.clearProperty(SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY);
	}
}
