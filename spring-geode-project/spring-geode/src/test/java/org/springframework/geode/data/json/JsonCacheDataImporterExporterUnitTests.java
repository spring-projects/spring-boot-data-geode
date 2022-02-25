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
package org.springframework.geode.data.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.data.json.converter.JsonToPdxArrayConverter;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;
import org.springframework.lang.NonNull;

/**
 * Unit Tests for {@link JsonCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceReader
 * @see org.springframework.geode.core.io.ResourceWriter
 * @see org.springframework.geode.data.json.JsonCacheDataImporterExporter
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonCacheDataImporterExporterUnitTests {

	@Spy
	private TestJsonCacheDataImporterExporter importerExporter;

	@Test
	@SuppressWarnings("unchecked")
	public void doExportFromRegionSavesJson() {

		String json = "[{ \"name\": \"Jon Doe\"}, { \"name\": \"Jane Doe\" }]";

		Resource mockResource = mock(Resource.class);

		ResourceWriter mockResourceWriter = mock(ResourceWriter.class);

		Region<?, ?> mockRegion = mock(Region.class);

		ExportResourceResolver mockExportResourceResolver = mock(ExportResourceResolver.class);

		doReturn("TestRegion").when(mockRegion).getName();
		doReturn(mockExportResourceResolver).when(this.importerExporter).getExportResourceResolver();
		doReturn(mockResourceWriter).when(this.importerExporter).getResourceWriter();
		doReturn(Optional.of(mockResource)).when(mockExportResourceResolver).resolve(eq(mockRegion));
		doReturn(json).when(this.importerExporter).toJson(eq(mockRegion));

		assertThat(this.importerExporter.doExportFrom(mockRegion)).isEqualTo(mockRegion);

		InOrder order = inOrder(this.importerExporter, mockRegion, mockExportResourceResolver, mockResourceWriter);

		order.verify(this.importerExporter, times(1)).getExportResourceResolver();
		order.verify(mockExportResourceResolver, times(1)).resolve(eq(mockRegion));
		order.verify(this.importerExporter, times(1)).toJson(eq(mockRegion));
		order.verify(mockRegion, times(1)).getName();
		order.verify(this.importerExporter, times(1)).getResourceWriter();
		order.verify(mockResourceWriter, times(1)).write(eq(mockResource), eq(json.getBytes()));
		verifyNoMoreInteractions(mockRegion, mockExportResourceResolver, mockResourceWriter);
		verifyNoInteractions(mockResource);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doExportFromWithNoResource() {

		ExportResourceResolver mockExportResourceResolver = mock(ExportResourceResolver.class);

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn(mockExportResourceResolver).when(this.importerExporter).getExportResourceResolver();
		doReturn(Optional.empty()).when(mockExportResourceResolver).resolve(eq(mockRegion));

		assertThat(this.importerExporter.doExportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(this.importerExporter, times(1)).getExportResourceResolver();
		verify(mockExportResourceResolver, times(1)).resolve(eq(mockRegion));
		verifyNoMoreInteractions(mockExportResourceResolver, mockRegion);
		verifyNoInteractions(mockRegion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void doExportFromNullRegion() {

		try {
			this.importerExporter.doExportFrom(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doImportIntoPutsPdxIntoRegionForJson() {

		Resource mockResource = mock(Resource.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		Region<Integer, PdxInstance> mockRegion = mock(Region.class);

		PdxInstance mockPdxInstanceOne = mock(PdxInstance.class);
		PdxInstance mockPdxInstanceTwo = mock(PdxInstance.class);

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		byte[] json = "[{ \"name\": \"Jon Doe\"}, { \"name\": \"Jane Doe\" }]".getBytes();

		doReturn(mockImportResourceResolver).when(this.importerExporter).getImportResourceResolver();
		doReturn(mockResourceReader).when(this.importerExporter).getResourceReader();
		doReturn(Optional.of(mockResource)).when(mockImportResourceResolver).resolve(eq(mockRegion));
		doReturn(json).when(mockResourceReader).read(eq(mockResource));
		doReturn(ArrayUtils.asArray(mockPdxInstanceOne, mockPdxInstanceTwo)).when(this.importerExporter).toPdx(eq(json));
		doReturn(1).when(this.importerExporter).resolveKey(eq(mockPdxInstanceOne));
		doReturn(2).when(this.importerExporter).resolveKey(eq(mockPdxInstanceTwo));

		assertThat(this.importerExporter.doImportInto(mockRegion)).isEqualTo(mockRegion);

		InOrder order =
			inOrder(this.importerExporter, mockRegion, mockResource, mockResourceReader, mockImportResourceResolver);

		order.verify(this.importerExporter, times(1)).getImportResourceResolver();
		order.verify(mockImportResourceResolver, times(1)).resolve(eq(mockRegion));
		order.verify(this.importerExporter, times(1)).getResourceReader();
		order.verify(mockResourceReader, times(1)).read(eq(mockResource));
		order.verify(this.importerExporter, times(1)).toPdx(eq(json));
		order.verify(this.importerExporter, times(1)).resolveKey(eq(mockPdxInstanceOne));
		order.verify(this.importerExporter, times(1)).resolveValue(eq(mockPdxInstanceOne));
		order.verify(this.importerExporter, times(1)).postProcess(eq(mockPdxInstanceOne));
		order.verify(mockRegion, times(1)).put(eq(1), eq(mockPdxInstanceOne));
		order.verify(this.importerExporter, times(1)).resolveKey(eq(mockPdxInstanceTwo));
		order.verify(this.importerExporter, times(1)).resolveValue(eq(mockPdxInstanceTwo));
		order.verify(this.importerExporter, times(1)).postProcess(eq(mockPdxInstanceTwo));
		order.verify(mockRegion, times(1)).put(eq(2), eq(mockPdxInstanceTwo));

		verifyNoMoreInteractions(mockRegion, mockImportResourceResolver, mockResourceReader);
		verifyNoInteractions(mockResource, mockPdxInstanceOne, mockPdxInstanceTwo);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doImportIntoWithNoResource() {

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		Region<?, ?> mockRegion = mock(Region.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		doReturn(mockImportResourceResolver).when(this.importerExporter).getImportResourceResolver();
		doReturn(mockResourceReader).when(this.importerExporter).getResourceReader();
		doReturn(Optional.empty()).when(mockImportResourceResolver).resolve(eq(mockRegion));

		assertThat(this.importerExporter.doImportInto(mockRegion)).isEqualTo(mockRegion);

		verify(this.importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(this.importerExporter, times(1)).getImportResourceResolver();
		verify(this.importerExporter, times(1)).getResourceReader();
		verify(mockImportResourceResolver, times(1)).resolve(eq(mockRegion));
		verifyNoMoreInteractions(this.importerExporter, mockImportResourceResolver);
		verifyNoInteractions(mockRegion, mockResourceReader);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doImportIntoWithResourceContainingNoContent() {

		byte[] json = {};

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		Region<?, ?> mockRegion = mock(Region.class);

		Resource mockResource = mock(Resource.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		doReturn(mockImportResourceResolver).when(this.importerExporter).getImportResourceResolver();
		doReturn(mockResourceReader).when(this.importerExporter).getResourceReader();
		doReturn(JsonCacheDataImporterExporter.EMPTY_PDX_INSTANCE_ARRAY).when(this.importerExporter).toPdx(any());
		doReturn(Optional.of(mockResource)).when(mockImportResourceResolver).resolve(eq(mockRegion));
		doReturn(json).when(mockResourceReader).read(eq(mockResource));

		assertThat(this.importerExporter.doImportInto(mockRegion)).isEqualTo(mockRegion);

		verify(this.importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(this.importerExporter, times(1)).getImportResourceResolver();
		verify(this.importerExporter, times(1)).getResourceReader();
		verify(this.importerExporter, times(1)).toPdx(eq(json));
		verify(this.importerExporter, times(1))
			.regionPutPdx(eq(mockRegion), eq(JsonCacheDataImporterExporter.EMPTY_PDX_INSTANCE_ARRAY));
		verify(mockImportResourceResolver, times(1)).resolve(eq(mockRegion));
		verify(mockResourceReader, times(1)).read(eq(mockResource));
		verifyNoMoreInteractions(this.importerExporter, mockImportResourceResolver, mockResourceReader);
		verifyNoInteractions(mockRegion, mockResource);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doImportIntoWithNoPdx() {

		byte[] json = "[{ \"name\":\"Jon Doe\" }]".getBytes();

		ImportResourceResolver mockImportResourceResolver = mock(ImportResourceResolver.class);

		Region<?, ?> mockRegion = mock(Region.class);

		Resource mockResource = mock(Resource.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		doReturn(mockImportResourceResolver).when(this.importerExporter).getImportResourceResolver();
		doReturn(mockResourceReader).when(this.importerExporter).getResourceReader();
		doReturn(JsonCacheDataImporterExporter.EMPTY_PDX_INSTANCE_ARRAY).when(this.importerExporter).toPdx(eq(json));
		doReturn(Optional.of(mockResource)).when(mockImportResourceResolver).resolve(eq(mockRegion));
		doReturn(json).when(mockResourceReader).read(eq(mockResource));

		assertThat(this.importerExporter.doImportInto(mockRegion)).isEqualTo(mockRegion);

		verify(this.importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(this.importerExporter, times(1)).getImportResourceResolver();
		verify(this.importerExporter, times(1)).getResourceReader();
		verify(this.importerExporter, times(1)).toPdx(eq(json));
		verify(this.importerExporter, times(1)).regionPutPdx(eq(mockRegion),
			eq(JsonCacheDataImporterExporter.EMPTY_PDX_INSTANCE_ARRAY));
		verify(mockImportResourceResolver, times(1)).resolve(eq(mockRegion));
		verify(mockResourceReader, times(1)).read(eq(mockResource));
		verifyNoMoreInteractions(this.importerExporter, mockImportResourceResolver, mockResourceReader);
		verifyNoInteractions(mockRegion, mockResource);
	}

	@Test(expected = IllegalArgumentException.class)
	public void doImportIntoNullRegion() {

		try {
			this.importerExporter.doImportInto(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void toJsonFromEmptyRegion() {

		Region<?, ?> mockRegion = mock(Region.class);

		assertThat(this.importerExporter.toJson(mockRegion)).isEqualTo("[]");

		verify(mockRegion, times(1)).values();
	}

	@Test(expected = IllegalArgumentException.class)
	public void toJsonFromNullRegion() {

		try {
			this.importerExporter.toJson(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void toPdxArrayFromJsonCallsJsonToPdxArrayConverter() {

		byte[] json = "[{ \"name\": \"Jon Doe\" }, { \"name\": \"Jane Doe\" }]".getBytes();

		JsonToPdxArrayConverter mockConverter = mock(JsonToPdxArrayConverter.class);

		PdxInstance mockPdxInstanceOne = mock(PdxInstance.class);
		PdxInstance mockPdxInstanceTwo = mock(PdxInstance.class);

		PdxInstance[] pdxArray = ArrayUtils.asArray(mockPdxInstanceOne, mockPdxInstanceTwo);

		doReturn(mockConverter).when(this.importerExporter).getJsonToPdxArrayConverter();
		doReturn(pdxArray).when(mockConverter).convert(eq(json));

		assertThat(this.importerExporter.toPdx(json)).isEqualTo(pdxArray);

		verify(this.importerExporter, times(1)).getJsonToPdxArrayConverter();
		verify(mockConverter, times(1)).convert(eq(json));
		verifyNoMoreInteractions(mockConverter);
	}

	static class TestJsonCacheDataImporterExporter extends JsonCacheDataImporterExporter {

		@Override
		protected @NonNull ExportResourceResolver getExportResourceResolver() {
			return super.getExportResourceResolver();
		}

		@Override
		protected @NonNull ImportResourceResolver getImportResourceResolver() {
			return super.getImportResourceResolver();
		}

		@Override
		protected @NonNull ResourceReader getResourceReader() {
			return super.getResourceReader();
		}

		@Override
		protected @NonNull ResourceWriter getResourceWriter() {
			return super.getResourceWriter();
		}
	}
}
