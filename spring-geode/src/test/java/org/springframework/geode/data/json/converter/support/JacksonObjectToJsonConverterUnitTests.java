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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import org.springframework.core.convert.ConversionFailedException;

/**
 * Unit Tests for {@link JacksonObjectToJsonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
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
		doReturn(mockObjectMapper).when(converter).newObjectMapper();

		assertThat(converter.convert(source)).isEqualTo(json);

		verify(converter, times(1)).newObjectMapper();
		verify(mockObjectMapper, times(1)).writeValueAsString(eq(source));
	}

	@Test(expected = ConversionFailedException.class)
	public void convertHandlesJsonProcessingException() throws JsonProcessingException {

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JacksonObjectToJsonConverter converter = spy(new JacksonObjectToJsonConverter());

		doReturn(mockObjectMapper).when(converter).newObjectMapper();
		doThrow(new JsonGenerationException("TEST", (JsonGenerator) null))
			.when(mockObjectMapper).writeValueAsString(any());

		try {
			converter.convert(null);
		}
		catch (ConversionFailedException expected) {

			assertThat(expected.getCause()).isInstanceOf(JsonProcessingException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(converter, times(1)).newObjectMapper();
			verify(mockObjectMapper, times(1)).writeValueAsString(isNull());
		}
	}
}
