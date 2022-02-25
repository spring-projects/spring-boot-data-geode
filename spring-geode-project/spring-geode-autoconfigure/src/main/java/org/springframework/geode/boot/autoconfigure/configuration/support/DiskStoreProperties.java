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
package org.springframework.geode.boot.autoconfigure.configuration.support;

import java.util.Properties;

import org.apache.geode.cache.DiskStore;
import org.apache.geode.cache.DiskStoreFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode {@link DiskStore DiskStores}.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.DiskStore
 * @see org.apache.geode.cache.DiskStoreFactory
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class DiskStoreProperties {

	private final StoreProperties storeProperties = new StoreProperties();

	public StoreProperties getStore() {
		return this.storeProperties;
	}

	public static class DirectoryProperties {

		private int size = DiskStoreFactory.DEFAULT_DISK_DIR_SIZE;

		private String location;

		public String getLocation() {
			return this.location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public int getSize() {
			return this.size;
		}

		public void setSize(int size) {
			this.size = size;
		}
	}

	public static class StoreProperties {

		private boolean allowForceCompaction = DiskStoreFactory.DEFAULT_ALLOW_FORCE_COMPACTION;
		private boolean autoCompact = DiskStoreFactory.DEFAULT_AUTO_COMPACT;

		private float diskUsageCriticalPercentage = DiskStoreFactory.DEFAULT_DISK_USAGE_CRITICAL_PERCENTAGE;
		private float diskUsageWarningPercentage = DiskStoreFactory.DEFAULT_DISK_USAGE_WARNING_PERCENTAGE;

		private int compactionThreshold = DiskStoreFactory.DEFAULT_COMPACTION_THRESHOLD;
		private int queueSize = DiskStoreFactory.DEFAULT_QUEUE_SIZE;
		private int writeBufferSize = DiskStoreFactory.DEFAULT_WRITE_BUFFER_SIZE;

		private long maxOplogSize = DiskStoreFactory.DEFAULT_MAX_OPLOG_SIZE;
		private long timeInterval = DiskStoreFactory.DEFAULT_TIME_INTERVAL;

		private DirectoryProperties[] directoryProperties = {};

		public boolean isAllowForceCompaction() {
			return this.allowForceCompaction;
		}

		public void setAllowForceCompaction(boolean allowForceCompaction) {
			this.allowForceCompaction = allowForceCompaction;
		}

		public boolean isAutoCompact() {
			return this.autoCompact;
		}

		public void setAutoCompact(boolean autoCompact) {
			this.autoCompact = autoCompact;
		}

		public int getCompactionThreshold() {
			return this.compactionThreshold;
		}

		public void setCompactionThreshold(int compactionThreshold) {
			this.compactionThreshold = compactionThreshold;
		}

		public DirectoryProperties[] getDirectory() {
			return this.directoryProperties;
		}

		public void setDirectory(DirectoryProperties[] directoryProperties) {
			this.directoryProperties = directoryProperties;
		}

		public float getDiskUsageCriticalPercentage() {
			return this.diskUsageCriticalPercentage;
		}

		public void setDiskUsageCriticalPercentage(float diskUsageCriticalPercentage) {
			this.diskUsageCriticalPercentage = diskUsageCriticalPercentage;
		}

		public float getDiskUsageWarningPercentage() {
			return this.diskUsageWarningPercentage;
		}

		public void setDiskUsageWarningPercentage(float diskUsageWarningPercentage) {
			this.diskUsageWarningPercentage = diskUsageWarningPercentage;
		}

		public long getMaxOplogSize() {
			return this.maxOplogSize;
		}

		public void setMaxOplogSize(long maxOplogSize) {
			this.maxOplogSize = maxOplogSize;
		}

		public int getQueueSize() {
			return this.queueSize;
		}

		public void setQueueSize(int queueSize) {
			this.queueSize = queueSize;
		}

		public long getTimeInterval() {
			return this.timeInterval;
		}

		public void setTimeInterval(long timeInterval) {
			this.timeInterval = timeInterval;
		}

		public int getWriteBufferSize() {
			return this.writeBufferSize;
		}

		public void setWriteBufferSize(int writeBufferSize) {
			this.writeBufferSize = writeBufferSize;
		}
	}
}
