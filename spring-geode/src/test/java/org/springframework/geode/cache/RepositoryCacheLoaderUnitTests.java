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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.CacheRuntimeException;
import org.apache.geode.cache.LoaderHelper;

import org.springframework.core.env.Environment;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.repository.CrudRepository;

/**
 * Unit Test for {@link RepositoryCacheLoader}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.cache.RepositoryCacheLoader
 * @since 1.1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class RepositoryCacheLoaderUnitTests {

	@Mock
	private CrudRepository<Object, Object> mockCrudRepository;

	@Mock
	private Environment mockEnvironment;

	@Mock
	private LoaderHelper<Object, Object> mockLoaderHelper;

	private Object testEntity = new Object();

	private RepositoryCacheLoader<Object, Object> cacheLoader;

	@Before
	public void setup() {

		this.cacheLoader = new RepositoryCacheLoader<>(this.mockCrudRepository)
			.with(this.mockEnvironment);
	}

	@After
	public void tearDown() {
		verifyNoInteractions(this.mockEnvironment);
	}

	@Test
	public void loadsEntitySuccessfully() {

		when(this.mockCrudRepository.findById(eq("TestKey"))).thenReturn(Optional.of(this.testEntity));
		when(this.mockLoaderHelper.getKey()).thenReturn("TestKey");

		assertThat(this.cacheLoader.load(this.mockLoaderHelper)).isEqualTo(this.testEntity);

		verify(this.mockLoaderHelper, times(1)).getKey();
		verify(this.mockCrudRepository, times(1)).findById(eq("TestKey"));
	}

	@Test
	public void loadReturnsNull() {

		when(this.mockCrudRepository.findById(eq("TestKey"))).thenReturn(Optional.empty());
		when(this.mockLoaderHelper.getKey()).thenReturn("TestKey");

		assertThat(this.cacheLoader.load(this.mockLoaderHelper)).isNull();

		verify(this.mockLoaderHelper, times(1)).getKey();
		verify(this.mockCrudRepository, times(1)).findById(eq("TestKey"));
	}

	@Test(expected = CacheLoaderException.class)
	public void loadThrowsException() {

		when(this.mockCrudRepository.findById(eq("TestKey")))
			.thenThrow(new IncorrectResultSizeDataAccessException(1, 0));

		when(this.mockLoaderHelper.getKey()).thenReturn("TestKey");

		try {
			this.cacheLoader.load(this.mockLoaderHelper);
		}
		catch (CacheLoaderException expected) {

			assertThat(expected).hasMessage(RepositoryCacheLoader.CACHE_LOAD_EXCEPTION_MESSAGE,
				"TestKey", this.mockCrudRepository.getClass().getName());

			assertThat(expected).hasCauseInstanceOf(IncorrectResultSizeDataAccessException.class);
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(this.mockLoaderHelper, times(2)).getKey();
			verify(this.mockCrudRepository, times(1)).findById(eq("TestKey"));
		}
	}

	@Test
	public void newCacheRuntimeExceptionIsCorrect() {

		RuntimeException cause = new RuntimeException("TEST");

		CacheRuntimeException cacheRuntimeException = this.cacheLoader.newCacheRuntimeException(() -> "TEST", cause);

		assertThat(cacheRuntimeException).isInstanceOf(CacheLoaderException.class);
		assertThat(cacheRuntimeException.getMessage()).isEqualTo("TEST");
		assertThat(cacheRuntimeException.getCause()).isEqualTo(cause);
	}
}
