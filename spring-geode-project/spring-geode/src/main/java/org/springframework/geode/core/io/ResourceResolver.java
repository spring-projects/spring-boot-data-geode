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
package org.springframework.geode.core.io;

import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

/**
 * Interface defining a contract encapsulating an algorithm/strategy for resolving {@link Resource Resources}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.springframework.core.io.Resource
 * @since 1.3.1.
 */
@FunctionalInterface
public interface ResourceResolver {

	/**
	 * Gets the {@link ClassLoader} used by this {@link ResourceResolver} to resolve {@literal classpath}
	 * {@link Resource Resources}.
	 *
	 * By default, this method will return a {@link ClassLoader} determined by {@link ClassUtils#getDefaultClassLoader()},
	 * which first tries to return the {@link Thread#getContextClassLoader()}, then {@link Class#getClassLoader()},
	 * and finally, {@link ClassLoader#getSystemClassLoader()}.
	 *
	 * @return an {@link Optional} {@link ClassLoader} used to resolve {@literal classpath} {@link Resource Resources}.
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see java.lang.ClassLoader
	 * @see java.util.Optional
	 */
	default Optional<ClassLoader> getClassLoader() {
		return Optional.ofNullable(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Tries to resolve a {@link Resource} handle from the given, {@literal non-null} {@link String location}
	 * (e.g. {@link String filesystem path}).
	 *
	 * @param location {@link String location} identifying the {@link Resource} to resolve;
	 * must not be {@literal null}.
	 * @return an {@link Optional} {@link Resource} handle for the given {@link String location}.
	 * @see org.springframework.core.io.Resource
	 * @see java.util.Optional
	 */
	Optional<Resource> resolve(@NonNull String location);

	/**
	 * Returns a {@literal non-null}, {@literal existing} {@link Resource} handle resolved from the given,
	 * {@literal non-null} {@link String location} (e.g. {@link String filesystem path}).
	 *
	 * @param location {@link String location} identifying the {@link Resource} to resolve;
	 * must not be {@literal null}.
	 * @return a {@literal non-null}, {@literal existing} {@link Resource} handle for
	 * the resolved {@link String location}.
	 * @throws ResourceNotFoundException if a {@link Resource} cannot be resolved from the given {@link String location}.
	 * A {@link Resource} is unresolvable if the given {@link String location} does not exist (physically);
	 * see {@link Resource#exists()}.
	 * @see org.springframework.core.io.Resource
	 * @see #resolve(String)
	 */
	default @NonNull Resource require(@NonNull String location) {
		return resolve(location)
			.filter(Resource::exists)
			.orElseThrow(() -> new ResourceNotFoundException(String.format("Resource [%s] does not exist", location)));
	}
}
