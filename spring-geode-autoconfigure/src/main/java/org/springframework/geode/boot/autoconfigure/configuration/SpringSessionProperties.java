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
package org.springframework.geode.boot.autoconfigure.configuration;

import java.time.Duration;

import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Spring Session for Apache Geode (SSDG) in order to
 * manage (HTTP) Session state with Spring Session backed by Apache Geode.
 *
 * @author John Blum
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.boot.context.properties.NestedConfigurationProperty
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "spring.session.data.gemfire")
public class SpringSessionProperties {

	@NestedConfigurationProperty
	private final CacheProperties cache = new CacheProperties();

	@NestedConfigurationProperty
	private final SessionProperties session = new SessionProperties();

	public CacheProperties getCache() {
		return this.cache;
	}

	public SessionProperties getSession() {
		return this.session;
	}

	public static class CacheProperties {

		@NestedConfigurationProperty
		private final CacheServerProperties server = new CacheServerProperties();

		@NestedConfigurationProperty
		private final ClientCacheProperties client = new ClientCacheProperties();

		public ClientCacheProperties getClient() {
			return this.client;
		}

		public CacheServerProperties getServer() {
			return this.server;
		}
	}

	public static class CacheServerProperties {

		@NestedConfigurationProperty
		private final ServerRegionProperties region = new ServerRegionProperties();

		public ServerRegionProperties getRegion() {
			return region;
		}
	}

	public static class ClientCacheProperties {

		@NestedConfigurationProperty
		private final ClientRegionProperties region = new ClientRegionProperties();

		@NestedConfigurationProperty
		private final PoolProperties pool = new PoolProperties();

		public PoolProperties getPool() {
			return this.pool;
		}

		public ClientRegionProperties getRegion() {
			return this.region;
		}
	}

	public static class ClientRegionProperties {

		public static final ClientRegionShortcut DEFAULT_CLIENT_REGION_SHORTCUT = ClientRegionShortcut.PROXY;

		private ClientRegionShortcut shortcut = ClientRegionShortcut.PROXY;

		public ClientRegionShortcut getShortcut() {
			return this.shortcut != null ? this.shortcut : DEFAULT_CLIENT_REGION_SHORTCUT;
		}

		public void setShortcut(ClientRegionShortcut shortcut) {
			this.shortcut = shortcut;
		}
	}

	public static class PoolProperties {

		private String name;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class ServerRegionProperties {

		public static final RegionShortcut DEFAULT_SERVER_REGION_SHORTCUT = RegionShortcut.PARTITION;

		private RegionShortcut shortcut;

		public RegionShortcut getShortcut() {
			return this.shortcut != null ? this.shortcut : DEFAULT_SERVER_REGION_SHORTCUT;
		}

		public void setShortcut(RegionShortcut shortcut) {
			this.shortcut = shortcut;
		}
	}

	public static class SessionProperties {

		@NestedConfigurationProperty
		private final SessionAttributesProperties attributes = new SessionAttributesProperties();

		@NestedConfigurationProperty
		private final SessionExpirationProperties expiration = new SessionExpirationProperties();

		@NestedConfigurationProperty
		private final SessionRegionProperties region = new SessionRegionProperties();

		@NestedConfigurationProperty
		private final SessionSerializerProperties serializer = new SessionSerializerProperties();

		public SessionAttributesProperties getAttributes() {
			return this.attributes;
		}

		public SessionExpirationProperties getExpiration() {
			return this.expiration;
		}

		public SessionRegionProperties getRegion() {
			return this.region;
		}

		public SessionSerializerProperties getSerializer() {
			return this.serializer;
		}
	}

	public static class SessionAttributesProperties {

		private String[] indexable;

		public String[] getIndexable() {
			return this.indexable;
		}

		public void setIndexable(String[] indexable) {
			this.indexable = indexable;
		}
	}

	public static class SessionExpirationProperties {

		private int maxInactiveIntervalSeconds;

		public int getMaxInactiveIntervalSeconds() {
			return this.maxInactiveIntervalSeconds;
		}

		public void setMaxInactiveIntervalSeconds(int maxInactiveIntervalSeconds) {
			this.maxInactiveIntervalSeconds = maxInactiveIntervalSeconds;
		}

		public void setMaxInactiveInterval(Duration duration) {

			int maxInactiveIntervalInSeconds = duration != null
				? Long.valueOf(duration.getSeconds()).intValue()
				: GemFireHttpSessionConfiguration.DEFAULT_MAX_INACTIVE_INTERVAL_IN_SECONDS;

			setMaxInactiveIntervalSeconds(maxInactiveIntervalInSeconds);
		}
	}

	public static class SessionRegionProperties {

		private String name;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class SessionSerializerProperties {

		private String beanName;

		public String getBeanName() {
			return this.beanName;
		}

		public void setBeanName(String beanName) {
			this.beanName = beanName;
		}
	}
}
