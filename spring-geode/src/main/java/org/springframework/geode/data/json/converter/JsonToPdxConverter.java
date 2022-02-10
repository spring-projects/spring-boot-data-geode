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

import org.apache.geode.pdx.PdxInstance;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * A Spring {@link Converter} interface extension defining a contract to convert
 * from {@link String JSON} to a {@link PdxInstance}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.lang.String
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.3.0
 */
@FunctionalInterface
public interface JsonToPdxConverter extends Converter<String, PdxInstance> {

	/**
	 * Converts the array of {@link Byte#TYPE bytes} containing JSON into a {@link PdxInstance}.
	 *
	 * @param json array of {@link Byte#TYPE bytes} containing JSON to convert into a {@link PdxInstance};
	 * must not be {@literal null}.
	 * @return a {@link PdxInstance} converted from the array of {@link Byte#TYPE bytes} containing JSON.
	 * @see #convert(Object)
	 */
	default @NonNull PdxInstance convert(@NonNull byte[] json) {
		return convert(new String(json));
	}
}
