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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.Test;

import org.springframework.core.convert.ConversionFailedException;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link JacksonObjectToJsonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.springframework.geode.data.json.converter.support.JacksonObjectToJsonConverter
 * @since 1.3.0
 */
public class JacksonObjectToJsonConverterUnitTests {

	@Test
	public void convertObjectToJson() throws JsonProcessingException {

		String json = "{ \"name\": \"Jon Doe\" }";

		Object source = new Object();

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(json).when(mockObjectMapper).writeValueAsString(eq(source));
		doReturn(mockObjectMapper).when(converter).newObjectMapper(any());

		assertThat(converter.convert(source)).isEqualTo(json);

		verify(converter, times(1)).newObjectMapper(eq(source));
		verify(mockObjectMapper, times(1)).writeValueAsString(eq(source));
	}

	@Test
	@SuppressWarnings("all")
	public void convertNullThrowsIllegalArgumentException() {

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		assertThatIllegalArgumentException()
			.isThrownBy(() -> converter.convert(null))
			.withMessage("Source object to convert must not be null")
				.withNoCause();

		verify(converter, never()).newObjectMapper(any());
	}

	@Test
	public void convertHandlesJsonProcessingException() throws JsonProcessingException {

		Object source = new Object();

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(mockObjectMapper).when(converter).newObjectMapper(any());
		doThrow(new JsonGenerationException("TEST", (JsonGenerator) null))
			.when(mockObjectMapper).writeValueAsString(any());

		assertThatExceptionOfType(ConversionFailedException.class)
			.isThrownBy(() -> converter.convert(source))
			.withMessageStartingWith("Failed to convert from type [java.lang.Object] to type [java.lang.String] for value")
			.withMessageContaining("TEST")
			.withCauseInstanceOf(JsonGenerationException.class);

		verify(converter, times(1)).newObjectMapper(eq(source));
		verify(mockObjectMapper, times(1)).writeValueAsString(eq(source));
	}

	@Test
	@SuppressWarnings("all")
	public void newObjectMapperWithNullTarget() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> new JacksonObjectToJsonConverter().newObjectMapper(null))
			.withMessage("Target object must not be null")
			.withNoCause();
	}

	@Test
	public void newObjectMapperIsConfiguredCorrectly() {

		Object target = Customer.newCustomer(1L, "Jon Doe");

		JsonMapper mockJsonMapper = mock(JsonMapper.class);

		JsonMapper.Builder mockJsonMapperBuilder = mock(JsonMapper.Builder.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(mockJsonMapperBuilder).when(converter).newJsonMapperBuilder();
		doReturn(mockJsonMapperBuilder).when(mockJsonMapperBuilder).addMixIn(any(), any());
		doReturn(mockJsonMapperBuilder).when(mockJsonMapperBuilder).configure(any(JsonGenerator.Feature.class), anyBoolean());
		doReturn(mockJsonMapperBuilder).when(mockJsonMapperBuilder).configure(any(MapperFeature.class), anyBoolean());
		doReturn(mockJsonMapperBuilder).when(mockJsonMapperBuilder).configure(any(SerializationFeature.class), anyBoolean());
		doReturn(mockJsonMapper).when(mockJsonMapperBuilder).build();
		doReturn(mockJsonMapper).when(mockJsonMapper).findAndRegisterModules();

		ObjectMapper objectMapper = converter.newObjectMapper(target);

		assertThat(objectMapper).isNotNull();

		verify(converter, times(1)).newJsonMapperBuilder();

		verify(mockJsonMapperBuilder, times(1))
			.addMixIn(eq(target.getClass()), eq(JacksonObjectToJsonConverter.ObjectTypeMetadataMixin.class));
		verify(mockJsonMapperBuilder, times(1))
			.configure(eq(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN), eq(true));
		verify(mockJsonMapperBuilder, times(1))
			.configure(eq(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY), eq(true));
		verify(mockJsonMapperBuilder, times(1))
			.configure(eq(SerializationFeature.INDENT_OUTPUT), eq(true));
		verify(mockJsonMapperBuilder, times(1)).build();
		verify(mockJsonMapper, times(1)).findAndRegisterModules();
		verifyNoMoreInteractions(mockJsonMapperBuilder, mockJsonMapper);
	}
}
