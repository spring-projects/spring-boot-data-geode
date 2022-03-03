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
package example.app.geode.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Smoke Tests for {@link ApacheGeodeLoggingApplication}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings("unused")
public class ApacheGeodeLoggingApplicationSmokeTests extends IntegrationTestsSupport {

	@Autowired
	private ApacheGeodeLoggingApplication.Log log;

	@Test
	public void springApplicationLogsContent() {
		assertThat(this.log.getContent()).containsSequence("RUNNER RAN!");
	}

	@Test
	public void debugLogStatementNotLogged() {
		assertThat(this.log.getContent()).doesNotContain("DEBUG TEST");
	}

	@Test
	public void apacheGeodeLogsContent() {
		assertThat(this.log.getContent()).containsSequence("Product-Name: Apache Geode");
	}
}
