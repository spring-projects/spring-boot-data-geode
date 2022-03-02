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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;
import org.mockito.InOrder;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

/**
 * Unit Tests for {@link AbstractResourceWriter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.WritableResource
 * @see org.springframework.geode.core.io.AbstractResourceWriter
 * @since 1.3.1
 */
public class AbstractResourceWriterUnitTests {

	@Test
	public void writeToWritableResourceCallsDoWrite() throws IOException {

		byte[] array = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		OutputStream mockOutputStream = mock(OutputStream.class);

		WritableResource mockResource = mock(WritableResource.class);

		doCallRealMethod().when(mockResourceWriter).write(eq(mockResource), eq(array));
		doAnswer(invocation -> invocation.getArgument(0)).when(mockResourceWriter).preProcess(any());
		doReturn(true).when(mockResourceWriter).isAbleToHandle(eq(mockResource));
		doReturn(mockOutputStream).when(mockResource).getOutputStream();
		doReturn(true).when(mockResource).isWritable();

		mockResourceWriter.write(mockResource, array);

		InOrder order = inOrder(mockResourceWriter);

		order.verify(mockResourceWriter, times(1)).isAbleToHandle(eq(mockResource));
		order.verify(mockResourceWriter, times(1)).preProcess(eq(mockResource));
		order.verify(mockResourceWriter, times(1)).doWrite(eq(mockOutputStream), eq(array));

		verify(mockResource, times(1)).getOutputStream();
		verify(mockOutputStream, times(1)).close();
		verifyNoMoreInteractions(mockOutputStream, mockResource);
	}

	@Test(expected = UnhandledResourceException.class)
	public void writeToNonWritableResourceThrowsUnhandledResourceException() throws IOException {

		byte[] array = { (byte) 0x01 };

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		Resource mockResource = mock(Resource.class);

		doCallRealMethod().when(mockResourceWriter).write(any(), any(byte[].class));
		doReturn("MOCK").when(mockResource).getDescription();

		try {
			mockResourceWriter.write(mockResource, array);
		}
		catch (UnhandledResourceException expected) {

			assertThat(expected).hasMessage("Unable to handle Resource [MOCK]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResourceWriter, never()).isAbleToHandle(any());
			verify(mockResourceWriter, never()).preProcess(any());
			verify(mockResourceWriter, never()).doWrite(any(), any());
			verify(mockResource, times(1)).getDescription();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test(expected = UnhandledResourceException.class)
	public void writeToNullResourceThrowsUnhandledResourceException() throws IOException {

		byte[] array = { (byte) 0x02 };

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		doCallRealMethod().when(mockResourceWriter).write(any(), any(byte[].class));

		try {
			mockResourceWriter.write(null, array);
		}
		catch (UnhandledResourceException expected) {

			assertThat(expected).hasMessage("Unable to handle Resource [null]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResourceWriter, never()).isAbleToHandle(any());
			verify(mockResourceWriter, never()).preProcess(any());
			verify(mockResourceWriter, never()).doWrite(any(), any());
		}
	}

	@Test(expected = UnhandledResourceException.class)
	public void writeToUnhandledResourceThrowsUnhandledResourceException() throws IOException {

		byte[] array = { (byte) 0x04 };

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		WritableResource mockResource = mock(WritableResource.class);

		doCallRealMethod().when(mockResourceWriter).write(any(), any(byte[].class));
		doReturn(false).when(mockResourceWriter).isAbleToHandle(eq(mockResource));
		doReturn(true).when(mockResource).isWritable();
		doReturn("MOCK").when(mockResource).getDescription();

		try {
			mockResourceWriter.write(mockResource, array);
		}
		catch (UnhandledResourceException expected) {

			assertThat(expected).hasMessage("Unable to handle Resource [MOCK]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResourceWriter, times(1)).isAbleToHandle(eq(mockResource));
			verify(mockResourceWriter, never()).preProcess(eq(mockResource));
			verify(mockResourceWriter, never()).doWrite(any(), any());
			verify(mockResource, times(1)).getDescription();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test(expected = ResourceWriteException.class)
	public void writeThrowsResourceWriteExceptionForIoException() throws IOException {

		byte[] array = { (byte) 0x08 };

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		OutputStream mockOutputStream = mock(OutputStream.class);

		WritableResource mockResource = mock(WritableResource.class);

		doCallRealMethod().when(mockResourceWriter).write(any(), any(byte[].class));
		doReturn(true).when(mockResourceWriter).isAbleToHandle(eq(mockResource));
		doAnswer(invocation -> invocation.getArgument(0)).when(mockResourceWriter).preProcess(any());
		doThrow(new IOException("TEST")).when(mockResourceWriter).doWrite(eq(mockOutputStream), eq(array));
		doReturn("MOCK").when(mockResource).getDescription();
		doReturn(mockOutputStream).when(mockResource).getOutputStream();

		try {
			mockResourceWriter.write(mockResource, array);
		}
		catch (ResourceWriteException expected) {

			assertThat(expected).hasMessageStartingWith("Failed to write to Resource [MOCK]");
			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResourceWriter, times(1)).isAbleToHandle(eq(mockResource));
			verify(mockResourceWriter, times(1)).preProcess(eq(mockResource));
			verify(mockResourceWriter, times(1)).doWrite(eq(mockOutputStream), eq(array));
			verify(mockResource, times(1)).getDescription();
			verify(mockResource, times(1)).getOutputStream();
			verify(mockOutputStream, times(1)).close();
			verifyNoMoreInteractions(mockOutputStream, mockResource);
		}
	}

	@Test
	public void isAbleToHandleNonNullResourceReturnsTrue() {

		Resource mockResource = mock(Resource.class);

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		doCallRealMethod().when(mockResourceWriter).isAbleToHandle(any());

		assertThat(mockResourceWriter.isAbleToHandle(mockResource)).isTrue();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void isAbleToHandleNullResourceReturnsFalse() {

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		doCallRealMethod().when(mockResourceWriter).isAbleToHandle(any());

		assertThat(mockResourceWriter.isAbleToHandle(null)).isFalse();
	}

	@Test
	public void preProcessWritableResourceReturnsWritableResource() {

		WritableResource mockResource = mock(WritableResource.class);

		AbstractResourceWriter mockResourceWriter = mock(AbstractResourceWriter.class);

		doCallRealMethod().when(mockResourceWriter).preProcess(any());

		assertThat(mockResourceWriter.preProcess(mockResource)).isSameAs(mockResource);
		assertThat(mockResourceWriter.preProcess(null)).isNull();

		verify(mockResourceWriter, times(1)).preProcess(eq(mockResource));
		verify(mockResourceWriter, times(1)).preProcess(isNull());
		verifyNoInteractions(mockResource);
	}
}
