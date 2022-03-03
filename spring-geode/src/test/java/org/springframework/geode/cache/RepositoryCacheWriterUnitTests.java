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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.CacheRuntimeException;
import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.RegionEvent;

import org.springframework.core.env.Environment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.support.RepositoryCacheLoaderWriterSupport;

/**
 * Unit Tests for {@link RepositoryCacheWriter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.RepositoryCacheWriter
 * @see org.springframework.geode.cache.support.RepositoryCacheLoaderWriterSupport
 * @since 1.1.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RepositoryCacheWriterUnitTests {

	@Mock
	private CrudRepository<Object, Object> mockCrudRepository;

	@Mock
	private EntryEvent<Object, Object> mockEntryEvent;

	@Mock
	private Environment mockEnvironment;

	private final Object testEntity = new Object();

	private RepositoryCacheWriter<Object, Object> cacheWriter;

	@Before
	public void setup() {

		this.cacheWriter = new RepositoryCacheWriter<>(this.mockCrudRepository)
			.with(this.mockEnvironment);
	}
	@Test
	public void beforeCreateSavesEntityWithRepository() {

		when(this.mockEntryEvent.getNewValue()).thenReturn(this.testEntity);

		this.cacheWriter.beforeCreate(this.mockEntryEvent);

		verify(this.mockEntryEvent, times(1)).getNewValue();
		verify(this.mockCrudRepository, times(1)).save(eq(this.testEntity));
	}

	@Test
	public void beforeUpdateSavesEntityWithRepository() {

		when(this.mockEntryEvent.getNewValue()).thenReturn(this.testEntity);

		this.cacheWriter.beforeUpdate(this.mockEntryEvent);

		verify(this.mockEntryEvent, times(1)).getNewValue();
		verify(this.mockCrudRepository, times(1)).save(eq(this.testEntity));
	}

	@Test
	public void beforeDestroyDeletesByIdWithRepository() {

		when(this.mockEntryEvent.getKey()).thenReturn("TestKey");

		this.cacheWriter.beforeDestroy(this.mockEntryEvent);

		verify(this.mockEntryEvent, times(1)).getKey();
		verify(this.mockCrudRepository, times(1)).deleteById(eq("TestKey"));
	}

	@Test
	public void beforeRegionClearDeletesAllWithRepositoryWhenNukeAndPaveIsEnabled() {

		RegionEvent<Object, Object> mockRegionEvent = mock(RegionEvent.class);

		when(this.mockEnvironment.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY),
			eq(Boolean.class))).thenReturn(true);

		this.cacheWriter.beforeRegionClear(mockRegionEvent);

		verify(this.mockEnvironment, times(1))
			.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY), eq(Boolean.class));

		verify(this.mockCrudRepository, times(1)).deleteAll();

		verifyNoInteractions(mockRegionEvent);
	}

	@Test
	public void beforeRegionClearWillNotDeleteAllWithRepositoryWhenNukeAndPaveIsDisabled() {

		RegionEvent<Object, Object> mockRegionEvent = mock(RegionEvent.class);

		when(this.mockEnvironment.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY),
			eq(Boolean.class))).thenReturn(false);

		this.cacheWriter.beforeRegionClear(mockRegionEvent);

		verify(this.mockEnvironment, times(1))
			.getProperty(eq(RepositoryCacheLoaderWriterSupport.NUKE_AND_PAVE_PROPERTY), eq(Boolean.class));

		verify(this.mockCrudRepository, never()).deleteAll();

		verifyNoInteractions(mockRegionEvent);
	}

	@Test
	public void beforeRegionDestroyDoesNothing() {

		RegionEvent<Object, Object> mockRegionEvent = mock(RegionEvent.class);

		this.cacheWriter.beforeRegionDestroy(mockRegionEvent);

		verifyNoInteractions(this.mockEnvironment);
		verifyNoInteractions(this.mockCrudRepository);
		verifyNoInteractions(mockRegionEvent);
	}

	@Test
	public void newCacheRuntimeExceptionIsCorrect() {

		RuntimeException cause = new RuntimeException("TEST");

		CacheRuntimeException cacheRuntimeException = this.cacheWriter.newCacheRuntimeException(() -> "TEST", cause);

		assertThat(cacheRuntimeException).isInstanceOf(CacheWriterException.class);
		assertThat(cacheRuntimeException.getMessage()).isEqualTo("TEST");
		assertThat(cacheRuntimeException.getCause()).isEqualTo(cause);
	}
}
