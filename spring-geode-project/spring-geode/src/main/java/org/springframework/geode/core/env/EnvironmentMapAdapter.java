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
package org.springframework.geode.core.env;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newUnsupportedOperationException;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link Map} implementation adapting an {@link Environment} object in order to use the {@link Environment}
 * as a {@link Map}.
 *
 * @author John Blum
 * @see java.util.Map
 * @see java.util.AbstractMap
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see <a href="https://en.wikipedia.org/wiki/Adapter_pattern">Adapter Software Design Pattern</a>
 * @since 1.3.1
 */
public class EnvironmentMapAdapter extends AbstractMap<String, String> {

	/**
	 * Factory method used to construct an new instance of {@link EnvironmentMapAdapter} initialized with
	 * the given {@link Environment}.
	 *
	 * @param environment {@link Environment} to adapt; must not be {@literal null}.
	 * @return a new instance of {@link EnvironmentMapAdapter} for the given {@link Environment}.
	 * @throws IllegalArgumentException if {@link Environment} is {@literal null}.
	 * @see org.springframework.core.env.Environment
	 * @see #EnvironmentMapAdapter(Environment)
	 */
	public static EnvironmentMapAdapter from(@NonNull Environment environment) {
		return new EnvironmentMapAdapter(environment);
	}

	private final Environment environment;

	/**
	 * Constructs a new instance of {@link EnvironmentMapAdapter} initialized with the given {@link Environment}.
	 *
	 * @param environment {@link Environment} to adapt; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link Environment} is {@literal null}.
	 * @see org.springframework.core.env.Environment
	 */
	public EnvironmentMapAdapter(@NonNull Environment environment) {

		Assert.notNull(environment, "Environment must not be null");

		this.environment = environment;
	}

	/**
	 * Gets the configured {@link Environment} object being adapted by this {@link Map}.
	 *
	 * @return the configured {@link Environment}; never {@literal null}.
	 * @see org.springframework.core.env.Environment
	 */
	protected @NonNull Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * Null-safe method determining whether the given {@link Object key} is a property
	 * in the underlying {@link Environment}.
	 *
	 * @return a boolean value indicating whether the given {@link Object key} is a property
	 * in the underlying {@link Environment}.
	 * @see org.springframework.core.env.Environment#containsProperty(String)
	 * @see #getEnvironment()
	 */
	@Override
	public boolean containsKey(@Nullable Object key) {
		return key != null && getEnvironment().containsProperty(String.valueOf(key));
	}

	/**
	 * Gets the {@link String value} for the property identified by the given {@link Map} {@link Object key}
	 * from the underlying {@link Environment}.
	 *
	 * @param key {@link Object key} identifying the property whose value will be retrieved from the {@link Environment}.
	 * @return the {@link String value} of the property identified by the given {@link Map} {@link Object key}
	 * from the {@link Environment}.
	 * @see org.springframework.core.env.Environment#getProperty(String)
	 * @see #getEnvironment()
	 */
	@Override
	public @Nullable String get(@Nullable Object key) {
		return key != null ? getEnvironment().getProperty(String.valueOf(key)) : null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Set<Entry<String, String>> entrySet() {

		Environment environment = getEnvironment();

		if (environment instanceof ConfigurableEnvironment) {

			Set<Entry<String, String>> entrySet = new HashSet<>();

			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String propertyName : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
						entrySet.add(new EnvironmentEntry(environment, propertyName));
					}
				}
			}

			return Collections.unmodifiableSet(entrySet);
		}

		throw newUnsupportedOperationException("Unable to determine the entrySet from the Environment [%s]",
			getEnvironment().getClass().getName());
	}

	/**
	 * {@link EnvironmentEntry} is a {@code Map.Entry} implementation mapping an {@link Environment} property (key)
	 * to its value.
	 *
	 * @see java.util.Map.Entry
	 * @see org.springframework.core.env.Environment
	 */
	protected static class EnvironmentEntry implements Map.Entry<String, String> {

		private final Environment environment;

		private final String key;

		/**
		 * Constructs a new instance of {@link EnvironmentEntry} initialized with the given {@link Environment}
		 * and {@link String key} (property).
		 *
		 * @param environment {@link Environment} to which the {@link String key} belongs; must not be {@literal null}.
		 * @param key {@link String} referring to the property from the {@link Environment}; must not be {@literal null}.
		 * @throws IllegalArgumentException if the {@link Environment} or the {@link String key} is {@literal null}.
		 * @see org.springframework.core.env.Environment
		 */
		public EnvironmentEntry(@NonNull Environment environment, @NonNull String key) {

			Assert.notNull(environment, "Environment must not be null");
			Assert.hasText(key, () -> String.format("Key [%s] must be specified", key));

			this.environment = environment;
			this.key = key;
		}

		/**
		 * Returns the configured {@link Environment} to which this {@code Map.Entry} belongs.
		 *
		 * @return the configured {@link Environment}; never {@literal null}.
		 * @see org.springframework.core.env.Environment
		 */
		protected @NonNull Environment getEnvironment() {
			return this.environment;
		}

		/**
		 * Gets the {@link String key} (property) of this {@code Map.Entry}.
		 *
		 * @return the {@link String key} (property) of this {@code Map.Entry}.
		 */
		@Override
		public @NonNull String getKey() {
			return this.key;
		}

		/**
		 * Gets the {@link String value} mapped to the {@link #getKey() key} (property) in this {@code Map.Entry}
		 * ({@link Environment}).
		 *
		 * @return the {@link String value} mapped to the {@link #getKey() key} (property) in this {@code Map.Entry}
		 * ({@link Environment}).
		 * @see org.springframework.core.env.Environment#getProperty(String)
		 * @see #getEnvironment()
		 * @see #getKey()
		 */
		@Override
		public @Nullable String getValue() {
			return getEnvironment().getProperty(getKey());
		}

		/**
		 * @inheritDoc
		 * @throws UnsupportedOperationException
		 */
		@Override
		public String setValue(String value) {
			throw newUnsupportedOperationException("Setting the value of Environment property [%s] is not supported",
				getKey());
		}
	}
}
