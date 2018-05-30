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

package org.springframework.geode.security.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Properties;

import org.apache.geode.security.ResourcePermission;
import org.junit.Test;

/**
 * Unit tests for {@link SecurityManagerProxy}
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.security.support.SecurityManagerProxy
 * @since 1.0.0
 */
public class SecurityManagerProxyUnitTests {

	@Test
	public void setAndGetSecurityManager() {

		org.apache.geode.security.SecurityManager mockSecurityManager =
			mock(org.apache.geode.security.SecurityManager.class);

		SecurityManagerProxy securityManagerProxy = new SecurityManagerProxy();

		securityManagerProxy.setSecurityManager(mockSecurityManager);

		assertThat(securityManagerProxy.getSecurityManager()).isEqualTo(mockSecurityManager);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setSecurityManagerToNullThrowsIllegalArgumentException() {

		try {
			new SecurityManagerProxy().setSecurityManager(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("SecurityManager must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void getSecurityManagerWhenUninitializedThrowsIllegalStateException() {

		try {
			new SecurityManagerProxy().getSecurityManager();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("No SecurityManager configured");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void authenticateDelegatesToConfiguredSecurityManager() {

		Properties securityProperties = new Properties();

		org.apache.geode.security.SecurityManager mockSecurityManager =
			mock(org.apache.geode.security.SecurityManager.class);

		when(mockSecurityManager.authenticate(any(Properties.class))).thenReturn("TestUser");

		SecurityManagerProxy securityManagerProxy = new SecurityManagerProxy();

		securityManagerProxy.setSecurityManager(mockSecurityManager);

		assertThat(securityManagerProxy.getSecurityManager()).isEqualTo(mockSecurityManager);
		assertThat(securityManagerProxy.authenticate(securityProperties)).isEqualTo("TestUser");

		verify(mockSecurityManager, times(1)).authenticate(eq(securityProperties));
	}

	@Test
	public void authorizeDelegatesToConfiguredSecurityManager() {

		Principal mockPrincipal = mock(Principal.class);

		ResourcePermission resourcePermission =
			new ResourcePermission(ResourcePermission.Resource.DATA, ResourcePermission.Operation.READ);

		org.apache.geode.security.SecurityManager mockSecurityManager =
			mock(org.apache.geode.security.SecurityManager.class);

		when(mockSecurityManager.authorize(any(Object.class), any(ResourcePermission.class))).thenReturn(true);

		SecurityManagerProxy securityManagerProxy = new SecurityManagerProxy();

		securityManagerProxy.setSecurityManager(mockSecurityManager);

		assertThat(securityManagerProxy.getSecurityManager()).isEqualTo(mockSecurityManager);
		assertThat(securityManagerProxy.authorize(mockPrincipal, resourcePermission)).isTrue();

		verify(mockSecurityManager, times(1))
			.authorize(eq(mockPrincipal), eq(resourcePermission));
	}

	@Test
	public void closeDelegatesToConfiguredSecurityManager() {

		org.apache.geode.security.SecurityManager mockSecurityManager =
			mock(org.apache.geode.security.SecurityManager.class);

		when(mockSecurityManager.authorize(any(Object.class), any(ResourcePermission.class))).thenReturn(true);

		SecurityManagerProxy securityManagerProxy = new SecurityManagerProxy();

		securityManagerProxy.setSecurityManager(mockSecurityManager);

		assertThat(securityManagerProxy.getSecurityManager()).isEqualTo(mockSecurityManager);

		securityManagerProxy.close();

		verify(mockSecurityManager, times(1)).close();
	}
}
