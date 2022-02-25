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
package org.springframework.geode.config.annotation;

/**
 * The {@link ClusterNotAvailableException} is a {@link RuntimeException} indicating that no Apache Geode cluster
 * was provisioned and available to service Apache Geode {@link org.apache.geode.cache.client.ClientCache} applications.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @see org.apache.geode.cache.client.ClientCache
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ClusterNotAvailableException extends RuntimeException {

	/**
	 * Constructs a new uninitialized instance of {@link ClusterNotAvailableException}.
	 */
	public ClusterNotAvailableException() { }

	/**
	 * Constructs a new instance of {@link ClusterNotAvailableException} initialized with
	 * the given {@link String message} describing the exception.
	 *
	 * @param message {@link String} containing a description of the exception.
	 */
	public ClusterNotAvailableException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of {@link ClusterNotAvailableException} initialized with
	 * the given {@link Throwable} as the cause of this exception.
	 *
	 * @param cause {@link Throwable} indicating the cause of this exception.
	 */
	public ClusterNotAvailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new instance of {@link ClusterNotAvailableException} initialized with
	 * the given {@link String message} describing the exception along with the given {@link Throwable}
	 * as the cause of this exception.
	 *
	 * @param message {@link String} containing a description of the exception.
	 * @param cause {@link Throwable} indicating the cause of this exception.
	 */
	public ClusterNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
