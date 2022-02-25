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
package org.springframework.geode.context.logging;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring {@link ApplicationListener} used to log the state of the Spring {@link Environment}
 * on the {@link ContextRefreshedEvent}.
 *
 * @author John Blum
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.event.ContextRefreshedEvent
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @since 1.4.0
 */
@SuppressWarnings("unused")
public class EnvironmentLoggingApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	protected static final String SYSTEM_ERR_ENABLED_PROPERTY =
		"spring.context.environment.logging.system-err.enabled";

	static final ThreadLocal<Environment> threadLocalEnvironmentReference = new ThreadLocal<>();

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @inheritDoc
	 */
	@Override
	public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {

		Environment environment = contextRefreshedEvent.getApplicationContext().getEnvironment();

		threadLocalEnvironmentReference.set(environment);

		try {
			log("ENV: [%s]", ObjectUtils.nullSafeClassName(environment));
			log("ENV: Active Profiles %s", Arrays.toString(environment.getActiveProfiles()));
			log("ENV: Default Profiles %s", Arrays.toString(environment.getDefaultProfiles()));

			if (environment instanceof ConfigurableEnvironment) {

				ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

				for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
					log("ENV: PropertySource [%s]", propertySource.getName());
					getCompositePropertySourceLoggingFunction().apply(propertySource);
				}
			}
		}
		finally {
			threadLocalEnvironmentReference.remove();
		}
	}

	/**
	 * Returns a {@literal Composite} {@link Function} capable of introspecting and logging properties
	 * from specifically typed {@link PropertySource PropertySources}.
	 *
	 * @return a {@literal Composite} {@link Function} capable of introspecting and logging the properties
	 * from specifically typed {@link PropertySource PropertySources}.
	 * @see org.springframework.core.env.PropertySource
	 * @see java.util.function.Function
	 */
	protected Function<PropertySource<?>, PropertySource<?>> getCompositePropertySourceLoggingFunction() {
		return new EnumerablePropertySourceLoggingFunction().andThen(new MapPropertySourceLoggingFunction());
	}

	/**
	 * Gets a reference to the configured SLF4J {@link Logger}.
	 *
	 * @return a reference to the configured SLF4J {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	/**
	 * Logs the given {@link String message} formatted with the given array of {@link Object arguments}
	 * to the configured Spring Boot application log.
	 *
	 * The given {@link String message} will only be logged if it contains text, otherwise this method does nothing
	 * and silently returns.
	 *
	 * @param message {@link String} containing the message to log.
	 * @param args optional array of {@link Object arguments} to apply when formatting the {@link String message}.
	 * @see #logToSlf4jLogger(String, Object...)
	 * @see #logToSystemErr(String, Object...)
	 */
	protected void log(String message, Object... args) {

		if (StringUtils.hasText(message)) {
			logToSlf4jLogger(message, args);
			logToSystemErr(message, args);
		}
	}

	/**
	 * Logs the given {@link String message} to the configured SLF4J {@link Logger}.
	 *
	 * @param message {@link String} containing the message to log.
	 * @param args optional array of {@link Object arguments} to apply when formatting the {@link String message}.
	 * @see #getLogger()
	 */
	void logToSlf4jLogger(String message, Object... args) {
		//getLogger().debug(String.format(message, args), args);
		getLogger().debug(String.format(message, args));
	}

	/**
	 * Logs the given {@link String message} to {@link System#err}.
	 *
	 * This logging method is available to perform poor mans logging when explicit SLF4J {@link Logger} configuration
	 * was not provided in the deployed Spring Boot application. However, in most cases, this logging method should not
	 * be used and proper SLF4J {@link Logger} configuration should be provided in most cases.
	 *
	 * This logging option is only enabled when the {@literal spring.context.environment.logging.system-err.enabled}
	 * property is set to {@literal true}.
	 *
	 * @param message {@link String} containing the message to log.
	 * @param args optional array of {@link Object arguments} to apply when formatting the {@link String message}.
	 */
	void logToSystemErr(String message, Object... args) {

		if (isSystemErrLoggingEnabled()) {
			message = message.trim().endsWith("%n") ? message : message.concat("%n");
			System.err.printf(message, args);
			System.err.flush();
		}
	}

	private boolean isSystemErrLoggingEnabled() {

		return Optional.ofNullable(threadLocalEnvironmentReference.get())
			.map(environment -> environment.getProperty(SYSTEM_ERR_ENABLED_PROPERTY, Boolean.class, false))
			.orElseGet(() -> Boolean.getBoolean(SYSTEM_ERR_ENABLED_PROPERTY));
	}

	protected abstract class AbstractPropertySourceLoggingFunction
			implements Function<PropertySource<?>, PropertySource<?>> {

		protected void logProperties(@NonNull Iterable<String> propertyNames,
				@NonNull Function<String, Object> propertyValueFunction) {

			log("Properties [");

			for (String propertyName : CollectionUtils.nullSafeIterable(propertyNames)) {
				log("\t%1$s = %2$s", propertyName, propertyValueFunction.apply(propertyName));
			}

			log("]");
		}
	}

	protected class EnumerablePropertySourceLoggingFunction extends AbstractPropertySourceLoggingFunction {

		@Override
		public @Nullable PropertySource<?> apply(@Nullable PropertySource<?> propertySource) {

			if (propertySource instanceof EnumerablePropertySource) {

				EnumerablePropertySource<?> enumerablePropertySource =
					(EnumerablePropertySource<?>) propertySource;

				String[] propertyNames = enumerablePropertySource.getPropertyNames();

				Arrays.sort(propertyNames);

				logProperties(Arrays.asList(propertyNames), enumerablePropertySource::getProperty);
			}

			return propertySource;
		}
	}

	// The PropertySource may not be enumerable but may use a Map as its source.
	protected class MapPropertySourceLoggingFunction extends AbstractPropertySourceLoggingFunction {

		@Override
		@SuppressWarnings("unchecked")
		public @Nullable PropertySource<?> apply(@Nullable PropertySource<?> propertySource) {

			if (!(propertySource instanceof EnumerablePropertySource)) {

				Object source = propertySource != null
					? propertySource.getSource()
					: null;

				if (source instanceof Map) {

					Map<String, Object> map = new TreeMap<>((Map<String, Object>) source);

					logProperties(map.keySet(), map::get);
				}
			}

			return propertySource;
		}
	}
}
