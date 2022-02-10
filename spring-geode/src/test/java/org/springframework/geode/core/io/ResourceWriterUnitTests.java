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
 * Unit Tests for {@link ResourceWriter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceWriter
 * @since 1.3.1
 */
public class ResourceWriterUnitTests {

	@Test
	public void writeWithByteBufferCallsWriteWithByteArray() {

		byte[] data = { (byte) 0x1 };

		Resource mockResource = mock(Resource.class);

		ResourceWriter writer = mock(ResourceWriter.class);

		doCallRealMethod().when(writer).write(any(), any(ByteBuffer.class));

		writer.write(mockResource, ByteBuffer.wrap(data));

		verify(writer, times(1)).write(eq(mockResource), eq(data));
	}

	@Test
	public void thenWriteToNullReturnsThis() {

		ResourceWriter mockResourceWriter = mock(ResourceWriter.class);

		doCallRealMethod().when(mockResourceWriter).thenWriteTo(any());

		assertThat(mockResourceWriter.thenWriteTo(null)).isEqualTo(mockResourceWriter);
	}

	@Test
	public void thenWriteToResourceWriterReturnsComposite() {

		ResourceWriter thisMockResourceWriter = mock(ResourceWriter.class, "this");
		ResourceWriter thatMockResourceWriter = mock(ResourceWriter.class, "that");

		doCallRealMethod().when(thisMockResourceWriter).thenWriteTo(any());

		ResourceWriter composite = thisMockResourceWriter.thenWriteTo(thatMockResourceWriter);

		assertThat(composite).isNotNull();
		assertThat(composite).isNotEqualTo(thisMockResourceWriter);
		assertThat(composite).isNotEqualTo(thatMockResourceWriter);
	}

	@Test
	public void thenWriteToResourceWriterWritesToThis() {

		byte[] array = { (byte) 0xCA, (byte) 0xFE };

		Resource mockResource = mock(Resource.class);

		ResourceWriter thisMockResourceWriter = mock(ResourceWriter.class, "this");
		ResourceWriter thatMockResourceWriter = mock(ResourceWriter.class, "that");

		doCallRealMethod().when(thisMockResourceWriter).thenWriteTo(any());

		ResourceWriter composite = thisMockResourceWriter.thenWriteTo(thatMockResourceWriter);

		assertThat(composite).isNotNull();

		composite.write(mockResource, array);

		verify(thisMockResourceWriter, times(1)).write(eq(mockResource), eq(array));
		verify(thatMockResourceWriter, never()).write(any(), any(byte[].class));
		verifyNoInteractions(mockResource);
	}

	@Test
	public void thenWriteToResourceWriterWritesToThat() {

		byte[] array = { (byte) 0xBA, (byte) 0xBE };

		Resource mockResource = mock(Resource.class);

		ResourceWriter thisMockResourceWriter = mock(ResourceWriter.class, "this");
		ResourceWriter thatMockResourceWriter = mock(ResourceWriter.class, "that");

		doCallRealMethod().when(thisMockResourceWriter).thenWriteTo(any());
		doThrow(new UnhandledResourceException("TEST")).when(thisMockResourceWriter).write(eq(mockResource), eq(array));

		ResourceWriter composite = thisMockResourceWriter.thenWriteTo(thatMockResourceWriter);

		assertThat(composite).isNotNull();

		composite.write(mockResource, array);

		verify(thisMockResourceWriter, times(1)).write(eq(mockResource), eq(array));
		verify(thatMockResourceWriter, times(1)).write(any(), any(byte[].class));
		verifyNoInteractions(mockResource);
	}
}
