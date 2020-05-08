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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.geode.data.json.jackson.databind.serializer.BigDecimalSerializer;
import org.springframework.geode.data.json.jackson.databind.serializer.BigIntegerSerializer;
import org.springframework.geode.data.json.jackson.databind.serializer.TypelessCollectionSerializer;
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

	protected static final ObjectMapper.DefaultTyping DEFAULT_TYPING = ObjectMapper.DefaultTyping.NON_FINAL;

	protected static final String AT_TYPE_METADATA_PROPERTY_NAME = "@type";

	private static final String SPRING_BOOT_DATA_GEODE_JACKSON_MODULE_NAME = "spring.boot.data.geode.module";

	// Since SBDG Version
	// Only change version when the Module definition changes since SimpleModule implements java.io.Serializable;
	// Although, as of Jackson 2.5.0, SimpleModule defines a fixed serialVersionUID, so...
	private static final Version VERSION = new Version(1, 3, 0, null,
		"org.springframework.geode", "spring-geode-starter");

	/**
	 * Converts the given {@link Object} into {@link String JSON}.
	 *
	 * @param source {@link Object} to convert into {@link String JSON}.
	 * @return {@link String JSON} generated from the given {@link Object} using Jackson's {@link ObjectMapper}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 * @see #newObjectMapper()
	 */
	@Nullable @Override
	public String convert(Object source) {

		try {
			return newObjectMapper().writeValueAsString(source);
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

		ObjectMapper objectMapper = new ObjectMapper()
			.activateDefaultTypingAsProperty(null, DEFAULT_TYPING, AT_TYPE_METADATA_PROPERTY_NAME)
			.addMixIn(NonAccessibleType.class, ObjectTypeMetadataMixin.class)
			.addMixIn(Object.class, IgnorePropertiesMixin.class)
			.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
			.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
			.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

		objectMapper.registerModule(new SimpleModule(SPRING_BOOT_DATA_GEODE_JACKSON_MODULE_NAME, VERSION)
			.addSerializer(BigDecimalSerializer.INSTANCE)
			.addSerializer(BigIntegerSerializer.INSTANCE)
			.addSerializer(new TypelessCollectionSerializer(objectMapper)));

		return objectMapper;
	}

	@SuppressWarnings("unused")
	private interface NonAccessibleType { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	@SuppressWarnings("unused")
	interface IgnorePropertiesMixin { }

	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = AT_TYPE_METADATA_PROPERTY_NAME
	)
	@SuppressWarnings("all")
	interface ObjectTypeMetadataMixin { }

}
