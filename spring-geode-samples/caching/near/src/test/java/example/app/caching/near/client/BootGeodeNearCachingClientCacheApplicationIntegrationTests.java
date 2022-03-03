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
package example.app.caching.near.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.geode.cache.client.ClientCache;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.caching.near.client.model.Person;
import example.app.caching.near.client.service.YellowPagesService;
import example.app.caching.near.server.BootGeodeNearCachingCacheServerApplication;

/**
 * Integration Tests for the Spring Boot, {@link ClientCache} application example for {@literal Near Caching}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.caching.near.client.model.Person
 * @see example.app.caching.near.client.service.YellowPagesService
 * @see example.app.caching.near.server.BootGeodeNearCachingCacheServerApplication
 * @since 1.1.0
 */
@ActiveProfiles("client")
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = { "spring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=false" },
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@SuppressWarnings("unused")
public class BootGeodeNearCachingClientCacheApplicationIntegrationTests
		extends ForkingClientServerIntegrationTestsSupport {

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		startGemFireServer(BootGeodeNearCachingCacheServerApplication.class,
			"-Dspring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=false",
						"-Dspring.profiles.active=server");
	}

	@Autowired
	private YellowPagesService yellowPagesService;

	@Test
	public void cachingIsInEffect() {

		assertThat(this.yellowPagesService.isCacheMiss()).isFalse();

		Person jonDoe = this.yellowPagesService.find("JonDoe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("JonDoe");
		assertThat(this.yellowPagesService.isCacheMiss()).isTrue();

		Person jonDoeCopy = this.yellowPagesService.find(jonDoe.getName());

		assertThat(jonDoeCopy).isEqualTo(jonDoe);
		assertThat(this.yellowPagesService.isCacheMiss()).isFalse();
	}
}
