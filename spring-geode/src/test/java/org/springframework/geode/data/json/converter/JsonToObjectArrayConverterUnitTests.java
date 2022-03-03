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

import org.junit.Test;

import example.app.crm.model.Customer;

/**
 * Unit Tests for {@link JsonToObjectArrayConverter}
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.data.json.converter.JsonToObjectArrayConverter
 * @since 1.3.0
 */
public class JsonToObjectArrayConverterUnitTests {

	@Test
	public void convertByteArrayCallsConvertJsonString() {

		String json = "[{\"name\":\"Jon Doe\"},{\"name\":\"Jane Doe\"}]";

		Object[] array = {
			Customer.newCustomer(1L, "Jon Doe"),
			Customer.newCustomer(2L, "Jane Doe")
		};

		JsonToObjectArrayConverter converter = mock(JsonToObjectArrayConverter.class);

		doCallRealMethod().when(converter).convert(any(byte[].class));
		doReturn(array).when(converter).convert(eq(json));

		assertThat(converter.convert(json.getBytes())).containsExactly(array);

		verify(converter, times(1)).convert(eq(json));
	}
}
