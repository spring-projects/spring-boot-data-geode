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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;
import org.mockito.InOrder;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

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

		doCallRealMethod().when(importerExporter).getLogger();
		doCallRealMethod().when(importerExporter).isExportEnabled(any());
		doCallRealMethod().when(importerExporter).isImportEnabled(any());
		doCallRealMethod().when(importerExporter).exportFrom(any());
		doCallRealMethod().when(importerExporter).importInto(any());

		return importerExporter;
	}

	private Environment configureExport(Environment mockEnvironment, boolean enabled) {

		doReturn(enabled).when(mockEnvironment)
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));

		return mockEnvironment;
	}

	private Environment disableExport(Environment mockEnvironment) {
		return configureExport(mockEnvironment, false);
	}

	private Environment enableExport(Environment mockEnvironment) {
		return configureExport(mockEnvironment, true);
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
		assertThat(importerExporter.requireApplicationContext()).isEqualTo(mockApplicationContext);

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

		verify(importerExporter, times(2)).getApplicationContext();
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
		finally {
			verify(importerExporter, times(1)).getApplicationContext();
		}
	}

	@Test
	public void setAndGetEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		AbstractCacheDataImporterExporter importerExporter = mock(AbstractCacheDataImporterExporter.class);

		doCallRealMethod().when(importerExporter).getEnvironment();
		doCallRealMethod().when(importerExporter).requireEnvironment();
		doCallRealMethod().when(importerExporter).setEnvironment(any());

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
		doCallRealMethod().when(importerExporter).requireEnvironment();
		doCallRealMethod().when(importerExporter).setEnvironment(any());

		importerExporter.setEnvironment(mockEnvironment);

		assertThat(importerExporter.getEnvironment().orElse(null)).isEqualTo(mockEnvironment);
		assertThat(importerExporter.requireEnvironment()).isEqualTo(mockEnvironment);

		verify(importerExporter, times(2)).getEnvironment();
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
		finally {
			verify(importerExporter, times(1)).getEnvironment();
		}
	}

	@Test
	public void isExportEnabledReturnsTrueWhenExportIsEnabled() {

		Environment mockEnvironment = enableExport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		assertThat(importerExporter.isExportEnabled(mockEnvironment)).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isExportEnabledReturnsFalseWhenExportIsDisabled() {

		Environment mockEnvironment = disableExport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		assertThat(importerExporter.isExportEnabled(mockEnvironment)).isFalse();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isExportEnabledWhenEnvironmentIsNullIsNullSafeAndReturnsFalse() {
		assertThat(mockAbstractCacheDataImporterExporter().isExportEnabled(null)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenEnvironmentIsPresentExportIsEnabledAndRegionPredicateSaysYesCallsDoExportFrom() {

		Environment mockEnvironment = enableExport(mock(Environment.class));

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		doReturn(true).when(mockPredicate).test(any(Region.class));

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(mockRegion).when(importerExporter).doExportFrom(eq(mockRegion));
		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isExportEnabled(eq(mockEnvironment));
		verify(importerExporter, times(1)).getRegionPredicate();
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
	public void exportFromWhenExportIsDisabledWillNotCallDoExportFrom() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = disableExport(mock(Environment.class));

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isExportEnabled(eq(mockEnvironment));
		verify(importerExporter, never()).getRegionPredicate();
		verify(importerExporter, never()).doExportFrom(any(Region.class));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void exportFromWhenRegionPredicateSaysNoWillNotCallDoExportFrom() {

		Environment mockEnvironment = enableExport(mock(Environment.class));

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importerExporter = mockAbstractCacheDataImporterExporter();

		doReturn(Optional.of(mockEnvironment)).when(importerExporter).getEnvironment();
		doReturn(mockPredicate).when(importerExporter).getRegionPredicate();
		doReturn(false).when(mockPredicate).test(any());

		assertThat(importerExporter.exportFrom(mockRegion)).isEqualTo(mockRegion);

		verify(importerExporter, times(1)).getEnvironment();
		verify(importerExporter, times(1)).isExportEnabled(eq(mockEnvironment));
		verify(importerExporter, times(1)).getRegionPredicate();
		verify(importerExporter, never()).doExportFrom(any(Region.class));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_EXPORT_ENABLED));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
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

		doCallRealMethod().when(importerExporter).commaDelimitedStringToSet(anyString());
		doCallRealMethod().when(importerExporter).getActiveProfiles(any(Environment.class));
		doCallRealMethod().when(importerExporter).getRegionPredicate();
		doCallRealMethod().when(importerExporter).importInto(any());
		doCallRealMethod().when(importerExporter).isImportEnabled(any());
		doCallRealMethod().when(importerExporter).isImportProfilesActive(any());
		doCallRealMethod().when(importerExporter).isNotDefaultProfileOnlySet(any(Set.class));
		doCallRealMethod().when(importerExporter).useDefaultProfilesIfEmpty(any(Environment.class), any(Set.class));

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

	@SuppressWarnings("unchecked")
	private <T> T[] toArray(T... array) {
		return array;
	}

	private Environment withEnvironmentActiveProfiles(Environment mockEnvironment, String... activeProfiles) {

		doReturn(activeProfiles).when(mockEnvironment).getActiveProfiles();

		return mockEnvironment;
	}

	private Environment withEnvironmentDefaultProfiles(Environment mockEnvironment, String... defaultProfiles) {

		doReturn(defaultProfiles).when(mockEnvironment).getDefaultProfiles();

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

		Environment mockEnvironment = enableImport(mock(Environment.class));

		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(mockEnvironment)).isTrue();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isImportEnabledReturnsFalseWhenImportIsDisabled() {

		Environment mockEnvironment = disableImport(mock(Environment.class));

		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(mockEnvironment)).isFalse();

		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void isImportEnabledWhenEnvironmentIsNullIsNullSafeAndReturnsFalse() {
		assertThat(mockAbstractCacheDataImporterExporter().isImportEnabled(null)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentIsPresentImportIsEnabledRegionPredicateSaysYesAndActiveProfilesMatchCallsDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentDefaultProfiles(
			enableImport(mock(Environment.class)), "DEV"), "DEV, TEST");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(toArray("TEST")).doReturn(toArray("  ")).when(mockEnvironment).getActiveProfiles();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));
		doReturn(mockRegion).when(importer).doImportInto(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(2)).getEnvironment();
		verify(importer, times(2)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(2)).getRegionPredicate();
		verify(importer, times(2)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, times(2)).doImportInto(eq(mockRegion));

		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));

		verify(mockPredicate, times(2)).test(eq(mockRegion));

		verifyNoMoreInteractions(mockEnvironment, mockPredicate);

		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentIsNull() {

		Region<?, ?> mockRegion = mock(Region.class);

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(1)).getEnvironment();
		verify(importer, never()).isImportEnabled(any());
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).isImportProfilesActive(any());
		verify(importer, never()).doImportInto(eq(mockRegion));
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenImportIsDisabled() {

		Region<?, ?> mockRegion = mock(Region.class);

		Environment mockEnvironment = disableImport(mock(Environment.class));

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(1)).getEnvironment();
		verify(importer, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).isImportProfilesActive(any());
		verify(importer, never()).doImportInto(any());
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenRegionPredicateSaysNo() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment = mock(Environment.class);

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(true).when(importer).isImportEnabled(eq(mockEnvironment));
		doReturn(true).when(importer).isImportProfilesActive(eq(mockEnvironment));
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(false).when(mockPredicate).test(any());

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(1)).getEnvironment();
		verify(importer, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(1)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, times(1)).getRegionPredicate();
		verify(importer, never()).doImportInto(any(Region.class));
		verify(mockPredicate, times(1)).test(eq(mockRegion));
		verifyNoMoreInteractions(mockPredicate);
		verifyNoInteractions(mockEnvironment, mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenImportActiveProfilesAreSetAndEnvironmentActiveAndDefaultProfilesAreNotSet() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment =
			withImportActiveProfiles(enableImport(mock(Environment.class)), "DEV, TEST");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(1)).getEnvironment();
		verify(importer, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(1)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockPredicate, mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenImportActiveProfilesAreNotSetCallsDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentActiveProfiles(
			enableImport(mock(Environment.class)), "PROD"), "  ");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));
		doReturn(mockRegion).when(importer).doImportInto(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		InOrder order = inOrder(importer);

		order.verify(importer, times(1)).getEnvironment();
		order.verify(importer, times(1)).isImportEnabled(eq(mockEnvironment));
		order.verify(importer, times(1)).isImportProfilesActive(eq(mockEnvironment));
		order.verify(importer, times(1)).getRegionPredicate();
		order. verify(importer, times(1)).doImportInto(eq(mockRegion));

		verify(mockEnvironment, never()).getActiveProfiles();
		verify(mockEnvironment, never()).getDefaultProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));

		verify(mockPredicate, times(1)).test(eq(mockRegion));

		verifyNoMoreInteractions(mockEnvironment, mockPredicate);

		verifyNoInteractions(mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentActiveProfilesDoesNotContainImportActiveProfilesWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment =
			withImportActiveProfiles(enableImport(mock(Environment.class)), "DEV,TEST");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(new String[0]).doReturn(toArray("PROD")).when(mockEnvironment).getActiveProfiles();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(2)).getEnvironment();
		verify(importer, times(2)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(2)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).doImportInto(eq(mockRegion));
		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockPredicate, mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentDefaultProfilesDoesNotContainImportActiveProfilesWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment =
			withImportActiveProfiles(enableImport(mock(Environment.class)), "DEV,TEST");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(toArray("PROD")).doReturn(toArray("default")).when(mockEnvironment).getDefaultProfiles();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);
		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(2)).getEnvironment();
		verify(importer, times(2)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(2)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).doImportInto(any(Region.class));
		verify(mockEnvironment, times(2)).getActiveProfiles();
		verify(mockEnvironment, times(2)).getDefaultProfiles();
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(2))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockPredicate, mockRegion);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void importIntoWhenEnvironmentDefaultAndActiveProfilesConflictWillNotCallDoImportInto() {

		Region<?, ?> mockRegion = mock(Region.class);

		Predicate<Region<?, ?>> mockPredicate = mock(Predicate.class);

		Environment mockEnvironment = withImportActiveProfiles(withEnvironmentDefaultProfiles(withEnvironmentActiveProfiles(
			enableImport(mock(Environment.class)), "PROD"), "DEV", "TEST"),
			"DEV,TEST");

		AbstractCacheDataImporterExporter importer = callRealMethodsFor(mockAbstractCacheDataImporterExporter());

		doReturn(Optional.of(mockEnvironment)).when(importer).getEnvironment();
		doReturn(mockPredicate).when(importer).getRegionPredicate();
		doReturn(true).when(mockPredicate).test(eq(mockRegion));

		assertThat(importer.importInto(mockRegion)).isEqualTo(mockRegion);

		verify(importer, times(1)).getEnvironment();
		verify(importer, times(1)).isImportEnabled(eq(mockEnvironment));
		verify(importer, times(1)).isImportProfilesActive(eq(mockEnvironment));
		verify(importer, never()).getRegionPredicate();
		verify(importer, never()).doImportInto(any(Region.class));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockEnvironment, never()).getDefaultProfiles();
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME),
				eq(Boolean.class), eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ENABLED));
		verify(mockEnvironment, times(1))
			.getProperty(eq(AbstractCacheDataImporterExporter.CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME),
				eq(AbstractCacheDataImporterExporter.DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockPredicate, mockRegion);
	}

	@Test(expected = IllegalArgumentException.class)
	public void importIntoNullRegionThrowsIllegalArgumentException() {

		AbstractCacheDataImporterExporter importer = mockAbstractCacheDataImporterExporter();

		try {
			importer.importInto(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(importer, never()).getEnvironment();
			verify(importer, never()).doImportInto(any(Region.class));
		}
	}
}
