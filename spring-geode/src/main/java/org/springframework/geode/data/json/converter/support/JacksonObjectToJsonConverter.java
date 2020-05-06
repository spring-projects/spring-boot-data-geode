/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.geode.data.json.converter.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A {@link ObjectToJsonConverter} implementation using Jackson's {@link ObjectMapper} to convert
 * from an {@link Object} to a {@literal JSON} {@link String}.
 *
 * @author John Blum
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
 * @since 1.3.0
 */
public class JacksonObjectToJsonConverter implements ObjectToJsonConverter {

	/**
	 * @inheritDoc
	 */
	@Nullable @Override
	public String convert(Object source) {

		try {

			ObjectMapper objectMapper = newObjectMapper();

			return objectMapper.writeValueAsString(source);
		}
		catch (JsonProcessingException cause) {
			throw new ConversionFailedException(TypeDescriptor.forObject(source), TypeDescriptor.valueOf(String.class),
				source, cause);
		}
	}

	/**
	 * Constructs a new instance of the Jackson {@link ObjectMapper} class.
	 *
	 * @return a new instance of the Jackson {@link ObjectMapper} class.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 */
	protected @NonNull ObjectMapper newObjectMapper() {
		return new ObjectMapper();
	}
}
