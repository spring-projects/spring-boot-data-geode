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
package org.springframework.geode.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.core.util.SpringExtensions;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A {@literal refreshable} {@link ApplicationContext} capable of loading {@link Class component classes} used for
 * {@link Annotation} based configuration in addition to scanning {@link String configuration locations}, and then
 * providing the ability to reload/refresh the context at some point later during runtime.
 *
 * DISCLAIMER: Currently, this {@link ApplicationContext} implementation (and extension) is being used exclusively for
 * testing and experimental (R&D) purposes. It was designed around Apache Geode's forced-disconnect / auto-reconnect
 * functionality, providing support for this behavior inside a Spring context. Specifically, this concern is only
 * applicable when using Spring Boot to configure and bootstrap Apache Geode peer member
 * {@link org.apache.geode.cache.Cache} applications, such as when annotating your Spring Boot application with
 * SDG's {@link PeerCacheApplication} annotation. This {@link ApplicationContext} implementation is not recommended for
 * use in Production Systems/Applications (yet).
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.BeanNameGenerator
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.AnnotatedBeanDefinitionReader
 * @see org.springframework.context.annotation.AnnotationConfigRegistry
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 * @see org.springframework.context.annotation.ScopeMetadataResolver
 * @see org.springframework.context.support.AbstractRefreshableConfigApplicationContext
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class RefreshableAnnotationConfigApplicationContext extends AbstractRefreshableConfigApplicationContext
		implements AnnotationConfigRegistry {

	protected static final boolean DEFAULT_COPY_CONFIGURATION = false;
	protected static final boolean USE_DEFAULT_FILTERS = true;

	@Nullable
	private BeanNameGenerator beanNameGenerator;

	@Nullable
	private volatile DefaultListableBeanFactory beanFactory;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Set<String> basePackages = new LinkedHashSet<>();
	private final Set<Class<?>> componentClasses = new LinkedHashSet<>();

	@Nullable
	private ScopeMetadataResolver scopeMetadataResolver;

	// TODO: WARNING - Calling refreshBeanFactory() in the constructor to eagerly create a BeanFactory is problematic.
	//  However, it does follow the BeanFactory creation pattern used by the GenericApplicationContext (and extensions
	//  like AnnotationConfigApplicationContext) that this RefreshableAnnotationConfigApplicationContext implementation
	//  is trying to preserve. Although, the AnnotationConfigApplicationContext implementation is NOT refreshable either,
	//  yet the AnnotationConfigWebApplicationContext is but doesn't eagerly create a BeanFactory during construction,
	//  so... The main problem with eagerly creating the BeanFactory in the constructor is the BeanFactory will be
	//  closed and reconstructed on refresh, as well as on each refresh thereafter.

	/**
	 * Constructs an new instance of the {@link RefreshableAnnotationConfigApplicationContext}
	 * with default container state and no {@literal parent} {@link ApplicationContext}.
	 *
	 * @see #RefreshableAnnotationConfigApplicationContext(ApplicationContext)
	 */
	public RefreshableAnnotationConfigApplicationContext() {
		this(null);
	}

	/**
	 * Constructs a new instance of the {@link RefreshableAnnotationConfigApplicationContext} initialized with
	 * the {@literal parent} {@link ApplicationContext}.
	 *
	 * Additionally, this constructor eagerly initializes a {@link ConfigurableListableBeanFactory},
	 * unlike {@link org.springframework.context.support.AbstractRefreshableApplicationContext} implementations,
	 * but exactly like {@link org.springframework.context.support.GenericApplicationContext} implementations.
	 *
	 * @param parent parent {@link ApplicationContext} to this child context.
	 * @see org.springframework.context.ApplicationContext
	 * @see #refreshBeanFactory()
	 */
	public RefreshableAnnotationConfigApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
		refreshBeanFactory();
	}

	/**
	 * Configures the {@link BeanNameGenerator} strategy used by this {@link ApplicationContext} to generate
	 * {@link String bean names} for {@link BeanDefinition bean definitions}.
	 *
	 * @param beanNameGenerator {@link BeanNameGenerator} used to generate {@link String bean names}
	 * for {@link BeanDefinition bean definitions}.
	 * @see org.springframework.beans.factory.support.BeanNameGenerator
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	/**
	 * Returns the {@link Optional optionally} configured {@link BeanNameGenerator} strategy used by this
	 * {@link ApplicationContext} to generate {@link String bean names} for {@link BeanDefinition bean definitions}.
	 *
	 * @return the {@link BeanNameGenerator} strategy used to generate {@link String bean names}
	 * for {@link BeanDefinition bean definitions}.
	 * @see org.springframework.beans.factory.support.BeanNameGenerator
	 * @see java.util.Optional
	 */
	protected Optional<BeanNameGenerator> getBeanNameGenerator() {
		return Optional.ofNullable(this.beanNameGenerator);
	}

	/**
	 * Returns the configured {@link Logger} used to log framework messages to the application log.
	 *
	 * @return the configured {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	/**
	 * Configures the {@link ScopeMetadataResolver} strategy used by this {@link ApplicationContext} to resolve
	 * the {@literal scope} for {@link BeanDefinition bean definitions}.
	 *
	 * @param scopeMetadataResolver {@link ScopeMetadataResolver} used to resolve the {@literal scope}
	 * of {@link BeanDefinition bean definitions}.
	 * @see org.springframework.context.annotation.ScopeMetadataResolver
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}

	/**
	 * Returns the {@link Optional optionally} configured {@link ScopeMetadataResolver} strategy used by
	 * this {@link ApplicationContext} to resolve the {@literal scope} for {@link BeanDefinition bean definitions}.
	 *
	 * @return the configured {@link ScopeMetadataResolver} used to resolve the {@literal scope}
	 * for {@link BeanDefinition bean definitions}.
	 * @see org.springframework.context.annotation.ScopeMetadataResolver
	 * @see java.util.Optional
	 */
	public Optional<ScopeMetadataResolver> getScopeMetadataResolver() {
		return Optional.ofNullable(this.scopeMetadataResolver);
	}

	protected boolean isCopyConfigurationEnabled() {
		return DEFAULT_COPY_CONFIGURATION;
	}

	protected boolean isUsingDefaultFilters() {
		return USE_DEFAULT_FILTERS;
	}

	/**
	 * Loads {@link BeanDefinition BeanDefinitions} from Annotation configuration (component) classes
	 * as well as from other resource locations (e.g. XML).
	 *
	 * @param beanFactory {@link DefaultListableBeanFactory} to configure.
	 * @throws BeansException if loading and configuring the {@link BeanDefinition BeanDefintions} for the target
	 * {@link DefaultListableBeanFactory} fails.
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
	 * @see #newAnnotatedBeanDefinitionReader(BeanDefinitionRegistry)
	 * @see #newClassBeanDefinitionScanner(BeanDefinitionRegistry)
	 * @see #getConfigLocations()
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException {

		AnnotatedBeanDefinitionReader reader = configure(newAnnotatedBeanDefinitionReader(beanFactory));

		ClassPathBeanDefinitionScanner scanner = configure(newClassBeanDefinitionScanner(beanFactory));

		getBeanNameGenerator().ifPresent(beanNameGenerator -> {
			reader.setBeanNameGenerator(beanNameGenerator);
			scanner.setBeanNameGenerator(beanNameGenerator);
			beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
		});

		getScopeMetadataResolver().ifPresent(scopeMetadataResolver -> {
			reader.setScopeMetadataResolver(scopeMetadataResolver);
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		});

		Arrays.stream(ArrayUtils.nullSafeArray(getConfigLocations(), String.class)).forEach(configLocation -> {
			try {
				Class<?> type = ClassUtils.forName(configLocation, getClassLoader());
				getLogger().trace("Registering [{}]", configLocation);
				reader.register(type);
			}
			catch (ClassNotFoundException cause) {

				getLogger().trace(String.format("Could not load class for config location [%s] - trying package scan.",
					configLocation), cause);

				if (scanner.scan(configLocation) == 0) {
					getLogger().debug("No component classes found for specified class/package [{}]", configLocation);
				}
			}
		});
	}

	private AnnotatedBeanDefinitionReader configure(AnnotatedBeanDefinitionReader reader) {

		Set<Class<?>> componentClasses = this.componentClasses;

		if (!componentClasses.isEmpty()) {
			getLogger().debug("Registering component classes: {}", componentClasses);
			reader.register(ClassUtils.toClassArray(componentClasses));
		}

		return reader;
	}

	private ClassPathBeanDefinitionScanner configure(ClassPathBeanDefinitionScanner scanner) {

		Set<String> basePackages = this.basePackages;

		if (!basePackages.isEmpty()) {
			getLogger().debug("Scanning base packages: {}", basePackages);
			scanner.scan(StringUtils.toStringArray(basePackages));
		}

		return scanner;
	}

	protected AnnotatedBeanDefinitionReader newAnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		return new AnnotatedBeanDefinitionReader(registry, getEnvironment());
	}

	protected ClassPathBeanDefinitionScanner newClassBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		return new ClassPathBeanDefinitionScanner(registry, isUsingDefaultFilters(), getEnvironment());
	}

	/**
	 * Re-registers Singleton beans registered with the previous {@link ConfigurableListableBeanFactory BeanFactory}
	 * (prior to refresh) with this {@link ApplicationContext}, iff this context was previously active
	 * and subsequently refreshed.
	 *
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#copyConfigurationFrom(ConfigurableBeanFactory)
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#registerSingleton(String, Object)
	 * @see #getBeanFactory()
	 */
	@Override
	protected void onRefresh() {

		super.onRefresh();

		ConfigurableListableBeanFactory currentBeanFactory = getBeanFactory();

		if (this.beanFactory != null) {

			Arrays.stream(ArrayUtils.nullSafeArray(this.beanFactory.getSingletonNames(), String.class))
				.filter(singletonBeanName -> !currentBeanFactory.containsSingleton(singletonBeanName))
				.forEach(singletonBeanName -> currentBeanFactory
					.registerSingleton(singletonBeanName, this.beanFactory.getSingleton(singletonBeanName)));

			if (isCopyConfigurationEnabled()) {
				currentBeanFactory.copyConfigurationFrom(this.beanFactory);
			}
		}
	}

	/**
	 * Stores a reference to the previous {@link ConfigurableListableBeanFactory} in order to copy its configuration
	 * and state on {@link ApplicationContext} refresh invocations.
	 *
	 * @see #getBeanFactory()
	 */
	@Override
	protected void prepareRefresh() {
		this.beanFactory = (DefaultListableBeanFactory) SpringExtensions.safeGetValue(this::getBeanFactory);
		super.prepareRefresh();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void register(Class<?>... componentClasses) {

		Arrays.stream(ArrayUtils.nullSafeArray(componentClasses, Class.class))
			.filter(Objects::nonNull)
			.forEach(this.componentClasses::add);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void scan(String... basePackages) {

		Arrays.stream(ArrayUtils.nullSafeArray(basePackages, String.class))
			.filter(StringUtils::hasText)
			.forEach(this.basePackages::add);
	}
}
