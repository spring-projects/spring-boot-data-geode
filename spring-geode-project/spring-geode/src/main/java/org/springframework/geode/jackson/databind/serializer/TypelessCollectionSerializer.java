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
package org.springframework.geode.jackson.databind.serializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;

import org.springframework.data.gemfire.util.CollectionUtils;

/**
 * The {@link TypelessCollectionSerializer} class is a custom, typeless {@link CollectionSerializer} implementation.
 *
 * This {@link AsArraySerializerBase} implementation is a lot like {@link CollectionSerializer}, however it excludes
 * unnecessary type metadata in the context of Apache Geode.
 *
 * @author John Blum
 * @see java.util.Collection
 * @see com.fasterxml.jackson.core.JsonGenerator
 * @see com.fasterxml.jackson.databind.JsonSerializer
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.SerializerProvider
 * @see com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class TypelessCollectionSerializer extends AsArraySerializerBase<Collection<?>> {

	protected static final boolean DEFAULT_UNWRAP_SINGLE = false;
	protected static final boolean DEFAULT_STATIC_TYPING = false;

	public TypelessCollectionSerializer(ObjectMapper mapper) {
		super(Collection.class, mapper.getTypeFactory().constructType(Object.class), DEFAULT_STATIC_TYPING,
			null, null);
	}

	public TypelessCollectionSerializer(TypelessCollectionSerializer serializer, BeanProperty property,
		TypeSerializer typeSerializer, JsonSerializer<?> elementSerializer) {

		super(serializer, property, typeSerializer, elementSerializer, DEFAULT_UNWRAP_SINGLE);
	}

	@Override
	public void serializeWithType(Collection<?> value, JsonGenerator jsonGenerator,
			SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException {

		serialize(value, jsonGenerator, serializerProvider);
	}

	@Override
	public boolean hasSingleElement(Collection<?> value) {
		return value != null && value.size() == 1;
	}

	@Override
	protected void serializeContents(Collection<?> value, JsonGenerator jsonGenerator,
			SerializerProvider serializerProvider) throws IOException {

		jsonGenerator.setCurrentValue(value);

		PropertySerializerMap serializers = this._dynamicSerializers;
		TypeSerializer typeSerializer = this._valueTypeSerializer;

		int index = -1;

		try {
			for (Object element : CollectionUtils.nullSafeCollection(value)) {

				index++;

				if (Objects.isNull(element)) {
					serializerProvider.defaultSerializeNull(jsonGenerator);
				}
				else {

					Class<?> elementType = element.getClass();

					JsonSerializer<Object> serializer = resolveSerializer(serializerProvider, elementType);

					if (typeSerializer != null) {
						serializer.serializeWithType(element, jsonGenerator, serializerProvider, typeSerializer);
					}
					else {
						serializer.serialize(element, jsonGenerator, serializerProvider);
					}
				}
			}
		}
		catch(Exception cause) {
			wrapAndThrow(serializerProvider, cause, value, index);
		}
	}

	private JavaType constructSpecializedType(SerializerProvider serializerProvider, JavaType baseType, Class<?> subclass) {
		return serializerProvider.constructSpecializedType(baseType, subclass);
	}

	private JsonSerializer<Object> resolveSerializer(SerializerProvider serializerProvider, Class<?> type)
			throws JsonMappingException {

		JsonSerializer<Object> resolvedSerializer = this._elementSerializer;

		if (Objects.isNull(resolvedSerializer)) {

			PropertySerializerMap dynamicSerializers = this._dynamicSerializers;

			resolvedSerializer = dynamicSerializers.serializerFor(type);

			if (Objects.isNull(resolvedSerializer)) {
				resolvedSerializer = Objects.nonNull(this._elementType) && this._elementType.hasGenericTypes()
					? this._findAndAddDynamic(dynamicSerializers,
						constructSpecializedType(serializerProvider, this._elementType, type), serializerProvider)
					: this._findAndAddDynamic(dynamicSerializers, type, serializerProvider);

			}
		}

		return resolvedSerializer;
	}

	@Override
	public TypelessCollectionSerializer withResolved(BeanProperty property, TypeSerializer typeSerializer,
			JsonSerializer<?> elementSerializer, Boolean unwrapSingle) {

		return new TypelessCollectionSerializer(this, property, typeSerializer, elementSerializer);
	}

	@Override
	public TypelessCollectionSerializer _withValueTypeSerializer(TypeSerializer typeSerializer) {
		return new TypelessCollectionSerializer(this, this._property, typeSerializer, this._elementSerializer);
	}
}
