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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.util.ArrayUtils;

/**
 * Unit Tests for {@link AbstractCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see java.util.function.Predicate
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.data.AbstractCacheDataImporterExporter
 * @since 1.3.0
 */
public class AbstractCacheDataImporterExporterUnitTests {

	private AbstractCacheDataImporterExporter mockAbstractCacheDataImporterExporter() {

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).exportFrom(any());
		doCallRealMethod().when(importerExporter).importInto(any());
		doCallRealMethod().when(importerExporter).getLogger();
		doCallRealMethod().when(importerExporter).getRegionPredicate();
		doCallRealMethod().when(importerExporter).isExportEnabled(any());
		doCallRealMethod().when(importerExporter).isImportEnabled(any());

		return importerExporter;
	}
	@Test
	public void setAndGetApplicationContext() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).getApplicationContext();
		doCallRealMethod().when(importerExporter).requireApplicationContext();
		doCallRealMethod().when(importerExporter).setApplicationContext(any());

		assertThat(importerExporter.getApplicationContext().orElse(null)).isNull();

		importerExporter.setApplicationContext(mockApplicationContext);

		assertThat(importerExporter.getApplicationContext().orElse(null)).isEqualTo(mockApplicationContext);

		importerExporter.setApplicationContext(null);

		assertThat(importerExporter.getApplicationContext().orElse(null)).isNull();
	}

	@Test
	public void callRequireApplicationContextWhenPresent() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).getApplicationContext();
		doCallRealMethod().when(importerExporter).requireApplicationContext();
		doCallRealMethod().when(importerExporter).setApplicationContext(any());

		importerExporter.setApplicationContext(mockApplicationContext);

		assertThat(importerExporter.getApplicationContext().orElse(null)).isEqualTo(mockApplicationContext);
		assertThat(importerExporter.requireApplicationContext()).isEqualTo(mockApplicationContext);
	}

	@Test(expected = IllegalStateException.class)
	public void calRequireApplicationContextWhenNotPresentThrowsIllegalStateException() {

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).requireApplicationContext();

		try {
			importerExporter.requireApplicationContext();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("ApplicationContext was not configured");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void setAndGetEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).setEnvironment(any());
		doCallRealMethod().when(importerExporter).getEnvironment();
		doCallRealMethod().when(importerExporter).requireEnvironment();

		assertThat(importerExporter.getEnvironment().orElse(null)).isNull();

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.requireEnvironment()).isEqualTo(mockEnvironment);

		importerExporter.setEnvironment(null);

		assertThat(importerExporter.getEnvironment().orElse(null)).isNull();
	}

	@Test
	public void callRequireEnvironmentWhenPresent() {

		Environment mockEnvironment = mock(Environment.class);

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).getEnvironment();
		doCallRealMethod().when(importerExporter).setEnvironment(any());
		doCallRealMethod().when(importerExporter).requireEnvironment();

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.requireEnvironment()).isEqualTo(mockEnvironment);
	}

	@Test(expected = IllegalStateException.class)
	public void callRequireEnvironmentWhenNotPresentThrowsIllegalStateException() {

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).requireEnvironment();

		try {
			importerExporter.requireEnvironment();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Environment was not configured");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void isExportEnabledReturnsTrueWhenExportIsEnabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		assertThat(importerExporter.isExportEnabled(mockEnvironment)).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isExportEnabledReturnsFalseWhenExportIsDisabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		assertThat(importerExporter.isExportEnabled(mockEnvironment)).isFalse();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isExportEnabledReturnsFalseWhenEnvironmentIsNull() {
		assertThat(mockAbstractCacheDataImporterExporter().isExportEnabled(null)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenEnvironmentIsPresentPropertyIsTrueAndPredicateSaysYes() {

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
			eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED)))
				.thenReturn(true);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		doReturn(true).when(mockPredicate).test(any(Region.class));

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(mockRegion).when(importerExporter).doExportFrom(eq(mockRegion));
		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).doExportFrom(eq(mockRegion));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenEnvironmentIsNotPresentWillNotCallDoExportFrom() {

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(Optional.empty()).when(importerExporter).getEnvironment();

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, never()).doExportFrom(any(Region.class));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenEnvironmentPropertyIsNotSetWillNotCallDoExportFrom() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
			eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED)))
				.thenReturn(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, never()).getRegionPredicate();
		verify(importerExporter, never()).doExportFrom(any(Region.class));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenPredicateSaysNo() {

		Environment mockEnvironment = mock(Environment.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(true).when(importerExporter).isExportEnabled(eq(mockEnvironment));
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();
		doReturn(false).when(mockPredicate).test(any());

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isExportEnabled(eq(mockEnvironment));
		verify(importerExporter, times(1)).getRegionPredicate();
		verify(importerExporter, never()).doExportFrom(any(Region.class));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
		verifyNoInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void exportFromNullRegionThrowsIllegalArgumentException() {

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		try {
			importerExporter.exportFrom(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(importerExporter, never()).getEnvironment();
			verify(importerExporter, never()).doExportFrom(any(Region.class));
		}
	}

	@SuppressWarnings("unchecked")
	private AbstractCacheDataImporterExporter callRealMethodsFor(AbstractCacheDataImporterExporter importerExporter) {

		doCallRealMethod().when(importerExporter).importInto(any());
		doCallRealMethod().when(importerExporter).commaDelimitedListOfStringsToSet(anyString());
		doCallRealMethod().when(importerExporter).containsAny(any(Collection.class), any(Collection.class));
		doCallRealMethod().when(importerExporter).getDefaultProfilesIfEmpty(any(Set.class));
		doCallRealMethod().when(importerExporter).getRegionPredicate();
		doCallRealMethod().when(importerExporter).isImportEnabled(any());
		doCallRealMethod().when(importerExporter).isImportEnabled(any(Set.class), anyString());
		doCallRealMethod().when(importerExporter).isNonDefaultProfileSet(any(Set.class));
		doCallRealMethod().when(importerExporter).isNotSet(anyString());

		return importerExporter;
	}

	private Environment configureImport(Environment mockEnvironment, boolean enabled) {

		doReturn(enabled).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));

		return mockEnvironment;
	}

	private Environment disableImport(Environment mockEnvironment) {
		return configureImport(mockEnvironment, false);
	}

	private Environment enableImport(Environment mockEnvironment) {
		return configureImport(mockEnvironment, true);
	}

	private Environment withEnvironmentActiveProfiles(Environment mockEnvironment,
			String... environmentActiveProfiles) {

		doReturn(ArrayUtils.nullSafeArray(environmentActiveProfiles, String.class))
			.when(mockEnvironment).getActiveProfiles();

		return mockEnvironment;
	}

	private Environment withEnvironmentDefaultProfiles(Environment mockEnvironment,
			String... environmentDefaultProfiles) {

		doReturn(ArrayUtils.nullSafeArray(environmentDefaultProfiles, String.class))
			.when(mockEnvironment).getDefaultProfiles();

		return mockEnvironment;
	}

	private Environment withImportActiveProfiles(Environment mockEnvironment, String importActiveProfiles) {

		doReturn(importActiveProfiles).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));

		return mockEnvironment;
	}

	@Test
	public void isImportEnabledReturnsTrueWhenImportIsEnabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));

		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(mockEnvironment)).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isImportEnabledReturnsFalseWhenImportIsDisabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(false).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));

		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(mockEnvironment)).isFalse();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isImportEnabledReturnsFalseWhenEnvironmentIsNull() {
		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(null)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentIsPresentImportIsEnabledPredicateSaysYesAndActiveProfilesMatchCallsDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		doReturn(true).when(mockPredicate).test(eq(mockRegion));

		Environment mockEnvironment = mock(Environment.class);

		doReturn(new String[] { "TEST" }).when(mockEnvironment).getActiveProfiles();
		doReturn(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		doReturn("  DEV , TEST").when(mockEnvironment).getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
			eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(mockRegion).when(importerExporter).doImportInto(eq(mockRegion));
		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).requireEnvironment();
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("TEST")), eq("  DEV , TEST"));
		verify(importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentIsNull() {

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, never()).requireEnvironment();
		verify(importerExporter, never()).isImportEnabled(any(Environment.class));
		verify(importerExporter, never()).isImportEnabled(any(Set.class), anyString());
		verify(importerExporter, never()).doImportInto(eq(mockRegion));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenImportIsDisabled() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = disableImport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importerExporter, never()).doImportInto(any());
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenPredicateSaysNo() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		doReturn(false).when(mockPredicate).test(any());

		Environment mockEnvironment = enableImport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importerExporter, times(1)).getRegionPredicate();
		verify(importerExporter, never()).doImportInto(any(Region.class));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
		verifyNoMoreInteractions(mockEnvironment, mockPredicate);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentActiveProfilesAreNull() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = enableImport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importerExporter, times(1)).getRegionPredicate();
		verify(importerExporter, never()).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenImportActiveProfilesAreNotSetCallsDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentActiveProfiles(
			enableImport(mock(Environment.class)), "PROD"), "  ");

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(mockRegion).when(importerExporter).doImportInto(eq(mockRegion));
		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).requireEnvironment();
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("PROD")), eq("  "));
		verify(importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentActiveProfilesDoesNotContainImportActiveProfilesWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment =
			withImportActiveProfiles(enableImport(mock(Environment.class)), "DEV,TEST");

		doReturn(new String[0]).doReturn(new String[] { "PROD" }).when(mockEnvironment).getActiveProfiles();

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(2)).getEnvironment();
		verify(importerExporter, times(3)).requireEnvironment();
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.emptySet()), eq("DEV,TEST"));
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("PROD")), eq("DEV,TEST"));
		verify(importerExporter, never()).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentDefaultProfilesDoesNotContainImportActiveProfilesWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentActiveProfiles(
			enableImport(mock(Environment.class))), "DEV,TEST");

		doReturn(new String[] { "PROD" }).doReturn(new String[] { "default" })
			.when(mockEnvironment).getDefaultProfiles();

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(2)).getEnvironment();
		verify(importerExporter, times(4)).requireEnvironment();
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("PROD")), eq("DEV,TEST"));
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.emptySet()), eq("DEV,TEST"));
		verify(importerExporter, never()).doImportInto(any(Region.class));
		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, times(2)).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentDefaultProfilesAndActiveProfilesConflictWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentDefaultProfiles(
			enableImport(mock(Environment.class)), "DEV", "TEST"),
			"DEV,TEST");

		doReturn(null).doReturn(new String[] { "PROD" }).when(mockEnvironment).getActiveProfiles();

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(2)).getEnvironment();
		verify(importerExporter, times(1)).requireEnvironment();
		verify(importerExporter, times(1))
			.getDefaultProfilesIfEmpty(Collections.singleton("PROD"));
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("PROD")), eq("DEV,TEST"));
		verify(importerExporter, never()).doImportInto(any(Region.class));
		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, never()).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentActiveProfilesAreNotSetAndDefaultProfilesContainImportActiveProfilesCallsDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentDefaultProfiles(withEnvironmentActiveProfiles(
			enableImport(mock(Environment.class))), "TEST"), "DEV,TEST");

		AbstractCacheDataImporterExporter importerExporter =
			callRealMethodsFor(mock(AbstractCacheDataImporterExporter.class));

		doReturn(mockRegion).when(importerExporter).doImportInto(eq(mockRegion));
		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockEnvironment).when(importerExporter).requireEnvironment();

		assertThat(importerExporter.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(2)).requireEnvironment();
		verify(importerExporter, times(1))
			.isImportEnabled(eq(Collections.singleton("TEST")), eq("DEV,TEST"));
		verify(importerExporter, times(1)).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void importIntoNullRegionThrowsIllegalArgumentException() {

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).importInto(any());

		try {
			importerExporter.importInto(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(importerExporter, never()).getEnvironment();
			verify(importerExporter, never()).doImportInto(any(Region.class));
		}
	}
}
