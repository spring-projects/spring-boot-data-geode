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
import org.springframework.core.io.WritableResource;
import org.springframework.lang.NonNull;

/**
 * Interface (contract) for writers to define the algorithm or strategy for writing data to a target {@link Resource},
 * such as by using the {@link WritableResource WritableResource's}
 * {@link WritableResource#getOutputStream()} OutputStream}.
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.WritableResource
 * @since 1.3.1
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ResourceWriter {

	/**
	 * Writes data to the target {@link Resource} as defined by the algorithm/strategy of this writer.
	 *
	 * This method should throw an {@link UnhandledResourceException} if the algorithm or strategy used by this writer
	 * is not able to or capable of writing to the {@link Resource} at its location. This allows subsequent writers
	 * in a composition to possibly handle the {@link Resource}. Any other {@link Exception} thrown by this
	 * {@code write} method will break the chain of write calls in the composition.
	 *
	 * @param resource {@link Resource} to write data to.
	 * @param data array of bytes containing the data to write to the target {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	void write(@NonNull Resource resource, byte[] data);

	/**
	 * Writes data contained in the {@link ByteBuffer} to the target {@link Resource} as defined by
	 * the algorithm/strategy of this writer.
	 *
	 * This method should throw an {@link UnhandledResourceException} if the algorithm or strategy used by this writer
	 * is not able to or capable of writing to the {@link Resource} at its location. This allows subsequent writers
	 * in a composition to possibly handle the {@link Resource}. Any other {@link Exception} thrown by this
	 * {@code write} method will break the chain of write calls in the composition.
	 *
	 * @param resource {@link Resource} to write data to.
	 * @param data {@link ByteBuffer} containing the data to write to the target {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 * @see java.nio.ByteBuffer
	 * @see #write(Resource, byte[])
	 */
	default void write (@NonNull Resource resource, ByteBuffer data) {
		write(resource, data.array());
	}

	/**
	 * Composes this {@link ResourceWriter} with the given {@link ResourceWriter}
	 * using the {@literal Composite Software Design Pattern}.
	 *
	 * @param writer {@link ResourceWriter} to compose with this writer.
	 * @return a composite {@link ResourceWriter} composed of this {@link ResourceWriter}
	 * and the given {@link ResourceWriter}. If the given {@link ResourceWriter} is {@literal null},
	 * then this {@link ResourceWriter} is returned.
	 * @see <a href="https://en.wikipedia.org/wiki/Composite_pattern">Compsite Software Design Pattern</a>
	 * @see ResourceWriter
	 */
	default ResourceWriter thenWriteTo(ResourceWriter writer) {

		return writer == null ? this
			: (resource, data) -> {
				try {
					this.write(resource, data);
				}
				catch (UnhandledResourceException ignore) {
					writer.write(resource, data);
				}
			};
	}
}
