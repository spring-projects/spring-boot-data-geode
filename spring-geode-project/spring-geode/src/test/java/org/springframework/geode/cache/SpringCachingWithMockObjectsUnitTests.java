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
package org.springframework.geode.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests using Spring's Cache Abstraction (Caching) with a Mock Cache.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.beans.factory.FactoryBean
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.stereotype.Service
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SpringCachingWithMockObjectsUnitTests extends IntegrationTestsSupport {

	@Autowired
	private TestCacheableService testService;

	@Test
	public void serviceOpsResultsInCacheHits() {

		assertThat(this.testService).isNotNull();
		assertThat(this.testService.isCacheMiss()).isFalse();
		assertThat(this.testService.nextSequenceNumber("sequenceOne")).isEqualTo(1L);
		assertThat(this.testService.isCacheMiss()).isTrue();
		assertThat(this.testService.nextSequenceNumber("sequenceOne")).isEqualTo(1L);
		assertThat(this.testService.isCacheMiss()).isFalse();
		assertThat(this.testService.nextSequenceNumber("sequenceTwo")).isEqualTo(2L);
		assertThat(this.testService.isCacheMiss()).isTrue();
		assertThat(this.testService.nextSequenceNumber("sequenceThree")).isEqualTo(3L);
		assertThat(this.testService.isCacheMiss()).isTrue();
		assertThat(this.testService.nextSequenceNumber("sequenceTwo")).isEqualTo(2L);
		assertThat(this.testService.isCacheMiss()).isFalse();
	}

	@Configuration
	@EnableCaching
	static class TestConfiguration {

		@Bean
		MockCacheManagerFactoryBean cacheManager() {
			return new MockCacheManagerFactoryBean();
		}

		@Bean
		TestCacheableService testService() {
			return new TestCacheableService();
		}
	}

	static class MockCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean {

		private CacheManager cacheManager;

		@Override
		public void afterPropertiesSet() {
			this.cacheManager = mockCacheManager();
		}

		private CacheManager mockCacheManager() {

			Map<String, Cache> mockCaches = new ConcurrentHashMap<>();

			CacheManager mockCacheManager = mock(CacheManager.class);

			when(mockCacheManager.getCache(anyString())).thenAnswer(invocation -> {

				String name = invocation.getArgument(0);

				return mockCaches.computeIfAbsent(name, this::mockCache);
			});

			when(mockCacheManager.getCacheNames()).thenAnswer(invocation -> mockCaches.keySet());

			return mockCacheManager;
		}

		@SuppressWarnings("unchecked")
		private Cache mockCache(String name) {

			Map<Object, Object> cacheMap = new ConcurrentHashMap<>();

			Cache mockCache = mock(Cache.class);

			doAnswer(invocation -> {
				cacheMap.clear();
				return null;
			}).when(mockCache).clear();

			doAnswer(invocation -> {
				Object key = invocation.getArgument(0);
				cacheMap.remove(key);
				return null;
			}).when(mockCache).evict(any());

			doAnswer(invocation -> {
				Object key = invocation.getArgument(0);
				cacheMap.remove(key);
				return null;
			}).when(mockCache).evictIfPresent(any());

			when(mockCache.get(any())).thenAnswer(invocation -> {

				Object key = invocation.getArgument(0);
				Object value = cacheMap.get(key);

				return asCacheValueWrapper(value);

			});

			when(mockCache.get(any(), any(Class.class))).thenAnswer(invocation -> {

				Object key = invocation.getArgument(0);
				Class<?> type = invocation.getArgument(1);

				return type.cast(cacheMap.get(key));
			});

			when(mockCache.get(any(), any(Callable.class))).thenAnswer(invocation -> {

				Object key = invocation.getArgument(0);
				Callable<?> valueLoader = invocation.getArgument(1);
				Object value = cacheMap.get(key);

				return value != null ? value : valueLoader.call();
			});

			when(mockCache.getName()).thenReturn(name);

			when(mockCache.getNativeCache()).thenReturn(cacheMap);

			when(mockCache.invalidate()).thenAnswer(invocation -> {
				cacheMap.clear();
				return cacheMap.isEmpty();
			});

			doAnswer(invocation -> {

				Object key = invocation.getArgument(0);
				Object value = invocation.getArgument(1);

				cacheMap.put(key, value);

				return null;

			}).when(mockCache).put(any(), any());

			doAnswer(invocation -> {

				Object key = invocation.getArgument(0);
				Object newValue = invocation.getArgument(1);
				Object existingValue = cacheMap.putIfAbsent(key, newValue);

				return asCacheValueWrapper(existingValue);

			}).when(mockCache).putIfAbsent(any(), any());

			return mockCache;
		}

		private Cache.ValueWrapper asCacheValueWrapper(Object value) {
			return value != null ? () -> value : null;
		}

		@Nullable @Override
		public CacheManager getObject() {
			return this.cacheManager;
		}

		@Nullable @Override
		public Class<?> getObjectType() {
			return this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}
	}

	@Service
	public static class TestCacheableService {

		private static final AtomicLong sequenceNumber = new AtomicLong(0L);

		private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

		boolean isCacheMiss() {
			return this.cacheMiss.compareAndSet(true, false);
		}

		@Cacheable("SequenceNumbers")
		public Long nextSequenceNumber(String sequenceName) {
			this.cacheMiss.set(true);
			return sequenceNumber.incrementAndGet();
		}
	}
}
