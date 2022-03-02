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
package org.springframework.geode.security.support;

import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;

/**
 * {@link SecurityManagerSupport} is an abstract base class implementing Apache Geode's
 * {@link org.apache.geode.security.SecurityManager} interface, providing default implementations of the
 * {@link org.apache.geode.security.SecurityManager} auth methods.
 *
 * @author John Blum
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SecurityManagerSupport implements org.apache.geode.security.SecurityManager {

	protected static final boolean DEFAULT_AUTHORIZATION = false;

	@Override
	public void init(Properties securityProperties) { }

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException {
		return new AuthenticationFailedException("Authentication Provider Not Present");
	}

	@Override
	public boolean authorize(Object principal, ResourcePermission permission) {
		return DEFAULT_AUTHORIZATION;
	}

	@Override
	public void close() { }

}
