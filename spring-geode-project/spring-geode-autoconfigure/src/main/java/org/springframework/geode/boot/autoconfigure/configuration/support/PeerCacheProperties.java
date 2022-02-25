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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure an Apache Geode peer {@link Cache}.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PeerCacheProperties {

	private static final boolean DEFAULT_ENABLE_AUTO_RECONNECT = false;
	private static final boolean DEFAULT_USE_CLUSTER_CONFIGURATION = false;

	private static final int DEFAULT_LOCK_LEASE_IN_SECONDS = 120;
	private static final int DEFAULT_LOCK_TIMEOUT_IN_SECONDS = 60;
	private static final int DEFAULT_MESSAGE_SYNC_INTERVAL_IN_SECONDS = 1;
	private static final int DEFAULT_SEARCH_TIMEOUT_IN_SECONDS = 300;

	private boolean enableAutoReconnect = DEFAULT_ENABLE_AUTO_RECONNECT;
	private boolean useClusterConfiguration = DEFAULT_USE_CLUSTER_CONFIGURATION;

	private int lockLease = DEFAULT_LOCK_LEASE_IN_SECONDS;
	private int lockTimeout = DEFAULT_LOCK_TIMEOUT_IN_SECONDS;
	private int messageSyncInterval = DEFAULT_MESSAGE_SYNC_INTERVAL_IN_SECONDS;
	private int searchTimeout = DEFAULT_SEARCH_TIMEOUT_IN_SECONDS;

	public boolean isEnableAutoReconnect() {
		return this.enableAutoReconnect;
	}

	public void setEnableAutoReconnect(boolean enableAutoReconnect) {
		this.enableAutoReconnect = enableAutoReconnect;
	}

	public int getLockLease() {
		return this.lockLease;
	}

	public void setLockLease(int lockLease) {
		this.lockLease = lockLease;
	}

	public int getLockTimeout() {
		return this.lockTimeout;
	}

	public void setLockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	public int getMessageSyncInterval() {
		return this.messageSyncInterval;
	}

	public void setMessageSyncInterval(int messageSyncInterval) {
		this.messageSyncInterval = messageSyncInterval;
	}

	public int getSearchTimeout() {
		return this.searchTimeout;
	}

	public void setSearchTimeout(int searchTimeout) {
		this.searchTimeout = searchTimeout;
	}

	public boolean isUseClusterConfiguration() {
		return this.useClusterConfiguration;
	}

	public void setUseClusterConfiguration(boolean useClusterConfiguration) {
		this.useClusterConfiguration = useClusterConfiguration;
	}
}
