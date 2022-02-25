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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;

import org.apache.geode.pdx.PdxInstance;

import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter;

/**
 * Unit Tests for {@link JsonToPdxArrayConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.JsonToPdxArrayConverter
 * @since 1.3.0
 */
public class JsonToPdxArrayConverterUnitTests {

	@Test
	public void convertJsonByteArrayToPdxArray() {

		String json = "[{ \"name\": \"Jon Doe\" }, { \"name\": \"Jane Doe\" }]";

		PdxInstance mockPdxInstanceOne = mock(PdxInstance.class);
		PdxInstance mockPdxInstanceTwo = mock(PdxInstance.class);

		PdxInstance[] pdxArray = ArrayUtils.asArray(mockPdxInstanceOne, mockPdxInstanceTwo);

		JacksonJsonToPdxConverter converter = mock(JacksonJsonToPdxConverter.class);

		doCallRealMethod().when(converter).convert(any(byte[].class));
		doReturn(pdxArray).when(converter).convert(anyString());

		assertThat(converter.convert(json.getBytes())).isEqualTo(pdxArray);

		verify(converter, times(1)).convert(eq(json));
		verifyNoInteractions(mockPdxInstanceOne, mockPdxInstanceTwo);
	}
}
