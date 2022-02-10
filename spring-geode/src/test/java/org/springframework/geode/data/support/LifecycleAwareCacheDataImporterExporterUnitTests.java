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
package org.springframework.geode.data.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.data.CacheDataImporterExporter;
import org.springframework.geode.data.support.LifecycleAwareCacheDataImporterExporter.ImportLifecycle;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;

/**
 * Unit Tests for {@link LifecycleAwareCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.data.CacheDataImporterExporter
 * @see org.springframework.geode.data.support.LifecycleAwareCacheDataImporterExporter
 * @since 1.3.0
 */
public class LifecycleAwareCacheDataImporterExporterUnitTests {

	@Test
	public void constructLifecycleAwareCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(importerExporter).isNotNull();
		assertThat(importerExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);
		assertThat(importerExporter.getEnvironment().orElse(null)).isNull();

		verifyNoInteractions(mockImporterExporter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructLifecycleAwareCacheDataImporterExporterWithNull() {

		try {
			new LifecycleAwareCacheDataImporterExporter(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("The CacheDataImporterExporter to decorate must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void setApplicationContextOnWrappedApplicationContextAwareCacheDataImporterExporter() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter mockImporterExporter =
			mock(ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setApplicationContext(mockApplicationContext);
		lifecycleImporterExporter.setApplicationContext(null);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1)).setApplicationContext(eq(mockApplicationContext));
		verifyNoMoreInteractions(mockImporterExporter);
	}

	@Test
	public void setApplicationContextDoesNotConfigureWrappedNonApplicationContextAwareCacheDataImporterExporter() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setApplicationContext(mockApplicationContext);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockApplicationContext, mockImporterExporter);
	}

	@Test
	public void setAndGetEnvironment() {

		ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter mockImporterExporter =
			mock(ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);
		assertThat(lifecycleImporterExporter.getEnvironment().orElse(null)).isNull();

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setEnvironment(mockEnvironment);

		assertThat(lifecycleImporterExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);

		lifecycleImporterExporter.setEnvironment(null);

		assertThat(lifecycleImporterExporter.getEnvironment().orElse(null)).isNull();

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1)).setEnvironment(eq(mockEnvironment));
		verifyNoMoreInteractions(mockImporterExporter);
	}

	@Test
	public void setEnvironmentDoesNotConfigureWrappedNonEnvironmentAwareCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);
		assertThat(lifecycleImporterExporter.getEnvironment().orElse(null)).isNull();

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setEnvironment(mockEnvironment);

		assertThat(lifecycleImporterExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void setExportResourceResolverConfiguresWrappedResourceCapableCacheDataImporterExporter() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		ExportResourceResolver mockExportResourceResolver = mock(ExportResourceResolver.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setExportResourceResolver(mockExportResourceResolver);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1))
			.setExportResourceResolver(eq(mockExportResourceResolver));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockExportResourceResolver);
	}

	@Test
	public void setExportResourceResolverDoesNotConfigureWrappedNonResourceCapableCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		ExportResourceResolver mockExportResourceResolver = mock(ExportResourceResolver.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setExportResourceResolver(mockExportResourceResolver);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockExportResourceResolver, mockImporterExporter);
	}

	@Test
	public void setExportResourceResolverWithNull() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setExportResourceResolver(null);

		verify(lifecycleImporterExporter, never()).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void setImportResourceResolverConfiguresWrappedResourceCapableCacheDataImporterExporter() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setImportResourceResolver(mockImportResourceResolver);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1))
			.setImportResourceResolver(eq(mockImportResourceResolver));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockImportResourceResolver);
	}

	@Test
	public void setImportResourceResolverDoesNotConfigureWrappedNonResourceCapableCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setImportResourceResolver(mockImportResourceResolver);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockImportResourceResolver, mockImporterExporter);
	}

	@Test
	public void setImportResourceResolverWithNull() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setImportResourceResolver(null);

		verify(lifecycleImporterExporter, never()).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void setResourceLoaderConfiguresWrappedResourceLoaderAwareCacheDataImporterExporter() {

		ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter mockImporterExporter =
			mock(ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceLoader(mockResourceLoader);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1)).setResourceLoader(eq(mockResourceLoader));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockResourceLoader);
	}

	@Test
	public void setResourceLoaderDoesNotConfigureWrappedNonResourceLoaderAwareCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceLoader(mockResourceLoader);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockResourceLoader, mockImporterExporter);
	}

	@Test
	public void setResourceLoaderWithNull() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceLoader(null);

		verify(lifecycleImporterExporter, never()).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void setResourceReaderConfiguresWrappedResourceCapableCacheDataImporterExporter() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceReader(mockResourceReader);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1)).setResourceReader(eq(mockResourceReader));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockResourceReader);
	}

	@Test
	public void setResourceReaderDoesNotConfigureWrappedNonResourceCapableCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceReader(mockResourceReader);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter, mockResourceReader);
	}

	@Test
	public void setResourceReaderWithNull() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceReader(null);

		verify(lifecycleImporterExporter, never()).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void setResourceWriterConfiguresWrappedResourceCapableCacheDataImporterExporter() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		ResourceWriter mockResourceWriter = mock(ResourceWriter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceWriter(mockResourceWriter);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verify(mockImporterExporter, times(1)).setResourceWriter(eq(mockResourceWriter));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockResourceWriter);
	}

	@Test
	public void setResourceWriterDoesNotConfigureWrappedNonResourceCapableCacheDataImporterExporter() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		ResourceWriter mockResourceWriter = mock(ResourceWriter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceWriter(mockResourceWriter);

		verify(lifecycleImporterExporter, times(1)).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter, mockResourceWriter);
	}

	@Test
	public void setResourceWriterWithNull() {

		TestResourceCapableCacheDataImporterExporter mockImporterExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter lifecycleImporterExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(lifecycleImporterExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		lifecycleImporterExporter = spy(lifecycleImporterExporter);
		lifecycleImporterExporter.setResourceWriter(null);

		verify(lifecycleImporterExporter, never()).getCacheDataImporterExporter();
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void exportFromRegionCallsWrappedCacheDataImporterExporterExportFrom() {

		Region<?, ?> mockRegion = mock(Region.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		assertThat(importerExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);

		importerExporter.exportFrom(mockRegion);

		verify(mockImporterExporter, times(1)).exportFrom(eq(mockRegion));
		verifyNoMoreInteractions(mockImporterExporter);
	}

	@Test
	public void importIntoRegionCallsWrappedCacheDataImporterExporterImmediatelyWhenImportLifecycleIsEager() {

		Region<?, ?> mockRegionOne = mock(Region.class);
		Region<?, ?> mockRegionTwo = mock(Region.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			spy(new LifecycleAwareCacheDataImporterExporter(mockImporterExporter));

		doReturn(ImportLifecycle.EAGER).when(importerExporter).resolveImportLifecycle();

		assertThat(importerExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);
		assertThat(importerExporter.getRegionsForImport()).isEmpty();

		importerExporter.importInto(mockRegionOne);
		importerExporter.importInto(mockRegionTwo);

		assertThat(importerExporter.getRegionsForImport()).isEmpty();

		verify(mockImporterExporter, times(1)).importInto(eq(mockRegionOne));
		verify(mockImporterExporter, times(1)).importInto(eq(mockRegionTwo));
		verifyNoMoreInteractions(mockImporterExporter);
	}

	@Test
	public void importIntoRegionStoresRegionReferenceWhenImportLifecycleIsLazy() {

		Region<?, ?> mockRegionOne = mock(Region.class);
		Region<?, ?> mockRegionTwo = mock(Region.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			spy(new LifecycleAwareCacheDataImporterExporter(mockImporterExporter));

		doReturn(ImportLifecycle.LAZY).when(importerExporter).resolveImportLifecycle();

		assertThat(importerExporter.getCacheDataImporterExporter()).isEqualTo(mockImporterExporter);
		assertThat(importerExporter.getRegionsForImport()).isEmpty();

		importerExporter.importInto(mockRegionOne);
		importerExporter.importInto(mockRegionTwo);

		assertThat(importerExporter.getRegionsForImport()).containsExactlyInAnyOrder(mockRegionOne, mockRegionTwo);

		verify(mockImporterExporter, never()).importInto(any());
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockRegionOne, mockRegionTwo);
	}

	@Test
	public void resolveImportLifecycleCachesResultAndReturnsEager() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(ImportLifecycle.EAGER.name()).when(mockEnvironment)
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME),
				eq(String.class), eq(ImportLifecycle.getDefault().name()));

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.resolveImportLifecycle()).isEqualTo(ImportLifecycle.EAGER);
		assertThat(importerExporter.resolveImportLifecycle()).isEqualTo(ImportLifecycle.EAGER);

		verify(mockEnvironment, times(1))
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME),
				eq(String.class), eq(ImportLifecycle.getDefault().name()));
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void resolveImportLifecycleCachesResultAndReturnsLazy() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn("INVALID").when(mockEnvironment)
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME),
				eq(String.class), eq(ImportLifecycle.getDefault().name()));

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.resolveImportLifecycle()).isEqualTo(ImportLifecycle.LAZY);
		assertThat(importerExporter.resolveImportLifecycle()).isEqualTo(ImportLifecycle.LAZY);

		verify(mockEnvironment, times(1))
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME),
				eq(String.class), eq(ImportLifecycle.getDefault().name()));
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void resolveImportPhaseCachesResultAndReturnsIntegerMinValuePlusOneMillion() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(42).when(mockEnvironment)
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME),
				eq(Integer.class), eq(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE));

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.resolveImportPhase()).isEqualTo(42);
		assertThat(importerExporter.resolveImportPhase()).isEqualTo(42);

		verify(mockEnvironment, times(1))
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME),
				eq(Integer.class), eq(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE));
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void resolveImportPhaseCachesResultAndReturnsDefaultImportPhase() {

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(null).when(mockEnvironment)
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME),
				eq(Integer.class), eq(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE));

		LifecycleAwareCacheDataImporterExporter importerExporter =
			new LifecycleAwareCacheDataImporterExporter(mockImporterExporter);

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.resolveImportPhase()).isEqualTo(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE);
		assertThat(importerExporter.resolveImportPhase()).isEqualTo(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE);

		verify(mockEnvironment, times(1))
			.getProperty(eq(LifecycleAwareCacheDataImporterExporter.CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME),
				eq(Integer.class), eq(LifecycleAwareCacheDataImporterExporter.DEFAULT_IMPORT_PHASE));
		verifyNoInteractions(mockImporterExporter);
	}

	@Test
	public void startImportsIntoRegionsWhenImportLifecycleIsLazy() {

		Region<?, ?> mockRegionOne = mock(Region.class);
		Region<?, ?> mockRegionTwo = mock(Region.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			spy(new LifecycleAwareCacheDataImporterExporter(mockImporterExporter));

		doReturn(ImportLifecycle.LAZY).when(importerExporter).resolveImportLifecycle();

		importerExporter.getRegionsForImport().add(mockRegionOne);
		importerExporter.getRegionsForImport().add(mockRegionTwo);

		assertThat(importerExporter.getRegionsForImport()).containsExactlyInAnyOrder(mockRegionOne, mockRegionTwo);

		importerExporter.start();

		verify(mockImporterExporter, times(1)).importInto(eq(mockRegionOne));
		verify(mockImporterExporter, times(1)).importInto(eq(mockRegionTwo));
		verifyNoMoreInteractions(mockImporterExporter);
		verifyNoInteractions(mockRegionOne, mockRegionTwo);
	}

	@Test
	public void startDoesNothingWhenImportLifecycleIsEager() {

		Region<?, ?> mockRegionOne = mock(Region.class);
		Region<?, ?> mockRegionTwo = mock(Region.class);

		CacheDataImporterExporter mockImporterExporter = mock(CacheDataImporterExporter.class);

		LifecycleAwareCacheDataImporterExporter importerExporter =
			spy(new LifecycleAwareCacheDataImporterExporter(mockImporterExporter));

		doReturn(ImportLifecycle.EAGER).when(importerExporter).resolveImportLifecycle();

		importerExporter.getRegionsForImport().add(mockRegionOne);
		importerExporter.getRegionsForImport().add(mockRegionTwo);

		assertThat(importerExporter.getRegionsForImport()).containsExactlyInAnyOrder(mockRegionOne, mockRegionTwo);

		importerExporter.start();

		assertThat(importerExporter.getRegionsForImport()).containsExactlyInAnyOrder(mockRegionOne, mockRegionTwo);

		verify(mockImporterExporter, never()).importInto(any());
		verifyNoInteractions(mockRegionOne, mockRegionTwo);
	}

	@Test
	public void importLifecycleDefaultIsLazy() {
		assertThat(ImportLifecycle.getDefault()).isEqualTo(ImportLifecycle.LAZY);
	}

	@Test
	public void importLifecycleFromIsCaseInsensitive() {

		assertThat(ImportLifecycle.from("Eager")).isEqualTo(ImportLifecycle.EAGER);
		assertThat(ImportLifecycle.from("lazy")).isEqualTo(ImportLifecycle.LAZY);
	}

	@Test
	public void importLifecycleFromReturnsEnum() {

		for (ImportLifecycle enumeratedValue : ImportLifecycle.values()) {
			assertThat(ImportLifecycle.from(enumeratedValue.name())).isEqualTo(enumeratedValue);
		}
	}

	@Test
	public void importLifecycleFromUnknownReturnsNull() {
		assertThat(ImportLifecycle.from("UNKNOWN")).isNull();
	}

	@Test
	public void importLifecycleIsEager() {
		assertThat(ImportLifecycle.EAGER.isEager()).isTrue();
		assertThat(ImportLifecycle.LAZY.isEager()).isFalse();
	}

	@Test
	public void importLifecycleIsLazy() {
		assertThat(ImportLifecycle.EAGER.isLazy()).isFalse();
		assertThat(ImportLifecycle.LAZY.isLazy()).isTrue();
	}

	@Test
	public void importLifecycleToStringIsDescriptive() {

		assertThat(ImportLifecycle.EAGER.toString())
			.isEqualTo("Imports cache data during Region bean post processing, after initialization");

		assertThat(ImportLifecycle.LAZY.toString())
			.isEqualTo("Imports cache data during the appropriate phase on Lifecycle start");
	}

	interface ApplicationContextEnvironmentAndResourceLoaderAwareCacheDataImporterExporter
		extends ApplicationContextAware, CacheDataImporterExporter, EnvironmentAware, ResourceLoaderAware { }

	abstract static class TestResourceCapableCacheDataImporterExporter
		extends ResourceCapableCacheDataImporterExporter { }

}
