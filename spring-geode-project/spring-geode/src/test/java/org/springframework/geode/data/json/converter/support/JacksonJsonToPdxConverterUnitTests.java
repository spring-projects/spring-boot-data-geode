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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;

import org.apache.geode.pdx.PdxInstance;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.geode.data.json.converter.JsonToPdxConverter;

/**
 * Unit Tests for {@link JacksonJsonToPdxConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.JsonToPdxConverter
 * @see org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @since 1.3.0
 */
public class JacksonJsonToPdxConverterUnitTests {

	@Test
	public void convertJsonArrayToPdx() throws JsonProcessingException {

		String jonDoeJson = "{ \"name\": \"Jon Doe\" }";
		String janeDoeJson = "{ \"name\": \"Jane Doe\" }";
		String json = String.format("[%s, %s]", jonDoeJson, janeDoeJson);

		PdxInstance jonDoePdx = mock(PdxInstance.class);
		PdxInstance janeDoePdx = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		ObjectNode jonDoeNode = mock(ObjectNode.class);
		ObjectNode janeDoeNode = mock(ObjectNode.class);

		ArrayNode mockArrayNode = mock(ArrayNode.class);

		doReturn(mockArrayNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(true).when(mockArrayNode).isArray();
		doReturn(Arrays.asList(jonDoeNode, janeDoeNode).iterator()).when(mockArrayNode).elements();
		doReturn(jonDoeJson).when(jonDoeNode).toString();
		doReturn(janeDoeJson).when(janeDoeNode).toString();

		JsonToPdxConverter mockJsonToPdxConverter = mock(JsonToPdxConverter.class);

		doReturn(jonDoePdx).when(mockJsonToPdxConverter).convert(eq(jonDoeJson));
		doReturn(janeDoePdx).when(mockJsonToPdxConverter).convert(eq(janeDoeJson));

		JacksonJsonToPdxConverter converter = spy(new JacksonJsonToPdxConverter());

		doReturn(mockJsonToPdxConverter).when(converter).getJsonToPdxConverter();
		doReturn(mockObjectMapper).when(converter).getObjectMapper();

		assertThat(converter.convert(json)).containsExactlyInAnyOrder(jonDoePdx, janeDoePdx);

		verify(converter, times(1)).getObjectMapper();
		verify(converter, times(1)).getJsonToPdxConverter();
		verify(mockObjectMapper, times(1)).readTree(eq(json));
		verify(mockJsonToPdxConverter, times(1)).convert(eq(jonDoeJson));
		verify(mockJsonToPdxConverter, times(1)).convert(eq(janeDoeJson));
		verify(mockArrayNode, times(1)).isArray();
		verify(mockArrayNode, never()).isObject();
		verify(mockArrayNode, times(1)).elements();
		verifyNoInteractions(jonDoePdx, janeDoePdx);
	}

	@Test
	public void convertJsonObjectToPdx() throws JsonProcessingException {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		ObjectNode mockObjectNode = mock(ObjectNode.class);

		doReturn(mockObjectNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(JsonNodeType.OBJECT).when(mockObjectNode).getNodeType();
		doReturn(json).when(mockObjectNode).toString();

		JsonToPdxConverter mockJsonToPdxConverter = mock(JsonToPdxConverter.class);

		doReturn(mockPdxInstance).when(mockJsonToPdxConverter).convert(eq(json));

		JacksonJsonToPdxConverter converter = spy(new JacksonJsonToPdxConverter());

		doReturn(mockJsonToPdxConverter).when(converter).getJsonToPdxConverter();
		doReturn(mockObjectMapper).when(converter).getObjectMapper();

		assertThat(converter.convert(json)).isEqualTo(new PdxInstance[] { mockPdxInstance });

		verify(converter, times(1)).getObjectMapper();
		verify(converter, times(1)).getJsonToPdxConverter();
		verify(mockObjectMapper, times(1)).readTree(eq(json));
		verify(mockObjectNode, times(1)).getNodeType();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test(expected = IllegalStateException.class)
	public void convertUnhandledJsonNodeType() throws JsonProcessingException {

		String json = "{}";

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		JsonNode mockJsonNode = mock(JsonNode.class);

		doReturn(mockJsonNode).when(mockObjectMapper).readTree(eq(json));
		doReturn(JsonNodeType.BINARY).when(mockJsonNode).getNodeType();

		JacksonJsonToPdxConverter converter = spy(new JacksonJsonToPdxConverter());

		doReturn(mockObjectMapper).when(converter).getObjectMapper();

		try {
			converter.convert(json);
		}
		catch (IllegalStateException expected) {

			assertThat(expected)
				.hasMessage("Unable to process JSON node of type [BINARY]; expected either an [OBJECT] or an [ARRAY]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(converter, times(1)).getObjectMapper();
			verify(mockObjectMapper, times(1)).readTree(eq(json));
			verify(mockJsonNode, times(3)).getNodeType();
		}
	}

	@Test(expected = DataRetrievalFailureException.class)
	public void convertHandlesJsonProcessingException() throws JsonProcessingException {

		String json = "[]";

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		doThrow(new JsonEOFException(null, null, "TEST")).when(mockObjectMapper).readTree(eq(json));

		JacksonJsonToPdxConverter converter = spy(new JacksonJsonToPdxConverter());

		doReturn(mockObjectMapper).when(converter).getObjectMapper();

		try {
			converter.convert(json);
		}
		catch (DataRetrievalFailureException expected) {

			assertThat(expected).hasMessageStartingWith("Failed to read JSON content");
			assertThat(expected).hasCauseInstanceOf(JsonProcessingException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
	}
}
