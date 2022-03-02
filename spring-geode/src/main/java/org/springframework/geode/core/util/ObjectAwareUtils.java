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
package org.springframework.geode.core.util;

import java.util.function.Consumer;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Abstract utility class used to process {@link Object managed object} {@literal aware} {@link Object objects},
 * such as {@link ApplicationContextAware} {@link Object objects} in a Spring context.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @see org.springframework.beans.factory.BeanClassLoaderAware
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ResourceLoaderAware
 * @since 1.3.1
 */
public abstract class ObjectAwareUtils {

	public static final Consumer<Object> NO_OP = target -> { };

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link ApplicationContextAware} {@link Object}
	 * with the given {@link ApplicationContext}.
	 *
	 * The {@link ApplicationContextAware#setApplicationContext(ApplicationContext)} method is only called on
	 * the {@link ApplicationContextAware} {@link Object} if the {@link ApplicationContext} is not {@literal null}.
	 *
	 * @param applicationContext {@link ApplicationContext} set on the {@link ApplicationContextAware} {@link Object}
	 * by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link ApplicationContextAware} {@link Object}
	 * with the given {@link ApplicationContext}; never {@literal null}.
	 * @see org.springframework.context.ApplicationContextAware
	 * @see org.springframework.context.ApplicationContext
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> applicationContextAwareObjectInitializer(
			@Nullable ApplicationContext applicationContext) {

		return applicationContext == null ? NO_OP : target -> {
			if (target instanceof ApplicationContextAware) {
				((ApplicationContextAware) target).setApplicationContext(applicationContext);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link ApplicationEventPublisherAware} {@link Object}
	 * with the given {@link ApplicationEventPublisher}.
	 *
	 * The {@link ApplicationEventPublisherAware#setApplicationEventPublisher(ApplicationEventPublisher)} method is only
	 * called on the {@link ApplicationEventPublisherAware} {@link Object} if the {@link ApplicationEventPublisherAware}
	 * is not {@literal null}.
	 *
	 * @param applicationEventPublisher {@link ApplicationEventPublisher} set on
	 * the {@link ApplicationEventPublisherAware} {@link Object} by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link ApplicationEventPublisherAware} {@link Object}
	 * with the given {@link ApplicationEventPublisher}; never {@literal null}.
	 * @see org.springframework.context.ApplicationEventPublisherAware
	 * @see org.springframework.context.ApplicationEventPublisher
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> applicationEventPublisherAwareObjectInitializer(
			@Nullable ApplicationEventPublisher applicationEventPublisher) {

		return applicationEventPublisher == null ? NO_OP : target -> {
			if (target instanceof ApplicationEventPublisherAware) {
				((ApplicationEventPublisherAware) target).setApplicationEventPublisher(applicationEventPublisher);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link BeanClassLoaderAware} {@link Object}
	 * with the given bean {@link ClassLoader}.
	 *
	 * The {@link BeanClassLoaderAware#setBeanClassLoader(ClassLoader)} method is only called on
	 * the {@link BeanClassLoaderAware} {@link Object} if the {@link ClassLoader} is not {@literal null}.
	 *
	 * @param beanClassLoader {@link ClassLoader} set on the {@link BeanClassLoaderAware} {@link Object}
	 * by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link BeanClassLoaderAware} {@link Object}
	 * with the given bean {@link ClassLoader}; never {@literal null}.
	 * @see org.springframework.beans.factory.BeanClassLoaderAware
	 * @see java.util.function.Consumer
	 * @see java.lang.ClassLoader
	 */
	public static @NonNull Consumer<Object> beanClassLoaderAwareObjectInitializer(@Nullable ClassLoader beanClassLoader) {

		return beanClassLoader == null ? NO_OP : target -> {
			if (target instanceof BeanClassLoaderAware) {
				((BeanClassLoaderAware) target).setBeanClassLoader(beanClassLoader);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link BeanFactoryAware} {@link Object}
	 * with the given {@link BeanFactory}.
	 *
	 * The {@link BeanFactoryAware#setBeanFactory(BeanFactory)} method is only called on the {@link BeanFactoryAware}
	 * {@link Object} if the {@link BeanFactory} is not {@literal null}.
	 *
	 * @param beanFactory {@link BeanFactory} set on the {@link BeanFactoryAware} {@link Object} by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link BeanFactoryAware} {@link Object}
	 * with the given {@link BeanFactory}; never {@literal null}.
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.beans.factory.BeanFactory
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> beanFactoryAwareObjectInitializer(@Nullable BeanFactory beanFactory) {

		return beanFactory == null ? NO_OP : target -> {
			if (target instanceof BeanFactoryAware) {
				((BeanFactoryAware) target).setBeanFactory(beanFactory);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link BeanNameAware} {@link Object}
	 * with the given {@link String bean name}.
	 *
	 * The {@link BeanNameAware#setBeanName(String)} method is only called on the {@link BeanNameAware} {@link Object}
	 * if the {@link String bean name} is not {@literal null}.
	 *
	 * @param beanName {@link String} containing the bean name to set on the {@link BeanNameAware} {@link Object}
	 * by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link BeanNameAware} {@link Object}
	 * with the given {@link String bean name}; never {@literal null}.
	 * @see org.springframework.beans.factory.BeanNameAware
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> beanNameAwareObjectInitializer(@Nullable String beanName) {

		return hasNoText(beanName) ? NO_OP : target -> {
			if (target instanceof BeanNameAware) {
				((BeanNameAware) target).setBeanName(beanName);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link EnvironmentAware} {@link Object}
	 * with the given {@link Environment}.
	 *
	 * The {@link EnvironmentAware#setEnvironment(Environment)} method is only called on the {@link EnvironmentAware}
	 * {@link Object} if the {@link Environment} is not {@literal null}.
	 *
	 * @param environment {@link Environment} set on the {@link EnvironmentAware} {@link Object} by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link EnvironmentAware} {@link Object}
	 * with the given {@link Environment}; never {@literal null}.
	 * @see org.springframework.context.EnvironmentAware
	 * @see org.springframework.core.env.Environment
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> environmentAwareObjectInitializer(@Nullable Environment environment) {

		return environment == null ? NO_OP : target -> {
			if (target instanceof EnvironmentAware) {
				((EnvironmentAware) target).setEnvironment(environment);
			}
		};
	}

	/**
	 * Returns a {@link Consumer} capable of initializing an {@link ResourceLoaderAware} {@link Object}
	 * with the given {@link ResourceLoader}.
	 *
	 * The {@link ResourceLoaderAware#setResourceLoader(ResourceLoader)} method is only called on
	 * the {@link ResourceLoaderAware} {@link Object} if the {@link ResourceLoader} is not {@literal null}.
	 *
	 * @param resourceLoader {@link ResourceLoader} set on the {@link ResourceLoaderAware} {@link Object}
	 * by the {@link Consumer}.
	 * @return a {@link Consumer} capable of initializing an {@link ResourceLoaderAware} {@link Object}
	 * with the given {@link ResourceLoader}; never {@literal null}.
	 * @see org.springframework.context.ResourceLoaderAware
	 * @see org.springframework.core.io.ResourceLoader
	 * @see java.util.function.Consumer
	 */
	public static @NonNull Consumer<Object> resourceLoaderAwareObjectInitializer(@Nullable ResourceLoader resourceLoader) {

		return resourceLoader == null ? NO_OP : target -> {
			if (target instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) target).setResourceLoader(resourceLoader);
			}
		};
	}

	private static boolean hasNoText(@Nullable String value) {
		return !StringUtils.hasText(value);
	}
}
