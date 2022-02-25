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
package org.springframework.geode.test.context;

import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.context.annotation.RefreshableAnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractContextLoader;

/**
 * An {@link AbstractContextLoader} from the Spring {@link TestContext} Framework used to load
 * a {@link RefreshableAnnotationConfigApplicationContext}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigRegistry
 * @see org.springframework.geode.context.annotation.RefreshableAnnotationConfigApplicationContext
 * @see org.springframework.test.context.ContextConfigurationAttributes
 * @see org.springframework.test.context.MergedContextConfiguration
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.support.AbstractContextLoader
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class TestRefreshableApplicationContextLoader extends AbstractContextLoader {

	protected static final String DEFAULT_RESOURCE_SUFFIX = "-context";

	private Class<?> testClass;

	/**
	 * @inheritDoc
	 */
	@Override
	public void processContextConfiguration(ContextConfigurationAttributes configAttributes) {

		super.processContextConfiguration(configAttributes);

		this.testClass = configAttributes.getDeclaringClass();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) {

		ConfigurableApplicationContext applicationContext =
			configure(newApplicationContext(mergedConfig), mergedConfig);

		applicationContext.registerShutdownHook();
		applicationContext.refresh();

		return applicationContext;
	}

	/**
	 * Constructs a new instance of the {@link RefreshableAnnotationConfigApplicationContext} initialized from
	 * the given {{@link TestContext} @link MergedContextConfiguration merged configuration meta-data}.
	 *
	 * @param contextConfiguration {@link MergedContextConfiguration} from the {@link TestContext} used to configure
	 * and bootstrap a new {@link RefreshableAnnotationConfigApplicationContext}.
	 * @return a configured and bootstrapped {@link ConfigurableApplicationContext} implementation.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see org.springframework.geode.context.annotation.RefreshableAnnotationConfigApplicationContext
	 * @see org.springframework.test.context.MergedContextConfiguration
	 * @see #prepareContext(ConfigurableApplicationContext, MergedContextConfiguration)
	 */
	protected ConfigurableApplicationContext newApplicationContext(MergedContextConfiguration contextConfiguration) {

		RefreshableAnnotationConfigApplicationContext applicationContext =
			new RefreshableAnnotationConfigApplicationContext(contextConfiguration.getParentApplicationContext());

		prepareContext(applicationContext, contextConfiguration);

		return applicationContext;
	}

	private ConfigurableApplicationContext configure(ConfigurableApplicationContext applicationContext,
			MergedContextConfiguration contextConfiguration) {

		applicationContext = configureComponentClasses(applicationContext, contextConfiguration);
		applicationContext = configureScan(applicationContext, contextConfiguration);

		customizeContext(applicationContext, contextConfiguration);

		return applicationContext;
	}

	private ConfigurableApplicationContext configureComponentClasses(
			ConfigurableApplicationContext applicationContext, MergedContextConfiguration contextConfiguration) {

		Optional.ofNullable(applicationContext)
			.filter(it -> ArrayUtils.isNotEmpty(contextConfiguration.getClasses()))
			.filter(AnnotationConfigRegistry.class::isInstance)
			.map(AnnotationConfigRegistry.class::cast)
			.ifPresent(registry -> registry.register(contextConfiguration.getClasses()));

		return applicationContext;
	}

	private ConfigurableApplicationContext configureScan(ConfigurableApplicationContext applicationContext,
			MergedContextConfiguration contextConfiguration) {

		Optional.ofNullable(applicationContext)
			.filter(it -> ArrayUtils.isNotEmpty(contextConfiguration.getLocations()))
			.filter(AnnotationConfigRegistry.class::isInstance)
			.map(AnnotationConfigRegistry.class::cast)
			.ifPresent(registry -> registry.scan(contextConfiguration.getLocations()));

		return applicationContext;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public ApplicationContext loadContext(String... locations) {
		return loadContext(new MergedContextConfiguration(this.testClass, locations, new Class[0], new String[0],
			this));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected String getResourceSuffix() {
		return DEFAULT_RESOURCE_SUFFIX;
	}
}
