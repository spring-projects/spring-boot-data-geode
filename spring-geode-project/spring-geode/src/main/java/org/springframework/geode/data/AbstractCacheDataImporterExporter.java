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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class implementing the {@link CacheDataImporter} and {@link CacheDataExporter} interfaces in order to
 * simplify import/export data access operation implementations in a consistent way.
 *
 * @author John Blum
 * @see java.util.function.Predicate
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.data.CacheDataImporterExporter
 * @since 1.3.0
 */
@SuppressWarnings({ "rawtypes", "unused" })
public abstract class AbstractCacheDataImporterExporter
		implements ApplicationContextAware, CacheDataImporterExporter, EnvironmentAware {

	protected static final boolean DEFAULT_CACHE_DATA_EXPORT_ENABLED = false;
	protected static final boolean DEFAULT_CACHE_DATA_IMPORT_ENABLED = true;

	protected static final String CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.export.enabled";

	protected static final String CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.active-profiles";

	protected static final String CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.enabled";

	protected static final String DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES = "";

	private static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

	private ApplicationContext applicationContext;

	private Environment environment;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Predicate<Region<?, ?>> regionPredicate = newRegionPredicate();

	/**
	 * Constructs a new instance of {@link Predicate} used to filter {@link Region Regions} on data import/export.
	 *
	 * The default {@link Predicate} accepts all {@link Region Regions}. Override the {@link #getRegionPredicate()}
	 * method to change the default behavior.
	 *
	 * @return a new instance of {@link Predicate} used to filter {@link Region Regions} on data import/export.
	 * @see org.apache.geode.cache.Region
	 * @see java.util.function.Predicate
	 * @see #getRegionPredicate()
	 */
	private Predicate<Region<?, ?>> newRegionPredicate() {
		return region -> true;
	}

	/**
	 * Sets a reference to a {@link ApplicationContext} used by this data importer/exporter to perform its function.
	 *
	 * @param applicationContext {@link ApplicationContext} used by this data importer/exporter.
	 * @see org.springframework.context.ApplicationContext
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;

	}

	/**
	 * Return an {@link Optional} reference to the configured {@link ApplicationContext} used by
	 * this data importer/exporter to perform its function.
	 *
	 * @return an {@link Optional} reference to the configured {@link ApplicationContext} used by
	 * this data importer/exporter.
	 * @see org.springframework.context.ApplicationContext
	 * @see java.util.Optional
	 */
	protected Optional<ApplicationContext> getApplicationContext() {
		return Optional.ofNullable(this.applicationContext);
	}

	/**
	 * Returns a required reference to the configured {@link ApplicationContext} used by this data importer/exporter.
	 *
	 * @return a required reference to the configured {@link ApplicationContext} used by this data importer/exporter.
	 * @throws IllegalStateException if an {@link ApplicationContext} was not configured
	 * ({@link #setApplicationContext(ApplicationContext)} set).
	 * @see org.springframework.context.ApplicationContext
	 * @see #getApplicationContext()
	 */
	protected ApplicationContext requireApplicationContext() {

		return getApplicationContext()
			.orElseThrow(() -> newIllegalStateException("ApplicationContext was not configured"));
	}

	/**
	 * Sets a reference to the configured {@link Environment} used by this data importer/exporter
	 * to perform its function.
	 *
	 * @param environment reference to the configured {@link Environment}.
	 * @see org.springframework.core.env.Environment
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Returns an {@link Optional} reference to the configured {@link Environment} used by this data importer/exporter
	 * to access {@link Environment} specific configuration.
	 *
	 * @return an {@link Optional} reference to the configured {@link Environment} used by this data importer/exporter
	 * to access {@link Environment} specific configuration.
	 * @see org.springframework.core.env.Environment
	 * @see java.util.Optional
	 */
	protected Optional<Environment> getEnvironment() {
		return Optional.ofNullable(this.environment);
	}

	/**
	 * Returns a required reference to the configured {@link Environment} used by this data importer/exporter
	 * to access {@link Environment} specific configuration.
	 *
	 * @return a required reference to the configured {@link Environment}.
	 * @throws IllegalStateException if the {@link Environment} was not configured
	 * ({@link #setEnvironment(Environment) set}).
	 * @see org.springframework.core.env.Environment
	 * @see #getEnvironment()
	 */
	protected Environment requireEnvironment() {

		return getEnvironment()
			.orElseThrow(() -> newIllegalStateException("Environment was not configured"));
	}

	/**
	 * Return the configured {@link Logger} to log messages.
	 *
	 * @return the configured {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected Logger getLogger() {
		return this.logger;
	}

	/**
	 * Returns the configured {@link Predicate} used to filter {@link Region Regions} on data import/export.
	 *
	 * @return the configured {@link Predicate} used to filter {@link Region Regions} on data import/export.
	 * @see org.apache.geode.cache.Region
	 * @see java.util.function.Predicate
	 */
	protected @NonNull Predicate<Region<?, ?>> getRegionPredicate() {

		return Optional.ofNullable(this.regionPredicate)
			.orElseGet(this::newRegionPredicate);
	}

	/**
	 * Null-safe method to determine whether export has been explicitly configured and enabled or disabled.
	 *
	 * @param environment {@link Environment} used to assess the configuration of export.
	 * @return a boolean value indicating whether the export is enabled ({@literal true})
	 * or disabled ({@literal false}); {@literal false} by default.
	 * @see org.springframework.core.env.Environment
	 */
	protected boolean isExportEnabled(@Nullable Environment environment) {

		return environment != null
			&& Boolean.TRUE.equals(environment.getProperty(CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME, Boolean.class,
				DEFAULT_CACHE_DATA_EXPORT_ENABLED));
	}

	/**
	 * Exports data contained in the given {@link Region}.
	 *
	 * @param region {@link Region} to export data from.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #isExportEnabled(Environment)
	 * @see #getRegionPredicate()
	 * @see #doExportFrom(Region)
	 */
	@NonNull @Override
	public Region exportFrom(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		boolean exportEnabled = getEnvironment()
			.filter(this::isExportEnabled)
			.filter(environment -> getRegionPredicate().test(region))
			.isPresent();

		return exportEnabled ? doExportFrom(region) : region;
	}

	/**
	 * Exports data contained in the given {@link Region}.
	 *
	 * @param region {@link Region} to export data from.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #exportFrom(Region)
	 */
	protected abstract @NonNull Region doExportFrom(@NonNull Region region);

	/**
	 * Null-safe method to determine whether import has been explicitly configured and enabled or disabled.
	 *
	 * @param environment {@link Environment} used to assess the configuration of the import.
	 * @return a boolean value indicating whether the import is enabled ({@literal true})
	 * or disabled ({@literal false}).
	 * @see org.springframework.core.env.Environment
	 */
	protected boolean isImportEnabled(@Nullable Environment environment) {

		return environment != null
			&& Boolean.TRUE.equals(environment.getProperty(CACHE_DATA_IMPORT_ENABLED_PROPERTY_NAME, Boolean.class,
				DEFAULT_CACHE_DATA_IMPORT_ENABLED));
	}

	/**
	 * Determines whether the Cache Data Import data access operation is enabled based on the configured, active/default
	 * {@literal Profiles} as declared in the Spring {@link Environment}.
	 *
	 * @param environment {@link Environment} used to evaluate the configured, active {@literal Profiles};
	 * must not be {@literal null}.
	 * @return a boolean value indicating whether the the Cache Data Import data access operation is enabled based on
	 * the configured, active/default {@literal Profiles}.
	 * @throws IllegalArgumentException if {@link Environment} is {@literal null}.
	 * @see org.springframework.core.env.Environment
	 * @see #useDefaultProfilesIfEmpty(Environment, Set)
	 * @see #getActiveProfiles(Environment)
	 */
	protected boolean isImportProfilesActive(@NonNull Environment environment) {

		Assert.notNull(environment, "Environment must not be null");

		boolean importProfilesActive = true;

		String cacheDataImportActiveProfiles =
			environment.getProperty(CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME,
				DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES);

		Set<String> cacheDataImportProfiles = commaDelimitedStringToSet(cacheDataImportActiveProfiles);

		if (!cacheDataImportProfiles.isEmpty()) {

			Set<String> configuredProfiles = useDefaultProfilesIfEmpty(environment, getActiveProfiles(environment));

			// The configured, "Active Profiles" must contain at least 1 of the configured cacheDataImportProfiles.
			importProfilesActive = CollectionUtils.containsAny(configuredProfiles, cacheDataImportProfiles);
		}

		return importProfilesActive;
	}

	/**
	 * Imports data into the given {@link Region}.
	 *
	 * @param region {@link Region} to import data into.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #isImportEnabled(Environment)
	 * @see #isImportProfilesActive(Environment)
	 * @see #getRegionPredicate()
	 * @see #doImportInto(Region)
	 */
	@NonNull @Override
	public Region importInto(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		boolean importEnabled = getEnvironment()
			.filter(this::isImportEnabled)
			.filter(this::isImportProfilesActive)
			.filter(environment -> getRegionPredicate().test(region))
			.isPresent();

		return importEnabled ? doImportInto(region) : region;
	}

	/**
	 * Imports data into the given {@link Region}.
	 *
	 * @param region {@link Region} to import data into.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #importInto(Region)
	 */
	protected abstract @NonNull Region doImportInto(@NonNull Region region);

	@NonNull Set<String> commaDelimitedStringToSet(@Nullable String commaDelimitedString) {

		return StringUtils.hasText(commaDelimitedString)
			? Arrays.stream(commaDelimitedString.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet())
			: Collections.emptySet();
	}

	@NonNull Set<String> getActiveProfiles(@NonNull Environment environment) {

		return environment != null
			? toSet(environment.getActiveProfiles(), String.class)
			: Collections.emptySet();
	}

	@NonNull Set<String> useDefaultProfilesIfEmpty(@NonNull Environment environment,
			@Nullable Set<String> activeProfiles) {

		Set<String> resolvedProfiles = CollectionUtils.nullSafeSet(activeProfiles).stream()
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());

		if (resolvedProfiles.isEmpty()) {

			Set<String> defaultProfiles = environment != null
				? toSet(environment.getDefaultProfiles(), String.class).stream()
					.filter(StringUtils::hasText)
					.collect(Collectors.toSet())
				: Collections.emptySet();

			if (isNotDefaultProfileOnlySet(defaultProfiles)) {
				resolvedProfiles = defaultProfiles;
			}
		}

		return resolvedProfiles;
	}

	// The Set of configured Profiles cannot be null, empty or contain only the "default" Profile.
	boolean isNotDefaultProfileOnlySet(@Nullable Set<String> profiles) {

		return Objects.nonNull(profiles)
			&& !profiles.isEmpty()
			&& !Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME).containsAll(profiles);
	}

	private static @NonNull <T> Set<T> toSet(@Nullable T[] array, @NonNull Class<T> type) {
		return CollectionUtils.asSet(ArrayUtils.nullSafeArray(array, type));
	}
}
