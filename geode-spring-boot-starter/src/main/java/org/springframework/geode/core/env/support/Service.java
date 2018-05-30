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

package org.springframework.geode.core.env.support;

import org.springframework.util.Assert;

/**
 * The {@link Service} class is an Abstract Data Type (ADT) modeling a Pivotal CloudFoundry Service.
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Service {

	/**
	 * Factory method to construct a new {@link Service} initialized with a {@link String name}.
	 *
	 * @param name {@link String} containing the name of the {@link Service}.
	 * @return a new {@link Service} configured with the given {@link String name}.
	 * @throws IllegalArgumentException if the {@link String name} is {@literal null} or empty.
	 * @see #Service(String)
	 */
	public static Service with(String name) {
		return new Service(name);
	}

	private final String name;

	/**
	 * Constructs a new {@link Service} initialized with a {@link String name}.
	 *
	 * @param name {@link String} containing the name of the {@link Service}.
	 * @throws IllegalArgumentException if the {@link String name} is {@literal null} or empty.
	 */
	Service(String name) {

		Assert.hasText(name, String.format("Service name [%s] is required", name));

		this.name = name;
	}

	/**
	 * Returns the {@link String name} of this {@link Service}.
	 *
	 * @return this {@link Service Service's} {@link String name}.
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return getName();
	}
}
