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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheWriter;

import org.springframework.data.gemfire.PeerRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.repository.CrudRepository;

/**
 * Unit Tests for {@link InlineCachingRegionConfigurer}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see java.util.function.Predicate
 * @see org.apache.geode.cache.CacheLoader
 * @see org.apache.geode.cache.CacheWriter
 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.InlineCachingRegionConfigurer
 * @since 1.1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class InlineCachingRegionConfigurerUnitTests {

	@Mock
	private CrudRepository<?, ?> mockRepository;

	@Mock
	private Predicate<String> mockPredicate;

	@Test
	public void constructInlineCachingRegionConfigurer() {

		InlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new InlineCachingRegionConfigurer<>(this.mockRepository, this.mockPredicate);

		assertThat(regionConfigurer).isNotNull();
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructInlineCachingRegionConfigurerWithNullCrudRepositoryThrowsException() {

		try {
			new InlineCachingRegionConfigurer<>(null, this.mockPredicate);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CrudRepository is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void configureClientRegionFactoryBeanWithInlineCachingWhenPredicateReturnsTrue() {

		ClientRegionFactoryBean<?, ?> clientRegionFactoryBean = spy(new ClientRegionFactoryBean<>());

		doAnswer(answer -> {

			CacheLoader cacheLoader = answer.getArgument(0);

			assertThat(cacheLoader).isInstanceOf(RepositoryCacheLoader.class);
			assertThat(((RepositoryCacheLoader) cacheLoader).getRepository()).isEqualTo(this.mockRepository);

			return null;

		}).when(clientRegionFactoryBean).setCacheLoader(any(CacheLoader.class));

		doAnswer(answer -> {

			CacheWriter cacheWriter = answer.getArgument(0);

			assertThat(cacheWriter).isInstanceOf(RepositoryCacheWriter.class);
			assertThat(((RepositoryCacheWriter) cacheWriter).getRepository()).isEqualTo(this.mockRepository);

			return null;

		}).when(clientRegionFactoryBean).setCacheWriter(any(CacheWriter.class));

		when(this.mockPredicate.test(anyString())).thenReturn(true);

		InlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new InlineCachingRegionConfigurer<>(this.mockRepository, this.mockPredicate);

		regionConfigurer.configure("Example", clientRegionFactoryBean);

		verify(clientRegionFactoryBean, times(1))
			.setCacheLoader(isA(RepositoryCacheLoader.class));

		verify(clientRegionFactoryBean, times(1))
			.setCacheWriter(isA(RepositoryCacheWriter.class));

		verify(this.mockPredicate, times(2)).test(eq("Example"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doesNotConfigureClientRegionFactoryBeanWithInlineCachingWhenPredicateReturnsFalse() {

		ClientRegionFactoryBean<?, ?> clientRegionFactoryBean = spy(new ClientRegionFactoryBean<>());

		when(this.mockPredicate.test(anyString())).thenReturn(false);

		InlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new InlineCachingRegionConfigurer<>(this.mockRepository, this.mockPredicate);

		regionConfigurer.configure("Example", clientRegionFactoryBean);

		verify(clientRegionFactoryBean, never()).setCacheLoader(any(CacheLoader.class));
		verify(clientRegionFactoryBean, never()).setCacheWriter(any(CacheWriter.class));
		verify(this.mockPredicate, times(2)).test(eq("Example"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void configurePeerRegionFactoryBeanWithInlineCachingWhenPredicateReturnsFalse() {

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		doAnswer(answer -> {

			CacheLoader cacheLoader = answer.getArgument(0);

			assertThat(cacheLoader).isInstanceOf(RepositoryCacheLoader.class);
			assertThat(((RepositoryCacheLoader) cacheLoader).getRepository()).isEqualTo(this.mockRepository);

			return null;

		}).when(peerRegionFactoryBean).setCacheLoader(any(CacheLoader.class));

		doAnswer(answer -> {

			CacheWriter cacheWriter = answer.getArgument(0);

			assertThat(cacheWriter).isInstanceOf(RepositoryCacheWriter.class);
			assertThat(((RepositoryCacheWriter) cacheWriter).getRepository()).isEqualTo(this.mockRepository);

			return null;

		}).when(peerRegionFactoryBean).setCacheWriter(any(CacheWriter.class));

		when(this.mockPredicate.test(anyString())).thenReturn(true);

		InlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new InlineCachingRegionConfigurer<>(this.mockRepository, this.mockPredicate);

		regionConfigurer.configure("Example", peerRegionFactoryBean);

		verify(peerRegionFactoryBean, times(1))
			.setCacheLoader(isA(RepositoryCacheLoader.class));

		verify(peerRegionFactoryBean, times(1))
			.setCacheWriter(isA(RepositoryCacheWriter.class));

		verify(this.mockPredicate, times(2)).test(eq("Example"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doesNotConfigurePeerRegionFactoryBeanWithInlineCachingWhenPredicateReturnsFalse() {

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		when(this.mockPredicate.test(anyString())).thenReturn(false);

		InlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new InlineCachingRegionConfigurer<>(this.mockRepository, this.mockPredicate);

		regionConfigurer.configure("Example", peerRegionFactoryBean);

		verify(peerRegionFactoryBean, never()).setCacheLoader(any(CacheLoader.class));
		verify(peerRegionFactoryBean, never()).setCacheWriter(any(CacheWriter.class));
		verify(this.mockPredicate, times(2)).test(eq("Example"));
	}
}
