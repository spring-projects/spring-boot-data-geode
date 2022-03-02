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

	@Test(expected = IllegalArgumentException.class)
	public void convertNullThrowsIllegalArgumentException() {

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		try {
			converter.convert(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Source object to convert must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(converter, never()).newObjectMapper(any());
		}
	}

	@Test(expected = ConversionFailedException.class)
	public void convertHandlesJsonProcessingException() throws JsonProcessingException {

		Object source = new Object();

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(mockObjectMapper).when(converter).newObjectMapper(any());
		doThrow(new JsonGenerationException("TEST", (JsonGenerator) null))
			.when(mockObjectMapper).writeValueAsString(any());

		try {
			converter.convert(source);
		}
		catch (ConversionFailedException expected) {

			assertThat(expected.getCause()).isInstanceOf(JsonProcessingException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(converter, times(1)).newObjectMapper(eq(source));
			verify(mockObjectMapper, times(1)).writeValueAsString(eq(source));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void newObjectMapperWithNullTarget() {

		try {
			new JacksonObjectToJsonConverter().newObjectMapper(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Target object must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void newObjectMapperIsConfiguredCorrectly() {

		Object target = Customer.newCustomer(1L, "Jon Doe");

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(mockObjectMapper).when(converter).newObjectMapper();
		doReturn(mockObjectMapper).when(mockObjectMapper).addMixIn(any(), any());
		doReturn(mockObjectMapper).when(mockObjectMapper).configure(any(JsonGenerator.Feature.class), anyBoolean());
		doReturn(mockObjectMapper).when(mockObjectMapper).configure(any(MapperFeature.class), anyBoolean());
		doReturn(mockObjectMapper).when(mockObjectMapper).configure(any(SerializationFeature.class), anyBoolean());
		doReturn(mockObjectMapper).when(mockObjectMapper).findAndRegisterModules();

		ObjectMapper objectMapper = converter.newObjectMapper(target);

		assertThat(objectMapper).isNotNull();

		verify(converter, times(1)).newObjectMapper();

		verify(mockObjectMapper, times(1))
			.addMixIn(eq(target.getClass()), eq(JacksonObjectToJsonConverter.ObjectTypeMetadataMixin.class));
		verify(mockObjectMapper, times(1))
			.configure(eq(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN), eq(true));
		verify(mockObjectMapper, times(1))
			.configure(eq(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY), eq(true));
		verify(mockObjectMapper, times(1))
			.configure(eq(SerializationFeature.INDENT_OUTPUT), eq(true));
		verify(mockObjectMapper, times(1)).findAndRegisterModules();
		verifyNoMoreInteractions(mockObjectMapper);
	}
}
