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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxInstanceFactory;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.geode.pdx.PdxInstanceBuilder;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link JSONFormatterPdxToJsonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see com.fasterxml.jackson.databind.JsonNode
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.node.ObjectNode
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.PdxInstanceFactory
 * @see org.springframework.geode.data.json.converter.support.JSONFormatterPdxToJsonConverter
 * @see org.springframework.geode.pdx.PdxInstanceBuilder
 * @since 1.3.0
 */
public class JSONFormatterPdxToJsonConverterUnitTests {

	@Test
	public void convertsObjectToJson() {

		String json = "{ \"name\": \"Jason Doe\" }";

		Object object = new Object();

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(json).when(converter).convertPojoToJson(eq(object));

		assertThat(converter.convert(object)).isEqualTo(json);

		verify(converter, times(1)).convertPojoToJson(eq(object));
		verify(converter, never()).convertPdxToJson(any());
	}

	@Test
	public void convertsPdxToJson() {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(json).when(converter).convertPdxToJson(eq(mockPdxInstance));

		assertThat(converter.convert(mockPdxInstance)).isEqualTo(json);

		verify(converter, times(1)).convertPdxToJson(eq(mockPdxInstance));
		verify(converter, never()).convertPojoToJson(any());
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void convertPdxToJsonCallsDecorateAndJsonFormatterToJson() {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(json).when(converter).jsonFormatterToJson(eq(mockPdxInstance));
		doReturn(json).when(converter).decorate(eq(mockPdxInstance), eq(json));

		assertThat(converter.convertPdxToJson(mockPdxInstance)).isEqualTo(json);

		verify(converter, times(1)).jsonFormatterToJson(eq(mockPdxInstance));
		verify(converter, times(1)).decorate(eq(mockPdxInstance), eq(json));
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void decoratePdxInstance() {

		PdxInstance mockPdxInstanceSource = mock(PdxInstance.class);
		PdxInstance mockPdxInstanceTarget = mock(PdxInstance.class);

		PdxInstanceBuilder mockPdxInstanceBuilder = mock(PdxInstanceBuilder.class);

		PdxInstanceFactory mockPdxInstanceFactory = mock(PdxInstanceFactory.class);

		doReturn(false).when(mockPdxInstanceSource).hasField(anyString());
		doReturn("example.app.test.model.Type").when(mockPdxInstanceSource).getClassName();
		doReturn(mockPdxInstanceFactory).when(mockPdxInstanceBuilder).copy(eq(mockPdxInstanceSource));
		doReturn(mockPdxInstanceFactory).when(mockPdxInstanceFactory)
			.writeString(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME),
				eq("example.app.test.model.Type"));
		doReturn(mockPdxInstanceTarget).when(mockPdxInstanceFactory).create();

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(mockPdxInstanceBuilder).when(converter).newPdxInstanceBuilder();

		assertThat(converter.decorate(mockPdxInstanceSource)).isEqualTo(mockPdxInstanceTarget);

		verify(mockPdxInstanceSource, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(mockPdxInstanceSource, times(2)).getClassName();
		verify(converter,times(1)).newPdxInstanceBuilder();
		verify(mockPdxInstanceBuilder, times(1)).copy(eq(mockPdxInstanceSource));
		verify(mockPdxInstanceFactory, times(1))
			.writeString(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME),
				eq("example.app.test.model.Type"));
		verify(mockPdxInstanceFactory, times(1)).create();
		verifyNoInteractions(mockPdxInstanceTarget);

	}

	@Test
	public void decoratePdxInstanceIsUnnecessary() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(true).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));

		JSONFormatterPdxToJsonConverter converter = new JSONFormatterPdxToJsonConverter();

		assertThat(converter.decorate(mockPdxInstance)).isSameAs(mockPdxInstance);

		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test(expected = IllegalArgumentException.class)
	public void decoratePdxInstanceWithInvalidClassName() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(false).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		doReturn(JSONFormatter.JSON_CLASSNAME).when(mockPdxInstance).getClassName();

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		try {
			converter.decorate(mockPdxInstance);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Class name [%s] is required and cannot be equal to [%s]",
				JSONFormatter.JSON_CLASSNAME);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1))
				.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
			verify(mockPdxInstance, times(2)).getClassName();
			verify(converter, never()).newPdxInstanceBuilder();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void decoratePdxInstanceWithNoClassName() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(false).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		doReturn(null).when(mockPdxInstance).getClassName();

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		try {
			converter.decorate(mockPdxInstance);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Class name [null] is required and cannot be equal to [%s]",
				JSONFormatter.JSON_CLASSNAME);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1))
				.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
			verify(mockPdxInstance, times(2)).getClassName();
			verify(converter, never()).newPdxInstanceBuilder();
		}
	}

	@Test
	public void decorateWithNullPdxInstanceIsNullSafe() {
		assertThat(new JSONFormatterPdxToJsonConverter().decorate(null)).isNull();
	}

	@Test
	@SuppressWarnings("all")
	public void decorateJson() throws JsonProcessingException {

		String sourceJson = "{ \"name\": \"Jon Doe\" }";
		String targetJson = String.format("{ \"%s\": \"%s\"\"name\": \"Jon Doe\" }",
			JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME, Customer.class.getName());

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(ObjectNode.class);

		JSONFormatterPdxToJsonConverter converter = spy(JSONFormatterPdxToJsonConverter.class);

		doReturn(Customer.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(false).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		doReturn(mockObjectMapper).when(converter).newObjectMapper(eq(sourceJson));
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(sourceJson));
		doReturn(false).when(mockJsonNode)
			.has(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		doReturn(targetJson).when(mockObjectMapper).writeValueAsString(eq(mockJsonNode));

		assertThat(converter.decorate(mockPdxInstance, sourceJson)).isEqualTo(targetJson);

		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(converter, times(1)).newObjectMapper(eq(sourceJson));
		verify(mockObjectMapper, times(1)).readTree(eq(sourceJson));
		verify(mockJsonNode, times(1))
			.has(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(mockPdxInstance, times(1)).getClassName();
		verify((ObjectNode) mockJsonNode, times(1))
			.put(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME), eq(Customer.class.getName()));
		verify(mockObjectMapper, times(1)).writeValueAsString(eq(mockJsonNode));
	}

	@Test
	public void decorateJsonForReal() {

		String json = "{ \"name\": \"Jon Doe\" }";
		String expectedJson = String.format("{\n  \"name\" : \"Jon Doe\",\n  \"%s\" : \"%s\"\n}",
			JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME, Customer.class.getName());

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(Customer.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(false).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));

		JSONFormatterPdxToJsonConverter converter = new JSONFormatterPdxToJsonConverter();

		assertThat(converter.decorate(mockPdxInstance, json)).isEqualTo(expectedJson);

		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void decorateJsonIsUnnecessary() {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(true).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		assertThat(converter.decorate(mockPdxInstance, json)).isEqualTo(json);

		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(converter, never()).newObjectMapper(any());
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void decorateJsonNodeIsUnnecessary() throws JsonProcessingException {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(ObjectNode.class);

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(false).when(mockPdxInstance)
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		doReturn(mockObjectMapper).when(converter).newObjectMapper(eq(json));
		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(true).when(mockJsonNode)
			.has(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));

		assertThat(converter.decorate(mockPdxInstance, json)).isEqualTo(json);

		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(converter, times(1)).newObjectMapper(eq(json));
		verify(mockObjectMapper, times(1)).readTree(eq(json));
		verify(mockJsonNode, times(1))
			.has(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verifyNoMoreInteractions(mockPdxInstance, mockObjectMapper, mockJsonNode);
	}

	@Test
	public void decoratesJsonThrowsJsonProcessingExceptionWhenPdxInstanceClassNameIsSpecifiedAndValid()
		throws JsonProcessingException {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(jonDoe.getClass().getName()).when(mockPdxInstance).getClassName();
		doReturn(jonDoe).when(mockPdxInstance).getObject();
		doReturn(false).when(mockPdxInstance).hasField(anyString());
		doReturn(mockObjectMapper).when(converter).newObjectMapper(eq(json));
		doThrow(new JsonGenerationException("TEST", (JsonGenerator) null)).when(mockObjectMapper).readTree(eq(json));
		doReturn(json).when(converter).convertPojoToJson(eq(jonDoe));

		assertThat(converter.decorate(mockPdxInstance, json)).isEqualTo(json);

		verify(mockPdxInstance, times(1))
			.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
		verify(converter, times(1)).newObjectMapper(eq(json));
		verify(mockObjectMapper, times(1)).readTree(eq(json));
		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, times(1)).getObject();
		verify(converter, times(1)).convertPojoToJson(eq(jonDoe));
		verifyNoMoreInteractions(mockPdxInstance, mockObjectMapper);
	}

	@Test(expected = DataRetrievalFailureException.class)
	public void decorateJsonThrowsDataRetrievalFailureException() throws JsonProcessingException {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(false).when(mockPdxInstance).hasField(anyString());
		doReturn(mockObjectMapper).when(converter).newObjectMapper(eq(json));
		doThrow(new JsonGenerationException("TEST", (JsonGenerator) null)).when(mockObjectMapper).readTree(eq(json));
		doReturn(JSONFormatter.JSON_CLASSNAME).when(mockPdxInstance).getClassName();

		try {
			converter.decorate(mockPdxInstance, json);
		}
		catch (DataRetrievalFailureException expected) {

			assertThat(expected).hasMessageStartingWith("Failed to parse JSON [%s]", json);
			assertThat(expected).hasCauseInstanceOf(JsonGenerationException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1))
				.hasField(eq(JSONFormatterPdxToJsonConverter.AT_TYPE_METADATA_PROPERTY_NAME));
			verify(converter, times(1)).newObjectMapper(eq(json));
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockPdxInstance, times(1)).getClassName();
			verifyNoMoreInteractions(mockPdxInstance, mockObjectMapper);
		}
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingValidClassName() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(Customer.class.getName()).when(mockPdxInstance).getClassName();

		assertThat(new JSONFormatterPdxToJsonConverter().hasValidClassName(mockPdxInstance)).isTrue();

		verify(mockPdxInstance, times(1)).getClassName();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingNonExistingClassType() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("non.existing.class.Type").when(mockPdxInstance).getClassName();

		assertThat(new JSONFormatterPdxToJsonConverter().hasValidClassName(mockPdxInstance)).isTrue();

		verify(mockPdxInstance, times(1)).getClassName();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	private void testHasValidClassNameWithPdxInstanceHavingInvalidClassName(String className) {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(className).when(mockPdxInstance).getClassName();

		assertThat(new JSONFormatterPdxToJsonConverter().hasValidClassName(mockPdxInstance)).isFalse();

		verify(mockPdxInstance, times(1)).getClassName();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingJsonFormatterJsonClassName() {
		testHasValidClassNameWithPdxInstanceHavingInvalidClassName(JSONFormatter.JSON_CLASSNAME);
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingBlankClassName() {
		testHasValidClassNameWithPdxInstanceHavingInvalidClassName("  ");
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingEmptyClassName() {
		testHasValidClassNameWithPdxInstanceHavingInvalidClassName("");
	}

	@Test
	public void hasValidClassNameWithPdxInstanceHavingNullClassName() {
		testHasValidClassNameWithPdxInstanceHavingInvalidClassName(null);
	}

	@Test
	public void isNotObjectNodeWhenJsonNodeIsObjectReturnsFalse() {

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(false).when(mockJsonNode).isObject();

		assertThat(new JSONFormatterPdxToJsonConverter().isObjectNode(mockJsonNode)).isFalse();

		verify(mockJsonNode, times(1)).isObject();
		verifyNoMoreInteractions(mockJsonNode);
	}

	@Test
	public void isObjectNodeWhenJsonNodeIsObjectReturnsTrue() {

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(true).when(mockJsonNode).isObject();

		assertThat(new JSONFormatterPdxToJsonConverter().isObjectNode(mockJsonNode)).isTrue();

		verify(mockJsonNode, times(1)).isObject();
		verifyNoMoreInteractions(mockJsonNode);
	}

	@Test
	public void isObjectNodeWithObjectNode() {
		assertThat(new JSONFormatterPdxToJsonConverter().isObjectNode(mock(ObjectNode.class))).isTrue();
	}

	@Test
	public void isObjectNodeIsNullSafe() {
		assertThat(new JSONFormatterPdxToJsonConverter().isObjectNode(null)).isFalse();
	}

	@Test
	public void isValidJsonWithJson() {

		assertThat(new JSONFormatterPdxToJsonConverter()
			.isValidJson("[{ \"name\": \"Jon Doe\" }, { \"name\": \"Jane Doe\" }]")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("{ \"name\": \"Jon Doe\" }")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("{}")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("[]")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("test")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("<xml>")).isTrue();
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("<html></html>")).isTrue();
	}

	@Test
	public void isValidJsonWithNonJsonString() {
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("non-Json String")).isTrue();
	}

	@Test
	public void isValidJsonWithBlankString() {
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("  ")).isFalse();
	}

	@Test
	public void isValidJsonWithEmptyString() {
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson("")).isFalse();
	}

	@Test
	public void isValidJsonWithNull() {
		assertThat(new JSONFormatterPdxToJsonConverter().isValidJson(null)).isFalse();
	}
}
