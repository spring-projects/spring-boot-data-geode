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

import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.geode.pdx.PdxInstance;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ObjectUtils} class is an abstract utility class with operations for {@link Object objects}.
 *
 * @author John Blum
 * @see java.lang.Object
 * @see java.lang.reflect.Constructor
 * @see java.lang.reflect.Field
 * @see java.lang.reflect.Method
 * @see org.apache.geode.pdx.PdxInstance
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class ObjectUtils extends org.springframework.util.ObjectUtils {

	private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

	/**
	 * Tries to cast the given source {@link Object} into an instance of the given {@link Class} type.
	 *
	 * This method is cable of handling Apache Geode {@link PdxInstance} types.
	 *
	 * @param <T> desired {@link Class type} of the source {@link Object}.
	 * @param source {@link Object} to evaluate.
	 * @param type desired target {@link Class} type; must not be {@literal null}.
	 * @return the source {@link Object} cast to an instance of the given {@link Class} type.
	 * @throws IllegalArgumentException if the source {@link Object} is not an instance of
	 * the given {@link Class} type or the {@link Class} type is {@literal null}.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see java.lang.Class
	 * @see java.lang.Object
	 */
	public static @Nullable <T> T asType(@Nullable Object source, @NonNull Class<T> type) {

		Assert.notNull(type, "Class type must not be null");

		Object target = source instanceof PdxInstance
			? ((PdxInstance) source).getObject()
			: source;

		return target == null ? null
			: Optional.of(target)
				.filter(type::isInstance)
				.map(type::cast)
				.orElseThrow(() -> newIllegalArgumentException("Object [%s] is not an instance of type [%s]",
					nullSafeClassName(target), type.getName()));
	}

	/**
	 * Safely executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception}
	 * thrown during the normal execution of the operation by rethrowing an {@link IllegalStateException}
	 * wrapping the original checked {@link Exception}.
	 *
	 * @param <T> {@link Class type} of {@link Object value} returned from the execution of the operation.
	 * @param operation {@link ExceptionThrowingOperation} to execute; must not be {@literal null}.
	 * @return the result of the {@link ExceptionThrowingOperation}.
	 * @throws IllegalStateException wrapping any checked {@link Exception} thrown by the operation.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see #doOperationSafely(ExceptionThrowingOperation, Object)
	 */
	@Nullable
	public static <T> T doOperationSafely(@NonNull ExceptionThrowingOperation<T> operation) {
		return doOperationSafely(operation, (T) null);
	}

	/**
	 * Safely executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception}
	 * thrown during the normal execution of the operation by returning the given {@link Object default value}
	 * or throwing an {@link IllegalStateException} if the {@link Object default value} is {@literal null}.
	 *
	 * @param <T> {@link Class type} of {@link Object value} returned from the execution of the operation
	 * as well as the {@link Class type} of the {@link Object default value}.
	 * @param operation {@link ExceptionThrowingOperation} to execute; must not be {@literal null}.
	 * @param defaultValue {@link Object value} to return if the execution of the operation
	 * results in a checked {@link Exception}.
	 * @return the result of the {@link ExceptionThrowingOperation}, returning the {@link Object default value}
	 * if the execution of the operation throws a checked {@link Exception}
	 * @throws IllegalStateException wrapping any checked {@link Exception} thrown by the operation
	 * when the {@link Object default value} is {@literal null}.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see #doOperationSafely(ExceptionThrowingOperation, Supplier)
	 * @see #returnValueThrowOnNull(Object, RuntimeException)
	 */
	@Nullable
	public static <T> T doOperationSafely(@NonNull ExceptionThrowingOperation<T> operation, @NonNull T defaultValue) {

		Supplier<T> valueSupplier = () -> defaultValue;

		return doOperationSafely(operation, valueSupplier);
	}

	/**
	 * Safely executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception}
	 * thrown during the normal execution of the operation by returning a {@link Object default value}
	 * supplied by the given {@link Supplier}, or throws an {@link IllegalStateException}
	 * if the {@link Supplier supplied value} is {@literal null}.
	 *
	 * @param <T> {@link Class type} of {@link Object value} returned from the execution of the operation
	 * as well as the {@link Class type} of the {@link Object default value}.
	 * @param operation {@link ExceptionThrowingOperation} to execute; must not be {@literal null}.
	 * @param valueSupplier {@link Supplier} of the {@link Object value} to return if the execution of the operation
	 * results in a checked {@link Exception}; must not be {@literal null}.
	 * @return the result of the {@link ExceptionThrowingOperation}, returning the {@link Supplier supplied value}
	 * if the execution of the operation throws a checked {@link Exception}
	 * @throws IllegalStateException wrapping any checked {@link Exception} thrown by the operation
	 * when the {@link Supplier supplied value} is {@literal null}.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see #doOperationSafely(ExceptionThrowingOperation, Function)
	 * @see #returnValueThrowOnNull(Object, RuntimeException)
	 * @see java.util.function.Supplier
	 */
	public static <T> T doOperationSafely(@NonNull ExceptionThrowingOperation<T> operation,
			@NonNull Supplier<T> valueSupplier) {

		Function<Throwable, T> exceptionHandlingFunction = cause ->
			returnValueThrowOnNull(valueSupplier.get(), newIllegalStateException(cause, "Failed to execute operation"));

		return doOperationSafely(operation, exceptionHandlingFunction);
	}

	/**
	 * Safely executes the given {@link ExceptionThrowingOperation} handling any checked {@link Exception}
	 * thrown during the normal execution of the operation by invoking the provided {@link Exception}
	 * handling {@link Function}.
	 *
	 * @param <T> {@link Class type} of {@link Object value} returned from the execution of the operation.
	 * @param operation {@link ExceptionThrowingOperation} to execute; must not be {@literal null}.
	 * @param exceptionHandlingFunction {@link Function} used to handle any checked {@link Exception}
	 * thrown by {@link ExceptionThrowingOperation}; must not be {@literal null}.
	 * @return the result of the {@link ExceptionThrowingOperation}.
	 * @see org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation
	 * @see java.util.function.Function
	 * @see java.lang.Throwable
	 */
	public static <T> T doOperationSafely(@NonNull ExceptionThrowingOperation<T> operation,
			@NonNull Function<Throwable, T> exceptionHandlingFunction) {

		try {
			return operation.run();
		}
		catch (Exception cause) {

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Failed to execute operation [%s]", operation), cause);
			}

			return exceptionHandlingFunction.apply(cause);
		}
	}

	/**
	 * Finds a {@link Method} with the given {@link Method#getName() name} on {@link Class type} which can be invoked
	 * with the given {@link Object arguments}.
	 *
	 * @param type {@link Class} type to evaluate for the {@link Method}.
	 * @param methodName {@link String} containing the name of the {@link Method} to find.
	 * @param args {@link Object array of arguments} used when invoking the method.
	 * @return an {@link Optional} {@link Method} on {@link Class type} potentially matching
	 * the {@link Object arguments} of the invocation.
	 * @see java.lang.Class
	 * @see java.lang.reflect.Method
	 * @see java.util.Optional
	 */
	public static Optional<Method> findMethod(@NonNull Class<?> type, @NonNull String methodName, Object... args) {

		return Arrays.stream(nullSafeArray(type.getDeclaredMethods(), Method.class))
			.filter(methodNameMatchesPredicate(methodName))
			.filter(argumentsMatchParameterTypesPredicate(args))
			.findFirst();
	}

	private static Predicate<Method> argumentsMatchParameterTypesPredicate(Object... args) {

		return method -> {

			Class<?>[] parameterTypes = nullSafeArray(method.getParameterTypes(), Class.class);

			Object[] arguments = nullSafeArray(args, Object.class);

			if (arguments.length != parameterTypes.length) {
				return false;
			}

			for (int index = 0; index < parameterTypes.length; index++) {

				Object argument = arguments[index];

				if (argument != null && !parameterTypes[index].isInstance(argument)) {
					return false;
				}
			}

			return true;
		};
	}

	private static Predicate<Method> methodNameMatchesPredicate(String methodName) {
		return method -> method.getName().equals(methodName);
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

		Field field = ReflectionUtils.findField(obj.getClass(), fieldName);

		if (field != null) {

			field = makeAccessible(field);

			return get(obj, field);
		}

		throw newIllegalArgumentException("No field with name [%s] exists on object of type [%s]",
			fieldName, ObjectUtils.nullSafeClassName(obj));
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
	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj, Field field) {

		Assert.notNull(obj, "Object is required");
		Assert.notNull(field, "Field is required");

		return doOperationSafely(() -> (T) field.get(obj), (T) null);
	}

	/**
	 * An initialization operator used to evalutate a given {@link Object target} and conditionally
	 * {@link Supplier supply} a new value if the {@link Object target} is {@literal null}.
	 *
	 * The {@code initialize} operator simplifies a common initialization safety pattern that appears in code as:
	 *
	 * <code>
	 *   target = target != null ? target : new Target();
	 * </code>
	 *
	 * While the expression uses Java's ternary operator, users could very well use a if-then-else statement instead.
	 * Either way, since Java is {@literal call-by-value} then the above statement and expression can be replaced with:
	 *
	 * <code>
	 *   target = initialize(target, Target::new);
	 * </code>
	 *
	 * @param <T> {@link Class type} of the {@link Object target}.
	 * @param target {@link Object} to evaluate and initialize; must not be {@literal null}.
	 * @param supplier {@link Supplier} used to initialize the {@link Object target} on return.
	 * @return the existing {@link Object target} if not {@literal null}, otherwise invoke the {@link Supplier}
	 * to supply a new instance of {@code T}.
	 */
	public static <T> T initialize(@Nullable T target, @NonNull Supplier<T> supplier) {
		return target != null ? target : supplier.get();
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

	/**
	 * Makes the {@link Constructor} accessible.
	 *
	 * @param constructor {@link Constructor} to make accessible; must not be {@literal null}.
	 * @return the given {@link Constructor}.
	 * @see java.lang.reflect.Constructor
	 */
	public static Constructor<?> makeAccessible(@NonNull Constructor<?> constructor) {

		ReflectionUtils.makeAccessible(constructor);

		return constructor;
	}

	/**
	 * Makes the {@link Field} accessible.
	 *
	 * @param field {@link Field} to make accessible; must not be {@literal null}.
	 * @return the given {@link Field}.
	 * @see java.lang.reflect.Field
	 */
	public static Field makeAccessible(Field field) {

		ReflectionUtils.makeAccessible(field);

		return field;
	}

	/**
	 * Makes the {@link Method} accessible.
	 *
	 * @param method {@link Method} to make accessible; must not be {@literal null}.
	 * @return the given {@link Method}.
	 * @see java.lang.reflect.Method
	 */
	public static Method makeAccessible(Method method) {

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

	/**
	 * Resolves the {@link Object invocation target} for the given {@link Method}.
	 *
	 * If the {@link Method} is {@link Modifier#STATIC} then {@literal null} is returned,
	 * otherwise {@link Object target} will be returned.
	 *
	 * @param <T> {@link Class type} of the {@link Object target}.
	 * @param target {@link Object} on which the {@link Method} will be invoked.
	 * @param method {@link Method} to invoke on the {@link Object}.
	 * @return the resolved {@link Object invocation method}.
	 * @see java.lang.Object
	 * @see java.lang.reflect.Method
	 */
	public static <T> T resolveInvocationTarget(T target, Method method) {
		return Modifier.isStatic(method.getModifiers()) ? null : target;
	}

	@FunctionalInterface
	public interface ExceptionThrowingOperation<T> {
		T run() throws Exception;
	}
}
