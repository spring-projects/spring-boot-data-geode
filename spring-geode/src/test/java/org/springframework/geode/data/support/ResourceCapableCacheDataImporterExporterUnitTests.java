/*
 * Copyright 2020 the original author or authors.
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.data.gemfire.tests.util.ReflectionUtils;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.core.io.support.ByteArrayResourceReader;
import org.springframework.geode.core.io.support.FileResourceWriter;
import org.springframework.geode.core.io.support.ResourcePrefix;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.AbstractCacheResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.AbstractExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.AbstractImportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ClassPathImportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.FileSystemExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import org.slf4j.Logger;

/**
 * Unit Tests for {@link ResourceCapableCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.core.io.WritableResource
 * @see org.springframework.geode.core.io.ResourceReader
 * @see org.springframework.geode.core.io.ResourceWriter
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter
 * @since 1.3.1
 */
public class ResourceCapableCacheDataImporterExporterUnitTests {

	@SuppressWarnings("unchecked")
	private <T> T inject(@NonNull T target, @NonNull String fieldName, @Nullable Object value) {

		try {
			return (T) ReflectionUtils.setField(target, fieldName, value);
		}
		catch (NoSuchFieldException cause) {
			throw newRuntimeException(cause, "Failed to set field [%s] on object of type [%s] to value [%s]",
				fieldName, ObjectUtils.nullSafeClassName(target), value);
		}
	}

	@Test
	public void afterPropertiesSetUsesImportExportReaderWriterResourceDefaults() {

		ResourceCapableCacheDataImporterExporter importerExporter = new TestResourceCapableCacheDataImporterExporter();

		importerExporter.afterPropertiesSet();

		assertThat(importerExporter.getExportResourceResolver()).isInstanceOf(FileSystemExportResourceResolver.class);
		assertThat(importerExporter.getImportResourceResolver()).isInstanceOf(ClassPathImportResourceResolver.class);
		assertThat(importerExporter.getResourceLoader().orElse(null)).isNull();
		assertThat(importerExporter.getResourceReader()).isInstanceOf(ByteArrayResourceReader.class);
		assertThat(importerExporter.getResourceWriter()).isInstanceOf(FileResourceWriter.class);
	}

	@Test
	public void afterPropertiesSetWithInjectedImportExportResourceResolversAndResourceReaderWriter() {

		ResourceLoaderAwareExportResourceResolver mockExportResourceResolver =
			mock(ResourceLoaderAwareExportResourceResolver.class);

		ResourceLoaderAwareImportResourceResolver mockImportResourceResolver =
			mock(ResourceLoaderAwareImportResourceResolver.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		ResourceWriter mockResourceWriter = mock(ResourceWriter.class);

		ResourceCapableCacheDataImporterExporter importerExporter = new TestResourceCapableCacheDataImporterExporter();

		inject(importerExporter, "exportResourceResolver", mockExportResourceResolver);
		inject(importerExporter, "importResourceResolver", mockImportResourceResolver);
		inject(importerExporter, "resourceReader", mockResourceReader);
		inject(importerExporter, "resourceWriter", mockResourceWriter);

		importerExporter.setResourceLoader(mockResourceLoader);
		importerExporter.afterPropertiesSet();

		assertThat(importerExporter.getExportResourceResolver()).isEqualTo(mockExportResourceResolver);
		assertThat(importerExporter.getImportResourceResolver()).isEqualTo(mockImportResourceResolver);
		assertThat(importerExporter.getResourceLoader().orElse(null)).isEqualTo(mockResourceLoader);
		assertThat(importerExporter.getResourceReader()).isEqualTo(mockResourceReader);
		assertThat(importerExporter.getResourceWriter()).isEqualTo(mockResourceWriter);

		verify(mockExportResourceResolver, times(1)).setResourceLoader(eq(mockResourceLoader));
		verify(mockImportResourceResolver, times(1)).setResourceLoader(eq(mockResourceLoader));
		verifyNoInteractions(mockResourceLoader, mockResourceReader, mockResourceWriter);
	}

	@Test
	public void setAndGetResourceLoader() {

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceCapableCacheDataImporterExporter importerExporter = new TestResourceCapableCacheDataImporterExporter();

		assertThat(importerExporter.getResourceLoader().orElse(null)).isNull();

		importerExporter.setResourceLoader(mockResourceLoader);

		assertThat(importerExporter.getResourceLoader().orElse(null)).isEqualTo(mockResourceLoader);

		importerExporter.setResourceLoader(null);

		assertThat(importerExporter.getResourceLoader().orElse(null)).isNull();
	}

	// Tests for CacheResourceResolver and friends (sub-types)

	@Test
	public void expressionParserIsASpelExpressionParser() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		assertThat(resourceResolver.getExpressionParser()).isInstanceOf(SpelExpressionParser.class);
	}

	@Test
	public void parserContextIsTemplateBased() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		assertThat(resourceResolver.getParserContext()).isEqualTo(ParserContext.TEMPLATE_EXPRESSION);
	}

	@Test
	public void isQualifiedWithExistingResource() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).exists();

		assertThat(resourceResolver.isQualified(mockResource)).isTrue();

		verify(mockResource, times(1)).exists();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isQualifiedWithNonExistingResource() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		Resource mockResource = mock(Resource.class);

		doReturn(false).when(mockResource).exists();

		assertThat(resourceResolver.isQualified(mockResource)).isFalse();

		verify(mockResource, times(1)).exists();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isQualifiedWithNullResource() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		assertThat(resourceResolver.isQualified(null)).isFalse();
	}

	@Test
	public void getFullyQualifiedResourceLocationCallsGetResourcePathAndGetResourceNameWithRegion() {

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheResourceResolver resourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver());

		doReturn("/path/to/").when(resourceResolver).getResourcePath();
		doReturn("resource.xml").when(resourceResolver).getResourceName(eq(mockRegion));

		assertThat(resourceResolver.getFullyQualifiedResourceLocation(mockRegion)).isEqualTo("/path/to/resource.xml");

		verify(resourceResolver, times(1)).getResourcePath();
		verify(resourceResolver, times(1)).getResourceName(eq(mockRegion));
	}

	@Test
	public void getResourceLocationIsEvaluated() {

		String testPropertyName = "test.property.name";
		String rawPropertyValue = "https://skullbox:8181/nurv/cache/%s/data/resource.json";
		String evaluatedPropertyValue = "https://skullbox:8181/nurv/cache/example/data/resource.json";

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn("Example").when(mockRegion).getName();

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true).when(mockEnvironment).containsProperty(eq(testPropertyName));
		doReturn(rawPropertyValue).when(mockEnvironment).getProperty(eq(testPropertyName));

		TestResourceCapableCacheDataImporterExporter importerExporter =
			spy(new TestResourceCapableCacheDataImporterExporter());

		AbstractCacheResourceResolver resourceResolver = spy(importerExporter.new TestCacheResourceResolver());

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();

		doAnswer(invocation -> {

			String expression = invocation.getArgument(0);
			Region<?, ?> region = invocation.getArgument(1);

			return String.format(expression, region.getName().toLowerCase());

		}).when(resourceResolver).evaluate(anyString(), any(Region.class));

		assertThat(resourceResolver.getResourceLocation(mockRegion, testPropertyName))
			.isEqualTo(evaluatedPropertyValue);

		verify(mockEnvironment, times(1)).containsProperty(eq(testPropertyName));
		verify(mockEnvironment, times(1)).getProperty(eq(testPropertyName));
		verify(mockRegion, times(1)).getName();
		verify(importerExporter, times(1)).getEnvironment();
		verify(resourceResolver, times(1)).evaluate(eq(rawPropertyValue), eq(mockRegion));
		verifyNoMoreInteractions(mockEnvironment, mockRegion);
	}

	@Test
	public void getResourceLocationCallsGetFullyQualifiedResourceLocationWhenEnvironmentIsNull() {

		Region<?, ?> mockRegion = mock(Region.class);

		TestResourceCapableCacheDataImporterExporter importerExporter =
			spy(new TestResourceCapableCacheDataImporterExporter());

		AbstractCacheResourceResolver resourceResolver = spy(importerExporter.new TestCacheResourceResolver());

		doReturn(Optional.empty()).when(importerExporter).getEnvironment();
		doReturn("/path/to/resource.xml").when(resourceResolver)
			.getFullyQualifiedResourceLocation(eq(mockRegion));

		assertThat(resourceResolver.getResourceLocation(mockRegion, "test.property.name"))
			.isEqualTo("/path/to/resource.xml");

		verify(importerExporter, times(1)).getEnvironment();
		verify(resourceResolver, never()).evaluate(anyString(), any(Region.class));
		verifyNoInteractions(mockRegion);
	}

	@Test
	public void getResourceLocationCallsGetFullyQualifiedResourceLocationWhenEnvironmentDoesNotContainProperty() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(false).when(mockEnvironment).containsProperty(anyString());

		TestResourceCapableCacheDataImporterExporter importerExporter =
			spy(new TestResourceCapableCacheDataImporterExporter());

		AbstractCacheResourceResolver resourceResolver = spy(importerExporter.new TestCacheResourceResolver());

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn("/path/to/resource.bin").when(resourceResolver)
			.getFullyQualifiedResourceLocation(eq(mockRegion));

		assertThat(resourceResolver.getResourceLocation(mockRegion, "test.property.name"))
			.isEqualTo("/path/to/resource.bin");

		verify(importerExporter, times(1)).getEnvironment();
		verify(resourceResolver, never()).evaluate(anyString(), any(Region.class));
		verify(mockEnvironment, times(1)).containsProperty(eq("test.property.name"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	public void getResourceLocationCallsGetFullyQualifiedResourceLocationWhenEnvironmentPropertyIsNotSet() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true).when(mockEnvironment).containsProperty(eq("test.property.name"));
		doReturn(null).when(mockEnvironment).getProperty(anyString());

		TestResourceCapableCacheDataImporterExporter importerExporter =
			spy(new TestResourceCapableCacheDataImporterExporter());

		AbstractCacheResourceResolver resourceResolver = spy(importerExporter.new TestCacheResourceResolver());

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn("/path/to/resource.dat").when(resourceResolver)
			.getFullyQualifiedResourceLocation(eq(mockRegion));

		assertThat(resourceResolver.getResourceLocation(mockRegion, "test.property.name"))
			.isEqualTo("/path/to/resource.dat");

		verify(importerExporter, times(1)).getEnvironment();
		verify(resourceResolver, never()).evaluate(anyString(), any(Region.class));
		verify(mockEnvironment, times(1)).containsProperty(eq("test.property.name"));
		verify(mockEnvironment, times(1)).getProperty(eq("test.property.name"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getResourceLocationWithNullRegion() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		try {
			resourceResolver.getResourceLocation(null, "test.property.name");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void getResourceLocationWithNoPropertyName() {

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		try {
			resourceResolver.getResourceLocation(mockRegion, "  ");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Property name [  ] must be specified");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockRegion);
		}
	}

	@Test
	public void getResourceNameFromRegion() {

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn("Example").when(mockRegion).getName();

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		assertThat(resourceResolver.getResourceName(mockRegion))
			.isEqualTo(String.format(ResourceCapableCacheDataImporterExporter.RESOURCE_NAME_PATTERN, "example"));

		verify(mockRegion, times(1)).getName();
		verifyNoMoreInteractions(mockRegion);
	}

	@Test
	public void getResourceNameFromString() {

		AbstractCacheResourceResolver resourceResolver =
			new TestResourceCapableCacheDataImporterExporter().new TestCacheResourceResolver();

		assertThat(resourceResolver.getResourceName("EXAMPLE"))
			.isEqualTo(String.format(ResourceCapableCacheDataImporterExporter.RESOURCE_NAME_PATTERN, "EXAMPLE"));
	}

	@Test
	@SuppressWarnings("all")
	public void evaluatesExpressionContainingBeanAndPropertyAndRegionReferences() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		Environment mockEnvironment = mock(Environment.class);

		Map<String, String> environmentProperties = MapBuilder.<String, String>newMapBuilder()
			.put("port", "8181")
			.put("user.password", "s3c3rt")
			.put("user.name", System.getProperty("user.name", "jonDoe"))
			.build();

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn(true).when(mockApplicationContext).containsBean("utility");
		doReturn(UtilityBean.INSTANCE).when(mockApplicationContext).getBean("utility");
		doReturn(mockEnvironment).when(mockApplicationContext).getEnvironment();
		doAnswer(invocation -> environmentProperties.containsKey(invocation.getArgument(0)))
			.when(mockEnvironment).containsProperty(any());
		doAnswer(invocation -> environmentProperties.get(invocation.getArgument(0)))
			.when(mockEnvironment).getProperty(any());
		doReturn("Example").when(mockRegion).getName();

		TestResourceCapableCacheDataImporterExporter importerExporter =
			mock(TestResourceCapableCacheDataImporterExporter.class);

		doReturn(Optional.ofNullable(mockApplicationContext)).when(importerExporter).getApplicationContext();
		doReturn(Optional.ofNullable(mockEnvironment)).when(importerExporter).getEnvironment();

		AbstractCacheResourceResolver resourceResolver = importerExporter.new TestCacheResourceResolver();

		String expression = "https://#{#env['user.name']}:#{#env['user.password']}@skullbox:#{port}/nurv/cache/#{utility.toUpperCase(#regionName)}/data/import";
		String parsedExpression = String.format("https://%s:s3c3rt@skullbox:8181/nurv/cache/EXAMPLE/data/import",
			environmentProperties.get("user.name"));

		assertThat(resourceResolver.evaluate(expression, mockRegion)).isEqualTo(parsedExpression);

		verify(mockApplicationContext, times(1)).containsBean(eq("utility"));
		verify(mockApplicationContext, times(1)).getBean(eq("utility"));
		verify(mockApplicationContext, times(2)).getEnvironment();
		verify(mockEnvironment, times(1)).containsProperty(eq("port"));
		verify(mockEnvironment, times(1)).getProperty(eq("port"));
		verify(mockEnvironment, times(1)).getProperty(eq("user.password"));
		verify(mockEnvironment, times(1)).getProperty(eq("user.name"));
		verify(mockRegion, times(1)).getName();
		verifyNoMoreInteractions(mockEnvironment, mockRegion);
	}

	@Test
	public void resolveExportResource() {

		Logger mockLogger = mock(Logger.class);

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn("/Example").when(mockRegion).getFullPath();

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		WritableResource mockResource = mock(WritableResource.class);

		doReturn(mockResource).when(mockResourceLoader).getResource(eq("/path/to/resource.xml"));
		doReturn(false).when(mockResource).exists();
		doReturn(false).when(mockResource).isWritable();

		TestResourceCapableCacheDataImporterExporter importerExporter =
			spy(new TestResourceCapableCacheDataImporterExporter());

		AbstractExportResourceResolver exportResourceResolver = spy(importerExporter.new TestExportResourceResolver());

		exportResourceResolver.setResourceLoader(mockResourceLoader);

		doReturn(mockLogger).when(importerExporter).getLogger();

		doReturn("/path/to/resource.xml")
			.when(exportResourceResolver).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_EXPORT_RESOURCE_LOCATION_PROPERTY_NAME));

		//doReturn(Optional.of(mockResource)).when(exportResourceResolver).resolve(eq("/path/to/resource.xml"));

		assertThat(exportResourceResolver.resolve(mockRegion)).isEqualTo(Optional.of(mockResource));

		verify(importerExporter, times(2)).getLogger();
		verify(exportResourceResolver, times(1)).getResourceLocation(eq(mockRegion),
			eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_EXPORT_RESOURCE_LOCATION_PROPERTY_NAME));
		verify(exportResourceResolver, times(1)).isQualified(eq(mockResource));
		verify(exportResourceResolver, times(1))
			.onMissingResource(eq(mockResource), eq("/path/to/resource.xml"));
		verify(exportResourceResolver, times(1)).resolve(eq("/path/to/resource.xml"));
		verify(mockResourceLoader, times(1)).getResource(eq("/path/to/resource.xml"));
		verify(mockResource, times(1)).exists();
		verify(mockResource, times(1)).isWritable();
		verify(mockLogger, times(1))
			.warn(eq("WARNING! Resource at location [{}] does not exist; will try to create it"),
				eq("/path/to/resource.xml"));
		verify(mockLogger, times(1))
			.warn(eq("WARNING! Resource [{}] for Region [{}] is not writable"), eq("/path/to/resource.xml"),
				eq("/Example"));
		verify(mockRegion, times(1)).getFullPath();
		verifyNoMoreInteractions(mockLogger, mockRegion, mockResource, mockResourceLoader);
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveExportResourceWithNullRegionThrowsIllegalArgumentException() {

		AbstractExportResourceResolver exportResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestExportResourceResolver());

		try {
			exportResourceResolver.resolve((Region<?, ?>) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void fileSystemExportResourceResolverResolvesToFileSystemPath() {

		AbstractExportResourceResolver exportResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new FileSystemExportResourceResolver());

		assertThat(exportResourceResolver.getResourcePath())
			.isEqualTo(String.format("file://%s", System.getProperty("user.dir")));
	}

	@Test
	public void resolveImportResource() {

		Region<?, ?> mockRegion = mock(Region.class);

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).isReadable();

		AbstractImportResourceResolver importResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestImportResourceResolver());

		doReturn("/path/to/resource.json")
			.when(importResourceResolver).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));

		doReturn(Optional.of(mockResource)).when(importResourceResolver).resolve(eq("/path/to/resource.json"));

		assertThat(importResourceResolver.resolve(mockRegion)).isEqualTo(Optional.of(mockResource));

		verify(importResourceResolver, times(1)).getResourceLocation(eq(mockRegion),
			eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));
		verify(importResourceResolver, times(1)).resolve(eq("/path/to/resource.json"));
		verify(mockResource, times(1)).isReadable();
		verifyNoMoreInteractions(mockRegion);
		verifyNoInteractions(mockRegion);
	}

	@Test(expected = IllegalStateException.class)
	public void resolveImportResourceWhenResourceIsNotPresentThrowsIllegalStateException() {

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn("/Example").when(mockRegion).getFullPath();

		AbstractImportResourceResolver importResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestImportResourceResolver());

		doReturn("/path/to/resource.json")
			.when(importResourceResolver).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));

		doReturn(Optional.empty()).when(importResourceResolver).resolve(eq("/path/to/resource.json"));

		try {
			importResourceResolver.resolve(mockRegion);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [/path/to/resource.json] for Region [/Example] does not exist");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(importResourceResolver, times(1)).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));
			verify(importResourceResolver, times(1)).resolve(eq("/path/to/resource.json"));
			verify(mockRegion, times(1)).getFullPath();
			verifyNoMoreInteractions(mockRegion);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void resolveImportResourceWhenResourceIsNotReadableThrowsIllegalStateException() {

		Region<?, ?> mockRegion = mock(Region.class);

		doReturn("/Example").when(mockRegion).getFullPath();

		Resource mockResource = mock(Resource.class);

		doReturn(false).when(mockResource).isReadable();

		AbstractImportResourceResolver importResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestImportResourceResolver());

		doReturn("/path/to/resource.json")
			.when(importResourceResolver).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));

		doReturn(Optional.of(mockResource)).when(importResourceResolver).resolve(eq("/path/to/resource.json"));

		try {
			importResourceResolver.resolve(mockRegion);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [/path/to/resource.json] for Region [/Example] is not readable");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(importResourceResolver, times(1)).getResourceLocation(eq(mockRegion),
				eq(ResourceCapableCacheDataImporterExporter.CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME));
			verify(importResourceResolver, times(1)).resolve(eq("/path/to/resource.json"));
			verify(mockResource, times(1)).isReadable();
			verify(mockRegion, times(1)).getFullPath();
			verifyNoMoreInteractions(mockRegion, mockResource);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveImportResourceWithNullRegionThrowsIllegalArgumentException() {

		AbstractImportResourceResolver importResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new TestImportResourceResolver());

		try {
			importResourceResolver.resolve((Region<?, ?>) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void classPathImportResourceResolverResolvesToClassPath() {

		AbstractImportResourceResolver importResourceResolver =
			spy(new TestResourceCapableCacheDataImporterExporter().new ClassPathImportResourceResolver());

		assertThat(importResourceResolver.getResourcePath())
			.isEqualTo(ResourcePrefix.CLASSPATH_URL_PREFIX.toUrlPrefix());
	}

	interface ResourceLoaderAwareExportResourceResolver extends ResourceLoaderAware, ExportResourceResolver { }

	interface ResourceLoaderAwareImportResourceResolver extends ResourceLoaderAware, ImportResourceResolver { }

	@SuppressWarnings("rawtypes")
	static class TestResourceCapableCacheDataImporterExporter extends ResourceCapableCacheDataImporterExporter {

		@Override
		protected Optional<ApplicationContext> getApplicationContext() {
			return super.getApplicationContext();
		}

		@Override
		protected Optional<Environment> getEnvironment() {
			return super.getEnvironment();
		}

		@Override
		protected Logger getLogger() {
			return super.getLogger();
		}

		@Override
		protected @NonNull Region<?, ?> doExportFrom(@NonNull Region region) {
			return region;
		}

		@Override
		protected @NonNull Region<?, ?> doImportInto(@NonNull Region region) {
			return region;
		}

		class TestCacheResourceResolver extends AbstractCacheResourceResolver {

			@Override
			public Optional<Resource> resolve(@NonNull Region<?, ?> region) {
				return Optional.empty();
			}

			@NonNull @Override
			protected String getResourcePath() {
				return null;
			}
		}

		class TestExportResourceResolver extends AbstractExportResourceResolver {

			@NonNull @Override
			protected String getResourcePath() {
				return null;
			}
		}

		class TestImportResourceResolver extends AbstractImportResourceResolver {

			@NonNull @Override
			protected String getResourcePath() {
				return null;
			}
		}
	}

	@SuppressWarnings("unused")
	public static class UtilityBean {

		public static UtilityBean INSTANCE = new UtilityBean();

		public String toUpperCase(String value) {
			return value.toUpperCase();
		}
	}
}
