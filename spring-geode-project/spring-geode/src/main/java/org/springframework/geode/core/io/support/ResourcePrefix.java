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
package org.springframework.geode.core.io.support;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An enumeration of {@link Resource} {@link String prefixes} recognized by the Spring Framework.
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @since 1.3.1
 */
public enum ResourcePrefix {

	CLASSPATH_URL_PREFIX(ResourceLoader.CLASSPATH_URL_PREFIX),
	FILESYSTEM_URL_PREFIX("file:"),
	HTTP_URL_PREFIX("http:");

	public static final String RESOURCE_PATH_SEPARATOR = "/";

	/**
	 * Factory method used to try and find a {@link ResourcePrefix} enumerated value
	 * matching the given {@link String prefix}.
	 *
	 * @param prefix {@link String} with the name of the prefix.
	 * @return a {@link ResourcePrefix} matching the given {@link String prefix} by name, or {@literal null}
	 * if the {@link String prefix} does not match any {@link ResourcePrefix} enumerated value.
	 */
	public static @Nullable ResourcePrefix from(@Nullable String prefix) {

		if (StringUtils.hasText(prefix)) {

			prefix = prefix.trim().toLowerCase();

			for (ResourcePrefix resourcePrefix : values()) {
				if (resourcePrefix.toString().equals(prefix)) {
					return resourcePrefix;
				}
			}
		}

		return null;
	}

	private final String prefix;

	/**
	 * Constructs a new instance of {@link ResourcePrefix} initialized with the named {@link String prefix}.
	 *
	 * @param prefix {@link String name} of the prefix.
	 * @throws IllegalArgumentException if the {@link String prefix} is not specified.
	 */
	ResourcePrefix(String prefix) {
		Assert.hasText(prefix, "Resource prefix must be specified");
		this.prefix = prefix;
	}

	/**
	 * Gets the network protocol that this {@link ResourcePrefix} represents.
	 *
	 * @return the network protocol that this {@link ResourcePrefix} represents.
	 */
	public String getProtocol() {

		StringBuilder buffer = new StringBuilder();

		for (char character : this.prefix.toCharArray()) {
			if (Character.isAlphabetic(character)) {
				buffer.append(character);
			}
		}

		return buffer.toString();
	}

	/**
	 * Gets the {@link String pattern} or template used to construct a {@link java.net.URL} prefix
	 * from this {@link ResourcePrefix}.
	 *
	 * @return the {@link String pattern} or template used to construct a {@link java.net.URL} prefix
	 * from this {@link ResourcePrefix}.
	 * @see #toUrlPrefix()
	 */
	protected String getUrlPrefixPattern() {
		return this.equals(CLASSPATH_URL_PREFIX) ? "%s" : "%1$s%2$s%2$s";
	}

	@Override
	public String toString() {
		return this.prefix;
	}

	/**
	 * Gets the {@link ResourcePrefix} as a prefix use in a {@link java.net.URL}.
	 *
	 * @return the {@link java.net.URL} prefix.
	 * @see #getUrlPrefixPattern()
	 */
	public String toUrlPrefix() {
		return String.format(getUrlPrefixPattern(), toString(), RESOURCE_PATH_SEPARATOR);
	}
}
