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
package org.springframework.geode.data.json.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link ObjectArrayToJsonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.data.json.converter.ObjectArrayToJsonConverter
 * @since 1.3.0
 */
public class ObjectArrayToJsonConverterUnitTests {

	@Test
	public void convertObjectArrayCallsConvertIterable() {

		String json = "[{ \"name\": \"Jon Doe\"}, { \"name\": \"Jane Doe\"}]";

		ObjectArrayToJsonConverter mockConverter = mock(ObjectArrayToJsonConverter.class);

		doCallRealMethod().when(mockConverter).convert(ArgumentMatchers.<Object[]>any());
		doReturn(json).when(mockConverter).convert(any(Iterable.class));

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");
		Customer janeDoe = Customer.newCustomer(2L, "Jane Doe");

		assertThat(mockConverter.convert(jonDoe, janeDoe)).isEqualTo(json);

		verify(mockConverter, times(1)).convert(eq(Arrays.asList(jonDoe, janeDoe)));
	}

	@Test
	public void convertEmptyObjectArrayToEmptyJsonArray() {

		String json = "[]";

		ObjectArrayToJsonConverter mockConverter = mock(ObjectArrayToJsonConverter.class);

		doCallRealMethod().when(mockConverter).convert(ArgumentMatchers.<Object[]>any());
		doReturn(json).when(mockConverter).convert(any(Iterable.class));

		assertThat(mockConverter.convert()).isEqualTo(json);

		verify(mockConverter, times(1)).convert(eq(Collections.emptyList()));
	}

	@Test
	public void convertNullObjectArrayIsNullSafeAndReturnsEmptyJsonArray() {

		String json = "[]";

		ObjectArrayToJsonConverter mockConverter = mock(ObjectArrayToJsonConverter.class);

		doCallRealMethod().when(mockConverter).convert(ArgumentMatchers.<Object[]>any());
		doReturn(json).when(mockConverter).convert(any(Iterable.class));

		assertThat(mockConverter.convert((Object[]) null)).isEqualTo(json);

		verify(mockConverter, times(1)).convert(eq(Collections.emptyList()));
	}
}
