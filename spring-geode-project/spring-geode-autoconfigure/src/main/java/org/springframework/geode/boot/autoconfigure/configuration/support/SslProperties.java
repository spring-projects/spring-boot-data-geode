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
import org.springframework.data.gemfire.config.annotation.EnableSsl;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode Socket layer SSL.
 *
 * The SSL configuration is used to secure communications and data in motion between clients and servers
 * as well as between peers in a cluster.
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
public class SslProperties {

	private static final boolean DEFAULT_REQUIRE_AUTHENTICATION = true;
	private static final boolean DEFAULT_WEB_REQUIRE_AUTHENTICATION = false;

	private boolean requireAuthentication = DEFAULT_REQUIRE_AUTHENTICATION;
	private boolean webRequireAuthentication = DEFAULT_WEB_REQUIRE_AUTHENTICATION;

	private EnableSsl.Component[] components;

	@NestedConfigurationProperty
	private final KeyStoreProperties keystoreConfig = new KeyStoreProperties();

	@NestedConfigurationProperty
	private final KeyStoreProperties truststoreConfig = new KeyStoreProperties();

	@NestedConfigurationProperty
	private final SslCertificateProperties certificate = new SslCertificateProperties();

	private String keystore;
	private String truststore;

	private String[] ciphers;

	private String[] protocols;

	public SslCertificateProperties getCertificate() {
		return this.certificate;
	}

	public String[] getCiphers() {
		return this.ciphers;
	}

	public void setCiphers(String[] ciphers) {
		this.ciphers = ciphers;
	}

	public EnableSsl.Component[] getComponents() {
		return this.components;
	}

	public void setComponents(EnableSsl.Component[] components) {
		this.components = components;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public KeyStoreProperties getKeystoreConfig() {
		return this.keystoreConfig;
	}

	public String[] getProtocols() {
		return this.protocols;
	}

	public void setProtocols(String[] protocols) {
		this.protocols = protocols;
	}

	public boolean isRequireAuthentication() {
		return this.requireAuthentication;
	}

	public void setRequireAuthentication(boolean requireAuthentication) {
		this.requireAuthentication = requireAuthentication;
	}

	public String getTruststore() {
		return this.truststore;
	}

	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}

	public KeyStoreProperties getTruststoreConfig() {
		return this.truststoreConfig;
	}

	public boolean isWebRequireAuthentication() {
		return this.webRequireAuthentication;
	}

	public void setWebRequireAuthentication(boolean webRequireAuthentication) {
		this.webRequireAuthentication = webRequireAuthentication;
	}

	public static class KeyStoreProperties {

		private String password;
		private String type;

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static class SslCertificateProperties {

		@NestedConfigurationProperty
		private SslCertificateAliasProperties alias = new SslCertificateAliasProperties();

		public SslCertificateAliasProperties getAlias() {
			return this.alias;
		}
	}

	public static class SslCertificateAliasProperties {

		private String all;
		private String cluster;
		private String defaultAlias;
		private String gateway;
		private String jmx;
		private String locator;
		private String server;
		private String web;

		public String getAll() {
			return this.all;
		}

		public void setAll(String all) {
			this.all = all;
		}

		public String getCluster() {
			return this.cluster;
		}

		public void setCluster(String cluster) {
			this.cluster = cluster;
		}

		public String getDefaultAlias() {
			return this.defaultAlias;
		}

		public void setDefaultAlias(String defaultAlias) {
			this.defaultAlias = defaultAlias;
		}

		public String getGateway() {
			return this.gateway;
		}

		public void setGateway(String gateway) {
			this.gateway = gateway;
		}

		public String getJmx() {
			return this.jmx;
		}

		public void setJmx(String jmx) {
			this.jmx = jmx;
		}

		public String getLocator() {
			return this.locator;
		}

		public void setLocator(String locator) {
			this.locator = locator;
		}

		public String getServer() {
			return this.server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public String getWeb() {
			return this.web;
		}

		public void setWeb(String web) {
			this.web = web;
		}
	}
}
