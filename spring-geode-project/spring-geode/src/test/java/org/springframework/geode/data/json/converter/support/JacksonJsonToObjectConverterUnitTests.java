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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.POJONode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.mapping.MappingException;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link JacksonJsonToObjectConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.Spy
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.geode.data.json.converter.support.JacksonJsonToObjectConverter
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.JsonNode
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class JacksonJsonToObjectConverterUnitTests {

	@Spy
	JacksonJsonToObjectConverter converter;

	@Test
	public void objectMapperConfigurationIsCorrect() {

		ObjectMapper objectMapper = this.converter.getObjectMapper();

		assertThat(objectMapper).isNotNull();
		assertThat(objectMapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)).isTrue();
		assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)).isFalse();
		assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
	}

	@Test
	public void convertingInvalidJsonReturnsNull() {
		assertThat(this.converter.convert((String) null)).isNull();
		assertThat(this.converter.convert("")).isNull();
		assertThat(this.converter.convert("  ")).isNull();
	}

	@Test
	public void convertPojoReturnsPojo() throws JsonProcessingException {

		String json = "{\"id\":1,\"name\":\"Jon Doe\"}";

		POJONode mockJsonNode = mock(POJONode.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		doReturn(mockObjectMapper).when(this.converter).getObjectMapper();
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(jonDoe).when(mockJsonNode).getPojo();

		assertThat(this.converter.convert(json)).isEqualTo(jonDoe);

		verify(this.converter, times(1)).getObjectMapper();
		verify(mockObjectMapper, times(1)).readTree(eq(json));
		verify(mockJsonNode, times(1)).getPojo();
	}

	@Test
	public void convertJsonObjectReturnsObject() {

		String json = String.format("{\"@type\":\"%s\",\"id\":2,\"name\":\"Jane Doe\"}", Customer.class.getName());

		Object value = this.converter.convert(json);

		assertThat(value).isInstanceOf(Customer.class);

		Customer janeDoe = (Customer) value;

		assertThat(janeDoe.getId()).isEqualTo(2L);
		assertThat(janeDoe.getName()).isEqualTo("Jane Doe");
	}

	@Test(expected = IllegalStateException.class)
	public void convertJsonArrayThrowsIllegalStateException() throws JsonProcessingException {

		String json = "[]";

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(mockObjectMapper).when(this.converter).getObjectMapper();
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(false).when(mockJsonNode).isObject();

		try {
			this.converter.convert(json);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("The JSON [%s] must be an object", json);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(this.converter, times(1)).getObjectMapper();
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockJsonNode, times(1)).isObject();
			verify(mockJsonNode, never()).has(anyString());
			verify(mockJsonNode, never()).get(anyString());
			verifyNoMoreInteractions(mockObjectMapper);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void convertJsonObjectWithNoTypeMetadataThrowsIllegalStateException() throws JsonProcessingException {

		String json = "{\"id\":3,\"name\":\"Pie Doe\"}";

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(mockObjectMapper).when(this.converter).getObjectMapper();
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(true).when(mockJsonNode).isObject();
		doReturn(false).when(mockJsonNode).has(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME);

		try {
			this.converter.convert(json);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("The JSON object [%s] must have an '@type' metadata field", json);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(this.converter, times(1)).getObjectMapper();
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockJsonNode, times(1)).isObject();
			verify(mockJsonNode, times(1)).has(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
			verify(mockJsonNode, never()).get(anyString());
			verifyNoMoreInteractions(mockObjectMapper);
		}
	}

	@Test(expected = MappingException.class)
	public void convertJsonHandlesClassNotFoundException() throws JsonProcessingException {

		String json = "{\"@type\":\"non.existing.class.Type\",\"id\":4,\"name\":\"Play Doe\"}";

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(JsonNode.class);
		JsonNode mockAtTypeJsonNode = mock(JsonNode.class);

		doReturn(mockObjectMapper).when(this.converter).getObjectMapper();
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(true).when(mockJsonNode).isObject();
		doReturn(true).when(mockJsonNode).has(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME);
		doReturn(mockAtTypeJsonNode).when(mockJsonNode).get(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
		doReturn("non.existing.class.Type").when(mockAtTypeJsonNode).asText();

		try {
			this.converter.convert(json);
		}
		catch (MappingException expected) {

			assertThat(expected)
				.hasMessage("Failed to map JSON [%s] to an Object of type [non.existing.class.Type]", json);

			assertThat(expected).hasCauseInstanceOf(ClassNotFoundException.class);
			assertThat(expected.getCause()).hasMessageContaining("non.existing.class.Type");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(this.converter, times(1)).getObjectMapper();
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockJsonNode, times(1)).isObject();
			verify(mockJsonNode, times(1)).has(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
			verify(mockJsonNode, times(1)).get(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
			verify(mockAtTypeJsonNode, times(1)).asText();
			verifyNoMoreInteractions(mockObjectMapper);
		}
	}

	@Test(expected = DataRetrievalFailureException.class)
	public void convertJsonHandlesJsonProcessingException() throws JsonProcessingException {

		String json = String.format("{\"@type\":\"%s\",\"id\":5,\"name\":\"Poe Doe\"}", Customer.class.getName());

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(JsonNode.class);
		JsonNode mockAtTypeJsonNode = mock(JsonNode.class);

		doReturn(mockObjectMapper).when(this.converter).getObjectMapper();
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(true).when(mockJsonNode).isObject();
		doReturn(true).when(mockJsonNode).has(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME);
		doReturn(mockAtTypeJsonNode).when(mockJsonNode).get(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
		doReturn(Customer.class.getName()).when(mockAtTypeJsonNode).asText();
		doThrow(new JsonParseException(null, "TEST")).when(mockObjectMapper).readValue(eq(json), eq(Customer.class));

		try {
			this.converter.convert(json);
		}
		catch (DataRetrievalFailureException expected) {

			assertThat(expected).hasMessageContaining("Failed to read JSON [%s]", json);
			assertThat(expected).hasCauseInstanceOf(JsonParseException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(this.converter, times(1)).getObjectMapper();
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockJsonNode, times(1)).isObject();
			verify(mockJsonNode, times(1)).has(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
			verify(mockJsonNode, times(1)).get(eq(JacksonJsonToObjectConverter.AT_TYPE_FIELD_NAME));
			verify(mockAtTypeJsonNode, times(1)).asText();
			verify(mockObjectMapper, times(1)).readValue(eq(json), eq(Customer.class));
			verifyNoMoreInteractions(mockObjectMapper);
		}
	}

	@Test
	public void isPojoWithPojoNode() {

		POJONode mockPojoNode = mock(POJONode.class);

		assertThat(this.converter.isPojo(mockPojoNode)).isTrue();

		verifyNoInteractions(mockPojoNode);
	}

	@Test
	public void isPojoWhenJsonNodeGetNodeTypeReturnsPojo() {

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(JsonNodeType.POJO).when(mockJsonNode).getNodeType();

		assertThat(this.converter.isPojo(mockJsonNode)).isTrue();

		verify(mockJsonNode, times(1)).getNodeType();
		verifyNoMoreInteractions(mockJsonNode);
	}

	@Test
	public void isNotPojoWhenJsonNodeGetNodeTypeReturnsBinary() {

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(JsonNodeType.BINARY).when(mockJsonNode).getNodeType();

		assertThat(this.converter.isPojo(mockJsonNode)).isFalse();

		verify(mockJsonNode, times(2)).getNodeType();
		verifyNoMoreInteractions(mockJsonNode);
	}

	@Test
	public void isNotPojoWithNullIsNullSafe() {
		assertThat(this.converter.isPojo(null)).isFalse();
	}
}
