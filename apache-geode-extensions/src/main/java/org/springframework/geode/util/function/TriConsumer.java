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
package org.springframework.geode.util.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link Consumer} like interface accepting 3 arguments.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @since 1.3.0
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

	/**
	 * Performs a given operation on the 3 arguments.
	 *
	 * @param t first {@link Object argument}.
	 * @param u second {@link Object argument}.
	 * @param v third {@link Object argument}.
	 */
	void accept(T t, U u, V v);

	/**
	 * Composes this {@link TriConsumer} with the given {@link TriConsumer} after this {@link TriConsumer}.
	 *
	 * @param after {@link TriConsumer} to composed with this {@link TriConsumer}; must not be {@literal null}.
	 * @return a new {@link TriConsumer} with the given {@link TriConsumer} composed after this {@link TriConsumer}.
	 * @throws NullPointerException if {@link TriConsumer} is {@literal null}.
	 */
	default TriConsumer<T, U, V> andThen(TriConsumer<T, U, V> after) {

		Objects.requireNonNull(after);

		return (t, u, v) -> {
			accept(t, u, v);
			after.accept(t, u, v);
		};
	}
}
