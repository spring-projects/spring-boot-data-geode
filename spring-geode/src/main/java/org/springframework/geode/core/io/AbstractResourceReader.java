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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.geode.core.io.support.ResourceUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Abstract base class providing functionality common to all {@link ResourceReader} implementations.
 *
 * @author John Blum
 * @see java.io.InputStream
 * @see org.springframework.core.io.Resource
 * @see ResourceReader
 * @since 1.3.1
 */
public abstract class AbstractResourceReader implements ResourceReader {

	/**
	 * @inheritDoc
	 */
	@Override
	public @NonNull byte[] read(@NonNull Resource resource) {

		return Optional.ofNullable(resource)
			.filter(this::isAbleToHandle)
			.map(this::preProcess)
			.map(it -> {
				try (InputStream in = it.getInputStream()) {
					return doRead(in);
				}
				catch (IOException cause) {
					throw new ResourceReadException(String.format("Failed to read from Resource [%s]",
						it.getDescription()), cause);
				}
			})
			.orElseThrow(() -> new UnhandledResourceException(String.format("Unable to handle Resource [%s]",
				ResourceUtils.nullSafeGetDescription(resource))));
	}

	/**
	 * Determines whether this reader is able to handle and read from the target {@link Resource}.
	 *
	 * The default implementation determines that the {@link Resource} can be handled if the {@link Resource} handle
	 * is not {@literal null}.
	 *
	 * @param resource {@link Resource} to evaluate.
	 * @return a boolean value indicating whether this reader is able to handle and read from
	 * the target {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	@SuppressWarnings("unused")
	protected boolean isAbleToHandle(@Nullable Resource resource) {
		return resource != null;
	}

	/**
	 * Reads data from the target {@link Resource} (intentionally) by using the {@link InputStream} returned by
	 * {@link Resource#getInputStream()}.
	 *
	 * However, other algorithm/strategy implementations are free to read from the {@link Resource} as is appropriate
	 * for the given context (e.g. cloud environment).  In those cases, implementors should override
	 * the {@link #read(Resource)} method.
	 *
	 * @param resourceInputStream {@link InputStream} used to read data from the target {@link Resource}.
	 * @return a {@literal non-null} byte array containing the data from the target {@link Resource}.
	 * @throws IOException if an I/O error occurs while reading from the {@link Resource}.
	 * @see java.io.InputStream
	 * @see #read(Resource)
	 */
	protected abstract @NonNull byte[] doRead(@NonNull InputStream resourceInputStream) throws IOException;

	/**
	 * Pre-processes the target {@link Resource} before reading from the {@link Resource}.
	 *
	 * @param resource {@link Resource} to pre-process; never {@literal null}.
	 * @return the given, target {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	protected @NonNull Resource preProcess(@NonNull Resource resource) {
		return resource;
	}
}
