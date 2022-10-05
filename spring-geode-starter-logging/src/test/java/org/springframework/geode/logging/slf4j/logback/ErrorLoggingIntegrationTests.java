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
 * with the log level {@link Level#ERROR}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @see org.springframework.geode.logging.slf4j.logback.ErrorLoggingIntegrationTests
 * @see ch.qos.logback.classic.Level#ERROR
 * @since 1.7.5
 */
public class ErrorLoggingIntegrationTests extends AbstractLoggingIntegrationTests {

  private static final Level TEST_LOG_LEVEL = Level.ERROR;

  @BeforeClass
  public static void setupLogback() {
    setupLogback(TEST_LOG_LEVEL);
  }

  @Test
  public void logLevelIsSetToDebug() {
    assertApacheGeodeLoggerLogLevel(TEST_LOG_LEVEL);
  }

  @Test
  public void logMessagesAtDebug() {
    assertLogMessages("ERROR TEST");
  }
}
