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

import org.apache.geode.cache.client.ClientCache;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure an Apache Geode {@link ClientCache}.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClientCacheProperties {

	private static final boolean DEFAULT_KEEP_ALIVE = false;

	private static final int DEFAULT_DURABLE_CLIENT_TIMEOUT_IN_SECONDS = 300;

	private boolean keepAlive = DEFAULT_KEEP_ALIVE;

	private int durableClientTimeout = DEFAULT_DURABLE_CLIENT_TIMEOUT_IN_SECONDS;

	private String durableClientId;

	public String getDurableClientId() {
		return this.durableClientId;
	}

	public void setDurableClientId(String durableClientId) {
		this.durableClientId = durableClientId;
	}

	public int getDurableClientTimeout() {
		return this.durableClientTimeout;
	}

	public void setDurableClientTimeout(int durableClientTimeout) {
		this.durableClientTimeout = durableClientTimeout;
	}

	public boolean isKeepAlive() {
		return this.keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
}
