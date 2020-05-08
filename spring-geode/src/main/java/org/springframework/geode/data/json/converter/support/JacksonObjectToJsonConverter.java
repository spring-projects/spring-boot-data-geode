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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * A {@link ObjectToJsonConverter} implementation using Jackson's {@link ObjectMapper} to convert
 * from an {@link Object} to a {@literal JSON} {@link String}.
 *
 * @author John Blum
 * @see com.fasterxml.jackson.annotation.JsonTypeInfo
 * @see com.fasterxml.jackson.core.JsonGenerator
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.MapperFeature
 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
 * @since 1.3.0
 */
public class JacksonObjectToJsonConverter implements ObjectToJsonConverter {

	protected static final String AT_TYPE_METADATA_PROPERTY_NAME = "@type";

	/**
	 * Converts the given {@link Object} into {@link String JSON}.
	 *
	 * @param source {@link Object} to convert into {@link String JSON}.
	 * @return {@link String JSON} generated from the given {@link Object} using Jackson's {@link ObjectMapper}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 * @see #newObjectMapper(Object)
	 */
	@Override
	public @NonNull String convert(@NonNull Object source) {

		Assert.notNull(source, "Source object to convert must not be null");

		try {
			return newObjectMapper(source).writeValueAsString(source);
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
	protected @NonNull ObjectMapper newObjectMapper(@NonNull Object target) {

		Assert.notNull(target, "Target object must not be null");

		return new ObjectMapper()
			.addMixIn(target.getClass(), ObjectTypeMetadataMixin.class)
			.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
	}

	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = AT_TYPE_METADATA_PROPERTY_NAME
	)
	@SuppressWarnings("all")
	interface ObjectTypeMetadataMixin { }

}
