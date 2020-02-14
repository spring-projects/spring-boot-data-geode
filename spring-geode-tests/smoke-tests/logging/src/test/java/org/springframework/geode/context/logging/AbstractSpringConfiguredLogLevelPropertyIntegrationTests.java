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
package org.springframework.geode.context.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.logging.slf4j.logback.StringAppender;
import org.springframework.geode.logging.slf4j.logback.support.LogbackSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class containing functionality common to all Spring {@literal log-level} property Integration Tests.
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.logging.slf4j.logback.StringAppender
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSpringConfiguredLogLevelPropertyIntegrationTests extends IntegrationTestsSupport {

	@Test
	public void debugLogStatementsIsLogged() {
		assertThat(getStringAppender().getLogOutput()).containsSequence("DEBUG TEST");
	}

	@Test
	public void traceLogStatementIsNotLogged() {
		assertThat(getStringAppender().getLogOutput()).doesNotContain("TRACE TEST");
	}

	protected Logger getOrgApacheGeodeLogger() {
		return GeodeConfiguration.logger;
	}

	protected StringAppender getStringAppender() {
		return GeodeConfiguration.stringAppender;
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class GeodeConfiguration {

		// NOTE: The 'org.apache.geode' Logger declaration must be in this GeodeConfiguration class so Spring Boot
		// LoggingSystem auto-configuration has a opportunity to configure logging and the logging provider first!
		// This is needed by SBDG since the GeodeLoggingApplicationListener must also run!
		private static final Logger logger = LoggerFactory.getLogger("org.apache.geode");

		private static final StringAppender stringAppender = new StringAppender.Builder().buildAndStart();

		static {
			LogbackSupport.toLogbackLogger(logger).ifPresent(it -> {
				LogbackSupport.removeConsoleAppender(it);
				LogbackSupport.addAppender(it, stringAppender);
			});
		}

		@Bean
		ApplicationRunner runner() {
			return args -> {
				logger.debug("DEBUG TEST");
				logger.trace("TRACE TEST");
			};
		}
	}
}
