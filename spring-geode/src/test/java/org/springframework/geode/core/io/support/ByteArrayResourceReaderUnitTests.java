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
package org.springframework.geode.core.io.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import org.springframework.core.io.Resource;

/**
 * Unit Tests for {@link ByteArrayResourceReader}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.support.ByteArrayResourceReader
 * @since 1.3.1
 */
public class ByteArrayResourceReaderUnitTests {

	@Test
	public void doReadResourceReturnsByteArray() throws IOException {

		byte[] data = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		ByteArrayInputStream in = spy(new ByteArrayInputStream(data));

		ByteArrayResourceReader reader = spy(new ByteArrayResourceReader());

		Resource mockResource = mock(Resource.class);

		doReturn(in).when(mockResource).getInputStream();
		doReturn(2).when(reader).getBufferSize();

		assertThat(reader.read(mockResource)).isEqualTo(data);

		verify(mockResource, times(1)).getInputStream();
		verify(in, times(1)).available();
		verify(in, times(3)).read(isA(byte[].class));
		verify(reader, times(1)).doRead(eq(in));
	}

	@Test
	public void getBufferSizeIsDefault() {
		assertThat(new ByteArrayResourceReader().getBufferSize())
			.isEqualTo(ByteArrayResourceReader.DEFAULT_BUFFER_SIZE);
	}
}
