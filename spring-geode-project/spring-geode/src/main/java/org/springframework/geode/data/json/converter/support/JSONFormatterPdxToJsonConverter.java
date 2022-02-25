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

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.WritablePdxInstance;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.geode.pdx.PdxInstanceBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An {@link ObjectToJsonConverter} implementation using the Apache Geode {@link JSONFormatter} to convert
 * from a {@link PdxInstance} to a {@literal JSON} {@link String}.
 *
 * @author John Blum
 * @see com.fasterxml.jackson.databind.JsonNode
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.node.ObjectNode
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.WritablePdxInstance
 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
 * @see org.springframework.geode.data.json.converter.support.JacksonObjectToJsonConverter
 * @see org.springframework.geode.pdx.PdxInstanceBuilder
 * @since 1.3.0
 */
public class JSONFormatterPdxToJsonConverter extends JacksonObjectToJsonConverter {

	/**
	 * @inheritDoc
	 */
	@Nullable @Override
	public final String convert(@Nullable Object source) {

		return Optional.ofNullable(source)
			.filter(PdxInstance.class::isInstance)
			.map(PdxInstance.class::cast)
			.map(this::convertPdxToJson)
			.orElseGet(() -> convertPojoToJson(source));
	}

	/**
	 * Converts the given {@link Object} to JSON.
	 *
	 * @param source {@link Object} to convert to JSON.
	 * @return the JSON generated from the given {@link Object}.
	 * @see JacksonObjectToJsonConverter#convert(Object)
	 */
	protected @Nullable String convertPojoToJson(Object source) {
		return super.convert(source);
	}

	/**
	 * Converts the given {@link PdxInstance PDX} to {@link String JSON}.
	 *
	 * @param pdxInstance {@link PdxInstance} to convert to JSON; must not be {@literal null}.
	 * @return JSON generated from the given {@link PdxInstance}.
	 * @see org.apache.geode.pdx.JSONFormatter#toJSON(PdxInstance)
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #jsonFormatterToJson(PdxInstance)
	 */
	protected @NonNull String convertPdxToJson(@NonNull PdxInstance pdxInstance) {
		return decorate(pdxInstance, jsonFormatterToJson(pdxInstance));
	}

	/**
	 * Converts {@link PdxInstance PDX} into {@link String JSON} using {@link JSONFormatter#toJSON(PdxInstance)}.
	 *
	 * @param pdxInstance {@link PdxInstance PDX} to convert to {@link String JSON}; must not be {@literal null}.
	 * @return {@link String JSON} generated from the given, required {@link PdxInstance PDX}; never {@literal null}.
	 * @see org.apache.geode.pdx.JSONFormatter#toJSON(PdxInstance)
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	@NonNull String jsonFormatterToJson(@NonNull PdxInstance pdxInstance) {
		return JSONFormatter.toJSON(pdxInstance);
	}

	/**
	 * WARNING!!!
	 *
	 * First, this method might be less than optimal and could lead to PDX type explosion!
	 *
	 * Second, this {@code pdxInstance.createWriter().setField(AT_TYPE_METADATA_PROPERTY_NAME, className);} ...
	 *
	 * Throws:
	 *  org.apache.geode.pdx.PdxFieldDoesNotExistException: A field named @type does not exist on ...
	 *  PdxType[dsid=0,typenum=7232261,name=example.app.crm.model.Customer,fields=[id:long:identity:0:idx0(relativeOffset)=0:idx1(vlfOffsetIndex)=0, name:String:1:idx0(relativeOffset)=8:idx1(vlfOffsetIndex)=-1,]]
	 *    at org.apache.geode.pdx.internal.WritablePdxInstanceImpl.setField(WritablePdxInstanceImpl.java:119)
	 *    ...
	 *
	 * This code needs to create a {@literal new} {@link PdxInstance} from an existing {@link PdxInstance}
	 * or add the new (PDX) field to the PDX type metadata using {@code PdxType.addField(:PdxField)} before
	 * setting the new field on the {@link PdxInstance} using the {@link WritablePdxInstance}. Unfortunately,
	 * {@code PdxType} is part of the internal API and updating and ditributing a {@code PdxType} is complicated,
	 * requiring a Distributed Lock, among other responsibilities.
	 */
	@SuppressWarnings("unused")
	protected @NonNull PdxInstance decorate(@NonNull PdxInstance pdxInstance) {

		if (isMissingObjectTypeMetadata(pdxInstance)) {

			String pdxInstanceClassName = pdxInstance.getClassName();

			Assert.isTrue(hasValidClassName(pdxInstance), () ->
				String.format("Class name [%s] is required and cannot be equal to [%s]",
					pdxInstanceClassName, JSONFormatter.JSON_CLASSNAME));

			pdxInstance = newPdxInstanceBuilder()
				.copy(pdxInstance)
				.writeString(AT_TYPE_METADATA_PROPERTY_NAME, pdxInstanceClassName)
				.create();
		}

		return pdxInstance;
	}

	/**
	 * Constructs a new instance of {@link PdxInstanceBuilder}.
	 *
	 * @return a new instance of {@link PdxInstanceBuilder}; never {@literal null}.
	 * @see org.springframework.geode.pdx.PdxInstanceBuilder
	 */
	@NonNull PdxInstanceBuilder newPdxInstanceBuilder() {
		return PdxInstanceBuilder.create();
	}

	/**
	 * Decorates the given {@link String JSON} to include the {@literal @type} metadata property in order to
	 * indicate the type of the {@literal JSON} object, which is required for deserialization back to PDX.
	 *
	 * If an {@link JsonProcessingException} is thrown during this operation and if the {@link PdxInstance}
	 * has a {@link #hasValidClassName(PdxInstance) valid class name}, then an attempt is made to serialize
	 * the {@link PdxInstance#getObject() object instance} of the {@link PdxInstance} to {@link String JSON}
	 * using Jackson's {@link ObjectMapper}.
	 *
	 * @param pdxInstance required {@link PdxInstance} from which the {@link String JSON} was serialized;
	 * must not be {@literal null}.
	 * @param json {@link String JSON} generated from the serialization of the {@link PdxInstance};
	 * must not be {@literal null}.
	 * @return the decorated {@link String JSON} including the {@literal @type} metadata property.
	 * @throws DataRetrievalFailureException if {@link String JSON} cannot be decorated with type metadata
	 * and the {@link PdxInstance} is not based on a valid {@link Class} type.
	 * @see JacksonObjectToJsonConverter#convert(Object)
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #newObjectMapper(Object)
	 */
	@SuppressWarnings("unused")
	protected @NonNull String decorate(@NonNull PdxInstance pdxInstance, @NonNull String json) {

		if (isDecorationRequired(pdxInstance, json)) {
			try {

				ObjectMapper objectMapper = newObjectMapper(json);

				JsonNode jsonNode = objectMapper.readTree(json);

				if (isMissingObjectTypeMetadata(jsonNode)) {
					((ObjectNode) jsonNode).put(AT_TYPE_METADATA_PROPERTY_NAME, pdxInstance.getClassName());
					json = objectMapper.writeValueAsString(jsonNode);
				}

				return json;
			}
			catch (JsonProcessingException cause) {

				if (hasValidClassName(pdxInstance)) {
					return convertPojoToJson(pdxInstance.getObject());
				}

				String message = String.format("Failed to parse JSON [%s]", json);

				throw new DataRetrievalFailureException(message, cause);
			}
		}

		return json;
	}

	/**
	 * Null-safe method to determine whether the given {@link PdxInstance}
	 * has a valid {@link Class#getName() Class Name}.
	 *
	 * @param pdxInstance {@link PdxInstance} to evaluate;
	 * @return a boolean value indicating whether the {@link PdxInstance}
	 * has a valid {@link Class#getName() Class Name}.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	boolean hasValidClassName(@Nullable PdxInstance pdxInstance) {

		return Optional.ofNullable(pdxInstance)
			.map(PdxInstance::getClassName)
			.filter(StringUtils::hasText)
			.filter(className -> !JSONFormatter.JSON_CLASSNAME.equals(className))
			.isPresent();
	}

	private boolean isDecorationRequired(@Nullable PdxInstance pdxInstance, @Nullable String json) {
		return isMissingObjectTypeMetadata(pdxInstance) && isValidJson(json);
	}

	private boolean isMissingObjectTypeMetadata(@Nullable JsonNode node) {
		return isObjectNode(node) && !node.has(AT_TYPE_METADATA_PROPERTY_NAME);
	}

	private boolean isMissingObjectTypeMetadata(@Nullable PdxInstance pdxInstance) {
		return pdxInstance != null && !pdxInstance.hasField(AT_TYPE_METADATA_PROPERTY_NAME);
	}

	/**
	 * Null-safe method to determine if the given {@link JsonNode} represents a valid {@link String JSON} object.
	 *
	 * @param node {@link JsonNode} to evaluate.
	 * @return a boolean valued indicating whether the given {@link JsonNode} is a valid {@link ObjectNode}.
	 * @see com.fasterxml.jackson.databind.node.ObjectNode
	 * @see com.fasterxml.jackson.databind.JsonNode
	 */
	boolean isObjectNode(@Nullable JsonNode node) {
		return node != null && (node.isObject() || node instanceof ObjectNode);
	}

	/**
	 * Null-safe method to determine whether the given {@link String JSON} is valid.
	 *
	 * @param json {@link String} containing JSON to evaluate.
	 * @return a boolean value indicating whether the given {@link String JSON} is valid.
	 */
	boolean isValidJson(@Nullable String json) {
		return StringUtils.hasText(json);
	}
}
