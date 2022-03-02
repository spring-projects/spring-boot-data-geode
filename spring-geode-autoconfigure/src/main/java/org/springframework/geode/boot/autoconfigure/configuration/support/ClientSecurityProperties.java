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
 * Spring Boot {@link ConfigurationProperties} used to configure an Apache Geode {@link ClientCache} Security
 * (authentication & authorization).
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
public class ClientSecurityProperties {

	private String accessor;
	private String accessorPostProcessor;
	private String authenticationInitializer;
	private String authenticator;
	private String diffieHellmanAlgorithm;

	public String getAccessor() {
		return this.accessor;
	}

	public void setAccessor(String accessor) {
		this.accessor = accessor;
	}

	public String getAccessorPostProcessor() {
		return this.accessorPostProcessor;
	}

	public void setAccessorPostProcessor(String accessorPostProcessor) {
		this.accessorPostProcessor = accessorPostProcessor;
	}

	public String getAuthenticationInitializer() {
		return this.authenticationInitializer;
	}

	public void setAuthenticationInitializer(String authenticationInitializer) {
		this.authenticationInitializer = authenticationInitializer;
	}

	public String getAuthenticator() {
		return this.authenticator;
	}

	public void setAuthenticator(String authenticator) {
		this.authenticator = authenticator;
	}

	public String getDiffieHellmanAlgorithm() {
		return this.diffieHellmanAlgorithm;
	}

	public void setDiffieHellmanAlgorithm(String diffieHellmanAlgorithm) {
		this.diffieHellmanAlgorithm = diffieHellmanAlgorithm;
	}
}
