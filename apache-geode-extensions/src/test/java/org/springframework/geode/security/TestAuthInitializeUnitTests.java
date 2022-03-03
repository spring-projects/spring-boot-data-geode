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
package org.springframework.geode.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;

import org.springframework.geode.util.GeodeConstants;

/**
 * Unit Tests for {@link TestAuthInitialize}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.springframework.geode.security.TestAuthInitialize
 * @since 1.0.0
 */
public class TestAuthInitializeUnitTests {

	private final TestAuthInitialize authInitialize = TestAuthInitialize.create();

	private boolean isSet(String value) {
		return !(value == null || value.trim().isEmpty());
	}

	private Properties newSecurityProperties(String username, String password) {

		Properties securityProperties = new Properties();

		if (isSet(username)) {
			securityProperties.setProperty(GeodeConstants.USERNAME, username);
		}

		if (isSet(password)) {
			securityProperties.setProperty(GeodeConstants.PASSWORD, password);
		}

		return securityProperties;
	}

	@Test
	public void getCredentialsUsesProperties() {

		Properties securityProperties = newSecurityProperties("testUser", "s3cr3t");
		Properties credentials = this.authInitialize.getCredentials(securityProperties, null, false);

		assertThat(credentials).isNotNull();
		assertThat(credentials.getProperty(GeodeConstants.USERNAME)).isEqualTo("testUser");
		assertThat(credentials.getProperty(GeodeConstants.PASSWORD)).isEqualTo("s3cr3t");
	}

	@Test
	public void getCredentialsUsesProvidedUsernameAndDefaultPassword() {

		Properties securityProperties = newSecurityProperties("testUser", null);
		Properties credentials = this.authInitialize.getCredentials(securityProperties, null, false);

		assertThat(credentials).isNotNull();
		assertThat(credentials.getProperty(GeodeConstants.USERNAME)).isEqualTo("testUser");
		assertThat(credentials.getProperty(GeodeConstants.PASSWORD)).isEqualTo("test");
	}

	@Test
	public void getCredentialsUsesProvidedPasswordAndDefaultUsername() {

		Properties securityProperties = newSecurityProperties(null, "s3cr3t");
		Properties credentials = this.authInitialize.getCredentials(securityProperties, null, false);

		assertThat(credentials).isNotNull();
		assertThat(credentials.getProperty(GeodeConstants.USERNAME)).isEqualTo("test");
		assertThat(credentials.getProperty(GeodeConstants.PASSWORD)).isEqualTo("s3cr3t");
	}

	@Test
	public void getCredentialsUsesDefaultUsernameAndPassword() {

		Properties securityProperties = newSecurityProperties(null, null);
		Properties credentials = this.authInitialize.getCredentials(securityProperties, null, false);

		assertThat(credentials).isNotNull();
		assertThat(credentials.getProperty(GeodeConstants.USERNAME)).isEqualTo("test");
		assertThat(credentials.getProperty(GeodeConstants.PASSWORD)).isEqualTo("test");
	}
}
