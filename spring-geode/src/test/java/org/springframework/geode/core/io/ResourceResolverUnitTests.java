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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import org.apache.shiro.util.CollectionUtils;

import org.springframework.core.io.Resource;

/**
 * Unit Tests for {@link ResourceResolver}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceResolver
 * @since 1.3.1
 */
public class ResourceResolverUnitTests {

	@Test
	public void getClassLoaderIsPresent() {

		ResourceResolver resourceResolver = mock(ResourceResolver.class);

		doCallRealMethod().when(resourceResolver).getClassLoader();

		Set<ClassLoader> classLoaders = CollectionUtils.asSet(
			Thread.currentThread().getContextClassLoader(),
			getClass().getClassLoader(),
			ClassLoader.getSystemClassLoader()
		);

		assertThat(classLoaders).contains(resourceResolver.getClassLoader().orElse(null));
	}

	@Test
	public void requireCallsResolveReturningExistingResourceForLocation() {

		String location = "/path/to/resource.dat";

		Resource mockResource = mock(Resource.class);

		ResourceResolver resourceResolver = mock(ResourceResolver.class);

		doReturn(true).when(mockResource).exists();
		doReturn(Optional.of(mockResource)).when(resourceResolver).resolve(eq(location));
		doCallRealMethod().when(resourceResolver).require(any());

		assertThat(resourceResolver.require(location)).isEqualTo(mockResource);

		verify(resourceResolver, times(1)).resolve(eq(location));
		verify(mockResource, times(1)).exists();
		verifyNoMoreInteractions(mockResource);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void requireCallsResolveReturningNonExistingResourceThrowsResourceNotFoundException() {

		String location = "/path/to/non-existing/resource.dat";

		Resource mockResource = mock(Resource.class);

		ResourceResolver resourceResolver = mock(ResourceResolver.class);

		doReturn(false).when(mockResource).exists();
		doReturn(Optional.of(mockResource)).when(resourceResolver).resolve(eq(location));
		doCallRealMethod().when(resourceResolver).require(any());

		try {
			resourceResolver.require(location);
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Resource [%s] does not exist", location);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(resourceResolver, times(1)).resolve(eq(location));
			verify(mockResource, times(1)).exists();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test(expected = ResourceNotFoundException.class)
	public void requireCallsResolveReturningNoResourceThrowsResourceNotFoundException() {

		String location = "/path/to/nowhere";

		ResourceResolver resourceResolver = mock(ResourceResolver.class);

		doReturn(Optional.empty()).when(resourceResolver).resolve(any());
		doCallRealMethod().when(resourceResolver).require(any());

		try {
			resourceResolver.require(location);
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Resource [%s] does not exist", location);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(resourceResolver, times(1)).resolve(eq(location));
		}
	}
}
