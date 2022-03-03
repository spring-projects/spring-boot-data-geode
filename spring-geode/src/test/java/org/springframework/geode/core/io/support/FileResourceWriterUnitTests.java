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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.junit.Test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.geode.core.io.ResourceDataAccessException;
import org.springframework.geode.core.io.ResourceWriteException;

/**
 * Unit Tests for {@link FileResourceWriter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.support.FileResourceWriter
 * @since 1.3.1
 */
public class FileResourceWriterUnitTests {

	@Test
	public void doWriteBytesToResourceOutputStream() throws IOException {

		byte[] data = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);

		FileResourceWriter writer = spy(new FileResourceWriter());

		WritableResource mockResource = mock(WritableResource.class);

		doReturn(true).when(mockResource).isFile();
		doReturn(true).when(mockResource).isWritable();
		doReturn(out).when(mockResource).getOutputStream();

		writer.write(mockResource, data);

		assertThat(out.toByteArray()).isEqualTo(data);

		verify(mockResource, times(1)).isFile();
		verify(mockResource, times(1)).getOutputStream();
		verify(writer, times(1)).doWrite(eq(out), eq(data));
		verifyNoMoreInteractions(mockResource);
	}

	@Test(expected = ResourceWriteException.class)
	public void doWriteHandlesIOExceptionThrowsDataAccessResourceFailureException() throws IOException {

		byte[] data = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		OutputStream mockOutputStream = mock(OutputStream.class);

		doThrow(new IOException("TEST")).when(mockOutputStream).write(any(byte[].class), anyInt(), anyInt());

		FileResourceWriter writer = spy(new FileResourceWriter());

		doReturn(data.length).when(writer).getBufferSize();

		try {
			writer.doWrite(mockOutputStream, data);
		}
		catch (ResourceWriteException expected) {

			assertThat(expected)
				.hasMessageStartingWith("Failed to write data (%d byte(s)) to Resource using [%s]",
					data.length, writer.getClass().getName());

			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockOutputStream, times(1)).write(eq(data), eq(0), eq(data.length));
			verify(mockOutputStream, times(1)).flush();
			verify(mockOutputStream, times(1)).close();
			verifyNoMoreInteractions(mockOutputStream);
		}
	}

	@Test
	public void isAbleToHandleFileResource() {

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).isFile();

		FileResourceWriter writer = new FileResourceWriter();

		assertThat(writer.getResource().orElse(null)).isNull();
		assertThat(writer.isAbleToHandle(mockResource)).isTrue();
		assertThat(writer.getResource().orElse(null)).isEqualTo(mockResource);

		verify(mockResource, times(1)).isFile();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isAbleToHandleNonFileResource() {

		Resource mockResource = mock(Resource.class);

		doReturn(false).when(mockResource).isFile();

		FileResourceWriter writer = new FileResourceWriter();

		assertThat(writer.getResource().orElse(null)).isNull();
		assertThat(writer.isAbleToHandle(mockResource)).isFalse();
		assertThat(writer.getResource().orElse(null)).isNull();

		verify(mockResource, times(1)).isFile();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isAbleToHandleNullResource() {

		FileResourceWriter writer = new FileResourceWriter();

		assertThat(writer.getResource().orElse(null)).isNull();
		assertThat(writer.isAbleToHandle(null)).isFalse();
		assertThat(writer.getResource().orElse(null)).isNull();
	}

	@Test
	public void getBufferSizeEqualsDefault() {
		assertThat(new FileResourceWriter().getBufferSize())
			.isEqualByComparingTo(FileResourceWriter.DEFAULT_BUFFER_SIZE);
	}

	@Test
	public void getOpenOptionsContainsCreateTruncateWrite() {
		assertThat(new FileResourceWriter().getOpenOptions())
			.containsExactly(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	}

	@Test
	public void decorateWithBufferedOutputStream() throws IOException {

		FileResourceWriter writer = spy(new FileResourceWriter());

		try (OutputStream mockOutputStream = new BufferedOutputStream(mock(OutputStream.class))) {
			assertThat(writer.decorate(mockOutputStream)).isSameAs(mockOutputStream);
		}
		finally {
			verify(writer, never()).newFileOutputStream();
		}
	}

	@Test
	public void decorateWithOutputStream() throws IOException {

		OutputStream mockOutputStream = mock(OutputStream.class);

		FileResourceWriter writer = spy(new FileResourceWriter());

		try (OutputStream out = writer.decorate(mockOutputStream)) {
			assertThat(out).isNotSameAs(mockOutputStream);
			assertThat(out).isInstanceOf(BufferedOutputStream.class);
		}
		finally {
			verify(writer, never()).newFileOutputStream();
		}
	}

	@Test
	public void decorateWithNullCallsNewFileOutputStream() {

		FileResourceWriter writer = spy(new FileResourceWriter());

		OutputStream mockOutputStream = mock(OutputStream.class);

		doReturn(mockOutputStream).when(writer).newFileOutputStream();

		assertThat(writer.decorate(null)).isEqualTo(mockOutputStream);

		verify(writer, times(1)).newFileOutputStream();
	}

	@Test(expected = IllegalStateException.class)
	public void newFileOutputStreamWithNonExistingResourceThrowsIllegalStateException() {

		FileResourceWriter writer = spy(new FileResourceWriter());

		assertThat(writer.getResource().orElse(null)).isNull();

		try {
			writer.newFileOutputStream();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [null] is not a file based resource");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(writer, never()).isAbleToHandle(any());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void newFileOutputStreamWithNonFileResourceThrowsIllegalStateException() {

		Resource mockResource = mock(Resource.class);

		doReturn("MOCK").when(mockResource).getDescription();
		doReturn(false).when(mockResource).isFile();

		FileResourceWriter writer = spy(new FileResourceWriter());

		doReturn(Optional.of(mockResource)).when(writer).getResource();

		try {
			writer.newFileOutputStream();
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [MOCK] is not a file based resource");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(writer, times(1)).isAbleToHandle(eq(mockResource));
			verify(writer, never()).getOpenOptions();
		}
	}

	@Test(expected = ResourceDataAccessException.class)
	public void newFileOutputStreamHandlesIOExceptionThrowsResourceDataAccessException() throws IOException {

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).isFile();
		doReturn("FILE").when(mockResource).getDescription();
		doThrow(new IOException("TEST")).when(mockResource).getFile();

		FileResourceWriter writer = spy(new FileResourceWriter());

		doReturn(Optional.of(mockResource)).when(writer).getResource();

		try {
			writer.newFileOutputStream();
		}
		catch (ResourceDataAccessException expected) {

			assertThat(expected).hasMessageStartingWith("Failed to access the Resource [FILE] as a file");
			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(writer, times(1)).getResource();
			verify(writer, times(1)).isAbleToHandle(eq(mockResource));
			verify(writer, never()).getOpenOptions();
			verify(mockResource, times(1)).isFile();
			verify(mockResource, times(1)).getFile();
		}
	}

	@Test
	public void newFileOutputStreamIsSuccessful() throws IOException {

		File file = new File(System.getProperty("java.io.tmpdir"), "test");

		file.deleteOnExit();

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).isFile();
		doReturn(file).when(mockResource).getFile();

		FileResourceWriter writer = spy(new FileResourceWriter());

		doReturn(Optional.of(mockResource)).when(writer).getResource();

		try (OutputStream out = writer.newFileOutputStream()) {
			assertThat(out).isInstanceOf(BufferedOutputStream.class);
		}
		finally {
			verify(writer, times(1)).getResource();
			verify(writer, times(1)).isAbleToHandle(eq(mockResource));
			verify(writer, times(1)).getOpenOptions();
			verify(mockResource, times(1)).isFile();
			verify(mockResource, times(1)).getFile();
		}
	}
}
