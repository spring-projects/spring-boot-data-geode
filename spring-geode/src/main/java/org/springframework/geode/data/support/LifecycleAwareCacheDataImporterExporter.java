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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.Region;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.Lifecycle;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.gemfire.support.SmartLifecycleSupport;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceResolver;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.data.CacheDataImporterExporter;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A {@link CacheDataImporterExporter} implementation using the {@literal Decorator Software Design Pattern} to wrap
 * an existing {@link CacheDataImporterExporter} in order to {@literal decorate} the cache (i.e. {@link Region}) data
 * import & export operations, making them Spring {@link ApplicationContext}, {@link Environment}, {@link Lifecycle},
 * {@link ResourceLoader} aware and capable.
 *
 * This wrapper {@literal decorates} the Apache Geode cache {@link Region} data import operation enabling it
 * to be configured {@link ImportLifecycle#EAGER eagerly}, after the {@link Region} bean as been initialized,
 * or {@link ImportLifecycle#LAZY lazily}, once all beans have been fully initialized and the Spring
 * {@link ApplicationContext} is refreshed.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.Lifecycle
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.data.gemfire.support.SmartLifecycleSupport
 * @see org.springframework.geode.core.io.ResourceReader
 * @see org.springframework.geode.core.io.ResourceResolver
 * @see org.springframework.geode.core.io.ResourceWriter
 * @see org.springframework.geode.data.CacheDataImporterExporter
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver
 * @see <a href="https://en.wikipedia.org/wiki/Decorator_pattern">Decorator Software Design Pattern</a>
 * @since 1.3.0
 */
@SuppressWarnings("rawtypes")
public class LifecycleAwareCacheDataImporterExporter implements CacheDataImporterExporter,
		ApplicationContextAware, EnvironmentAware, InitializingBean, ResourceLoaderAware, SmartLifecycleSupport {

	protected static final int DEFAULT_IMPORT_PHASE = Integer.MIN_VALUE + 1000000;

	protected static final String CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.lifecycle";

	protected static final String CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.phase";

	private final AtomicReference<ImportLifecycle> resolvedImportLifecycle = new AtomicReference<>(null);
	private final AtomicReference<Integer> resolvedImportPhase = new AtomicReference<>(null);

	private final CacheDataImporterExporter importerExporter;

	private Environment environment;

	private final Set<Region> regionsForImport = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Constructs a new instance of the {@link LifecycleAwareCacheDataImporterExporter} initialized with the given,
	 * target {@link CacheDataImporterExporter} that is wrapped by this implementation to decorate all cache import
	 * & export data operations in order to make them {@link Lifecycle} aware and capable.
	 *
	 * @param importerExporter {@link CacheDataImporterExporter} wrapped by this implementation to {@literal decorate}
	 * the cache data import/export operations to be {@link Lifecycle} aware and capable; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link CacheDataImporterExporter} is {@literal null}.
	 * @see org.springframework.geode.data.CacheDataImporterExporter
	 */
	public LifecycleAwareCacheDataImporterExporter(@NonNull CacheDataImporterExporter importerExporter) {

		Assert.notNull(importerExporter, "The CacheDataImporterExporter to decorate must not be null");

		this.importerExporter = importerExporter;
	}

	/**
	 * Initializes the wrapped {@link CacheDataImporterExporter} if the importer/exporter
	 * implements {@link InitializingBean}.
	 *
	 * @throws Exception if {@link CacheDataImporterExporter} initialization fails.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

		if (importerExporter instanceof InitializingBean) {
			((InitializingBean) importerExporter).afterPropertiesSet();
		}
	}

	/**
	 * Configures a reference to the Spring {@link ApplicationContext}.
	 *
	 * @param applicationContext Spring {@link ApplicationContext} in which this component operates.
	 * @see org.springframework.context.ApplicationContext
	 */
	@Override
	public void setApplicationContext(@Nullable ApplicationContext applicationContext) {

		if (applicationContext != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ApplicationContextAware) {
				((ApplicationContextAware) importerExporter).setApplicationContext(applicationContext);
			}
		}
	}

	/**
	 * Returns a reference to the configured {@link CacheDataImporterExporter} wrapped by this {@link Lifecycle} aware
	 * and capable {@link CacheDataImporterExporter}.
	 *
	 * @return the {@link CacheDataImporterExporter} enhanced and used as the delegate for this {@link Lifecycle} aware
	 * and capable {@link CacheDataImporterExporter}; never {@literal null}.
	 */
	protected @NonNull CacheDataImporterExporter getCacheDataImporterExporter() {
		return this.importerExporter;
	}

	/**
	 * Configures a reference to the {@link Environment} used to access configuration for the behavior of
	 * the cache data import.
	 *
	 * @param environment {@link Environment} used to access context specific configuration for the cache data import.
	 * @see org.springframework.core.env.Environment
	 */
	@Override
	public void setEnvironment(@Nullable Environment environment) {

		this.environment = environment;

		if (environment != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof EnvironmentAware) {
				((EnvironmentAware) importerExporter).setEnvironment(environment);
			}
		}
	}

	/**
	 * Returns an {@link Optional} reference to the configured {@link Environment} used to access configuration
	 * for the behavior of the cache data import.
	 *
	 * If a reference to {@link Environment} was not configured, then this method will return {@link Optional#empty()}.
	 *
	 * @return an {@link Optional} reference to the configured {@link Environment}, or {@link Optional#empty()} if no
	 * {@link Environment} was configured.
	 * @see org.springframework.core.env.Environment
	 * @see #setEnvironment(Environment)
	 * @see java.util.Optional
	 */
	protected Optional<Environment> getEnvironment() {
		return Optional.ofNullable(this.environment);
	}

	/**
	 * Configures the {@link ExportResourceResolver} of the wrapped {@link CacheDataImporterExporter}
	 * if the {@link ExportResourceResolver} is not {@literal null} and the {@link CacheDataImporterExporter}
	 * is {@link Resource} capable.
	 *
	 * @param exportResourceResolver {@link ResourceResolver} used to resolve a {@link Resource} for {@literal export}.
	 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ExportResourceResolver
	 * @see #getCacheDataImporterExporter()
	 */
	@Autowired(required = false)
	public void setExportResourceResolver(@Nullable ExportResourceResolver exportResourceResolver) {

		if (exportResourceResolver != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ResourceCapableCacheDataImporterExporter) {
				((ResourceCapableCacheDataImporterExporter) importerExporter)
					.setExportResourceResolver(exportResourceResolver);
			}
		}
	}

	/**
	 * Configures the {@link ImportResourceResolver} of the wrapped {@link CacheDataImporterExporter}
	 * if the {@link ImportResourceResolver} is not {@literal null} and the {@link CacheDataImporterExporter}
	 * is {@link Resource} capable.
	 *
	 * @param importResourceResolver {@link ResourceResolver} used to resolve a {@link Resource} for {@literal import}.
	 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver
	 * @see #getCacheDataImporterExporter()
	 */
	@Autowired(required = false)
	public void setImportResourceResolver(@Nullable ImportResourceResolver importResourceResolver) {

		if (importResourceResolver != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ResourceCapableCacheDataImporterExporter) {
				((ResourceCapableCacheDataImporterExporter) importerExporter)
					.setImportResourceResolver(importResourceResolver);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int getPhase() {
		return resolveImportPhase();
	}

	/**
	 * Returns the {@link Set} of {@link Region Regions} to import data into.
	 *
	 * @return a {@link Set} of {@link Region Regions} to evaluate on import; never {@literal null}.
	 * @see org.apache.geode.cache.Region
	 * @see java.util.Set
	 */
	@NonNull Set<Region> getRegionsForImport() {
		return this.regionsForImport;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {

		if (resourceLoader != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) importerExporter).setResourceLoader(resourceLoader);
			}
		}
	}

	/**
	 * Configures the {@link ResourceReader} of the wrapped {@link CacheDataImporterExporter}
	 * if the {@link ResourceReader} is not {@literal null} and the {@link CacheDataImporterExporter}
	 * is {@link Resource} capable.
	 *
	 * @param resourceReader {@link ResourceReader} used to read data from a {@link Resource} on {@literal import}.
	 * @see org.springframework.geode.core.io.ResourceReader
	 * @see #getCacheDataImporterExporter()
	 */
	@Autowired(required = false)
	public void setResourceReader(@Nullable ResourceReader resourceReader) {

		if (resourceReader != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ResourceCapableCacheDataImporterExporter) {
				((ResourceCapableCacheDataImporterExporter) importerExporter).setResourceReader(resourceReader);
			}
		}
	}

	/**
	 * Configures the {@link ResourceWriter} of the wrapped {@link CacheDataImporterExporter}
	 * if the {@link ResourceWriter} is not {@literal null} and the {@link CacheDataImporterExporter}
	 * is {@link Resource} capable.
	 *
	 * @param resourceWriter {@link ResourceWriter} used to write data to a {@link Resource} on {@literal export}.
	 * @see org.springframework.geode.core.io.ResourceWriter
	 * @see #getCacheDataImporterExporter()
	 */
	@Autowired(required = false)
	public void setResourceWriter(@Nullable ResourceWriter resourceWriter) {

		if (resourceWriter != null) {

			CacheDataImporterExporter importerExporter = getCacheDataImporterExporter();

			if (importerExporter instanceof ResourceCapableCacheDataImporterExporter) {
				((ResourceCapableCacheDataImporterExporter) importerExporter).setResourceWriter(resourceWriter);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@NonNull @Override
	public Region exportFrom(@NonNull Region region) {
		return getCacheDataImporterExporter().exportFrom(region);
	}

	/**
	 * @inheritDoc
	 */
	@NonNull @Override
	public Region importInto(@NonNull Region region) {

		if (resolveImportLifecycle().isEager()) {
			return getCacheDataImporterExporter().importInto(region);
		}
		else {
			getRegionsForImport().add(region);
			return region;
		}
	}

	/**
	 * Resolves the configured {@link ImportLifecycle}.
	 *
	 * The cache data import lifecycle is configured with the
	 * {@literal spring.boot.data.gemfire.cache.data.import.lifecycle} property
	 * in Spring Boot {@literal application.properties}.
	 *
	 * @return the configured {@link ImportLifecycle}.
	 * @see LifecycleAwareCacheDataImporterExporter.ImportLifecycle
	 */
	protected ImportLifecycle resolveImportLifecycle() {

		return resolvedImportLifecycle.updateAndGet(currentValue -> currentValue != null ? currentValue
			: getEnvironment()
				.map(env -> env.getProperty(CACHE_DATA_IMPORT_LIFECYCLE_PROPERTY_NAME, String.class,
					ImportLifecycle.getDefault().name()))
				.map(ImportLifecycle::from)
				.orElseGet(ImportLifecycle::getDefault));
	}

	/**
	 * Resolves the configured {@link SmartLifecycleSupport#getPhase() SmartLifecycle Phase} in which the cache data
	 * import will be performed.
	 *
	 * @return the configured {@link SmartLifecycleSupport#getPhase() SmartLifecycle Phase}.
	 * @see #getPhase()
	 */
	protected int resolveImportPhase() {

		return resolvedImportPhase.updateAndGet(currentValue -> currentValue != null ? currentValue
			: getEnvironment()
				.map(env -> env.getProperty(CACHE_DATA_IMPORT_PHASE_PROPERTY_NAME, Integer.class, DEFAULT_IMPORT_PHASE))
				.orElse(DEFAULT_IMPORT_PHASE));
	}

	/**
	 * Performs the cache data import for each of the targeted {@link Region Regions}.
	 *
	 * @see #getCacheDataImporterExporter()
	 * @see #getRegionsForImport()
	 */
	@Override
	public void start() {

		// Technically, the resolveImportLifecycle().isLazy() check is not strictly required since if the cache data
		// import is "eager", then the regionsForImport Set will be empty anyway.
		if (resolveImportLifecycle().isLazy()) {
			getRegionsForImport().forEach(getCacheDataImporterExporter()::importInto);
		}
	}

	/**
	 * An {@link Enum Enumeration} defining the different modes for the cache data import lifecycle.
	 */
	public enum ImportLifecycle {

		EAGER("Imports cache data during Region bean post processing, after initialization"),
		LAZY("Imports cache data during the appropriate phase on Lifecycle start");

		private final String description;

		ImportLifecycle(@NonNull String description) {

			Assert.hasText(description, "The enumerated value must have a description");

			this.description = description;
		}

		public static @NonNull ImportLifecycle getDefault() {
			return LAZY;
		}

		public static @Nullable ImportLifecycle from(String name) {

			for (ImportLifecycle importCycle : values()) {
				if (importCycle.name().equalsIgnoreCase(name)) {
					return importCycle;
				}
			}

			return null;
		}

		public boolean isEager() {
			return EAGER.equals(this);
		}

		public boolean isLazy() {
			return LAZY.equals(this);
		}

		@Override
		public String toString() {
			return this.description;
		}
	}
}
