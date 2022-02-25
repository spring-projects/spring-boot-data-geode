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
package org.springframework.geode.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;

import org.apache.geode.pdx.PdxInstance;

/**
 * Unit Tests for {@link ObjectUtils}.
 *
 * @author John Blum
 * @see java.lang.reflect.Field
 * @see java.lang.reflect.Method
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.core.util.ObjectUtils
 * @since 1.0.0
 */
public class ObjectUtilsUnitTests {

	@Test
	public void asMatchingBaseType() {

		B object = new B();
		A value = ObjectUtils.asType(object, A.class);

		assertThat(value).isNotNull();
		assertThat(value).isSameAs(object);
	}

	@Test
	public void asMatchingSubType() {

		A object = new B();
		B value = ObjectUtils.asType(object, B.class);

		assertThat(value).isNotNull();
		assertThat(value).isSameAs(object);
	}

	@Test(expected = IllegalArgumentException.class)
	public void asNonMatchingType() {

		Object object = new B();

		try {
			ObjectUtils.asType(object, C.class);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Object [%s] is not an instance of type [%s]",
				object.getClass().getName(), C.class.getName());
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void asPdxInstanceType() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(new B()).when(mockPdxInstance).getObject();

		assertThat(ObjectUtils.asType(mockPdxInstance, B.class)).isInstanceOf(B.class);

		verify(mockPdxInstance, times(1)).getObject();
	}

	@Test(expected = IllegalArgumentException.class)
	public void asPdxInstanceTypeReturningNonMatchingType() {

		C source = new C();

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(source).when(mockPdxInstance).getObject();

		try {
			ObjectUtils.asType(mockPdxInstance, A.class);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Object [%s] is not an instance of type [%s]",
				source.getClass().getName(), A.class.getName());
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockPdxInstance, times(1)).getObject();
		}
	}

	@Test
	public void asPdxInstanceTypeReturningNull() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(null).when(mockPdxInstance).getObject();

		assertThat(ObjectUtils.asType(mockPdxInstance, A.class)).isNull();

		verify(mockPdxInstance, times(1)).getObject();
	}

	@Test
	public void asTypeWithNullSource() {
		assertThat(ObjectUtils.asType(null, A.class)).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
	public void asTypeWithNullType() {

		try {
			ObjectUtils.asType(new A(), null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Class type must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void doOperationSafelyReturnsResult() {
		assertThat(ObjectUtils.doOperationSafely(() -> "test")).isEqualTo("test");
	}

	@Test
	public void doOperationSafelyReturnsDefaultValue() {
		assertThat(ObjectUtils.doOperationSafely(() -> { throw newRuntimeException("test"); },
			"default value")).isEqualTo("default value");
	}

	@Test
	public void doOperationSafelyReturnsSuppliedValue() {

		Supplier<String> suppliedValue = () -> "supplied value";

		assertThat(ObjectUtils.<String>doOperationSafely(() -> { throw newRuntimeException("test"); }, suppliedValue))
			.isEqualTo("supplied value");
	}

	@Test(expected = IllegalStateException.class)
	public void doOperationSafelyWithNullDefaultValueThrowsIllegalStateException() {

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

	@Test(expected = IllegalStateException.class)
	public void doOperationSafelyWithNullSuppliedValueThrowsIllegalStateException() {

		Supplier<String> suppliedValue = () -> null;

		try {
			ObjectUtils.<String>doOperationSafely(() -> { throw newRuntimeException("test"); }, suppliedValue);
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
	public void findMethodUsingExactArguments() {

		Optional<Method> method =
			ObjectUtils.findMethod(TestObject.class, "exactArgumentMethod", true);

		assertThat(method.orElse(null)).isNotNull();
		assertThat(method.map(Method::getName).orElse(null)).isEqualTo("exactArgumentMethod");
	}

	@Test
	public void findMethodUsingGenericArguments() {

		Optional<Method> method =
			ObjectUtils.findMethod(TestObject.class, "genericArgumentMethod", "test", 2L);

		assertThat(method.orElse(null)).isNotNull();
		assertThat(method.map(Method::getName).orElse(null)).isEqualTo("genericArgumentMethod");
	}

	@Test
	public void findMethodWithTooFewArguments() {

		Optional<Method> method = ObjectUtils.findMethod(TestObject.class, "exactArgumentMethod");

		assertThat(method.isPresent()).isFalse();
	}

	@Test
	public void findMethodWithTooManyArguments() {

		Optional<Method> method = ObjectUtils.findMethod(TestObject.class, "testMethod", 'X');

		assertThat(method.isPresent()).isFalse();
	}

	@Test
	public void findNonExistingMethod() {

		Optional<Method> method = ObjectUtils.findMethod(TestObject.class, "nonExistingMethod");

		assertThat(method.isPresent()).isFalse();
	}

	@Test
	public void getFieldValueIsSuccessful() throws Exception {

		TestObject testObject = new TestObject();

		Field existingField = testObject.getClass().getDeclaredField("existingField");

		existingField.setAccessible(true);

		assertThat(ObjectUtils.<String>get(testObject, existingField)).isEqualTo("MOCK");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldWithNullObjectThrowsIllegalArgumentException() throws Exception {

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
	public void getFieldWithNullFieldThrowsIllegalArgumentException() {

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
	public void initializeWithNonNullTarget() {
		assertThat(ObjectUtils.initialize("test", () -> "mock")).isEqualTo("test");
	}

	@Test
	public void initializeWithNullTarget() {
		assertThat(ObjectUtils.initialize(null, () -> "mock")).isEqualTo("mock");
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
	public void resolveInvocationTargetOnClassMethodReturnsNull() throws Exception {

		Method staticTestMethod = TestObject.class.getDeclaredMethod("staticTestMethod");

		assertThat(ObjectUtils.resolveInvocationTarget(TestObject.INSTANCE, staticTestMethod)).isNull();
	}

	@Test
	public void resolveInvocationTargetOnObjectMethodReturnsObject() throws Exception {

		Method testMethod = TestObject.class.getDeclaredMethod("testMethod");

		assertThat(ObjectUtils.resolveInvocationTarget(TestObject.INSTANCE, testMethod)).isEqualTo(TestObject.INSTANCE);
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

		public static final TestObject INSTANCE = new TestObject();

		private Object existingField = "MOCK";

		public static Object staticTestMethod() {
			return "STATIC";
		}

		public Object exactArgumentMethod(Boolean condition) {
			return "exactArgumentMethod";
		}

		public Object genericArgumentMethod(String name, Number value) {
			return "genericArgumentMethod";
		}

		public String testMethod() {
			return "TEST";
		}
	}

	static class A { }

	static class B extends A { }

	static class C { }

}
