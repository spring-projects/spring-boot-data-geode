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

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode management services, such as HTTP.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ManagementProperties {

	private static final boolean DEFAULT_USE_HTTP = false;

	private boolean useHttp = DEFAULT_USE_HTTP;

	private final HttpServiceProperties httpServiceProperties = new HttpServiceProperties();

	public HttpServiceProperties getHttp() {
		return this.httpServiceProperties;
	}

	public boolean isUseHttp() {
		return this.useHttp;
	}

	public void setUseHttp(boolean useHttp) {
		this.useHttp = useHttp;
	}

	public static class HttpServiceProperties {

		private static final int DEFAULT_PORT = 7070;

		private static final String DEFAULT_HOST = "localhost";

		private int port = DEFAULT_PORT;

		private String host = DEFAULT_HOST;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}
}
