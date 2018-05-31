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

package org.springframework.geode.boot.autoconfigure.cache.peer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration test testing the auto-configuration of an Apache Geode peer {@link Cache} instance, overriding
 * the default {@link ClientCache} instance.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class SpringBootApacheGeodePeerCacheApplicationIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	private GemFireCache peerCache;

	@Test
	public void peerCacheWithPeerLocalRegionAreAvailable() {

		Optional.ofNullable(this.peerCache)
			.filter(it -> it instanceof GemFireCacheImpl)
			.map(it -> (GemFireCacheImpl) it)
			.map(it -> assertThat(it.isClient()).isFalse())
			.orElseThrow(() -> newIllegalStateException("Peer cache was null"));

		Region<Object, Object> example = peerCache.getRegion("/Example");

		assertThat(example).isNotNull();
		assertThat(example.getName()).isEqualTo("Example");
		assertThat(example.getFullPath()).isEqualTo(RegionUtils.toRegionPath("Example"));

		example.put(1, "test");

		assertThat(example.get(1)).isEqualTo("test");
	}

	@SpringBootApplication
	@PeerCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
	static class TestConfiguration {

		@Bean("Example")
		public LocalRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			LocalRegionFactoryBean<Object, Object> exampleRegion = new LocalRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setClose(false);
			exampleRegion.setPersistent(false);

			return exampleRegion;
		}
	}
}
