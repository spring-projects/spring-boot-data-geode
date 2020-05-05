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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

/**
 * Abstract base class implementing the {@link CacheDataExporter} and {@link CacheDataImporter} interface in order to
 * simply import/export data operation implementations in a consistent way.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.data.CacheDataExporter
 * @see org.springframework.geode.data.CacheDataImporter
 * @since 1.3.0
 */
@SuppressWarnings({ "rawtypes", "unused" })
public abstract class AbstractCacheDataImporterExporter
		implements ApplicationContextAware, CacheDataImporterExporter, EnvironmentAware {

	protected static final boolean DEFAULT_CACHE_DATA_EXPORT_ENABLED = false;

	protected static final String CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.export.enabled";

	protected static final String CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.active-profiles";

	protected static final String DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES = "";

	private static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

	private ApplicationContext applicationContext;

	private Environment environment;

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
	 * Exports data contained in the given {@link Region}.
	 *
	 * @param region {@link Region} to export data from.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	@NonNull @Override
	public Region exportFrom(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		boolean exportEnabled = getEnvironment()
			.filter(environment -> environment.getProperty(CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME, Boolean.class,
				DEFAULT_CACHE_DATA_EXPORT_ENABLED))
			.isPresent();

		return exportEnabled ? doExportFrom(region) : region;
	}

	/**
	 * Exports data contained in the given {@link Region}.
	 *
	 * @param region {@link Region} to export data from.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	protected abstract @NonNull Region doExportFrom(@NonNull Region region);

	/**
	 * Imports data into the given {@link Region}.
	 *
	 * @param region {@link Region} to import data into.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	@NonNull @Override
	public Region importInto(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		boolean importEnabled = getEnvironment()
			.map(Environment::getActiveProfiles)
			.map(CollectionUtils::asSet)
			.map(this::getDefaultProfilesIfEmpty)
			.filter(activeProfiles -> {

				String cacheDataImportActiveProfiles = requireEnvironment()
					.getProperty(CACHE_DATA_IMPORT_ACTIVE_PROFILES_PROPERTY_NAME,
						DEFAULT_CACHE_DATA_IMPORT_ACTIVE_PROFILES);

				return isImportEnabled(activeProfiles, cacheDataImportActiveProfiles);

			})
			.isPresent();

		return importEnabled ? doImportInto(region) : region;
	}

	@Nullable Set<String> getDefaultProfilesIfEmpty(@Nullable Set<String> activeProfiles) {

		Set<String> resolvedProfiles = activeProfiles;

		if (CollectionUtils.nullSafeSet(activeProfiles).isEmpty()) {

			Set<String> defaultProfiles =
				CollectionUtils.asSet(ArrayUtils.nullSafeArray(requireEnvironment().getDefaultProfiles(), String.class));

			if (isNonDefaultProfileSet(defaultProfiles)) {
				resolvedProfiles = defaultProfiles;
			}
		}

		return resolvedProfiles;
	}

	// The Set of Profiles cannot be null, empty or contain only the "default" Profile.
	boolean isNonDefaultProfileSet(@Nullable Set<String> profiles) {

		return Objects.nonNull(profiles)
			&& !profiles.isEmpty()
			&& !Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME).containsAll(profiles);
	}

	// Active Spring Profiles must contain at least 1 of the configured cacheDataImportActiveProfiles unless unset.
	boolean isImportEnabled(Set<String> activeProfiles, String cacheDataImportActiveProfiles) {
		return isNotSet(cacheDataImportActiveProfiles)
			|| containsAny(activeProfiles, commaDelimitedListOfStringsToSet(cacheDataImportActiveProfiles));
	}

	Set<String> commaDelimitedListOfStringsToSet(@NonNull String commaDelimitedListOfStrings) {

		return StringUtils.hasText(commaDelimitedListOfStrings)
			? Arrays.stream(commaDelimitedListOfStrings.split(","))
				.map(String::trim)
				.collect(Collectors.toSet())
			: Collections.emptySet();
	}

	boolean containsAny(Collection<?> source, Collection<?> elements) {
		return CollectionUtils.containsAny(source, elements);
	}

	boolean isNotSet(String value) {
		return !StringUtils.hasText((value));
	}

	/**
	 * Imports data into the given {@link Region}.
	 *
	 * @param region {@link Region} to import data into.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	protected abstract @NonNull Region doImportInto(@NonNull Region region);

}
