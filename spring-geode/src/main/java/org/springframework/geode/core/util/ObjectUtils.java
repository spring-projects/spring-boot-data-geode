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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * The {@link ObjectUtils} class is an abstract utility class with operations for {@link Object objects}.
 *
 * @author John Blum
 * @see java.lang.Object
 * @see java.lang.reflect.Method
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class ObjectUtils extends org.springframework.util.ObjectUtils {

	private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

	/**
	 * Executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception} thrown during
	 * the normal execution of the operation by rethrowing an {@link IllegalStateException} wrapping
	 * the checked {@link Exception}.
	 *
	 * @param <T> {@link Class type} of value returned from the operation execution.
	 * @param operation {@link ExceptionThrowingOperation} to execute.
	 * @return the result of the given {@link ExceptionThrowingOperation} or throw an {@link IllegalStateException}
	 * wrapping the checked {@link Exception} thrown by the operation.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see #doOperationSafely(ExceptionThrowingOperation, Object)
	 */
	@Nullable
	public static <T> T doOperationSafely(ExceptionThrowingOperation<T> operation) {
		return doOperationSafely(operation, (T) null);
	}

	/**
	 * Executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception} thrown
	 * during the normal execution of the operation, returning the {@link Object default value} in its place
	 * or throwing a {@link RuntimeException} if the {@link Object default value} is {@literal null}.
	 *
	 * @param <T> {@link Class type} of value returned from the operation execution as well as
	 * the {@link Object default value}.
	 * @param operation {@link ExceptionThrowingOperation} to execute.
	 * @param defaultValue {@link Object value} to return if the operation results in a checked {@link Exception}.
	 * @return the result of the given {@link ExceptionThrowingOperation}, returning the {@link Object default value}
	 * if the operation throws a checked {@link Exception} or throws an {@link IllegalStateException} wrapping
	 * the checked {@link Exception} if the {@link Object default value} is {@literal null}.
	 * @throws IllegalStateException if the {@link ExceptionThrowingOperation} throws a checked {@link Exception}
	 * and {@link Object default value} is {@literal null}.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see #doOperationSafely(ExceptionThrowingOperation, Function)
	 * @see #returnValueThrowOnNull(Object, RuntimeException)
	 */
	@Nullable
	public static <T> T doOperationSafely(ExceptionThrowingOperation<T> operation, T defaultValue) {

		Function<Throwable, T> exceptionHandlingFunction = cause ->
			returnValueThrowOnNull(defaultValue, newIllegalStateException(cause, "Failed to execute operation"));

		return doOperationSafely(operation, exceptionHandlingFunction);
	}

	/**
	 * Executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception} thrown
	 * during the normal execution of the operation by invoking the provided {@link Exception} handling
	 * {@link Function}.
	 *
	 * @param <T> {@link Class type} of value returned from the operation execution.
	 * @param operation {@link ExceptionThrowingOperation} to execute.
	 * @param exceptionHandlingFunction {@link Function} used to handle any {@link Exception} thrown by
	 * the {@link ExceptionThrowingOperation operation}.
	 * @return the result of executing the given {@link ExceptionThrowingOperation}.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see java.util.function.Function
	 * @see java.lang.Throwable
	 */
	public static <T> T doOperationSafely(ExceptionThrowingOperation<T> operation,
			Function<Throwable, T> exceptionHandlingFunction) {

		try {
			return operation.doExceptionThrowingOperation();
		}
		catch (Exception cause) {

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Failed to execute operation [%s]", operation), cause);
			}

			return exceptionHandlingFunction.apply(cause);
		}
	}

	/**
	 * Gets the {@link Object value} of the given {@link String named} {@link Field} on the given {@link Object}.
	 *
	 * @param <T> {@link Class type} of the {@link Field Field's} value.
	 * @param obj {@link Object} containing the {@link String named} {@link Field}.
	 * @param fieldName {@link String} containing the name of the {@link Field}.
	 * @return the {@link Object value} of the {@link String named} {@link Field} on the given {@link Object}.
	 * @throws IllegalArgumentException if {@link Object} is {@literal null}, the {@link String named} {@link Field}
	 * is not specified or the given {@link Object} contains no {@link Field} with the given {@link String name}.
	 * @see #get(Object, Field)
	 * @see java.lang.Object
	 */
	public static <T> T get(Object obj, String fieldName) {

		Assert.notNull(obj, "Object is required");
		Assert.hasText(fieldName, String.format("Field name [%s] is required", fieldName));

		return Optional.ofNullable(ReflectionUtils.findField(obj.getClass(), fieldName))
			.map(ObjectUtils::makeAccessible)
			.map(field -> ObjectUtils.<T>get(obj, field))
			.orElseThrow(() -> newIllegalArgumentException("No field with name [%s] exists on object of type [%s]",
				fieldName, ObjectUtils.nullSafeClassName(obj)));
	}

	/**
	 * Gets the {@link Object value} of the given {@link Field} on the given {@link Object}.
	 *
	 * @param <T> {@link Class type} of the {@link Field Field's} value.
	 * @param obj {@link Object} containing the {@link Field}.
	 * @param field {@link Field} of the given {@link Object}.
	 * @return the {@link Object value} of the {@link Field} on the given {@link Object}.
	 * @throws IllegalArgumentException if {@link Object} or {@link Field} is {@literal null}.
	 * @see java.lang.reflect.Field
	 * @see java.lang.Object
	 */
	public static <T> T get(Object obj, Field field) {

		Assert.notNull(obj, "Object is required");
		Assert.notNull(field, "Field is required");

		return doOperationSafely(() -> (T) field.get(obj), (T) null);
	}

	/**
	 * Invokes a {@link Method} on an {@link Object} with the given {@link String name}.
	 *
	 * @param <T> {@link Class type} of the {@link Method} return value.
	 * @param obj {@link Object} on which to invoke the {@link Method}.
	 * @param methodName {@link String} containing the name of the {@link Method} to invoke on {@link Object}.
	 * @return the return value of the invoked {@link Method} on {@link Object}.
	 * @throws IllegalArgumentException if no {@link Method} with {@link String name} could be found on {@link Object}.
	 * @see java.lang.reflect.Method
	 * @see java.lang.Object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T invoke(Object obj, String methodName) {

		return (T) Optional.ofNullable(obj)
			.map(Object::getClass)
			.map(type -> ReflectionUtils.findMethod(type, methodName))
			.map(ObjectUtils::makeAccessible)
			.map(method -> ReflectionUtils.invokeMethod(method, obj))
			.orElseThrow(() -> newIllegalArgumentException("Method [%1$s] on Object of type [%2$s] not found",
				methodName, org.springframework.util.ObjectUtils.nullSafeClassName(obj)));
	}

	private static Constructor makeAccessible(Constructor<?> constructor) {
		ReflectionUtils.makeAccessible(constructor);
		return constructor;
	}

	private static Field makeAccessible(Field field) {
		ReflectionUtils.makeAccessible(field);
		return field;
	}

	private static Method makeAccessible(Method method) {
		ReflectionUtils.makeAccessible(method);
		return method;
	}

	/**
	 * Returns the given {@link Object value} or throws an {@link IllegalArgumentException}
	 * if {@link Object value} is {@literal null}.
	 *
	 * @param <T> {@link Class type} of the {@link Object value}.
	 * @param value {@link Object} to return.
	 * @return the {@link Object value} or throw an {@link IllegalArgumentException}
	 * if {@link Object value} is {@literal null}.
	 * @see #returnValueThrowOnNull(Object, RuntimeException)
	 */
	public static <T> T returnValueThrowOnNull(T value) {
		return returnValueThrowOnNull(value, newIllegalArgumentException("Value must not be null"));
	}

	/**
	 * Returns the given {@link Object value} or throws the given {@link RuntimeException}
	 * if {@link Object value} is {@literal null}.
	 *
	 * @param <T> {@link Class type} of the {@link Object value}.
	 * @param value {@link Object} to return.
	 * @param exception {@link RuntimeException} to throw if {@link Object value} is {@literal null}.
	 * @return the {@link Object value} or throw the given {@link RuntimeException}
	 * if {@link Object value} is {@literal null}.
	 */
	public static <T> T returnValueThrowOnNull(T value, RuntimeException exception) {

		if (value == null) {
			throw exception;
		}

		return value;
	}

	@FunctionalInterface
	public interface ExceptionThrowingOperation<T> {
		T doExceptionThrowingOperation() throws Exception;
	}
}
