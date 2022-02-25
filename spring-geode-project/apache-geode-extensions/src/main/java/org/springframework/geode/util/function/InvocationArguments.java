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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

/**
 * {@link Iterable} of {@link Method} invocation {@link Object arguments}.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @since 1.3.0
 */
public class InvocationArguments implements Iterable<Object> {

	public static InvocationArguments from(Object... arguments) {
		return new InvocationArguments(arguments);
	}

	private final Object[] arguments;

	/**
	 * Constructs a new instance of {@link InvocationArguments} initialized with the given array of
	 * {@link Object arguments}.
	 *
	 * @param arguments array of {@link Object arguments} indicating the values passed to the {@link Method} invocation
	 * parameters; may be {@literal null}.
	 */
	public InvocationArguments(Object[] arguments) {
		this.arguments = arguments != null ? arguments : new Object[0];
	}

	protected Object[] getArguments() {
		return this.arguments;
	}

	@SuppressWarnings("unchecked")
	protected <T> T getArgumentAt(int index) {
		return (T) getArguments()[index];
	}

	@Override
	public Iterator<Object> iterator() {

		return new Iterator<Object>() {

			int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < InvocationArguments.this.getArguments().length;
			}

			@Override
			public Object next() {
				return InvocationArguments.this.getArguments()[this.index++];
			}
		};
	}

	public int size() {
		return this.arguments.length;
	}

	@Override
	public String toString() {
		return Arrays.toString(getArguments());
	}
}
