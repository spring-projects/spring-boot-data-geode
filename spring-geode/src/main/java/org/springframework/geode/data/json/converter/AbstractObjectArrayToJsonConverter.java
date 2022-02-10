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

import java.util.Map;

import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.data.json.converter.support.JSONFormatterPdxToJsonConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * An abstract base class implementing {@link ObjectArrayToJsonConverter} encapsulating functionality common
 * to all implementations.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see java.util.Map
 * @see org.springframework.geode.data.json.converter.ObjectArrayToJsonConverter
 * @since 1.3.0
 */
public abstract class AbstractObjectArrayToJsonConverter implements ObjectArrayToJsonConverter {

	protected static final String BEGIN_ARRAY = "[";
	protected static final String EMPTY_STRING = "";
	protected static final String END_ARRAY = "]";
	protected static final String JSON_OBJECT_SEPARATOR = ", ";

	private ObjectToJsonConverter converter = newObjectToJsonConverter();

	// TODO configure via an SPI
	private @NonNull ObjectToJsonConverter newObjectToJsonConverter() {
		return new JSONFormatterPdxToJsonConverter();
	}

	/**
	 * Returns a reference to the configured {@link ObjectToJsonConverter} used to convert
	 * individual {@link Object Objects} into {@link String JSON}.
	 *
	 * @return a reference to the configured {@link ObjectToJsonConverter}; never {@literal null}.
	 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
	 */
	protected @NonNull ObjectToJsonConverter getObjectToJsonConverter() {
		return this.converter;
	}

	/**
	 * Converts the given {@link Iterable} of {@link Object Objects} into a {@link String JSON} array.
	 *
	 * @param iterable {@link Iterable} containing the {@link Object Objects} to convert into {@link String JSON};
	 * must not be {@literal null}.
	 * @return the {@link String JSON} generated from the given {@link Iterable} of {@link Object Objects};
	 * never {@literal null}.
	 * @throws IllegalArgumentException if {@link Iterable} is {@literal null}.
	 * @see #getObjectToJsonConverter()
	 * @see java.lang.Iterable
	 */
	@Override
	public @NonNull String convert(@NonNull Iterable<?> iterable) {

		Assert.notNull(iterable, "Iterable must not be null");

		StringBuilder json = new StringBuilder(BEGIN_ARRAY);

		ObjectToJsonConverter converter = getObjectToJsonConverter();

		boolean addComma = false;

		for (Object value : CollectionUtils.nullSafeIterable(iterable)) {
			json.append(addComma ? JSON_OBJECT_SEPARATOR : EMPTY_STRING);
			json.append(converter.convert(value));
			addComma = true;
		}

		json.append(END_ARRAY);

		return json.toString();
	}

	/**
	 * Converts the {@link Map#values() values} from the given {@link Map} into {@link String JSON}.
	 *
	 * @param <K> {@link Class} type of the {@link Map#keySet() keys}.
	 * @param <V> {@link Class} type of the {@link Map#values() values}.
	 * @param map {@link Map} containing the {@link Map#values() values} to convert into {@link String JSON}.
	 * @return {@link String JSON} generated from the {@link Map#values() values} in the given {@link Map}.
	 * @see #convert(Iterable)
	 * @see java.util.Map
	 */
	public @NonNull <K, V> String convert(@Nullable Map<K, V> map) {
		return convert(CollectionUtils.nullSafeMap(map).values());
	}
}
