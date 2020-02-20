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
package example.app.geode.logging;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.geode.logging.slf4j.logback.DelegatingAppender;
import org.springframework.geode.logging.slf4j.logback.StringAppender;
import org.springframework.geode.logging.slf4j.logback.support.LogbackSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * {@link SpringBootApplication Spring Boot application} used to assert and observe the log output from
 * Apache Geode Logging.
 *
 * Use the following JVM {@link System} {@link java.util.Properties Property} to adjust {@link Logger} log level
 * and log output:
 *
 * <code>
 *     -Dlogback.root.log.level=INFO
 *     -Dspring.boot.data.gemfire.log.level=INFO
 * </code>
 *
 * Use the following JVM {@link System} {@link java.util.Properties Property} to configure a custom Logback
 * log configuration file:
 *
 * <code>
 *      -Dlogback.configurationFile=logback.xml
 * </code>
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.geode.logging.slf4j.logback.DelegatingAppender
 * @see org.springframework.geode.logging.slf4j.logback.StringAppender
 * @see org.springframework.geode.logging.slf4j.logback.support.LogbackSupport
 * @see ch.qos.logback.classic.LoggerContext
 * @since 1.3.0
 */
@SpringBootApplication
@SuppressWarnings({ "rawtypes", "unused" })
public class ApacheGeodeLoggingApplication {

	private static boolean ENABLE_PRINT_LOG = false;

	private static final StringAppender stringAppender;

	static {
		stringAppender = configureAndInitializeLoggingSystem();
	}

	@SuppressWarnings("unchecked")
	private static StringAppender configureAndInitializeLoggingSystem() {

		LogbackSupport.resetLogback();

		LoggerContext loggerContext = LogbackSupport.requireLoggerContext();

		LogbackSupport.suppressSpringBootLogbackInitialization();

		ch.qos.logback.classic.Logger rootLogger = LogbackSupport.requireLogbackRootLogger();

		LogbackSupport.removeConsoleAppender(rootLogger);

		DelegatingAppender delegatingAppender =
			LogbackSupport.requireAppender(rootLogger, "delegate", DelegatingAppender.class);

		return new StringAppender.Builder()
			.applyTo(delegatingAppender)
			.setContext(loggerContext)
			.buildAndStart();
	}

	// Customizes the configuration of SLF4J using the Logback logging provider provided in
	// the 'spring-geode-starter-logging' module.
	public static void main(String[] args) {

		ENABLE_PRINT_LOG = true;

		new SpringApplicationBuilder(ApacheGeodeLoggingApplication.class)
			//.profiles("logging")
			.build()
			.run(args);
	}

	private final Logger logger = LoggerFactory.getLogger("org.apache.geode");

	@Bean
	ApplicationRunner runner() {

		return args -> {

			this.logger.info("RUNNER RAN!");
			this.logger.debug("DEBUG TEST");

			if (ENABLE_PRINT_LOG) {
				printLog();
			}

		};
	}

	@Bean
	Log log() {
		return this::logToString;
	}

	private String logToString() {
		return stringAppender.getLogOutput();
	}

	private void printLog() {
		System.out.printf("LOG [%s]%n", logToString());
	}

	@FunctionalInterface
	interface Log {
		String getContent();
	}
}
