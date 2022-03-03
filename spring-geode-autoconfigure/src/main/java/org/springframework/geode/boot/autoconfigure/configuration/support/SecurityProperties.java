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
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode Security.
 *
 * Security configuration covers Authentication & Authorization (AUTH) as well as Secure Transport using SSL
 * (i.e. securing data in motion).  Securing data at rest (e.g. disk based encryption) is not yet supported.
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
public class SecurityProperties {

	private final ApacheShiroProperties apacheShiroProperties = new ApacheShiroProperties();

	@NestedConfigurationProperty
	private final ClientSecurityProperties client = new ClientSecurityProperties();

	@NestedConfigurationProperty
	private final PeerSecurityProperties peer = new PeerSecurityProperties();

	private final SecurityLogProperties securityLogProperties = new SecurityLogProperties();

	private final SecurityManagerProperties securityManagerProperties = new SecurityManagerProperties();

	private final SecurityPostProcessorProperties securityPostProcessorProperties =
		new SecurityPostProcessorProperties();

	@NestedConfigurationProperty
	private final SslProperties ssl = new SslProperties();

	private String password;
	private String propertiesFile;
	private String username;

	public ClientSecurityProperties getClient() {
		return this.client;
	}

	public SecurityLogProperties getLog() {
		return this.securityLogProperties;
	}

	public SecurityManagerProperties getManager() {
		return this.securityManagerProperties;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PeerSecurityProperties getPeer() {
		return this.peer;
	}

	public SecurityPostProcessorProperties getPostProcessor() {
		return this.securityPostProcessorProperties;
	}

	public String getPropertiesFile() {
		return this.propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public ApacheShiroProperties getShiro() {
		return this.apacheShiroProperties;
	}

	public SslProperties getSsl() {
		return this.ssl;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public static class ApacheShiroProperties {

		private String iniResourcePath;

		public String getIniResourcePath() {
			return this.iniResourcePath;
		}

		public void setIniResourcePath(String iniResourcePath) {
			this.iniResourcePath = iniResourcePath;
		}
	}

	public static class SecurityLogProperties {

		private static final String DEFAULT_SECURITY_LOG_LEVEL = "config";

		private String file;
		private String level = DEFAULT_SECURITY_LOG_LEVEL;

		public String getFile() {
			return this.file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		public String getLevel() {
			return this.level;
		}

		public void setLevel(String level) {
			this.level = level;
		}
	}

	public static class SecurityManagerProperties {

		private String className;

		public String getClassName() {
			return this.className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
	}

	public static class SecurityPostProcessorProperties {

		private String className;

		public String getClassName() {
			return this.className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
	}
}
