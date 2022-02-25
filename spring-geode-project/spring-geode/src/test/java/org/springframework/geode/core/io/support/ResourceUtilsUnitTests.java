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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

/**
 * Unit Tests for {@link ResourceUtils}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.WritableResource
 * @see org.springframework.geode.core.io.support.ResourceUtils
 * @since 1.3.1
 */
public class ResourceUtilsUnitTests {

	@Test
	public void asStrictlyWritableResourceReturnsWritableResourceFromResource() {

		WritableResource mockResource = mock(WritableResource.class);

		doReturn(true).when(mockResource).isWritable();

		assertThat(ResourceUtils.asStrictlyWritableResource(mockResource)).isEqualTo(mockResource);

		verify(mockResource, times(1)).isWritable();
		verifyNoMoreInteractions(mockResource);
	}

	@Test(expected = IllegalStateException.class)
	public void asStrictlyWritableResourceFromResourceThrowsIllegalStateException() {

		Resource mockResource = mock(Resource.class);

		doReturn("MOCK").when(mockResource).getDescription();

		try {
			ResourceUtils.asStrictlyWritableResource(mockResource);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [MOCK] is not writable");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResource, times(1)).getDescription();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void asStrictlyWritableResourceFromNullThrowsIllegalStateException() {

		try {
			ResourceUtils.asStrictlyWritableResource(null);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [null] is not writable");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void asStrictlyWritableResourceFromNonWritableWritableResourceInstanceThrowsIllegalStateException() {

		WritableResource mockResource = mock(WritableResource.class);

		doReturn("TEST").when(mockResource).getDescription();
		doReturn(false).when(mockResource).isWritable();

		try {
			ResourceUtils.asStrictlyWritableResource(mockResource);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Resource [TEST] is not writable");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResource, times(1)).getDescription();
			verify(mockResource, times(1)).isWritable();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test
	public void asWritableResourceWithWritableResourceInstance() {

		WritableResource mockResource = mock(WritableResource.class);

		assertThat(ResourceUtils.asWritableResource(mockResource).orElse(null)).isEqualTo(mockResource);

		verify(mockResource, never()).isWritable();
	}

	@Test
	public void asWritableResourceWithResourceInstance() {

		Resource mockResource = mock(Resource.class);

		assertThat(ResourceUtils.asWritableResource(mockResource).orElse(null)).isNull();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void asWritableResourceWithNullIsNullSafe() {
		assertThat(ResourceUtils.asWritableResource(null).isPresent()).isFalse();
	}

	@Test
	public void isNotEmptyWithNonEmptyByteArrayReturnsTrue() {

		byte[] array = {
			(byte) 0xCA,
			(byte) 0xFE,
			(byte) 0xBA,
			(byte) 0xBE
		};

		assertThat(ResourceUtils.isNotEmpty(array)).isTrue();
	}

	@Test
	public void isNotEmptyWithEmptyByteArrayReturnsFalse() {
		assertThat(ResourceUtils.isNotEmpty(new byte[0])).isFalse();
	}

	@Test
	public void isNotEmptyWithNullByteArrayReturnsFalse() {
		assertThat(ResourceUtils.isNotEmpty(null)).isFalse();
	}

	@Test
	public void isReadableWithReadableResourceReturnsTrue() {

		Resource mockResource = mock(Resource.class);

		doReturn(true).when(mockResource).isReadable();

		assertThat(ResourceUtils.isReadable(mockResource)).isTrue();

		verify(mockResource, times(1)).isReadable();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isReadableWithNonReadableResourceReturnsFalse() {

		Resource mockResource = mock(Resource.class);

		doReturn(false).when(mockResource).isReadable();

		assertThat(ResourceUtils.isReadable(mockResource)).isFalse();

		verify(mockResource, times(1)).isReadable();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isReadableWithNullResourceIsNullSafeReturnsFalse() {
		assertThat(ResourceUtils.isReadable(null)).isFalse();
	}

	@Test
	public void isWritableWithWritableResourceReturnsTrue() {

		WritableResource mockResource = mock(WritableResource.class);

		doReturn(true).when(mockResource).isWritable();

		assertThat(ResourceUtils.isWritable(mockResource)).isTrue();

		verify(mockResource, times(1)).isWritable();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isWritableWithNonWritableResourceReturnsFalse() {

		WritableResource mockResource = mock(WritableResource.class);

		doReturn(false).when(mockResource).isWritable();

		assertThat(ResourceUtils.isWritable(mockResource)).isFalse();

		verify(mockResource, times(1)).isWritable();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void isWritableWithNonWritableResourceTypeReturnsFalse() {

		Resource mockResource = mock(Resource.class);

		assertThat(ResourceUtils.isWritable(mockResource)).isFalse();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void isWritableWithNullResourceIsNullSafeReturnsFalse() {
		assertThat(ResourceUtils.isWritable(null)).isFalse();
	}

	@Test
	public void nullSafeDescriptionWithNonNullResource() {

		Resource mockResource = mock(Resource.class);

		doReturn("TEST").when(mockResource).getDescription();

		assertThat(ResourceUtils.nullSafeGetDescription(mockResource)).isEqualTo("TEST");

		verify(mockResource, times(1)).getDescription();
		verifyNoMoreInteractions(mockResource);
	}

	@Test
	public void nullSafeGetDescriptionWithNullResource() {
		assertThat(ResourceUtils.nullSafeGetDescription(null)).isEqualTo(null);
	}
}
