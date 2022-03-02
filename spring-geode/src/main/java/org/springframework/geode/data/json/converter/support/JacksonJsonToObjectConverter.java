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
package org.springframework.geode.data.json.converter.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.POJONode;

import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.mapping.MappingException;
import org.springframework.geode.data.json.converter.JsonToObjectConverter;
import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link JsonToObjectConverter} implementation using Jackson to convert {@link String JSON}
 * to an {@link Object} (POJO).
 *
 * @author John Blum
 * @see com.fasterxml.jackson.databind.JsonNode
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.node.POJONode
 * @see org.springframework.core.convert.converter.Converter
 * @see org.springframework.geode.data.json.converter.JsonToObjectConverter
 * @since 1.3.0
 */
public class JacksonJsonToObjectConverter implements JsonToObjectConverter {

	protected static final String AT_TYPE_FIELD_NAME = PdxInstanceWrapper.AT_TYPE_FIELD_NAME;

	private ObjectMapper objectMapper = newObjectMapper();

	/**
	 * Constructs a new Jackson {@link ObjectMapper} to convert {@link String JSON} into an {@link Object} (POJO).
	 *
	 * @return a new Jackson {@link ObjectMapper}; never {@literal null}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 */
	// TODO configure via an SPI
	private @NonNull ObjectMapper newObjectMapper() {

		return new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
			.findAndRegisterModules();
	}

	/**
	 * Returns a reference to the configured Jackson {@link ObjectMapper} used by this {@link Converter}
	 * to convert {@link String JSON} into an {@link Object} (POJO).
	 *
	 * @return a reference to the configured Jackson {@link ObjectMapper}.
	 */
	protected @NonNull ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	/**
	 * Converts from {@link String JSON} to an {@link Object} (POJO) using Jackson's {@link ObjectMapper}.
	 *
	 * @param json {@link String} containing {@literal JSON} to convert.
	 * @return an {@link Object} (POJO) converted from the given {@link String JSON}.
	 * @see #getObjectMapper()
	 */
	@Override
	public @Nullable Object convert(@Nullable String json) {

		if (StringUtils.hasText(json)) {

			String objectTypeName = null;

			try {

				ObjectMapper objectMapper = getObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(json);

				if (isPojo(jsonNode)) {
					return ((POJONode) jsonNode).getPojo();
				}
				else {

					Assert.state(jsonNode.isObject(), () -> String.format("The JSON [%s] must be an object", json));

					Assert.state(jsonNode.has(AT_TYPE_FIELD_NAME),
						() -> String.format("The JSON object [%1$s] must have an '%2$s' metadata field",
							json, AT_TYPE_FIELD_NAME));

					objectTypeName = jsonNode.get(AT_TYPE_FIELD_NAME).asText();

					Class<?> objectType =
						ClassUtils.forName(objectTypeName, Thread.currentThread().getContextClassLoader());

					return objectMapper.readValue(json, objectType);
				}
			}
			catch (ClassNotFoundException cause) {
				throw new MappingException(String.format("Failed to map JSON [%1$s] to an Object of type [%2$s]",
					json, objectTypeName), cause);
			}
			catch (JsonProcessingException cause) {
				throw new DataRetrievalFailureException(String.format("Failed to read JSON [%s]", json), cause);
			}
		}

		return null;
	}

	/**
	 * Null-safe method to determine whether the given {@link JsonNode} represents a {@link Object POJO}.
	 *
	 * @param jsonNode {@link JsonNode} to evaluate.
	 * @return a boolean value indicating whether the given {@link JsonNode} represents a {@link Object POJO}.
	 * @see com.fasterxml.jackson.databind.JsonNode
	 */
	boolean isPojo(@Nullable JsonNode jsonNode) {

		return jsonNode != null
			&& (jsonNode instanceof POJONode || jsonNode.isPojo() || JsonNodeType.POJO.equals(jsonNode.getNodeType()));
	}
}
