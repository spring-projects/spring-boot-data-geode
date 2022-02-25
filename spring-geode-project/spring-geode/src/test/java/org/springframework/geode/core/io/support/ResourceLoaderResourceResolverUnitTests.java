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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;

import org.junit.Test;
import org.mockito.InOrder;

import org.apache.shiro.util.CollectionUtils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.geode.core.io.ResourceNotFoundException;
import org.springframework.util.ClassUtils;

/**
 * Unit Tests for {@link ResourceLoaderResourceResolver}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.geode.core.io.support.ResourceLoaderResourceResolver
 * @since 1.3.1
 */
public class ResourceLoaderResourceResolverUnitTests {

	@Test
	public void getsClassLoaderFromResolvedResourceLoader() {

		ClassLoader mockClassLoader = mock(ClassLoader.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		doReturn(mockClassLoader).when(mockResourceLoader).getClassLoader();

		ResourceLoaderResourceResolver resourceResolver = new ResourceLoaderResourceResolver();

		resourceResolver.setResourceLoader(mockResourceLoader);

		assertThat(resourceResolver.getClassLoader().orElse(null)).isEqualTo(mockClassLoader);

		verify(mockResourceLoader, times(1)).getClassLoader();
		verifyNoMoreInteractions(mockResourceLoader);
	}

	@Test
	public void getsDefaultClassLoader() {

		ResourceLoaderResourceResolver resourceResolver = new ResourceLoaderResourceResolver();

		Set<ClassLoader> classLoaders = CollectionUtils.asSet(
			Thread.currentThread().getContextClassLoader(),
			getClass().getClassLoader(),
			ClassLoader.getSystemClassLoader()
		);

		assertThat(classLoaders).contains(resourceResolver.getClassLoader().orElse(null));
	}

	@Test
	public void setAndGetResourceLoader() {

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		resourceResolver.setResourceLoader(mockResourceLoader);

		assertThat(resourceResolver.getResourceLoader()).isSameAs(mockResourceLoader);

		verify(resourceResolver, never()).newResourceLoader();
		verifyNoInteractions(mockResourceLoader);
	}

	@Test
	public void getUnresolvedResourceLoaderReturnsNewResourceLoader() {

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		doReturn(mockResourceLoader).when(resourceResolver).newResourceLoader();

		ResourceLoader resourceLoader = resourceResolver.getResourceLoader();

		assertThat(resourceLoader).isEqualTo(mockResourceLoader);

		verify(resourceResolver, times(1)).newResourceLoader();
		verifyNoInteractions(mockResourceLoader);
	}

	@Test
	public void newResourceLoaderReturnsDefaultResourceLoaderWithConfiguredClassLoader() {

		ClassLoader mockClassLoader = mock(ClassLoader.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		doReturn(mockClassLoader).when(mockResourceLoader).getClassLoader();

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		resourceResolver.setResourceLoader(mockResourceLoader);

		ResourceLoader resourceLoader = resourceResolver.newResourceLoader();

		assertThat(resourceLoader).isInstanceOf(DefaultResourceLoader.class);
		assertThat(resourceLoader.getClassLoader()).isSameAs(mockClassLoader);

		verify(resourceResolver, times(1)).getClassLoader();
		verify(mockResourceLoader, times(1)).getClassLoader();
		verifyNoMoreInteractions(mockResourceLoader);
	}

	@Test
	public void newResourceLoaderReturnsDefaultResourceLoaderWithDefaultClassLoader() {

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		ResourceLoader resourceLoader = resourceResolver.newResourceLoader();

		assertThat(resourceLoader).isInstanceOf(DefaultResourceLoader.class);
		assertThat(resourceLoader.getClassLoader()).isEqualTo(ClassUtils.getDefaultClassLoader());

		verify(resourceResolver, times(1)).getClassLoader();
	}

	@Test
	public void isQualifiedWitNonNullResourceReturnsTrue() {

		Resource mockResource = mock(Resource.class);

		assertThat(new ResourceLoaderResourceResolver().isQualified(mockResource)).isTrue();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void isQualifiedWitNullResourceIsNullSafeReturnsFalse() {
		assertThat(new ResourceLoaderResourceResolver().isQualified(null)).isFalse();
	}

	@Test
	public void newResourceReturnsClassPathResource() {

		ResourceLoaderResourceResolver resourceResolver = new ResourceLoaderResourceResolver();

		Resource resource = resourceResolver.newResource("/path/to/resource");

		assertThat(resource).isInstanceOf(ClassPathResource.class);
		assertThat(resource.getDescription()).containsSequence("path/to/resource");
	}

	private void testNewResourceWithInvalidLocation(String location) {

		try {
			new ResourceLoaderResourceResolver().newResource(location);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("The location [%s] of the Resource must be specified", location);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void newResourceWithBlankLocation() {
		testNewResourceWithInvalidLocation("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newResourceWithEmptyLocation() {
		testNewResourceWithInvalidLocation("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newResourceWithNullLocation() {
		testNewResourceWithInvalidLocation(null);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void onMissingResourceThrowsResourceNotFoundException() {

		try {
			new ResourceLoaderResourceResolver().onMissingResource(null, "/path/to/resource");
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Failed to resolve Resource [null] at location [/path/to/resource]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = ResourceNotFoundException.class)
	public void onMissingResourceWithResourceThrowsResourceNotFoundException() {

		Resource mockResource = mock(Resource.class);

		doReturn("MOCK").when(mockResource).getDescription();

		try {
			new ResourceLoaderResourceResolver().onMissingResource(mockResource, "/location/of/resource");
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Failed to resolve Resource [MOCK] at location [/location/of/resource]");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockResource, times(1)).getDescription();
			verifyNoMoreInteractions(mockResource);
		}
	}

	@Test
	public void postProcessReturnsGivenResource() {

		Resource mockResource = mock(Resource.class);

		ResourceLoaderResourceResolver resourceResolver = new ResourceLoaderResourceResolver();

		assertThat(resourceResolver.postProcess(mockResource)).isSameAs(mockResource);
		assertThat(resourceResolver.postProcess(null)).isNull();

		verifyNoInteractions(mockResource);
	}

	@Test
	public void resolveReturnsResource() {

		String location = "/path/to/resource";

		Resource mockResource = mock(Resource.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		doReturn(mockResourceLoader).when(resourceResolver).getResourceLoader();
		doReturn(mockResource).when(mockResourceLoader).getResource(eq(location));

		assertThat(resourceResolver.resolve(location).orElse(null)).isEqualTo(mockResource);

		InOrder order = inOrder(mockResource, mockResourceLoader, resourceResolver);

		order.verify(resourceResolver, times(1)).getResourceLoader();
		order.verify(mockResourceLoader, times(1)).getResource(eq(location));
		order.verify(resourceResolver, times(1)).postProcess(eq(mockResource));
		order.verify(resourceResolver, times(1)).isQualified(eq(mockResource));
		order.verify(resourceResolver, never()).onMissingResource(any(), any());

		verifyNoInteractions(mockResource);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void resolveUnresolvableResourceThrowsResourceNotFoundException() {

		String location = "/path/to/resource/a";

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		doReturn(mockResourceLoader).when(resourceResolver).getResourceLoader();
		doReturn(null).when(mockResourceLoader).getResource(any());

		try {
			resourceResolver.resolve(location);
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Failed to resolve Resource [null] at location [%s]", location);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(resourceResolver, times(1)).getResourceLoader();
			verify(resourceResolver, times(1)).postProcess(any());
			verify(resourceResolver, times(1)).isQualified(isNull());
			verify(resourceResolver, times(1)).onMissingResource(isNull(), eq(location));
			verify(mockResourceLoader, times(1)).getResource(eq(location));
			verifyNoMoreInteractions(mockResourceLoader);
		}
	}

	@Test(expected = ResourceNotFoundException.class)
	public void resolveUnqualifiedResourceThrowsResourceNotFoundException() {

		String location = "/path/to/resource/b";

		Resource mockResource = mock(Resource.class);

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		doReturn(mockResourceLoader).when(resourceResolver).getResourceLoader();
		doReturn(false).when(resourceResolver).isQualified(any());
		doReturn(mockResource).when(mockResourceLoader).getResource(eq(location));
		doReturn("MOCK").when(mockResource).getDescription();

		try {
			resourceResolver.resolve(location);
		}
		catch (ResourceNotFoundException expected) {

			assertThat(expected).hasMessage("Failed to resolve Resource [MOCK] at location [%s]", location);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(resourceResolver, times(1)).getResourceLoader();
			verify(resourceResolver, times(1)).postProcess(any());
			verify(resourceResolver, times(1)).isQualified(eq(mockResource));
			verify(resourceResolver, times(1)).onMissingResource(eq(mockResource), eq(location));
			verify(mockResourceLoader, times(1)).getResource(eq(location));
			verify(mockResource, times(1)).getDescription();
			verifyNoMoreInteractions(mockResource, mockResourceLoader);
		}
	}

	@Test
	public void resolveHandlesMissingResourceByReturningAlternateResource() {

		String location = "/path/to/resource/c";

		Resource mockResourceOne = mock(Resource.class, "one");
		Resource mockResourceTwo = mock(Resource.class, "two");

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderResourceResolver resourceResolver = spy(new ResourceLoaderResourceResolver());

		doReturn(mockResourceLoader).when(resourceResolver).getResourceLoader();
		doReturn(false).when(resourceResolver).isQualified(eq(mockResourceOne));
		doReturn(mockResourceTwo).when(resourceResolver).onMissingResource(eq(mockResourceOne), eq(location));
		doReturn(mockResourceOne).when(mockResourceLoader).getResource(eq(location));

		assertThat(resourceResolver.resolve(location).orElse(null)).isEqualTo(mockResourceTwo);

		verify(resourceResolver, times(1)).getResourceLoader();
		verify(resourceResolver, times(1)).postProcess(any());
		verify(resourceResolver, times(1)).isQualified(eq(mockResourceOne));
		verify(resourceResolver, times(1)).onMissingResource(eq(mockResourceOne), eq(location));
		verify(mockResourceLoader, times(1)).getResource(eq(location));
		verifyNoMoreInteractions(mockResourceLoader);
		verifyNoInteractions(mockResourceOne, mockResourceTwo);
	}

	private void testResolveWithInvalidLocation(String location) {

		try {
			new ResourceLoaderResourceResolver().resolve(location);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected)
				.hasMessage("The location [%s] of the Resource to resolve must be specified", location);

			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveWithBlankLocationThrowsIllegalArgumentException() {
		testResolveWithInvalidLocation("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveWithEmptyLocationThrowsIllegalArgumentException() {
		testResolveWithInvalidLocation("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveWithNullLocationThrowsIllegalArgumentException() {
		testResolveWithInvalidLocation(null);
	}
}
