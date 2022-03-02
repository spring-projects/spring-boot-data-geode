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
import static org.mockito.Mockito.mock;

import java.security.Principal;
import java.util.Properties;

import org.junit.Test;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;

import org.springframework.geode.util.GeodeConstants;

/**
 * Unit Tests for {@link org.springframework.geode.security.TestSecurityManager}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.security.TestSecurityManager
 * @since 1.0.0
 */
public class TestSecurityManagerUnitTests {

	private TestSecurityManager securityManager = new TestSecurityManager();

	private Properties newSecurityProperties(String username, String password) {

		Properties securityProperties = new Properties();

		securityProperties.setProperty(GeodeConstants.USERNAME, username);
		securityProperties.setProperty(GeodeConstants.PASSWORD, password);

		return securityProperties;
	}

	@Test
	public void userAuthenticates() {

		Object user = this.securityManager.authenticate(newSecurityProperties("test", "test"));

		assertThat(user).isInstanceOf(TestSecurityManager.User.class);
		assertThat(((TestSecurityManager.User) user).getName()).isEqualTo("test");
	}

	@Test(expected = AuthenticationFailedException.class)
	public void userDoesNotAuthenticateBecauseUsernamePasswordAreCaseSensitive() {

		try {
			this.securityManager.authenticate(newSecurityProperties("TestUser", "testuser"));
		}
		catch (AuthenticationFailedException expected) {

			assertThat(expected).hasMessage("User [TestUser] could not be authenticated");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = AuthenticationFailedException.class)
	public void userDoesNotAuthenticateWhenUsernamePasswordDoNotMatch() {

		try {
			this.securityManager.authenticate(newSecurityProperties("testUser", "testPassword"));
		}
		catch (AuthenticationFailedException expected) {

			assertThat(expected).hasMessage("User [testUser] could not be authenticated");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void userIsAlwaysAuthorized() {

		ResourcePermission clusterManage =
			new ResourcePermission(ResourcePermission.Resource.CLUSTER, ResourcePermission.Operation.MANAGE);

		assertThat(this.securityManager.authorize(null, clusterManage)).isTrue();
		assertThat(this.securityManager.authorize(new TestSecurityManager.User("test"), clusterManage)).isTrue();
		assertThat(this.securityManager.authorize(mock(Principal.class), clusterManage)).isTrue();
	}
}
