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

import java.nio.ByteBuffer;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Interface (contract) for readers to define the algorithm and strategy for reading data from a {@link Resource},
 * such as by using the {@link Resource Resource's} {@link Resource#getInputStream() InputStream}.
 *
 * @author John Blum
 * @see java.nio.ByteBuffer
 * @see org.springframework.core.io.Resource
 * @since 1.3.1
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ResourceReader {

	/**
	 * Reads data from the {@literal non-null} {@link Resource} into a byte array.
	 *
	 * This method should throw an {@link UnhandledResourceException} if the algorithm and strategy used by this reader
	 * is not able to or capable of reading from the {@link Resource} at its location. This allows subsequent readers
	 * in a composition to possibly handle the {@link Resource}. Any other {@link Throwable} thrown by this {@code read}
	 * method will break the chain of read calls in the composition.
	 *
	 * @param resource {@link Resource} to read data from.
	 * @return a {@literal non-null} byte array containing the data from the {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	@NonNull byte[] read(@NonNull Resource resource);

	/**
	 * Reads data from the {@literal non-null} {@link Resource} into a {@link ByteBuffer}.
	 *
	 * @param resource {@link Resource} to read data from.
	 * @return a {@literal non-null} {@link ByteBuffer} containing the data from the {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 * @see java.nio.ByteBuffer
	 * @see #read(Resource)
	 */
	default @NonNull ByteBuffer readIntoByteBuffer(@NonNull Resource resource) {
		return ByteBuffer.wrap(read(resource));
	}

	/**
	 * Composes this {@link ResourceReader} with the given {@link ResourceReader}
	 * using the {@literal Composite Software Design Pattern}.
	 *
	 * @param reader {@link ResourceReader} to compose with this reader.
	 * @return a composite {@link ResourceReader} composed of this {@link ResourceReader}
	 * and the given {@link ResourceReader}. If the given {@link ResourceReader} is {@literal null},
	 * then this {@link ResourceReader} is returned.
	 * @see <a href="https://en.wikipedia.org/wiki/Composite_pattern">Compsite Software Design Pattern</a>
	 * @see ResourceReader
	 */
	default @NonNull ResourceReader thenReadFrom(@Nullable ResourceReader reader) {

		return reader == null ? this
			: resource -> {
				try {
					return this.read(resource);
				}
				catch (UnhandledResourceException ignore) {
					return reader.read(resource);
				}
			};
	}
}
