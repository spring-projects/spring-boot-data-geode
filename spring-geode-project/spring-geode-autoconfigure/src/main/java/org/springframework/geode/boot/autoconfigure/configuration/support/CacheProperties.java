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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.control.ResourceManager;
import org.apache.geode.cache.server.CacheServer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode peer {@link Cache}, {@link ClientCache}
 * and {@link CacheServer} objects.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.control.ResourceManager
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.boot.context.properties.NestedConfigurationProperty
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CacheProperties {

	private static final boolean DEFAULT_COPY_ON_READ = false;
	private static final boolean DEFAULT_AUTO_REGION_LOOKUP = true;

	private static final float DEFAULT_CRITICAL_OFF_HEAP_PERCENTAGE = 0.0f;
	private static final float DEFAULT_EVICTION_OFF_HEAP_PERCENTAGE = 0.0f;

	private static final String DEFAULT_LOG_LEVEL = "config";

	private boolean copyOnRead = DEFAULT_COPY_ON_READ;
	private boolean enableAutoRegionLookup = DEFAULT_AUTO_REGION_LOOKUP;

	private float criticalHeapPercentage = ResourceManager.DEFAULT_CRITICAL_PERCENTAGE;
	private float criticalOffHeapPercentage = DEFAULT_CRITICAL_OFF_HEAP_PERCENTAGE;
	private float evictionHeapPercentage = ResourceManager.DEFAULT_EVICTION_PERCENTAGE;
	private float evictionOffHeapPercentage = DEFAULT_EVICTION_OFF_HEAP_PERCENTAGE;

	@NestedConfigurationProperty
	private final CacheServerProperties server = new CacheServerProperties();

	@NestedConfigurationProperty
	private final ClientCacheProperties client = new ClientCacheProperties();

	@NestedConfigurationProperty
	private final CompressionProperties compression = new CompressionProperties();

	@NestedConfigurationProperty
	private final OffHeapProperties offHeap = new OffHeapProperties();

	@NestedConfigurationProperty
	private final PeerCacheProperties peer = new PeerCacheProperties();

	private String logLevel = DEFAULT_LOG_LEVEL;
	private String name;

	public ClientCacheProperties getClient() {
		return this.client;
	}

	public CompressionProperties getCompression() {
		return this.compression;
	}

	public boolean isCopyOnRead() {
		return copyOnRead;
	}

	public void setCopyOnRead(boolean copyOnRead) {
		this.copyOnRead = copyOnRead;
	}

	public float getCriticalHeapPercentage() {
		return this.criticalHeapPercentage;
	}

	public void setCriticalHeapPercentage(float criticalHeapPercentage) {
		this.criticalHeapPercentage = criticalHeapPercentage;
	}

	public float getCriticalOffHeapPercentage() {
		return this.criticalOffHeapPercentage;
	}

	public void setCriticalOffHeapPercentage(float criticalOffHeapPercentage) {
		this.criticalOffHeapPercentage = criticalOffHeapPercentage;
	}

	public boolean isEnableAutoRegionLookup() {
		return this.enableAutoRegionLookup;
	}

	public void setEnableAutoRegionLookup(boolean enableAutoRegionLookup) {
		this.enableAutoRegionLookup = enableAutoRegionLookup;
	}

	public float getEvictionHeapPercentage() {
		return this.evictionHeapPercentage;
	}

	public void setEvictionHeapPercentage(float evictionHeapPercentage) {
		this.evictionHeapPercentage = evictionHeapPercentage;
	}

	public float getEvictionOffHeapPercentage() {
		return this.evictionOffHeapPercentage;
	}

	public void setEvictionOffHeapPercentage(float evictionOffHeapPercentage) {
		this.evictionOffHeapPercentage = evictionOffHeapPercentage;
	}

	public String getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OffHeapProperties getOffHeap() {
		return this.offHeap;
	}

	public PeerCacheProperties getPeer() {
		return this.peer;
	}

	public CacheServerProperties getServer() {
		return this.server;
	}

	public static class CompressionProperties {

		private String compressorBeanName;

		private String[] regionNames = {};

		public String getCompressorBeanName() {
			return this.compressorBeanName;
		}

		public void setCompressorBeanName(String compressorBeanName) {
			this.compressorBeanName = compressorBeanName;
		}

		public String[] getRegionNames() {
			return this.regionNames;
		}

		public void setRegionNames(String[] regionNames) {
			this.regionNames = regionNames;
		}
	}

	public static class OffHeapProperties {

		private String memorySize;

		private String[] regionNames = {};

		public String getMemorySize() {
			return this.memorySize;
		}

		public void setMemorySize(String memorySize) {
			this.memorySize = memorySize;
		}

		public String[] getRegionNames() {
			return this.regionNames;
		}

		public void setRegionNames(String[] regionNames) {
			this.regionNames = regionNames;
		}
	}
}
