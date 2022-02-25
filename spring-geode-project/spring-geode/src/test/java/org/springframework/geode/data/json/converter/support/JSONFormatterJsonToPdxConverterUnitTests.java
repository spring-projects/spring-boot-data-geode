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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.geode.pdx.JSONFormatterException;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.geode.data.json.converter.JsonToObjectConverter;
import org.springframework.geode.pdx.ObjectPdxInstanceAdapter;
import org.springframework.geode.pdx.PdxInstanceWrapper;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link JSONFormatterJsonToPdxConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.JsonToObjectConverter
 * @see org.springframework.geode.data.json.converter.support.JSONFormatterJsonToPdxConverter
 * @see org.springframework.geode.pdx.ObjectPdxInstanceAdapter
 * @see org.springframework.geode.pdx.PdxInstanceWrapper
 * @since 1.3.0
 */
public class JSONFormatterJsonToPdxConverterUnitTests {

	@Test
	public void convertCallsConvertJsonToPdx() {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		JSONFormatterJsonToPdxConverter converter = spy(new JSONFormatterJsonToPdxConverter());

		doReturn(mockPdxInstance).when(converter).jsonFormatterFromJson(json);

		PdxInstance pdx = converter.convert(json);

		assertThat(pdx).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) pdx).getDelegate()).isEqualTo(mockPdxInstance);

		verify(converter, times(1)).convertJsonToPdx(eq(json));
		verify(converter, times(1)).jsonFormatterFromJson(eq(json));
		verify(converter, times(1)).wrap(eq(mockPdxInstance));
	}

	@Test
	public void convertCallsConvertJsonToObjectToPdx() {

		String json = "{ \"name\": \"Jon Doe\" }";

		JsonToObjectConverter mockJsonToObjectConverter = mock(JsonToObjectConverter.class);

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		JSONFormatterJsonToPdxConverter converter = spy(new JSONFormatterJsonToPdxConverter());

		doThrow(new JSONFormatterException("TEST")).when(converter).convertJsonToPdx(eq(json));
		doReturn(mockJsonToObjectConverter).when(converter).getJsonToObjectConverter();
		doReturn(jonDoe).when(mockJsonToObjectConverter).convert(eq(json));

		PdxInstance pdx = converter.convert(json);

		assertThat(pdx).isInstanceOf(ObjectPdxInstanceAdapter.class);
		assertThat(pdx.getObject()).isSameAs(jonDoe);

		verify(converter, times(1)).convertJsonToPdx(eq(json));
		verify(converter, times(1)).convertJsonToObjectToPdx(eq(json));
		verify(converter, times(1)).getJsonToObjectConverter();
		verify(mockJsonToObjectConverter, times(1)).convert(eq(json));
	}
}
