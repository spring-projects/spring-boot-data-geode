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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.geode.cache.DiskStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.tests.mock.DiskStoreMockObjects;
import org.springframework.data.gemfire.util.ArrayUtils;

/**
 * Unit tests for {@link GeodeDiskStoresHealthIndicator}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.DiskStore
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.data.gemfire.tests.mock.DiskStoreMockObjects
 * @see org.springframework.geode.boot.actuate.GeodeDiskStoresHealthIndicator
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeDiskStoresHealthIndicatorUnitTests {

	@Mock
	private ApplicationContext mockApplicationContext;

	private GeodeDiskStoresHealthIndicator diskStoresHealthIndicator;

	@Before
	public void setup() {
		this.diskStoresHealthIndicator = new GeodeDiskStoresHealthIndicator(this.mockApplicationContext);
	}

	@Test
	public void healthCheckCapturesDetails() throws Exception {

		File mockDirectoryOne = mock(File.class);
		File mockDirectoryTwo = mock(File.class);

		when(mockDirectoryOne.getAbsolutePath()).thenReturn("/ext/gemfire/disk/stores/one");
		when(mockDirectoryTwo.getAbsolutePath()).thenReturn("/ext/gemfire/disk/stores/two");

		int[] diskDirectorySizes = { 1024, 8192 };

		Map<String, DiskStore> mockDiskStores = new HashMap<>();

		mockDiskStores.put("MockDiskStoreOne", DiskStoreMockObjects.mockDiskStore("MockDiskStoreOne",
			true, true, 90,
			ArrayUtils.asArray(mockDirectoryOne, mockDirectoryTwo), diskDirectorySizes, 0.95f,
			0.90f, 1024000L, 16384, 5000L, 32768));

		mockDiskStores.put("MockDiskStoreTwo", DiskStoreMockObjects.mockDiskStore("MockDiskStoreTwo",
			false, true, 50,null, null,
			0.90f,0.80f, 2048000L, 4096,
			15000L, 8192));

		when(this.mockApplicationContext.getBeansOfType(DiskStore.class)).thenReturn(mockDiskStores);

		Health.Builder builder = new Health.Builder();

		this.diskStoresHealthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getStatus()).isEqualTo(Status.UP);

		Map<String, Object> healthDetails = health.getDetails();

		assertThat(healthDetails).isNotNull();
		assertThat(healthDetails).isNotEmpty();
		assertThat(healthDetails).containsEntry("geode.disk-store.count", mockDiskStores.size());
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.allow-force-compaction", "Yes");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.auto-compact", "Yes");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.compaction-threshold", 90);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.disk-directories", "[/ext/gemfire/disk/stores/one, /ext/gemfire/disk/stores/two]");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.disk-directory-sizes", "[1024, 8192]");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.disk-usage-critical-percentage", 0.95f);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.disk-usage-warning-percentage", 0.90f);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.max-oplog-size", 1024000L);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.queue-size", 16384);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.time-interval", 5000L);
		assertThat(healthDetails).containsKey("geode.disk-store.MockDiskStoreOne.uuid");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreOne.write-buffer-size", 32768);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.allow-force-compaction", "No");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.auto-compact", "Yes");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.compaction-threshold", 50);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.disk-directories", "[]");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.disk-directory-sizes", "[]");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.disk-usage-critical-percentage", 0.90f);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.disk-usage-warning-percentage", 0.80f);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.max-oplog-size", 2048000L);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.queue-size", 4096);
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.time-interval", 15000L);
		assertThat(healthDetails).containsKey("geode.disk-store.MockDiskStoreOne.uuid");
		assertThat(healthDetails).containsEntry("geode.disk-store.MockDiskStoreTwo.write-buffer-size", 8192);

		verify(this.mockApplicationContext, times(1)).getBeansOfType(eq(DiskStore.class));
	}

	@Test
	public void healthCheckFailsWhenApplicationContextIsNotPresent() throws Exception {

		GeodeDiskStoresHealthIndicator healthIndicator = new GeodeDiskStoresHealthIndicator();

		Health.Builder builder = new Health.Builder();

		healthIndicator.doHealthCheck(builder);

		Health health = builder.build();

		assertThat(health).isNotNull();
		assertThat(health.getDetails()).isEmpty();
		assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
	}
}
