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

import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;

/**
 * Integration Tests testing the {@literal org.apache.geode} {@link org.slf4j.Logger}
 * with no logging enabled, i.e. {@link Level#OFF}.
 *
 * <code>
 *   -Dspring.boot.data.gemfire.log.level=DEBUG
 * </code>
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @see org.springframework.geode.logging.slf4j.logback.AbstractLoggingIntegrationTests
 * @see ch.qos.logback.classic.Level#OFF
 * @since 1.3.0
 */
public class NoLoggingIntegrationTests extends AbstractLoggingIntegrationTests {

	private static final Level TEST_LOG_LEVEL = Level.OFF;

	@BeforeClass
	public static void setupLogback() {
		setupLogback(TEST_LOG_LEVEL);
	}

	@Test
	public void logLevelIsSetToOff() {
		assertApacheGeodeLoggerLogLevel(TEST_LOG_LEVEL);
	}

	@Test
	public void logsNoMessages() {
		assertLogMessages();
	}
}
