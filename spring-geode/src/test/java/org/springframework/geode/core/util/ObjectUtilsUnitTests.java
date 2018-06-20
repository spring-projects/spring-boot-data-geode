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

package org.springframework.geode.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import org.junit.Test;

/**
 * Unit tests for {@link ObjectUtils}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.core.util.ObjectUtils
 * @since 1.0.0
 */
public class ObjectUtilsUnitTests {

	@Test
	public void doOperationSafelyReturnsResult() {
		assertThat(ObjectUtils.doOperationSafely(() -> "test")).isEqualTo("test");
	}

	@Test
	public void doOperationSafelyReturnsDefaultValue() {
		assertThat(ObjectUtils.doOperationSafely(() -> { throw newRuntimeException("test"); },
			"default value")).isEqualTo("default value");
	}

	@Test(expected = IllegalStateException.class)
	public void doOperationSafelyThrowsIllegalStateException() {

		try {
			ObjectUtils.doOperationSafely(() -> { throw newRuntimeException("test"); }, null);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Failed to execute operation");
			assertThat(expected).hasCauseInstanceOf(RuntimeException.class);
			assertThat(expected.getCause()).hasMessage("test");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void returnValueThrowOnNullWithNonNullValueReturnsValue() {
		assertThat(ObjectUtils.returnValueThrowOnNull("test")).isEqualTo("test");
	}

	@Test(expected = RuntimeException.class)
	public void returnValueThrowOnNullWithNullValueThrowsException() {

		try {
			ObjectUtils.returnValueThrowOnNull(null, newRuntimeException("test"));
		}
		catch (RuntimeException expected) {

			assertThat(expected).hasMessage("test");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}
}
