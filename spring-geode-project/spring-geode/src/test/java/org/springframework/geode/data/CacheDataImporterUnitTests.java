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
package org.springframework.geode.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.geode.cache.Region;

/**
 * Unit Tests for {@link CacheDataImporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.geode.data.CacheDataImporter
 * @since 1.3.0
 */
public class CacheDataImporterUnitTests {

	@Test
	public void postProcessAfterInitializationCallsImportFromGivenARegionArgument() {

		Region<?, ?> mockRegion = mock(Region.class);

		CacheDataImporter importer = mock(CacheDataImporter.class);

		doCallRealMethod().when(importer).postProcessAfterInitialization(any(), anyString());
		doReturn(mockRegion).when(importer).importInto(eq(mockRegion));

		assertThat(importer.postProcessAfterInitialization(mockRegion, "TestRegion")).isEqualTo(mockRegion);

		verify(importer, times(1)).importInto(eq(mockRegion));
	}

	@Test
	public void postProcessAfterInitializationWillNotCallImportFromGivenAnObject() {

		CacheDataImporter importer = mock(CacheDataImporter.class);

		doCallRealMethod().when(importer).postProcessAfterInitialization(any(), anyString());

		Object bean = new Object();

		assertThat(importer.postProcessAfterInitialization(bean, "TestRegion")).isEqualTo(bean);

		verify(importer, never()).importInto(any(Region.class));
	}

	@Test
	public void postProcessAfterInitializationIsNullSafe() {

		CacheDataImporter importer = mock(CacheDataImporter.class);

		doCallRealMethod().when(importer).postProcessAfterInitialization(any(), anyString());

		assertThat(importer.postProcessAfterInitialization(null, "TestRegion")).isNull();

		verify(importer, never()).importInto(any(Region.class));
	}
}
