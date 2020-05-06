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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;

import org.apache.geode.pdx.PdxInstance;

/**
 * Unit Tests for {@link JSONFormatterPdxToJsonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.support.JSONFormatterPdxToJsonConverter
 * @since 1.3.0
 */
public class JSONFormatterPdxToJsonConverterUnitTests {

	@Test
	public void convertsObjectToJson() {

		String json = "{ \"name\": \"Jason Doe\" }";

		Object object = new Object();

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(json).when(converter).convertObjectToJson(eq(object));

		assertThat(converter.convert(object)).isEqualTo(json);

		verify(converter, times(1)).convertObjectToJson(eq(object));
		verify(converter, never()).convertPdxToJson(any());
	}

	@Test
	public void convertsPdxInstanceToJson() {

		String json = "{ \"name\": \"Jon Doe\" }";

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		JSONFormatterPdxToJsonConverter converter = spy(new JSONFormatterPdxToJsonConverter());

		doReturn(json).when(converter).convertPdxToJson(eq(mockPdxInstance));

		assertThat(converter.convertPdxToJson(mockPdxInstance)).isEqualTo(json);

		verify(converter, times(1)).convertPdxToJson(eq(mockPdxInstance));
		verify(converter, never()).convertObjectToJson(any());
		verifyNoInteractions(mockPdxInstance);
	}
}
