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

import java.lang.reflect.Field;

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
			ObjectUtils.doOperationSafely(() -> { throw newRuntimeException("test"); }, (Object) null);
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
	public void getFieldValueIsSuccessful() throws Exception {

		TestObject testObject = new TestObject();

		Field existingField = testObject.getClass().getDeclaredField("existingField");

		existingField.setAccessible(true);

		assertThat(ObjectUtils.<String>get(testObject, existingField)).isEqualTo("MOCK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithNullObjectThrowsIllegalArgumentException() throws Exception {

		try {
			ObjectUtils.get(null, TestObject.class.getDeclaredField("existingField"));
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Object is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithNullFieldThrowsIllegalArgumentException() throws Exception {

		try {
			ObjectUtils.get(new TestObject(), (Field) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Field is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void getNamedFieldValueIsSuccessful() {
		assertThat(ObjectUtils.<String>get(new TestObject(), "existingField")).isEqualTo("MOCK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNamedFieldWithNullObjectThrowsIllegalArgumentException() {

		try {
			ObjectUtils.get(null, "testField");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Object is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNamedFieldWithNoFieldNameThrowsIllegalArgumentException() {

		try {
			ObjectUtils.get(new TestObject(), "");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Field name [] is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void invokeMethodOnObjectReturnsValue() {
		assertThat(ObjectUtils.<String>invoke(new TestObject(), "testMethod")).isEqualTo("TEST");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invokeNonExistingMethodOnObjectThrowsIllegalArgumentException() {

		try {
			ObjectUtils.invoke(new TestObject(), "nonExistingMethod");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Method [nonExistingMethod] on Object of type [%s] not found",
				TestObject.class.getName());

			assertThat(expected).hasNoCause();

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

	@SuppressWarnings("unused")
	public static class TestObject {

		private Object existingField = "MOCK";

		public String testMethod() {
			return "TEST";
		}
	}
}
