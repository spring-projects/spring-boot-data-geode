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
package org.springframework.geode.pdx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import org.apache.geode.internal.Sendable;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.WritablePdxInstance;

/**
 * Unit Tests for {@link PdxInstanceWrapper}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see com.fasterxml.jackson.core.JsonGenerator
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.WritablePdxInstance
 * @since 1.3.0
 */
public class PdxInstanceWrapperUnitTests {

	@Test
	public void constructPdxInstanceWrapper() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = new PdxInstanceWrapper(mockPdxInstance);

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		verifyNoInteractions(mockPdxInstance);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructPdxInstanceWrapperWithNull() {

		try {
			new PdxInstanceWrapper(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Argument must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void fromObjectIsObject() {
		assertThat(PdxInstanceWrapper.from("TEST")).isEqualTo("TEST");
	}

	@Test
	public void fromNullIsNull() {
		assertThat(PdxInstanceWrapper.from((Object) null)).isEqualTo(null);
	}

	@Test
	public void fromPdxInstanceIsWrapper() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = PdxInstanceWrapper.from(mockPdxInstance);

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void fromPdxInstanceWrapperIsSameWrapper() {

		PdxInstanceWrapper mockWrapper = mock(PdxInstanceWrapper.class);

		assertThat(PdxInstanceWrapper.from(mockWrapper)).isSameAs(mockWrapper);

		verifyNoInteractions(mockWrapper);
	}

	@Test
	public void unwrapPdxInstanceWrapperReturnsPdxInstanceDelegate() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = PdxInstanceWrapper.from(mockPdxInstance);

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);
		assertThat(PdxInstanceWrapper.unwrap(wrapper)).isEqualTo(mockPdxInstance);

		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void unwrapPdxInstanceReturnsPdxInstance() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		assertThat(PdxInstanceWrapper.unwrap(mockPdxInstance)).isEqualTo(mockPdxInstance);

		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void unwrapNullIsNullSafeAndReturnsNull() {
		assertThat(PdxInstanceWrapper.unwrap(null)).isNull();
	}

	@Test
	public void objectMapperConfigurationIsCorrect() {

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mock(PdxInstance.class)));

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		doReturn(mockObjectMapper).when(wrapper).newObjectMapper();
		doReturn(mockObjectMapper).when(mockObjectMapper).configure(any(DeserializationFeature.class), anyBoolean());
		doReturn(mockObjectMapper).when(mockObjectMapper).configure(any(MapperFeature.class), anyBoolean());
		doReturn(mockObjectMapper).when(mockObjectMapper).findAndRegisterModules();

		ObjectMapper objectMapper = wrapper.getObjectMapper().orElse(null);

		assertThat(objectMapper).isNotNull();

		verify(mockObjectMapper, times(1)).configure(eq(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES), eq(false));
		verify(mockObjectMapper, times(1)).configure(eq(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES), eq(false));
		verify(mockObjectMapper, times(1)).configure(eq(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS), eq(true));
		verify(mockObjectMapper, times(1)).findAndRegisterModules();
		verifyNoMoreInteractions(mockObjectMapper);
	}

	@Test
	public void getClassNameCallsPdxInstanceGetClassName() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("example.app.test.model.Type").when(mockPdxInstance).getClassName();

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).getClassName()).isEqualTo("example.app.test.model.Type");

		verify(mockPdxInstance, times(1)).getClassName();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void isDeserializaleCallsPdxInstanceIsDeserializable() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(true).when(mockPdxInstance).isDeserializable();

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).isDeserializable()).isTrue();

		verify(mockPdxInstance, times(1)).isDeserializable();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void isEnumCallsPdxInstanceIsEnum() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(false).when(mockPdxInstance).isEnum();

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).isEnum()).isFalse();

		verify(mockPdxInstance, times(1)).isEnum();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void isIdentityFieldCallsPdxInstanceIsIdentityField() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(false).when(mockPdxInstance).isIdentityField(anyString());
		doReturn(true).when(mockPdxInstance).isIdentityField("id");

		PdxInstanceWrapper wrapper = PdxInstanceWrapper.from(mockPdxInstance);

		assertThat(wrapper.isIdentityField("id")).isTrue();
		assertThat(wrapper.isIdentityField("randomField")).isFalse();

		verify(mockPdxInstance, times(1)).isIdentityField(eq("id"));
		verify(mockPdxInstance, times(1)).isIdentityField(eq("randomField"));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getFieldCallPdxInstanceGetField() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("TestValue").when(mockPdxInstance).getField(eq("TestField"));

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).getField("TestField")).isEqualTo("TestValue");

		verify(mockPdxInstance, times(1)).getField(eq("TestField"));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getFieldNameCallsPdxInstanceGetFieldNames() {

		List<String> fieldNames = Arrays.asList("FieldOne", "FieldTwo");

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(fieldNames).when(mockPdxInstance).getFieldNames();

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).getFieldNames()).isEqualTo(fieldNames);

		verify(mockPdxInstance, times(1)).getFieldNames();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getIdentifierFromPdxInstanceHavingAnIdentity() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(Arrays.asList("age", "id", "name")).when(mockPdxInstance).getFieldNames();
		doReturn(true).when(mockPdxInstance).isIdentityField(eq("id"));
		doReturn(42).when(mockPdxInstance).getField(eq("id"));

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		assertThat(wrapper.getIdentifier()).isEqualTo(42);

		verify(wrapper, never()).getId();
		verify(mockPdxInstance, times(1)).getFieldNames();
		verify(mockPdxInstance, times(1)).isIdentityField(eq("age"));
		verify(mockPdxInstance, times(1)).isIdentityField(eq("id"));
		verify(mockPdxInstance, never()).isIdentityField(eq("name"));
		verify(mockPdxInstance, times(1)).getField(eq("id"));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getIdentifierFromPdxInstanceHavingNoFields() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(null).when(mockPdxInstance).getFieldNames();
		doReturn(69).when(wrapper).getId();

		assertThat(wrapper.getIdentifier()).isEqualTo(69);

		verify(wrapper, times(1)).getId();
		verify(mockPdxInstance, times(1)).getFieldNames();
		verify(mockPdxInstance, never()).isIdentityField(anyString());
		verify(mockPdxInstance, never()).getField(anyString());
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getIdentifierFromPdxInstanceHavingNoIdentityFields() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Arrays.asList("", "age", null, "name", "  ")).when(mockPdxInstance).getFieldNames();
		doReturn(false).when(mockPdxInstance).isIdentityField(any());
		doReturn(99).when(wrapper).getId();

		assertThat(wrapper.getIdentifier()).isEqualTo(99);

		verify(wrapper, times(1)).getId();
		verify(mockPdxInstance, times(1)).getFieldNames();
		verify(mockPdxInstance, times(1)).isIdentityField(eq("age"));
		verify(mockPdxInstance, times(1)).isIdentityField(eq("name"));
		verify(mockPdxInstance, never()).isIdentityField(isNull());
		verify(mockPdxInstance, never()).isIdentityField(eq(""));
		verify(mockPdxInstance, never()).isIdentityField(eq("  "));
		verify(mockPdxInstance, never()).getField(anyString());
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test(expected = IllegalStateException.class)
	public void getIdentifierFromPdxInstanceWithNoIdentifier() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Collections.singletonList("name")).when(mockPdxInstance).getFieldNames();
		doReturn(false).when(mockPdxInstance).isIdentityField(anyString());
		doThrow(new IllegalStateException("NO ID")).when(wrapper).getId();

		try {
			wrapper.getIdentifier();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("NO ID");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getFieldNames();
			verify(mockPdxInstance, times(1)).isIdentityField(eq("name"));
			verify(mockPdxInstance, never()).getField(anyString());
			verify(wrapper, times(1)).getId();
			verifyNoMoreInteractions(mockPdxInstance);
		}
	}

	@Test
	public void getIdFromPdxInstanceHavingIdField() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		doReturn(42).when(mockPdxInstance).getField(eq(PdxInstanceWrapper.ID_FIELD_NAME));

		assertThat(wrapper.getId()).isEqualTo(42);

		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(mockPdxInstance, times(1)).getField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(wrapper, never()).getAtIdentifier();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getIdFromPdxInstanceHavingIdFieldWithNoValueReturnsNull() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		doReturn(null).when(mockPdxInstance).getField(eq(PdxInstanceWrapper.ID_FIELD_NAME));

		assertThat(wrapper.getId()).isNull();

		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(mockPdxInstance, times(1)).getField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(wrapper, never()).getAtIdentifier();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getIdFromPdxInstanceWithNoIdFieldCallsGetAtIdentifier() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(false).when(mockPdxInstance).hasField(any());
		doReturn(99).when(wrapper).getAtIdentifier();

		assertThat(wrapper.getId()).isEqualTo(99);

		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(mockPdxInstance, never()).getField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		verify(wrapper, times(1)).getAtIdentifier();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void getAtIdentifierFromPdxInstance() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		doReturn(true).when(mockPdxInstance).hasField(eq("isbn"));
		doReturn("isbn").when(mockPdxInstance).getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		doReturn("123456789").when(mockPdxInstance).getField(eq("isbn"));

		assertThat(wrapper.getAtIdentifier()).isEqualTo("123456789");

		verify(mockPdxInstance, times(1))
			.hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		verify(mockPdxInstance, times(1))
			.getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		verify(mockPdxInstance, times(1)).hasField(eq("isbn"));
		verify(mockPdxInstance, times(1)).getField(eq("isbn"));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test(expected = IllegalStateException.class)
	public void getAtIdentifierFromPdxInstanceWithNoDeclaredIdentity() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Account.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(false).when(mockPdxInstance).hasField(any());

		try {
			wrapper.getAtIdentifier();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("PdxInstance for type [%s] has no declared identifier",
				Account.class.getName());
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getClassName();
			verify(mockPdxInstance, times(2))
				.hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, times(1))
				.hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
			verify(mockPdxInstance, never()).getField(anyString());
			verifyNoMoreInteractions(mockPdxInstance);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void getAtIdentifierFromPdxInstanceWithNoId() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Account.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));

		try {
			wrapper.getAtIdentifier();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("PdxInstance for type [%s] has no id",
				Account.class.getName());
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getClassName();
			verify(mockPdxInstance, times(1))
				.hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, times(1))
				.hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
			verify(mockPdxInstance, never()).getField(any());
			verifyNoMoreInteractions(mockPdxInstance);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void getAtIdentifierFromPdxInstanceWithValidAtIdentifierAndIdentifierFieldButNoId() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Person.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		doReturn(false).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		doReturn(true).when(mockPdxInstance).hasField(eq("ssn"));
		doReturn("ssn").when(mockPdxInstance).getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		doReturn(null).when(mockPdxInstance).getField(eq("ssn"));

		try {
			wrapper.getAtIdentifier();
		}
		catch (IllegalStateException expected) {

			String expectedMessage = "PdxInstance for type [%s] has no value [null] for field [ssn] declared in [%s]";

			assertThat(expected).hasMessage(expectedMessage, Person.class.getName(),
				PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getClassName();
			verify(mockPdxInstance, times(2)).hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
			verify(mockPdxInstance, times(2)).hasField(eq("ssn"));
			verify(mockPdxInstance, times(2)).getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, times(2)).getField(eq("ssn"));
			verifyNoMoreInteractions(mockPdxInstance);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void getAtIdentifierFromPdxInstanceWithAtIdentifierReferringToInvalidIdentifierField() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		PdxInstanceWrapper wrapper = spy(new PdxInstanceWrapper(mockPdxInstance));

		doReturn(Person.class.getName()).when(mockPdxInstance).getClassName();
		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
		doReturn(false).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
		doReturn(false).when(mockPdxInstance).hasField(eq("ssn"));
		doReturn("ssn").when(mockPdxInstance).getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));

		try {
			wrapper.getAtIdentifier();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("PdxInstance for type [%s] has no field [ssn] declared in [%s]",
				Person.class.getName(), PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getClassName();
			verify(mockPdxInstance, times(2)).hasField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.ID_FIELD_NAME));
			verify(mockPdxInstance, times(2)).hasField(eq("ssn"));
			verify(mockPdxInstance, times(2)).getField(eq(PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME));
			verify(mockPdxInstance, never()).getField(eq("ssn"));
			verifyNoMoreInteractions(mockPdxInstance);
		}
	}

	@Test
	public void getObjectReturnsObject() throws JsonProcessingException {

		Account mockAccount = mock(Account.class);

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		String json = String.format("{ \"@type\": \"%s\", \"name\": \"Savings\"}", Account.class.getName());

		doReturn(JSONFormatter.JSON_CLASSNAME).when(mockPdxInstance).getClassName();
		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		doReturn(Account.class.getName()).when(mockPdxInstance).getField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mockPdxInstance));

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		doReturn(Optional.of(mockObjectMapper)).when(wrapper).getObjectMapper();
		doReturn(json).when(wrapper).jsonFormatterToJson(eq(mockPdxInstance));
		doReturn(mockAccount).when(mockObjectMapper).readValue(eq(json), eq(Account.class));

		assertThat(wrapper.getObject()).isEqualTo(mockAccount);

		verify(wrapper, atLeastOnce()).getDelegate();
		verify(wrapper, times(1)).getObjectMapper();
		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		verify(mockPdxInstance, times(1)).getField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		verify(wrapper, times(1)).jsonFormatterToJson(eq(mockPdxInstance));
		verify(mockObjectMapper, times(1)).readValue(eq(json), eq(Account.class));
		verify(mockPdxInstance, never()).getObject();
		verifyNoMoreInteractions(mockObjectMapper, mockPdxInstance);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObjectCallsPdxInstanceGetObjectWhenAtTypeFieldIsNotPresent() throws JsonProcessingException {

		Object value = new Object();

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(JSONFormatter.JSON_CLASSNAME).when(mockPdxInstance).getClassName();
		doReturn(false).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		doReturn(value).when(mockPdxInstance).getObject();

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mockPdxInstance));

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		doReturn(Optional.of(mockObjectMapper)).when(wrapper).getObjectMapper();

		assertThat(wrapper.getObject()).isEqualTo(value);

		verify(wrapper, atLeastOnce()).getDelegate();
		verify(wrapper, times(1)).getObjectMapper();
		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		verify(mockPdxInstance, never()).getField(anyString());
		verify(wrapper, never()).jsonFormatterToJson(any());
		verify(mockObjectMapper, never()).readValue(anyString(), any(Class.class));
		verify(mockPdxInstance, times(1)).getObject();
		verifyNoMoreInteractions(mockObjectMapper, mockPdxInstance);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObjectCallsPdxInstanceGetObjectWhenClassNameIsNotGemFireJson() throws JsonProcessingException {

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("non.existing.class.Name").when(mockPdxInstance).getClassName();
		doReturn("TEST").when(mockPdxInstance).getObject();

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mockPdxInstance));

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		doReturn(Optional.of(mockObjectMapper)).when(wrapper).getObjectMapper();

		assertThat(wrapper.getObject()).isEqualTo("TEST");

		verify(wrapper, atLeastOnce()).getDelegate();
		verify(wrapper, times(1)).getObjectMapper();
		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, never()).hasField(anyString());
		verify(mockPdxInstance, never()).getField(anyString());
		verify(wrapper, never()).jsonFormatterToJson(any());
		verify(mockObjectMapper, never()).readValue(anyString(), any(Class.class));
		verify(mockPdxInstance, times(1)).getObject();
		verifyNoMoreInteractions(mockObjectMapper, mockPdxInstance);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObjectCallsPdxInstanceGetObjectWhenExceptionIsThrown() throws JsonProcessingException {

		Object value = new Object();

		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		String json = String.format("{ \"@type\": \"%s\", \"name\": \"Checking\"}", Account.class.getName());

		doReturn(JSONFormatter.JSON_CLASSNAME).when(mockPdxInstance).getClassName();
		doReturn(true).when(mockPdxInstance).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		doReturn(Account.class.getName()).when(mockPdxInstance).getField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		doReturn(value).when(mockPdxInstance).getObject();

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mockPdxInstance));

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		doReturn(Optional.of(mockObjectMapper)).when(wrapper).getObjectMapper();
		doReturn(json).when(wrapper).jsonFormatterToJson(eq(mockPdxInstance));
		doThrow(new JsonGenerationException("TEST", mock(JsonGenerator.class)))
			.when(mockObjectMapper).readValue(anyString(), any(Class.class));

		assertThat(wrapper.getObject()).isEqualTo(value);

		verify(wrapper, times(1)).getObjectMapper();
		verify(mockPdxInstance, times(1)).getClassName();
		verify(mockPdxInstance, times(1)).hasField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		verify(mockPdxInstance, times(1)).getField(eq(PdxInstanceWrapper.AT_TYPE_FIELD_NAME));
		verify(wrapper, atLeastOnce()).getDelegate();
		verify(wrapper, times(1)).jsonFormatterToJson(eq(mockPdxInstance));
		verify(mockObjectMapper, times(1)).readValue(eq(json), eq(Account.class));
		verify(mockPdxInstance, times((1))).getObject();
		verifyNoMoreInteractions(mockObjectMapper, mockPdxInstance);
	}

	@Test
	public void getObjectCallsPdxInstanceGetObjectWhenObjectMapperIsNotPresent() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn("non.existing.class.Name").when(mockPdxInstance).getClassName();
		doReturn("MOCK").when(mockPdxInstance).getObject();

		PdxInstanceWrapper wrapper = spy(PdxInstanceWrapper.from(mockPdxInstance));

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockPdxInstance);

		doReturn(Optional.empty()).when(wrapper).getObjectMapper();

		assertThat(wrapper.getObject()).isEqualTo("MOCK");

		verify(wrapper, atLeastOnce()).getDelegate();
		verify(wrapper, times(1)).getObjectMapper();
		verify(wrapper, never()).jsonFormatterToJson(any());
		verify(mockPdxInstance, times(1)).getObject();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void createWriterCallsPdxInstanceCreateWriter() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		WritablePdxInstance mockWritablePdxInstance = mock(WritablePdxInstance.class);

		doReturn(mockWritablePdxInstance).when(mockPdxInstance).createWriter();

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).createWriter()).isEqualTo(mockWritablePdxInstance);

		verify(mockPdxInstance, times(1)).createWriter();
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void hasFieldCallsPdxInstanceHasField() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(true).when(mockPdxInstance).hasField("name");

		assertThat(PdxInstanceWrapper.from(mockPdxInstance).hasField("name")).isTrue();

		verify(mockPdxInstance, times(1)).hasField(eq("name"));
		verifyNoMoreInteractions(mockPdxInstance);
	}

	@Test
	public void sendToCallsPdxInstanceSendTo() throws IOException {

		SendablePdxInstance mockSendablePdxInstance = mock(SendablePdxInstance.class);

		PdxInstanceWrapper wrapper = PdxInstanceWrapper.from(mockSendablePdxInstance);

		DataOutput mockOut = mock(DataOutput.class);

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockSendablePdxInstance);

		wrapper.sendTo(mockOut);

		verify(mockSendablePdxInstance, times(1)).sendTo(eq(mockOut));
		verifyNoMoreInteractions(mockSendablePdxInstance);
		verifyNoMoreInteractions(mockOut);
	}

	interface Account {
		String getName();
	}

	interface Person { }

	interface SendablePdxInstance extends PdxInstance, Sendable { }

}
