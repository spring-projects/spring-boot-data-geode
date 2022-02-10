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
package example.app.caching.lookaside;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.annotation.Resource;

import org.apache.geode.cache.Region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.caching.lookaside.service.CounterService;

/**
 * Integration Tests for the Counter Service application.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.caching.lookaside.service.CounterService
 * @since 1.1.0
 */
// tag::class[]
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = { "spring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=false" },
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@SuppressWarnings("unused")
public class BootGeodeLookAsideCachingApplicationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private CounterService counterService;

	@Resource(name = "Counters")
	private Region<String, Long> counters;

	@Before
	public void setup() {

		assertThat(this.counterService).isNotNull();
		assertThat(this.counters).isNotNull();
		assertThat(this.counters.getName()).isEqualTo("Counters");
		assertThat(this.counters).isEmpty();
	}

	@Test
	public void counterServiceCachesCounts() {

		for (int count = 1; count < 10; count++) {
			assertThat(this.counterService.getCount("TestCounter")).isEqualTo(count);
		}

		assertThat(this.counterService.getCachedCount("TestCounter")).isEqualTo(9L);
		assertThat(this.counterService.getCachedCount("TestCounter")).isEqualTo(9L);
		assertThat(this.counterService.getCachedCount("MockCounter")).isEqualTo(1L);
		assertThat(this.counterService.getCachedCount("MockCounter")).isEqualTo(1L);
	}
}
// end::class[]
