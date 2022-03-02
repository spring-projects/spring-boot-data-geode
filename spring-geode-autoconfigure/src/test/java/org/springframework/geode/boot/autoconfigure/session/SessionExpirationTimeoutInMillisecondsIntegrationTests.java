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
package org.springframework.geode.boot.autoconfigure.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.Session;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for {@link Session} {@literal expiration timeout} in {@literal milliseconds}.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.geode.boot.autoconfigure.session.AbstractSessionExpirationTimeoutInTimeUnitIntegrationTests
 * @see org.springframework.session.Session
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 2.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = AbstractSessionExpirationTimeoutInTimeUnitIntegrationTests.TestConfiguration.class,
	properties = "spring.session.timeout=250ms",
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class SessionExpirationTimeoutInMillisecondsIntegrationTests
		extends AbstractSessionExpirationTimeoutInTimeUnitIntegrationTests {

	@Override
	protected int getExpectedMaxInactiveIntervalInSeconds() {
		int maxInactiveIntervalInSeconds = Long.valueOf(Duration.ofMillis(250).getSeconds()).intValue();
		assertThat(maxInactiveIntervalInSeconds).isZero();
		return maxInactiveIntervalInSeconds;
	}
}
