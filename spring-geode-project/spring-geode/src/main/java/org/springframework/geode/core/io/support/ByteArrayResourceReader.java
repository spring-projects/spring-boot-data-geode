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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.geode.core.io.AbstractResourceReader;
import org.springframework.lang.NonNull;

/**
 * A concrete {@link AbstractResourceReader} implementation that reads data from a target {@link Resource Resource's}
 * {@link Resource#getInputStream() InputStream} into a byte array.
 *
 * @author John Blum
 * @see java.io.InputStream
 * @see java.io.ByteArrayOutputStream
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.AbstractResourceReader
 * @since 1.3.1
 */
@SuppressWarnings("unused")
public class ByteArrayResourceReader extends AbstractResourceReader {

	protected static final int DEFAULT_BUFFER_SIZE = 32768;

	/**
	 * Returns the required {@link Integer#TYPE buffer size} used to capture data from the target {@link Resource}
	 * in chunks.
	 *
	 * Subclasses are encouraged to override this method as necessary to tune the buffer size.  By default, the
	 * buffer size is {@literal 32K} or {@literal 32768} bytes.
	 *
	 * @return the required {@link Integer#TYPE buffer size} to read from the {@link Resource} in chunks.
	 */
	protected int getBufferSize() {
		return DEFAULT_BUFFER_SIZE;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected @NonNull byte[] doRead(@NonNull InputStream resourceInputStream) throws IOException {

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(resourceInputStream.available())) {

			byte[] buffer = new byte[getBufferSize()];

			for (int bytesRead = resourceInputStream.read(buffer); bytesRead != -1; bytesRead = resourceInputStream.read(buffer)) {
				// using a buffer will trigger a flush() automatically in the OutputStream
				out.write(buffer, 0, bytesRead);
			}

			out.flush();

			return out.toByteArray();
		}
	}
}
