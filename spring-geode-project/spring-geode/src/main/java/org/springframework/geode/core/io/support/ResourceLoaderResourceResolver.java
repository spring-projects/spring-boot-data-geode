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

import static org.springframework.geode.core.util.ObjectUtils.initialize;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.geode.core.io.ResourceNotFoundException;
import org.springframework.geode.core.io.ResourceResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link ResourceResolver} implementation using Spring's {@link ResourceLoader} to resolve
 * and load {@link Resource Resources}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.geode.core.io.ResourceResolver
 * @since 1.3.1
 */
public class ResourceLoaderResourceResolver implements ResourceLoaderAware, ResourceResolver {

	private final AtomicReference<ResourceLoader> resolvedResourceLoader = new AtomicReference<>(null);

	/**
	 * Gets an {@link Optional} {@link ClassLoader} used by the {@link ResourceLoader} to resolve and load
	 * {@link Resource Resources} located on the {@literal classpath}.
	 *
	 * Returns the {@link ResourceLoader#getClassLoader() ClassLoader} from the configured {@link ResourceLoader},
	 * if present.  Otherwise, returns a {@link ClassLoader} determined by {@link ClassUtils#getDefaultClassLoader()},
	 * which first tries to return the {@link Thread#getContextClassLoader()}, then {@link Class#getClassLoader()},
	 * and finally, {@link ClassLoader#getSystemClassLoader()}.
	 *
	 * @return an {@link Optional} {@link ClassLoader} to resolve and load {@link Resource Resources}.
	 * @see org.springframework.core.io.ResourceLoader#getClassLoader()
	 * @see java.lang.ClassLoader
	 * @see java.util.Optional
	 */
	@Override
	public Optional<ClassLoader> getClassLoader() {

		return Optional.ofNullable(Optional.ofNullable(this.resolvedResourceLoader.get())
			.map(ResourceLoader::getClassLoader)
			.orElseGet(ClassUtils::getDefaultClassLoader));
	}

	/**
	 * Configures the {@link ResourceLoader} used by this {@link ResourceResolver} to resolve and load
	 * {@link Resource Resources}.
	 *
	 * @param resourceLoader {@link ResourceLoader} used to resolve and load {@link Resource Resources}.
	 * @see org.springframework.core.io.ResourceLoader
	 */
	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resolvedResourceLoader.set(resourceLoader);
	}

	/**
	 * Returns a reference to the configured {@link ResourceLoader} used to load {@link Resource Resources}.
	 *
	 * If a {@link ResourceLoader} was not explicitly configured, then a {@literal default} {@link ResourceLoader}
	 * using a {@literal default} {@link ClassLoader} is provided.
	 *
	 * @return a reference to the configured {@link ResourceLoader}; never {@literal null}.
	 * @see org.springframework.core.io.ResourceLoader
	 * @see #newResourceLoader()
	 */
	protected @NonNull ResourceLoader getResourceLoader() {
		return this.resolvedResourceLoader.updateAndGet(resourceLoader ->
			initialize(resourceLoader, this::newResourceLoader));
	}

	/**
	 * Constructs a new, {@literal default} instance of {@link ResourceLoader} to load {@link Resource Resources}.
	 *
	 * Specifically, creates a standalone {@link DefaultResourceLoader} initialized with a {@literal default}
	 * {@link ClassLoader} as determined by {@link #getClassLoader()}.
	 *
	 * @return a new, {@literal default} instance of {@link ResourceLoader}.
	 * @see org.springframework.core.io.ResourceLoader
	 */
	protected @NonNull ResourceLoader newResourceLoader() {

		return getClassLoader()
			.map(DefaultResourceLoader::new)
			.orElseGet(DefaultResourceLoader::new);
	}

	/**
	 * Constructs a new {@link Resource} handle at the given {@link String location}.
	 *
	 * By default, a {@link ClassPathResource} is constructed.
	 *
	 * @param location {@link String location} of the new {@link Resource}; must not be {@literal null}.
	 * @return a new {@link Resource} handle at the given {@link String location}.
	 * @throws IllegalArgumentException if {@link String location} is not specified.
	 * @see org.springframework.core.io.Resource
	 */
	protected @NonNull Resource newResource(@NonNull String location) {

		Assert.hasText(location, () ->
			String.format("The location [%s] of the Resource must be specified", location));

		return new ClassPathResource(location);
	}

	/**
	 * Determines whether the {@link Resource} is a {@literal qualified} {@link Resource}.
	 *
	 * Qualifications are determined by the application Requirements and Use Case (UC) at time of resolution.
	 * For example, it maybe that the {@link Resource} must {@link Resource#exists() exist} to qualify, or that
	 * the {@link Resource} must have a valid protocol, path and name.
	 *
	 * This default implementation requires the target {@link Resource} to not be {@literal null}.
	 *
	 * @param resource {@link Resource} to qualify.
	 * @return a boolean value indicating whether the {@link Resource} is qualified.
	 * @see org.springframework.core.io.Resource
	 */
	protected boolean isQualified(@Nullable Resource resource) {
		return resource != null;
	}

	/**
	 * Action to perform when the {@link Resource} identified at the specified {@link String location} is missing,
	 * or was not {@link #isQualified(Resource) qualified}.
	 *
	 * @param resource missing {@link Resource}.
	 * @param location {@link String} containing the location identifying the missing {@link Resource}.
	 * @throws ResourceNotFoundException if the {@link Resource} cannot be found at the specified {@link String location}.
	 * @return a different {@link Resource}, possibly.  Alternatively, this method may throw
	 * a {@link ResourceNotFoundException}.
	 * @see #isQualified(Resource)
	 */
	protected @Nullable Resource onMissingResource(@Nullable Resource resource, @NonNull String location) {
		throw new ResourceNotFoundException(String.format("Failed to resolve Resource [%1$s] at location [%2$s]",
			ResourceUtils.nullSafeGetDescription(resource), location));
	}

	/**
	 * Method used by subclasses to process the loaded {@link Resource} as determined by
	 * the {@link #getResourceLoader() ResourceLoader}.
	 *
	 * @param resource {@link Resource} to post-process.
	 * @return the {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	protected Resource postProcess(Resource resource) {
		return resource;
	}

	/**
	 * Tries to resolve a {@link Resource} at the given {@link String location} using a Spring {@link ResourceLoader},
	 * such as a Spring {@link ApplicationContext}.
	 *
	 * The targeted, identified {@link Resource} can be further {@link #isQualified(Resource) qualified} by subclasses
	 * based on application requirements or use case (UC).
	 *
	 * In the event that a {@link Resource} cannot be identified at the given {@link String location}, then applications
	 * have 1 last opportunity to handle the missing {@link Resource} event, and either return a different or default
	 * {@link Resource} or throw a {@link ResourceNotFoundException}.
	 *
	 * @param location {@link String location} identifying the {@link Resource} to resolve;
	 * must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link String location} is not specified.
	 * @return an {@link Optional} {@link Resource} handle for the given {@link String location}.
	 * @see org.springframework.core.io.Resource
	 * @see #isQualified(Resource)
	 * @see #onMissingResource(Resource, String)
	 * @see #postProcess(Resource)
	 * @see java.util.Optional
	 */
	@Override
	public Optional<Resource> resolve(@NonNull String location) {

		Assert.hasText(location, () ->
			String.format("The location [%s] of the Resource to resolve must be specified", location));

		Resource resource = getResourceLoader().getResource(location);

		resource = postProcess(resource);

		Resource resolvedResource = isQualified(resource)
			? resource
			: onMissingResource(resource, location);

		return Optional.ofNullable(resolvedResource);
	}
}
