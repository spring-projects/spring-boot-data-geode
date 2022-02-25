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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.gemfire.config.annotation.EnableMemcachedServer;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode embedded services.
 *
 * Currently, the supported embedded services include HTTP service (Jetty), Memcached (protocol) server
 * and a Redis (protocol) server.
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
public class ServiceProperties {

	private final HttpServiceProperties httpServiceProperties = new HttpServiceProperties();

	private final MemcachedServerProperties memcachedServerProperties = new MemcachedServerProperties();

	private final RedisServerProperties redisServerProperties = new RedisServerProperties();

	public HttpServiceProperties getHttp() {
		return this.httpServiceProperties;
	}

	public MemcachedServerProperties getMemcached() {
		return this.memcachedServerProperties;
	}

	public RedisServerProperties getRedis() {
		return this.redisServerProperties;
	}

	public static class DeveloperRestApiProperties {

		private static final boolean DEFAULT_START = false;

		private boolean start = DEFAULT_START;

		public boolean isStart() {
			return this.start;
		}

		public void setStart(boolean start) {
			this.start = start;
		}
	}

	public static class HttpServiceProperties {

		private static final boolean DEFAULT_SSL_REQUIRE_AUTHENTICATION = false;

		private static final int DEFAULT_PORT = 7070;

		private boolean sslRequireAuthentication = DEFAULT_SSL_REQUIRE_AUTHENTICATION;

		private int port = DEFAULT_PORT;

		private final DeveloperRestApiProperties developerRestApiProperties = new DeveloperRestApiProperties();

		private String bindAddress;

		public String getBindAddress() {
			return this.bindAddress;
		}

		public void setBindAddress(String bindAddress) {
			this.bindAddress = bindAddress;
		}

		public DeveloperRestApiProperties getDevRestApi() {
			return developerRestApiProperties;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isSslRequireAuthentication() {
			return this.sslRequireAuthentication;
		}

		public void setSslRequireAuthentication(boolean sslRequireAuthentication) {
			this.sslRequireAuthentication = sslRequireAuthentication;
		}
	}

	public static class MemcachedServerProperties {

		private static final int DEFAULT_PORT = 11211;

		private int port = DEFAULT_PORT;

		private EnableMemcachedServer.MemcachedProtocol protocol = EnableMemcachedServer.MemcachedProtocol.ASCII;

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public EnableMemcachedServer.MemcachedProtocol getProtocol() {
			return this.protocol;
		}

		public void setProtocol(EnableMemcachedServer.MemcachedProtocol protocol) {
			this.protocol = protocol;
		}
	}

	public static class RedisServerProperties {

		public static final int DEFAULT_PORT = 6379;

		private int port = DEFAULT_PORT;

		private String bindAddress;

		public String getBindAddress() {
			return this.bindAddress;
		}

		public void setBindAddress(String bindAddress) {
			this.bindAddress = bindAddress;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}
}
