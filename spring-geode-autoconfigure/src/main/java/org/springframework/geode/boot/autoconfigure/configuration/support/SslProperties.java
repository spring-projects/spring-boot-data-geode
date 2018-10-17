/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.configuration.support;

import org.springframework.data.gemfire.config.annotation.EnableSsl;

/**
 * The SslProperties class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SslProperties {

	private static final boolean DEFAULT_REQUIRE_AUTHENTICATION = true;
	private static final boolean DEFAULT_WEB_REQUIRE_AUTHENTICATION = false;

	private boolean requireAuthentication = DEFAULT_REQUIRE_AUTHENTICATION;
	private boolean webRequireAuthentication = DEFAULT_WEB_REQUIRE_AUTHENTICATION;

	private EnableSsl.Component[] components;

	private final KeyStoreProperties keystoreProperties = new KeyStoreProperties();
	private final KeyStoreProperties truststoreProperties = new KeyStoreProperties();

	private final SslCertificateProperties sslCertificateProperties = new SslCertificateProperties();

	private String keystore;
	private String truststore;

	private String[] ciphers;

	private String[] protocols;

	public SslCertificateProperties getCertificate() {
		return this.sslCertificateProperties;
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

		private SslCertificateAliasProperties sslCertificateAliasProperties = new SslCertificateAliasProperties();

		public SslCertificateAliasProperties getAlias() {
			return this.sslCertificateAliasProperties;
		}
	}

	public static class SslCertificateAliasProperties {

		private String allAlias;
		private String clusterAlias;
		private String defaultAlias;
		private String gatewayAlias;
		private String jmxAlias;
		private String locatorAlias;
		private String serverAlias;
		private String webAlias;

		public String getAllAlias() {
			return this.allAlias;
		}

		public void setAllAlias(String allAlias) {
			this.allAlias = allAlias;
		}

		public String getClusterAlias() {
			return this.clusterAlias;
		}

		public void setClusterAlias(String clusterAlias) {
			this.clusterAlias = clusterAlias;
		}

		public String getDefaultAlias() {
			return this.defaultAlias;
		}

		public void setDefaultAlias(String defaultAlias) {
			this.defaultAlias = defaultAlias;
		}

		public String getGatewayAlias() {
			return this.gatewayAlias;
		}

		public void setGatewayAlias(String gatewayAlias) {
			this.gatewayAlias = gatewayAlias;
		}

		public String getJmxAlias() {
			return this.jmxAlias;
		}

		public void setJmxAlias(String jmxAlias) {
			this.jmxAlias = jmxAlias;
		}

		public String getLocatorAlias() {
			return this.locatorAlias;
		}

		public void setLocatorAlias(String locatorAlias) {
			this.locatorAlias = locatorAlias;
		}

		public String getServerAlias() {
			return this.serverAlias;
		}

		public void setServerAlias(String serverAlias) {
			this.serverAlias = serverAlias;
		}

		public String getWebAlias() {
			return this.webAlias;
		}

		public void setWebAlias(String webAlias) {
			this.webAlias = webAlias;
		}
	}
}
