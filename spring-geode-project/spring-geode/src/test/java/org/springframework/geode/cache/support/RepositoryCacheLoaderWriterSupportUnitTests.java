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
package org.springframework.geode.cache.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.CacheRuntimeException;
import org.apache.geode.cache.LoaderHelper;

import org.springframework.core.env.Environment;
import org.springframework.data.repository.CrudRepository;

/**
 * Unit Tests for {@link RepositoryCacheLoaderWriterSupport}.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.support.RepositoryCacheLoaderWriterSupport
 * @since 1.1.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RepositoryCacheLoaderWriterSupportUnitTests {

	@Mock
	private CrudRepository<?, ?> mockCrudRepository;

	@After
	public void tearDown() {
		System.clearProperty(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY);
	}

	@Test
	public void constructsRepositoryCacheLoaderWriterSupportSuccessfully() {

		RepositoryCacheLoaderWriterSupport<Object, Object> cacheLoaderWriter =
			new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository);

		assertThat(cacheLoaderWriter).isNotNull();
		assertThat(cacheLoaderWriter.getEnvironment().orElse(null)).isNull();
		assertThat(cacheLoaderWriter.getRepository()).isEqualTo(mockCrudRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructsRepositoryCacheLoaderWriterSupportWithNull() {

		try {
			new TestRepositoryCacheLoaderWriterSupport<>(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Repository is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void setAndGetEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		RepositoryCacheLoaderWriterSupport<Object, Object> cacheLoaderWriter =
			new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository);

		cacheLoaderWriter.setEnvironment(mockEnvironment);

		assertThat(cacheLoaderWriter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);

		cacheLoaderWriter.setEnvironment(null);

		assertThat(cacheLoaderWriter.getEnvironment().orElse(null)).isNull();
	}

	@Test
	public void isNukeAndPaveEnabledReturnsFalse() {
		assertThat(new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository)
			.isNukeAndPaveEnabled()).isFalse();
	}

	@Test
	public void isNukeAndPaveEnabledWhenEnvironmentPropertyIsSetReturnsTrue() {

		System.setProperty(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY, String.valueOf(false));

		assertThat(Boolean.parseBoolean(System.getProperty(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY)))
			.isFalse();

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY),
			eq(Boolean.class))).thenReturn(true);

		assertThat(new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository)
			.with(mockEnvironment).isNukeAndPaveEnabled()).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY), eq(Boolean.class));
	}

	@Test
	public void isNukeAndPaveEnabledWhenSystemPropertyIsSetReturnsTrue() {

		System.setProperty(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY, String.valueOf(true));

		assertThat(Boolean.parseBoolean(System.getProperty(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY)))
			.isTrue();

		assertThat(new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository).isNukeAndPaveEnabled())
			.isTrue();
	}

	@Test
	public void doRepositoryOperationSuccessfully() {

		Function<Object, Object> mockRepositoryOperationFunction = mock(Function.class);

		when(mockRepositoryOperationFunction.apply(any())).thenReturn("TEST");

		Object testEntity = new Object();

		RepositoryCacheLoaderWriterSupport<Object, Object> cacheLoaderWriter =
			new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository);

		assertThat(cacheLoaderWriter.doRepositoryOp(testEntity, mockRepositoryOperationFunction)).isEqualTo("TEST");

		verify(mockRepositoryOperationFunction, times(1)).apply(eq(testEntity));
	}

	@Test
	public void loadReturnsNull() {

		LoaderHelper<?, ?> mockLoadHelper = mock(LoaderHelper.class);

		assertThat(new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository).load(mockLoadHelper)).isNull();

		verifyNoInteractions(mockLoadHelper);
	}

	@Test(expected = CacheRuntimeException.class)
	public void doRepositoryOperationThrowsException() {

		Function<Object, Object> mockRepositoryOperationFunction = mock(Function.class);

		when(mockRepositoryOperationFunction.apply(any())).thenThrow(new RuntimeException("TEST"));

		Object testEntity = new Object();

		RepositoryCacheLoaderWriterSupport<Object, Object> cacheLoaderWriter =
			new TestRepositoryCacheLoaderWriterSupport(this.mockCrudRepository);

		try {
			cacheLoaderWriter.doRepositoryOp(testEntity, mockRepositoryOperationFunction);
		}
		catch (CacheRuntimeException expected) {

			assertThat(expected).hasMessage(RepositoryCacheLoaderWriterSupport.DATA_ACCESS_ERROR, testEntity);
			assertThat(expected).hasCauseInstanceOf(RuntimeException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockRepositoryOperationFunction, times(1)).apply(eq(testEntity));
		}
	}

	static class TestRepositoryCacheLoaderWriterSupport<T, ID> extends RepositoryCacheLoaderWriterSupport<T, ID> {

		TestRepositoryCacheLoaderWriterSupport(CrudRepository<T, ID> crudRepository) {
			super(crudRepository);
		}

		@Override
		protected CacheRuntimeException newCacheRuntimeException(Supplier<String> messageSupplier, Throwable cause) {
			return new TestCacheRuntimeException(messageSupplier.get(), cause);
		}
	}

	@SuppressWarnings("unused")
	static final class TestCacheRuntimeException extends CacheRuntimeException {

		public TestCacheRuntimeException() { }

		public TestCacheRuntimeException(String message) {
			super(message);
		}

		public TestCacheRuntimeException(Throwable cause) {
			super(cause);
		}

		public TestCacheRuntimeException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
