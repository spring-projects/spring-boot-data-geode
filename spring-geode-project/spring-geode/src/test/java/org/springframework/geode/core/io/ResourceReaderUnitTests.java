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
package org.springframework.geode.core.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.ByteBuffer;

import org.junit.Test;

import org.springframework.core.io.Resource;

/**
 * Unit Tests for {@link ResourceReader}.
 *
 * @author John Blum
 * @see java.nio.ByteBuffer
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceReader
 * @since 1.3.1
 */
public class ResourceReaderUnitTests {

	@Test
	public void readIntoByteBufferCallsRead() {

		byte[] array = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		Resource mockResource = mock(Resource.class);

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		doReturn(array).when(mockResourceReader).read(eq(mockResource));
		doCallRealMethod().when(mockResourceReader).readIntoByteBuffer(any());

		ByteBuffer byteBuffer = mockResourceReader.readIntoByteBuffer(mockResource);

		assertThat(byteBuffer).isNotNull();
		assertThat(byteBuffer.array()).isEqualTo(array);

		verify(mockResourceReader, times(1)).read(eq(mockResource));
		verifyNoInteractions(mockResource);
	}

	@Test
	public void thenReadFromNullReturnsThis() {

		ResourceReader mockResourceReader = mock(ResourceReader.class);

		doCallRealMethod().when(mockResourceReader).thenReadFrom(any());

		assertThat(mockResourceReader.thenReadFrom(null)).isEqualTo(mockResourceReader);
	}

	@Test
	public void thenReadFromResourceReaderReturnsComposite() {

		ResourceReader thisMockResourceReader = mock(ResourceReader.class, "this");
		ResourceReader thatMockResourceReader = mock(ResourceReader.class, "that");

		doCallRealMethod().when(thisMockResourceReader).thenReadFrom(any());

		ResourceReader composite = thisMockResourceReader.thenReadFrom(thatMockResourceReader);

		assertThat(composite).isNotNull();
		assertThat(composite).isNotEqualTo(thisMockResourceReader);
		assertThat(composite).isNotEqualTo(thatMockResourceReader);
	}

	@Test
	public void thenReadFromResourceReaderReadsFromThis() {

		byte[] array = { (byte) 0xBA, (byte) 0xBE };

		Resource mockResource = mock(Resource.class);

		ResourceReader thisMockResourceReader = mock(ResourceReader.class, "this");
		ResourceReader thatMockResourceReader = mock(ResourceReader.class, "that");

		doCallRealMethod().when(thisMockResourceReader).thenReadFrom(any());
		doReturn(array).when(thisMockResourceReader).read(eq(mockResource));

		ResourceReader composite = thisMockResourceReader.thenReadFrom(thatMockResourceReader);

		assertThat(composite).isNotNull();
		assertThat(composite.read(mockResource)).isEqualTo(array);

		verify(thisMockResourceReader, times(1)).read(eq(mockResource));
		verify(thatMockResourceReader, never()).read(any());
		verifyNoInteractions(mockResource);
	}

	@Test
	public void thenReadFromResourceReaderReadsFromThat() {

		byte[] array = { (byte) 0xFA, (byte) 0xDE };

		Resource mockResource = mock(Resource.class);

		ResourceReader thisMockResourceReader = mock(ResourceReader.class, "this");
		ResourceReader thatMockResourceReader = mock(ResourceReader.class, "that");

		doCallRealMethod().when(thisMockResourceReader).thenReadFrom(any());
		doThrow(new UnhandledResourceException("TEST")).when(thisMockResourceReader).read(eq(mockResource));
		doReturn(array).when(thatMockResourceReader).read(eq(mockResource));

		ResourceReader composite = thisMockResourceReader.thenReadFrom(thatMockResourceReader);

		assertThat(composite).isNotNull();
		assertThat(composite.read(mockResource)).isEqualTo(array);

		verify(thisMockResourceReader, times(1)).read(eq(mockResource));
		verify(thatMockResourceReader, times(1)).read(eq(mockResource));
		verifyNoInteractions(mockResource);
	}
}
