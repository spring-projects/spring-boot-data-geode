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

import org.springframework.core.io.Resource;

/**
 * A {@link RuntimeException} indicating that a {@link Resource} was not properly handled during normal processing.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @see org.springframework.core.io.Resource
 * @since 1.3.1
 */
@SuppressWarnings("unused")
public class UnhandledResourceException extends RuntimeException {

	/**
	 * Constructs a new instance of {@link UnhandledResourceException} with no {@link String message}
	 * or no known {@link Throwable cause}.
	 */
	public UnhandledResourceException() { }

	/**
	 * Constructs a new instance of {@link UnhandledResourceException} initialized with the given {@link String message}
	 * to describe the error.
	 *
	 * @param message {@link String} describing the {@link RuntimeException}.
	 */
	public UnhandledResourceException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of {@link UnhandledResourceException} initialized with the given {@link Throwable}
	 * signifying the underlying cause of this {@link RuntimeException}.
	 *
	 * @param cause {@link Throwable} signifying the underlying cause of this {@link RuntimeException}.
	 * @see java.lang.Throwable
	 */
	public UnhandledResourceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new instance of {@link UnhandledResourceException} initialized with the given {@link String message}
	 * describing the error along with a {@link Throwable} signifying the underlying cause of this
	 * {@link RuntimeException}.
	 *
	 * @param message {@link String} describing the {@link RuntimeException}.
	 * @param cause {@link Throwable} signifying the underlying cause of this {@link RuntimeException}.
	 * @see java.lang.Throwable
	 */
	public UnhandledResourceException(String message, Throwable cause) {
		super(message, cause);
	}
}
