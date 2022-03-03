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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;

import org.springframework.core.io.Resource;

/**
 * Unit Tests for {@link SingleResourceResolver}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceResolver
 * @see org.springframework.geode.core.io.support.SingleResourceResolver
 * @since 1.3.1
 */
public class SingleResourceResolverUnitTests {

	@Test
	public void constructsSingleResourceResolverWithNonNullResource() {

		Resource mockResource = mock(Resource.class);

		SingleResourceResolver resourceResolver = new SingleResourceResolver(mockResource);

		assertThat(resourceResolver.resolve("/location/of/resource").orElse(null)).isEqualTo(mockResource);
		assertThat(resourceResolver.resolve("/path/to/resource").orElse(null)).isEqualTo(mockResource);
		assertThat(resourceResolver.resolve("/another/path/to/resource").orElse(null)).isEqualTo(mockResource);
		assertThat(resourceResolver.resolve("/yet/another/path/to/resource").orElse(null)).isEqualTo(mockResource);

		verifyNoInteractions(mockResource);
	}

	@Test
	public void constructSingleResourceResolverWithNullResource() {

		SingleResourceResolver resourceResolver = new SingleResourceResolver(null);

		assertThat(resourceResolver.resolve("/location/of/resource").orElse(null)).isNull();
		assertThat(resourceResolver.resolve("/path/to/resource").orElse(null)).isNull();
	}
}
