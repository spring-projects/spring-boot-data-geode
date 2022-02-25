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
package org.springframework.geode.boot.actuate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.geode.cache.DiskStore;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator;
import org.springframework.util.Assert;

/**
 * The {@link GeodeDiskStoresHealthIndicator} class is a Spring Boot {@link HealthIndicator} providing details about
 * the health of Apache Geode {@link DiskStore DiskStores}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.DiskStore
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.geode.boot.actuate.health.AbstractGeodeHealthIndicator
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeDiskStoresHealthIndicator extends AbstractGeodeHealthIndicator {

	private final ApplicationContext applicationContext;

	/**
	 * Default constructor to construct an uninitialized instance of {@link GeodeDiskStoresHealthIndicator},
	 * which will not provide any health information.
	 */
	public GeodeDiskStoresHealthIndicator() {
		super("Disk Stores health check failed");
		this.applicationContext = null;
	}

	/**
	 * Constructs an instance of the {@link GeodeDiskStoresHealthIndicator} initialized with a reference to
	 * the {@link ApplicationContext} instance.
	 *
	 * @param applicationContext reference to the Spring {@link ApplicationContext}.
	 * @throws IllegalArgumentException if {@link ApplicationContext} is {@literal null}.
	 * @see org.springframework.context.ApplicationContext
	 */
	public GeodeDiskStoresHealthIndicator(ApplicationContext applicationContext) {

		super("Disk Stores health check enabled");

		Assert.notNull(applicationContext, "ApplicationContext is required");

		this.applicationContext = applicationContext;
	}

	/**
	 * Returns an {@link Optional} reference to the Spring {@link ApplicationContext}.
	 *
	 * @return an {@link Optional} reference to the Spring {@link ApplicationContext}.
	 * @see org.springframework.context.ApplicationContext
	 * @see java.util.Optional
	 */
	protected Optional<ApplicationContext> getApplicationContext() {
		return Optional.ofNullable(this.applicationContext);
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {

		if (getApplicationContext().isPresent()) {

			Map<String, DiskStore> diskStores = getApplicationContext()
				.map(it -> it.getBeansOfType(DiskStore.class))
				.orElseGet(Collections::emptyMap);

			builder.withDetail("geode.disk-store.count", diskStores.size());

			diskStores.values().forEach(diskStore -> {

				String diskStoreName = diskStore.getName();

				builder.withDetail(diskStoreKey(diskStoreName, "allow-force-compaction"), toYesNoString(diskStore.getAllowForceCompaction()))
					.withDetail(diskStoreKey(diskStoreName, "auto-compact"), toYesNoString(diskStore.getAutoCompact()))
					.withDetail(diskStoreKey(diskStoreName, "compaction-threshold"), diskStore.getCompactionThreshold())
					.withDetail(diskStoreKey(diskStoreName, "disk-directories"), toFileAbsolutePathStrings(diskStore.getDiskDirs()))
					.withDetail(diskStoreKey(diskStoreName, "disk-directory-sizes"), Arrays.toString(nullSafeArray(diskStore.getDiskDirSizes())))
					.withDetail(diskStoreKey(diskStoreName, "disk-usage-critical-percentage"), diskStore.getDiskUsageCriticalPercentage())
					.withDetail(diskStoreKey(diskStoreName, "disk-usage-warning-percentage"), diskStore.getDiskUsageWarningPercentage())
					.withDetail(diskStoreKey(diskStoreName, "max-oplog-size"), diskStore.getMaxOplogSize())
					.withDetail(diskStoreKey(diskStoreName, "queue-size"), diskStore.getQueueSize())
					.withDetail(diskStoreKey(diskStoreName, "time-interval"), diskStore.getTimeInterval())
					.withDetail(diskStoreKey(diskStoreName, "uuid"), diskStore.getDiskStoreUUID().toString())
					.withDetail(diskStoreKey(diskStoreName, "write-buffer-size"), diskStore.getWriteBufferSize());
			});

			builder.up();

			return;
		}

		builder.unknown();
	}

	private String diskStoreKey(String diskStoreName, String suffix) {
		return String.format("geode.disk-store.%1$s.%2$s", diskStoreName, suffix);
	}

	private int[] nullSafeArray(int[] array) {
		return array != null ? array : new int[0];
	}

	private String toFileAbsolutePathStrings(File... files) {

		return Arrays.toString(Arrays.stream(ArrayUtils.nullSafeArray(files, File.class))
			.filter(Objects::nonNull)
			.map(File::getAbsolutePath)
			.distinct()
			.toArray());
	}
}
