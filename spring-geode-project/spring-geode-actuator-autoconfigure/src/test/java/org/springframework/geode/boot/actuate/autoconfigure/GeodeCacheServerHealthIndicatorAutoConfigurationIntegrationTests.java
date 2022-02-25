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

package org.springframework.geode.boot.actuate.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.server.ServerLoad;
import org.apache.geode.cache.server.ServerLoadProbe;
import org.apache.geode.cache.server.ServerMetrics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.server.CacheServerFactoryBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.CacheServerMockObjects;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.actuate.GeodeCacheServersHealthIndicator;
import org.springframework.geode.boot.actuate.health.support.ActuatorServerLoadProbeWrapper;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The GeodeCacheServerHealthIndicatorAutoConfigurationIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class GeodeCacheServerHealthIndicatorAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final String GEODE_LOG_LEVEL = "error";

	@Autowired
	private GeodeCacheServersHealthIndicator healthIndicator;

	@Test
	public void mockCacheServerHealthCheckWithServerLoadDetails() {

		Health health = this.healthIndicator.health();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.cache.server.count", 1);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.port", 48484);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.load.connection-load", 0.65f);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.load.load-per-connection", 0.35f);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.load.load-per-subscription-connection", 0.75f);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.load.subscription-connection-load", 0.55f);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.metrics.client-count", 21);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.metrics.max-connection-count", 800);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.metrics.open-connection-count", 400);
		assertThat(healthDetails).containsEntry("geode.cache.server.0.metrics.subscription-connection-count", 200);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	@PeerCacheApplication(
		name = "GeodeCacheServerHealthIndicatorAutoConfigurationIntegrationTests",
		logLevel = GEODE_LOG_LEVEL
	)
	static class TestConfiguration {

		@Bean("MockCacheServer")
		CacheServerFactoryBean mockCacheServer(Cache gemfireCache) {

			CacheServerFactoryBean mockCacheServer = new CacheServerFactoryBean();

			mockCacheServer.setCache(gemfireCache);
			mockCacheServer.setPort(48484);
			mockCacheServer.setServerLoadProbe(mockServerLoadProbe());

			return mockCacheServer;
		}

		@Bean("MockServerLoadProbe")
		ServerLoadProbe mockServerLoadProbe() {
			return CacheServerMockObjects.mockServerLoadProbe();
		}

		@Bean
		ApplicationRunner runner(ServerLoadProbe mockServerLoadProbe,
				@Qualifier("MockCacheServer") CacheServer mockCacheServer) {

			return args -> {

				assertThat(mockCacheServer.getLoadProbe()).isInstanceOf(ActuatorServerLoadProbeWrapper.class);

				ServerMetrics mockServerMetrics = CacheServerMockObjects.mockServerMetrics(21,
					400, 800, 200);

				ServerLoad mockServerLoad = CacheServerMockObjects.mockServerLoad(0.65f,
					0.35f, 0.75f, 0.55f);

				when(mockServerLoadProbe.getLoad(eq(mockServerMetrics))).thenReturn(mockServerLoad);

				ServerLoadProbe serverLoadProbe = mockCacheServer.getLoadProbe();

				if (serverLoadProbe != null) {
					serverLoadProbe.getLoad(mockServerMetrics);
				}
			};
		}
	}
}
