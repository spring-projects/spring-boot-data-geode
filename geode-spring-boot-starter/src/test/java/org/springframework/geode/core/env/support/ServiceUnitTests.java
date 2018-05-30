/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.core.env.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link Service}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.core.env.support.Service
 * @since 1.0.0
 */
public class ServiceUnitTests {

	@Test
	public void withNameReturnsNewService() {

		Service service = Service.with("test");

		assertThat(service).isNotNull();
		assertThat(service.getName()).isEqualTo("test");
	}

	private void testWithInvalidNameThrowsIllegalArgumentException(String name) {

		try {
			Service.with(name);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Service name [%s] is required", name);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void withBlankServiceNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withEmptyServiceNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNullServiceNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException("");
	}

	@Test
	public void toStringReturnsServiceName() {
		assertThat(Service.with("test").toString()).isEqualTo("test");
	}
}
