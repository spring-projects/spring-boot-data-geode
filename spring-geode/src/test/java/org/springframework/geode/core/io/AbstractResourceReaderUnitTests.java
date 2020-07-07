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
package org.springframework.geode.core.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Unit Tests for {@link AbstractResourceReader}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.AbstractResourceReader
 * @since 1.3.1
 */
public class AbstractResourceReaderUnitTests {

	@Test
	public void readCallsDoRead() throws IOException {

		byte[] array = { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };

		AbstractResourceReader mockResourceReader = mock(AbstractResourceReader.class);

		InputStream mockInputStream = mock((InputStream.class));

		Resource mockResource = mock(Resource.class);

		doCallRealMethod().when(mockResourceReader).read(any());
		doReturn(true).when(mockResourceReader).isAbleToHandle(eq(mockResource));
		doReturn(array).when(mockResourceReader).doRead(eq(mockInputStream));
		doReturn(mockInputStream).when(mockResource).getInputStream();

		assertThat(mockResourceReader.read(mockResource)).isEqualTo(array);

		verify(mockResourceReader, times(1)).isAbleToHandle(eq(mockResource));
		verify(mockResourceReader, times(1)).doRead(eq(mockInputStream));
		verify(mockInputStream, times(1)).close();
		verify(mockResource, times(1)).getInputStream();
		verifyNoMoreInteractions(mockInputStream, mockResource);
	}

	@Test(expected = UnhandledResourceException.class)
	public void readFromNullResourceThrowsUnhandledResourceException() {

		AbstractResourceReader mockResourceReader = mock(AbstractResourceReader.class);

		doCallRealMethod().when(mockResourceReader).read(any());

		try {
			mockResourceReader.read(null);
		}
		catch (UnhandledResourceException expected) {

			assertThat(expected).hasMessage("Unable to handle Resource [null]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = DataAccessResourceFailureException.class)
	public void readThrowsDataAccessResourceFailureExceptionOnIoException() throws IOException {

		AbstractResourceReader mockResourceReader = mock(AbstractResourceReader.class);

		InputStream mockInputStream = mock((InputStream.class));

		Resource mockResource = mock(Resource.class);

		doCallRealMethod().when(mockResourceReader).read(any());
		doReturn(true).when(mockResourceReader).isAbleToHandle(eq(mockResource));
		doThrow(new IOException("TEST")).when(mockResourceReader).doRead(eq(mockInputStream));
		doReturn("MOCK").when(mockResource).getDescription();
		doReturn(mockInputStream).when(mockResource).getInputStream();

		try {
			mockResourceReader.read(mockResource);
		}
		catch (DataAccessResourceFailureException expected) {

			assertThat(expected).hasMessageStartingWith("Failed to read from Resource [MOCK]");
			assertThat(expected).hasCauseInstanceOf(IOException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResourceReader, times(1)).doRead(eq(mockInputStream));
			verify(mockInputStream, times(1)).close();
			verify(mockResource, times(1)).getDescription();
			verify(mockResource, times(1)).getInputStream();
			verifyNoMoreInteractions(mockInputStream, mockResource);
		}
	}

	@Test
	public void isAbleToHandleNonNullResourceReturnsTrue() {

		Resource mockResource = mock(Resource.class);

		AbstractResourceReader mockResourceReader = mock(AbstractResourceReader.class);

		doCallRealMethod().when(mockResourceReader).isAbleToHandle(any());

		assertThat(mockResourceReader.isAbleToHandle(mockResource)).isTrue();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void isAbleToHandleNullResourceReturnsFalse() {

		AbstractResourceReader mockResourceReader = mock(AbstractResourceReader.class);

		doCallRealMethod().when(mockResourceReader).isAbleToHandle(any());

		assertThat(mockResourceReader.isAbleToHandle(null)).isFalse();
	}
}
