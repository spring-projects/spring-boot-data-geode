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
package org.springframework.geode.pdx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionService;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxInstanceFactory;

/**
 * Unit Tests for {@link PdxInstanceBuilder}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.RegionService
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.PdxInstanceFactory
 * @see org.springframework.geode.pdx.PdxInstanceBuilder
 * @since 1.3.0
 */
public class PdxInstanceBuilderUnitTests {

	@Test
	public void constructPdxInstanceBuilderInitializedWithRegionService() {

		RegionService mockRegionService = mock(RegionService.class);

		PdxInstanceBuilder builder = new PdxInstanceBuilder(mockRegionService);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockRegionService);

		verifyNoInteractions(mockRegionService);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructPdxInstanceBuilderWithNullRegionService() {

		try {
			new PdxInstanceBuilder(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("RegionService must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void createPdxInstanceBuilderWithRegionService() {

		RegionService mockRegionService = mock(RegionService.class);

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockRegionService);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockRegionService);

		verifyNoMoreInteractions(mockRegionService);
	}

	@Test(expected = IllegalStateException.class)
	public void createPdxInstanceBuilderWithUnresolvableRegionService() {

		try {
			PdxInstanceBuilder.create();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("GemFireCache not found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void copyPdxInstance() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("example.app.test.model.Type").when(mockPdxInstance).getClassName();
		doReturn("testOne").when(mockPdxInstance).getField(eq("fieldOne"));
		doReturn("testTwo").when(mockPdxInstance).getField(eq("fieldTwo"));
		doReturn(1).when(mockPdxInstance).getField(eq("id"));
		doReturn(Arrays.asList("fieldOne", "id", "fieldTwo")).when(mockPdxInstance).getFieldNames();
		doReturn(true).when(mockPdxInstance).isIdentityField(eq("id"));

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		RegionService mockRegionService = mock(RegionService.class);

		doReturn(mockPdxInstanceFactory).when(mockRegionService)
			.createPdxInstanceFactory(eq("example.app.test.model.Type"));

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockRegionService);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockRegionService);
		assertThat(builder.copy(mockPdxInstance)).isEqualTo(mockPdxInstanceFactory);

		InOrder inOrder = Mockito.inOrder(mockPdxInstance, mockPdxInstanceFactory, mockRegionService);

		inOrder.verify(mockPdxInstance, times(1)).getClassName();
		inOrder.verify(mockRegionService, times(1))
			.createPdxInstanceFactory(eq("example.app.test.model.Type"));
		inOrder.verify(mockPdxInstance, times(1)).getFieldNames();
		inOrder.verify(mockPdxInstance, times(1)).getField(eq("fieldOne"));
		inOrder.verify(mockPdxInstanceFactory, times(1))
			.writeObject(eq("fieldOne"), eq("testOne"));
		inOrder.verify(mockPdxInstance, times(1)).isIdentityField(eq("fieldOne"));
		inOrder.verify(mockPdxInstance, times(1)).getField(eq("id"));
		inOrder.verify(mockPdxInstanceFactory, times(1))
			.writeObject(eq("id"), eq(1));
		inOrder.verify(mockPdxInstance, times(1)).isIdentityField(eq("id"));
		inOrder.verify(mockPdxInstanceFactory, times(1)).markIdentityField(eq("id"));
		inOrder.verify(mockPdxInstance, times(1)).getField(eq("fieldTwo"));
		inOrder.verify(mockPdxInstanceFactory, times(1))
			.writeObject(eq("fieldTwo"), eq("testTwo"));
		inOrder.verify(mockPdxInstance, times(1)).isIdentityField(eq("fieldTwo"));
	}

	@Test
	public void copyPdxInstanceWhenGetFieldsNamesReturnsNullIsNullSafe() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		RegionService mockRegionService = mock(RegionService.class);

		doReturn("example.app.test.model.Type").when(mockPdxInstance).getClassName();
		doReturn(null).when(mockPdxInstance).getFieldNames();
		doReturn(mockPdxInstanceFactory).when(mockRegionService)
			.createPdxInstanceFactory(eq("example.app.test.model.Type"));

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockRegionService);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockRegionService);
		assertThat(builder.copy(mockPdxInstance)).isEqualTo(mockPdxInstanceFactory);

		verify(mockPdxInstance, times(1)).getFieldNames();
		verifyNoInteractions(mockPdxInstanceFactory);
		verify(mockRegionService, times(1))
			.createPdxInstanceFactory(eq("example.app.test.model.Type"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void copyNullPdxInstanceThrowsIllegalArgumentException() {

		RegionService mockRegionService = mock(RegionService.class);

		try {
			PdxInstanceBuilder.create(mockRegionService).copy(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("PdxInstance must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally  {
			verifyNoInteractions(mockRegionService);
		}
	}

	@Test
	public void fromSourceObject() {

		Object source = new Object();

		GemFireCache mockCache = mock(GemFireCache.class);

		PdxInstance mockPdxInstanceHolder = mock(PdxInstance.class);
		PdxInstance mockPdxInstanceSource = mock(PdxInstance.class);

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		doReturn(true).when(mockCache).getPdxReadSerialized();
		doReturn(mockPdxInstanceFactory).when(mockCache).createPdxInstanceFactory(eq(source.getClass().getName()));
		doReturn(mockPdxInstanceHolder).when(mockPdxInstanceFactory).create();
		doReturn(mockPdxInstanceSource).when(mockPdxInstanceHolder).getField(eq("source"));

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockCache);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockCache);

		PdxInstanceBuilder.Factory factory = builder.from(source);

		assertThat(factory).isNotNull();
		assertThat(factory.create()).isEqualTo(mockPdxInstanceSource);

		verify(mockCache, times(1)).getPdxReadSerialized();
		verify(mockCache, times(1)).createPdxInstanceFactory(eq(source.getClass().getName()));
		verify(mockPdxInstanceFactory, times(1)).writeObject(eq("source"), eq(source));
		verify(mockPdxInstanceFactory, times(1)).create();
		verify(mockPdxInstanceHolder, times(1)).getField(eq("source"));
		verifyNoInteractions(mockPdxInstanceSource);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromNullSourceObjectThrowsIllegalArgumentException() {

		RegionService mockRegionService = mock(RegionService.class);

		try {
			PdxInstanceBuilder.create(mockRegionService).from(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Source object to serialize to PDX must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockRegionService);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void fromNonNullSourceObjectWhenCacheIsNotPresent() {

		RegionService mockRegionService = mock(RegionService.class);

		try {
			PdxInstanceBuilder.create(mockRegionService).from("TEST");
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("PDX read-serialized must be set to true");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockRegionService);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void fromNonNullSourceObjectWhenCachePdxReadSerializedIsFalse() {

		GemFireCache mockCache = mock(GemFireCache.class);

		doReturn(false).when(mockCache).getPdxReadSerialized();

		try {
			PdxInstanceBuilder.create(mockCache).from("TEST");
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("PDX read-serialized must be set to true");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockCache, times(1)).getPdxReadSerialized();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromNonNullSourceObjectReturningNonPdxInstanceThrowsIllegalArgumentException() {

		Object source = new Object();

		GemFireCache mockCache = mock(GemFireCache.class);

		PdxInstance mockPdxInstanceHolder = mock(PdxInstance.class);

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		doReturn(true).when(mockCache).getPdxReadSerialized();
		doReturn(mockPdxInstanceFactory).when(mockCache).createPdxInstanceFactory(eq(source.getClass().getName()));
		doReturn(mockPdxInstanceHolder).when(mockPdxInstanceFactory).create();
		doReturn(source).when(mockPdxInstanceHolder).getField(eq("source"));

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockCache);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockCache);

		PdxInstanceBuilder.Factory factory = builder.from(source);

		assertThat(factory).isNotNull();

		try {
			factory.create();
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Expected an instance of PDX but was an instance of type [%s];"
				+ " Was PDX read-serialized set to true", source.getClass().getName());
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockCache, times(1)).getPdxReadSerialized();
			verify(mockCache, times(1)).createPdxInstanceFactory(eq(source.getClass().getName()));
			verify(mockPdxInstanceFactory, times(1)).writeObject(eq("source"), eq(source));
			verify(mockPdxInstanceHolder, times(1)).getField(eq("source"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromNonNullSourceObjectReturningNullOnPdxInstanceFactoryCreateIsNullSafe() {

		Object source = mock(Object.class);

		GemFireCache mockCache = mock(GemFireCache.class);

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		doReturn(true).when(mockCache).getPdxReadSerialized();
		doReturn(mockPdxInstanceFactory).when(mockCache).createPdxInstanceFactory(eq(source.getClass().getName()));
		doReturn(null).when(mockPdxInstanceFactory).create();

		PdxInstanceBuilder builder = PdxInstanceBuilder.create(mockCache);

		assertThat(builder).isNotNull();
		assertThat(builder.getRegionService()).isEqualTo(mockCache);

		PdxInstanceBuilder.Factory factory = builder.from(source);

		assertThat(factory).isNotNull();

		try {
			factory.create();
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Expected an instance of PDX but was an instance of type [null];"
				+ " Was PDX read-serialized set to true");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockCache, times(1)).getPdxReadSerialized();
			verify(mockCache, times(1)).createPdxInstanceFactory(eq(source.getClass().getName()));
			verify(mockPdxInstanceFactory, times(1)).writeObject(eq("source"), eq(source));
			verifyNoInteractions(source);
		}
	}
}
