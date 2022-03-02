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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Abstract utility class containing functionality to work with {@link Resource Resources}.
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.WritableResource
 * @since 1.3.1
 */
public abstract class ResourceUtils {

	/**
	 * Returns the {@link Resource} as a {@link WritableResource} if possible.
	 *
	 * This method makes a best effort to determine whether the target {@link Resource} is actually {@literal writable}.
	 * Even still, it may be possible that a write to the target {@link Resource} will fail.
	 *
	 * The {@link Resource} is {@literal writable} if the {@link Resource} is an instance of {@link WritableResource}
	 * and {@link WritableResource#isWritable()} returns {@literal true}.
	 *
	 * @param resource {@link Resource} to cast to a {@link WritableResource}.
	 * @return a {@link WritableResource} from the target {@link Resource} if possible; never {@literal null}.
	 * @throws IllegalStateException if the target {@link Resource} is not {@literal writable}.
	 * @see org.springframework.core.io.WritableResource
	 * @see org.springframework.core.io.Resource
	 */
	public static @NonNull WritableResource asStrictlyWritableResource(@Nullable Resource resource) {

		return Optional.ofNullable(resource)
			.filter(WritableResource.class::isInstance)
			.map(WritableResource.class::cast)
			.filter(WritableResource::isWritable)
			.orElseThrow(() -> newIllegalStateException("Resource [%s] is not writable",
				ResourceUtils.nullSafeGetDescription(resource)));
	}

	/**
	 * {@link Optional Optionally} return the {@link Resource} as a {@link WritableResource}.
	 *
	 * The {@link Resource} must be an instance of {@link WritableResource}.
	 *
	 * @param resource {@link Resource} to cast to a {@link WritableResource}.
	 * @return the {@link Resource} as a {@link WritableResource} if the {@link Resource}
	 * is an instance of {@link WritableResource}, otherwise returns {@link Optional#empty()}.
	 * @see org.springframework.core.io.WritableResource
	 * @see org.springframework.core.io.Resource
	 * @see java.util.Optional
	 */
	public static Optional<WritableResource> asWritableResource(@Nullable Resource resource) {

		return Optional.ofNullable(resource)
			.filter(WritableResource.class::isInstance)
			.map(WritableResource.class::cast);
	}

	/**
	 * Determines whether the given byte array is {@literal null} or {@literal empty}.
	 *
	 * @param array byte array to evaluate.
	 * @return a boolean value indicating whether the given byte array is {@literal null} or {@literal empty}.
	 */
	public static boolean isNotEmpty(@Nullable byte[] array) {
		return array != null && array.length > 0;
	}

	/**
	 * Null-safe operation to determine whether the given {@link Resource} is readable.
	 *
	 * @param resource {@link Resource} to evaluate.
	 * @return a boolean value indicating whether the given {@link Resource} is readable.
	 * @see org.springframework.core.io.Resource#isReadable()
	 * @see org.springframework.core.io.Resource
	 */
	public static boolean isReadable(@Nullable Resource resource) {
		return resource != null && resource.isReadable();
	}

	/**
	 * Null-safe operation to determine whether the given {@link Resource} is writable.
	 *
	 * @param resource {@link Resource} to evaluate.
	 * @return a boolean value indicating whether the given {@link Resource} is writable.
	 * @see org.springframework.core.io.WritableResource#isWritable()
	 * @see org.springframework.core.io.WritableResource
	 * @see org.springframework.core.io.Resource
	 */
	public static boolean isWritable(@Nullable Resource resource) {
		return resource instanceof WritableResource && ((WritableResource) resource).isWritable();
	}

	/**
	 * Null-safe method to get the {@link Resource#getDescription() description} of the given {@link Resource}.
	 *
	 * @param resource {@link Resource} to describe.
	 * @return a {@link Resource#getDescription() description} of the {@link Resource}, or {@literal null}
	 * if the {@link Resource} handle is {@literal null}.
	 * @see org.springframework.core.io.Resource
	 */
	public static @Nullable String nullSafeGetDescription(@Nullable Resource resource) {
		return resource != null ? resource.getDescription() : null;
	}
}
