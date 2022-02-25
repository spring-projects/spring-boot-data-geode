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
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;

/**
 * The {@link BigIntegerSerializer} class is a {@link NumberSerializers NumberSerializers.Base} serializer
 * for serializing {@link BigInteger} values.
 *
 * @author John Blum
 * @see java.math.BigInteger
 * @see com.fasterxml.jackson.databind.ser.std.NumberSerializers
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class BigIntegerSerializer extends NumberSerializers.Base<BigInteger> {

	public static final BigIntegerSerializer INSTANCE = new BigIntegerSerializer();

	public BigIntegerSerializer() {
		super(BigInteger.class, JsonParser.NumberType.BIG_INTEGER, "biginteger");
	}

	@Override
	public void serialize(BigInteger value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {

		jsonGenerator.writeNumber(value);
	}

	@Override
	public void serializeWithType(BigInteger value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider,
			TypeSerializer typeSerializer) throws IOException {

		serialize(value, jsonGenerator, serializerProvider);
	}
}
