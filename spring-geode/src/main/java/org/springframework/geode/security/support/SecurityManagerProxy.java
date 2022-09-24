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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.support.LazyWiringDeclarableSupport;
import org.springframework.util.Assert;

/**
 * The {@link SecurityManagerProxy} class is an Apache Geode {@link org.apache.geode.security.SecurityManager}
 * proxy implementation delegating to a backing {@link org.apache.geode.security.SecurityManager} implementation
 * which is registered as a managed bean in a Spring context.
 *
 * The idea behind this {@link org.apache.geode.security.SecurityManager} is to enable users to be able to configure
 * and manage the {@code SecurityManager} as a Spring bean.  However, Apache Geode require
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
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.data.gemfire.support.LazyWiringDeclarableSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityManagerProxy extends LazyWiringDeclarableSupport
		implements org.apache.geode.security.SecurityManager, DisposableBean, BeanFactoryAware {

	private static final AtomicReference<SecurityManagerProxy> INSTANCE = new AtomicReference<>();

	private BeanFactory beanFactory;

	private org.apache.geode.security.SecurityManager securityManager;

	/**
	 * Returns a reference to the single {@link SecurityManagerProxy} instance configured by Apache Geode in startup.
	 *
	 * @return a reference to the single {@link SecurityManagerProxy} instance.
	 */
	public static SecurityManagerProxy getInstance() {

		SecurityManagerProxy securityManagerProxy = INSTANCE.get();

		Assert.state(securityManagerProxy != null, "SecurityManagerProxy was not configured");

		return securityManagerProxy;
	}

	/**
	 * Constructs a new instance of {@link SecurityManagerProxy}, which will delegate all Apache Geode
	 * security operations to a Spring managed {@link org.apache.geode.security.SecurityManager} bean.
	 */
	public SecurityManagerProxy() {
		INSTANCE.compareAndSet(null, this);
	}

	/**
	 * Configures a reference to the current Spring {@link BeanFactory}.
	 *
	 * @param beanFactory reference to the current Spring {@link BeanFactory}.
	 * @throws BeansException if this operation fails to configure the reference to the {@link BeanFactory}.
	 * @see org.springframework.beans.factory.BeanFactory
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
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

	@Override
	public void destroy() throws Exception {
		super.destroy();
		INSTANCE.set(null);
	}

	@Override
	public void init(Properties props) {
		super.init(props);
	}

	@Override
	protected BeanFactory locateBeanFactory() {
		return this.beanFactory != null ? this.beanFactory : super.locateBeanFactory();
	}
}
