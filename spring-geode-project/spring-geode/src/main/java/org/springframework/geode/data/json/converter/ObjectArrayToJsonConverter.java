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
package org.springframework.geode.data.json.converter;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A Spring {@link Converter} interface extension defining a contract to convert
 * an {@link Iterable} or an array of {@link Object Objects} into {@link String JSON}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.lang.Iterable
 * @see java.lang.Object
 * @see java.lang.String
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.3.0
 */
@FunctionalInterface
public interface ObjectArrayToJsonConverter extends Converter<Iterable<?>, String> {

	/**
	 * Converts the given array of {@link Object Objects} into {@link String JSON}.
	 *
	 * @param array array of {@link Object Objects} to convert into {@link String JSON}.
	 * @return {@link String JSON} generated from the given array of {@link Object Objects}.
	 * @see #convert(Object)
	 */
	default @NonNull String convert(@Nullable Object... array) {
		return convert(Arrays.asList(ArrayUtils.nullSafeArray(array, Object.class)));
	}
}
