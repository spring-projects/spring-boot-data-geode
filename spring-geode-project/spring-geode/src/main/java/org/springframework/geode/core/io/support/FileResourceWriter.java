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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.core.io.AbstractResourceWriter;
import org.springframework.geode.core.io.ResourceDataAccessException;
import org.springframework.geode.core.io.ResourceWriteException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * {@link AbstractResourceWriter} implementation that writes data of a {@link File} based {@link Resource}.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.io.OutputStream
 * @see java.nio.file.Files
 * @see java.nio.file.OpenOption
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.AbstractResourceWriter
 * @since 1.3.1
 */
@SuppressWarnings("unused")
public class FileResourceWriter extends AbstractResourceWriter {

	protected static final boolean DEFAULT_APPEND_TO_FILE = false;

	protected static final int DEFAULT_BUFFER_SIZE = 16384;

	private final ThreadLocal<Resource> resource = new ThreadLocal<>();

	/**
	 * @inheritDoc
	 */
	@Override
	protected void doWrite(OutputStream resourceOutputStream, byte[] data) {

		if (ResourceUtils.isNotEmpty(data)) {

			int bufferSize = getBufferSize();
			int length = Math.min(bufferSize, data.length);
			int offset = 0;

			try (OutputStream out = decorate(resourceOutputStream)) {

				while (offset < data.length) {
					out.write(data, offset, length);
					offset += bufferSize;
					length = Math.min(bufferSize, data.length - offset);
				}

				out.flush();
			}
			catch (IOException cause) {

				String message = String.format("Failed to write data (%1$d byte(s)) to Resource using [%2$s]",
					data.length, getClass().getName());

				throw new ResourceWriteException(message, cause);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected boolean isAbleToHandle(@Nullable Resource resource) {

		if (super.isAbleToHandle(resource) && resource.isFile()) {
			this.resource.set(resource);
			return true;
		}

		return false;
	}

	/**
	 * Returns the configured {@link Integer#TYPE buffer size} used by this writer to chunk the data written to
	 * the {@link File}.
	 * <p>
	 * Subclasses should override this method to tune the buffer size based on the context and requirements.
	 *
	 * @return the configured {@link Integer#TYPE buffer size}.
	 */
	protected int getBufferSize() {
		return DEFAULT_BUFFER_SIZE;
	}

	/**
	 * Returns the configured {@link OpenOption OpenOptions} used to configure the stream writing to the {@link File}.
	 *
	 * By default, the {@link File} will be {@link StandardOpenOption#CREATE created},
	 * {@link StandardOpenOption#TRUNCATE_EXISTING truncated} and {@link StandardOpenOption#WRITE written} to.
	 *
	 * Subclasses should override this method to tune the {@link File} stream based on context and requirements.
	 *
	 * @return configured {@link OpenOption OpenOptions}.
	 * @see java.nio.file.OpenOption
	 */
	protected OpenOption[] getOpenOptions() {

		return ArrayUtils.asArray(
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE
		);
	}

	/**
	 * Returns an {@link Optional} reference to the target {@link Resource}.
	 *
	 * @return an {@link Optional} reference to the target {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 * @see java.util.Optional
	 */
	protected Optional<Resource> getResource() {
		return Optional.ofNullable(this.resource.get());
	}

	/**
	 * Decorates the given {@link OutputStream} by adding buffering capabilities.
	 *
	 * @param outputStream {@link OutputStream} to decorate.
	 * @return the decorated {@link OutputStream}.
	 * @see #newFileOutputStream()
	 * @see java.io.OutputStream
	 */
	protected @NonNull OutputStream decorate(@Nullable OutputStream outputStream) {

		return outputStream instanceof BufferedOutputStream ? outputStream
			: outputStream != null ? new BufferedOutputStream(outputStream, getBufferSize())
			: newFileOutputStream();
	}

	/**
	 * Tries to construct a new {@link File} based {@link OutputStream} from the {@literal target} {@link Resource}.
	 *
	 * By default, the constructed {@link OutputStream} is also buffered (e.g. {@link BufferedOutputStream}).
	 *
	 * @return a {@link OutputStream} writing to a {@link File} identified by the {@literal target} {@link Resource}.
	 * @throws IllegalStateException if the {@literal target} {@link Resource} cannot be handled as a {@link File}.
	 * @throws DataAccessResourceFailureException if the {@link OutputStream} could not be created.
	 * @see java.io.BufferedOutputStream
	 * @see java.io.OutputStream
	 * @see #getBufferSize()
	 * @see #getOpenOptions()
	 * @see #getResource()
	 */
	protected OutputStream newFileOutputStream() {

		return getResource()
			.filter(this::isAbleToHandle)
			.map(resource -> {
				try {

					OutputStream fileOutputStream =
						Files.newOutputStream(resource.getFile().toPath(), getOpenOptions());

					return new BufferedOutputStream(fileOutputStream, getBufferSize());
				}
				catch (IOException cause) {

					String message = String.format("Failed to access the Resource [%s] as a file",
						resource.getDescription());

					throw new ResourceDataAccessException(message, cause);
				}
			})
			.orElseThrow(() -> newIllegalStateException("Resource [%s] is not a file based resource",
				getResource().map(Resource::getDescription).orElse(null)));
	}
}
