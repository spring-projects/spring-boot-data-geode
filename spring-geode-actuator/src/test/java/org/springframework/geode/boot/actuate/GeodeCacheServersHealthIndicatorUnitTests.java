/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.actuate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.server.CacheServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * The GeodeCacheServersHealthIndicatorUnitTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeCacheServersHealthIndicatorUnitTests {

	@Mock
	private Cache mockCache;

	private GeodeCacheServersHealthIndicator cacheServersHealthIndicator;

	@Before
	public void setup() {
		this.cacheServersHealthIndicator = new GeodeCacheServersHealthIndicator(this.mockCache);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		List<CacheServer> mockCacheServers = new ArrayList<>();

		when(this.mockCache.getCacheServers()).thenReturn(mockCacheServers);

		Health.Builder builder = new Health.Builder();

		this.cacheServersHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> details = health.getDetails();

		assertThat(details).isNotNull();
		assertThat(details).isNotEmpty();

		verify(this.mockCache, times(1)).getCacheServers();
	}
}
