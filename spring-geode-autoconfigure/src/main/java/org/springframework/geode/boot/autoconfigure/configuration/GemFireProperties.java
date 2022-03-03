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
package org.springframework.geode.boot.autoconfigure.configuration;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.geode.boot.autoconfigure.configuration.support.CacheProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ClusterProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.DiskStoreProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.EntityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.LocatorProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.LoggingProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ManagementProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ManagerProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PdxProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PoolProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.SecurityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ServiceProperties;

/**
 * Spring Boot {@link ConfigurationProperties} for well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * This class assists the application developer in the auto-completion / content-assist of the well-known, documented
 * SDG {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.boot.context.properties.NestedConfigurationProperty
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.CacheProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.ClusterProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.DiskStoreProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.EntityProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.LocatorProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.LoggingProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.ManagementProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.ManagerProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.PdxProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.PoolProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.SecurityProperties
 * @see org.springframework.geode.boot.autoconfigure.configuration.support.ServiceProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "spring.data.gemfire")
public class GemFireProperties {

	private static final boolean DEFAULT_USE_BEAN_FACTORY_LOCATOR = false;

	private boolean useBeanFactoryLocator = DEFAULT_USE_BEAN_FACTORY_LOCATOR;

	@NestedConfigurationProperty
	private final CacheProperties cache = new CacheProperties();

	@NestedConfigurationProperty
	private final ClusterProperties cluster = new ClusterProperties();

	@NestedConfigurationProperty
	private final DiskStoreProperties disk = new DiskStoreProperties();

	@NestedConfigurationProperty
	private final EntityProperties entities = new EntityProperties();

	@NestedConfigurationProperty
	private final LocatorProperties locator = new LocatorProperties();

	@NestedConfigurationProperty
	private final LoggingProperties logging = new LoggingProperties();

	@NestedConfigurationProperty
	private final ManagementProperties management = new ManagementProperties();

	@NestedConfigurationProperty
	private final ManagerProperties manager = new ManagerProperties();

	@NestedConfigurationProperty
	private final PdxProperties pdx = new PdxProperties();

	@NestedConfigurationProperty
	private final PoolProperties pool = new PoolProperties();

	@NestedConfigurationProperty
	private final SecurityProperties security = new SecurityProperties();

	@NestedConfigurationProperty
	private final ServiceProperties service = new ServiceProperties();

	private String name;

	private String[] locators;

	public CacheProperties getCache() {
		return this.cache;
	}

	public ClusterProperties getCluster() {
		return this.cluster;
	}

	public DiskStoreProperties getDisk() {
		return this.disk;
	}

	public EntityProperties getEntities() {
		return this.entities;
	}

	public LocatorProperties getLocator() {
		return this.locator;
	}

	public String[] getLocators() {
		return this.locators;
	}

	public void setLocators(String[] locators) {
		this.locators = locators;
	}

	public LoggingProperties getLogging() {
		return this.logging;
	}

	public ManagementProperties getManagement() {
		return this.management;
	}

	public ManagerProperties getManager() {
		return this.manager;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PdxProperties getPdx() {
		return this.pdx;
	}

	public PoolProperties getPool() {
		return this.pool;
	}

	public SecurityProperties getSecurity() {
		return this.security;
	}

	public ServiceProperties getService() {
		return this.service;
	}

	public boolean isUseBeanFactoryLocator() {
		return this.useBeanFactoryLocator;
	}

	public void setUseBeanFactoryLocator(boolean useBeanFactoryLocator) {
		this.useBeanFactoryLocator = useBeanFactoryLocator;
	}
}
