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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.util.Assert;

/**
 * The {@link SecurityManagerProxy} class is an Apache Geode {@link org.apache.geode.security.SecurityManager}
 * proxy implementation delegating to a backing {@link org.apache.geode.security.SecurityManager} implementation
 * which is registered as a managed bean in a Spring context.
 *
 * The idea behind this {@link org.apache.geode.security.SecurityManager} is to enable users to be able to configure
 * and manage the {@code SecurityManager} as a Spring bean.  However, Apache Geode/Pivotal GemFire require
 * the {@link org.apache.geode.security.SecurityManager} to be configured using a System property when launching
 * Apache Geode Servers with Gfsh, which makes it difficult to "manage" the {@code SecurityManager} instance.
 *
 * Therefore, this implementation allows a developer to set the Apache Geode System property using this proxy...
 *
 * <code>
 *     gemfire.security-manager=org.springframework.geode.security.support.SecurityManagerProxy
 * </code>
 *
 * And then declare and define a bean in the Spring context implementing the
 * {@link org.apache.geode.security.SecurityManager} interface...
 *
 * <code>
 * Configuration
 * class MyApplicationConfiguration {
 *
 *     Bean
 *     ExampleSecurityManager exampleSecurityManager(Environment environment) {
 *         return new ExampleSecurityManager(environment);
 *     }
 *
 *     ...
 * }
 * </code>
 *
 * @author John Blum
 * @see org.apache.geode.security.ResourcePermission
 * @see org.apache.geode.security.SecurityManager
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.data.gemfire.support.LazyWiringDeclarableSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityManagerProxy extends LazyWiringDeclarableSupport
		implements org.apache.geode.security.SecurityManager {

	private static final AtomicReference<SecurityManagerProxy> INSTANCE = new AtomicReference<>();

	private org.apache.geode.security.SecurityManager securityManager;

	/**
	 * Returns a reference to the single {@link SecurityManagerProxy} instance configured by
	 * Apache Geode/Pivotal GemFire in startup.
	 *
	 * @return a reference to the single {@link SecurityManagerProxy} instance.
	 */
	public static SecurityManagerProxy getInstance() {

		return Optional.ofNullable(INSTANCE.get())
			.orElseThrow(() -> newIllegalStateException("SecurityManagerProxy was not configured"));
	}


	/**
	 * Constructs a new instance of {@link SecurityManagerProxy}, which will delegate all Apache Geode
	 * security operations to a Spring managed {@link org.apache.geode.security.SecurityManager} bean.
	 */
	public SecurityManagerProxy() {

		// TODO remove init() call when GEODE-2083 (https://issues.apache.org/jira/browse/GEODE-2083) is resolved!
		// NOTE: the init(:Properties) call in the constructor is less than ideal since...
		// 1) it allows the *this* reference to escape, and...
		// 2) it is Geode's responsibility to identify Geode Declarable objects and invoke their init(:Properties) method
		// However, the init(:Properties) method invocation in the constructor is necessary to enable this Proxy to be
		// identified and auto-wired in a Spring context.

		INSTANCE.compareAndSet(null, this);
		init(new Properties());
	}

	/**
	 * Configures a reference to the Apache Geode {@link org.apache.geode.security.SecurityManager} instance
	 * delegated to by this {@link SecurityManagerProxy}.
	 *
	 * @param securityManager reference to the underlying Apache Geode {@link org.apache.geode.security.SecurityManager}
	 * instance delegated to by this {@link SecurityManagerProxy}.
	 * @throws IllegalArgumentException if the {@link org.apache.geode.security.SecurityManager} reference
	 * is {@literal null}.
	 * @see org.apache.geode.security.SecurityManager
	 */
	@Autowired
	public void setSecurityManager(org.apache.geode.security.SecurityManager securityManager) {

		Assert.notNull(securityManager, "SecurityManager must not be null");

		this.securityManager = securityManager;
	}

	/**
	 * Returns a reference to the Apache Geode {@link org.apache.geode.security.SecurityManager} instance
	 * delegated to by this {@link SecurityManagerProxy}.
	 *
	 * @return a reference to the underlying {@link org.apache.geode.security.SecurityManager} instance
	 * delegated to by this {@link SecurityManagerProxy}.
	 * @throws IllegalStateException if the configured {@link org.apache.geode.security.SecurityManager}
	 * was not properly configured.
	 * @see org.apache.geode.security.SecurityManager
	 */
	protected org.apache.geode.security.SecurityManager getSecurityManager() {

		Assert.state(this.securityManager != null, "No SecurityManager configured");

		return this.securityManager;
	}

	@Override
	public Object authenticate(Properties properties) throws AuthenticationFailedException {
		return getSecurityManager().authenticate(properties);
	}

	@Override
	public boolean authorize(Object principal, ResourcePermission permission) {
		return getSecurityManager().authorize(principal, permission);
	}

	@Override
	public void close() {
		getSecurityManager().close();
	}
}
