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
package org.springframework.geode.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.geode.cache.Region;

/**
 * Unit Tests for {@link CacheDataExporter}
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.geode.data.CacheDataExporter
 * @since 1.3.0
 */
public class CacheDataExporterUnitTests {

	@Test
	public void postProcessBeforeDestructionCallsExportFromGivenARegionArgument() {

		Region<?, ?> mockRegion = mock(Region.class);

		CacheDataExporter exporter = mock(CacheDataExporter.class);

		doCallRealMethod().when(exporter).postProcessBeforeDestruction(any(), anyString());

		exporter.postProcessBeforeDestruction(mockRegion, "TestRegion");

		verify(exporter, times(1)).exportFrom(eq(mockRegion));
	}

	@Test
	public void postProcessBeforeDestructionWillNotCallExportFromGivenAnObject() {

		CacheDataExporter exporter = mock(CacheDataExporter.class);

		doCallRealMethod().when(exporter).postProcessBeforeDestruction(any(), anyString());

		exporter.postProcessBeforeDestruction(new Object(), "TestRegion");

		verify(exporter, never()).exportFrom(any(Region.class));
	}

	@Test
	public void postProcessBeforeDestructionIsNullSafe() {

		CacheDataExporter exporter = mock(CacheDataExporter.class);

		doCallRealMethod().when(exporter).postProcessBeforeDestruction(any(), anyString());

		exporter.postProcessBeforeDestruction(null, "TestRegion");

		verify(exporter, never()).exportFrom(any(Region.class));
	}
}
