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
package org.springframework.geode.logging.slf4j.logback.support;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * Abstract utility class containing functionality for invoking SLF4J and Logback APIs.
 *
 * @author John Blum
 * @see org.slf4j.ILoggerFactory
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see ch.qos.logback.classic.LoggerContext
 * @see ch.qos.logback.core.Appender
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class LogbackSupport {

	protected static final Function<Logger, Optional<ch.qos.logback.classic.Logger>> slf4jLoggerToLogbackLoggerConverter =
		logger -> Optional.ofNullable(logger)
			.filter(ch.qos.logback.classic.Logger.class::isInstance)
			.map(ch.qos.logback.classic.Logger.class::cast);

	protected static final String CONSOLE_APPENDER_NAME = "console";
	protected static final String DELEGATE_APPENDER_NAME = "delegate";

	protected static final String ILLEGAL_LOGGER_TYPE_EXCEPTION_MESSAGE =
		"[%1$s] Logger type [%2$s] is not a Logback Logger";

	protected static final String ROOT_LOGGER_NAME = Logger.ROOT_LOGGER_NAME;

	protected static final String SPRING_BOOT_LOGGING_SYSTEM_CLASS_NAME =
		"org.springframework.boot.logging.LoggingSystem";

	protected static final String UNRESOLVABLE_APPENDER_EXCEPTION_MESSAGE =
		"Could not resolve Appender with name [%1$s] as type [%2$s] from Logger [%3$s]";

	/**
	 * Disables Spring Boot's logging initialization, auto-configuration.
	 */
	public static void suppressSpringBootLogbackInitialization() {
		requireLoggerContext().putObject(SPRING_BOOT_LOGGING_SYSTEM_CLASS_NAME, new Object());
	}

	/**
	 * Resets the state of the SLF4J Logback logging provider and system.
	 */
	public static void resetLogback() {

		try {

			Method loggerFactoryReset = LoggerFactory.class.getDeclaredMethod("reset");

			loggerFactoryReset.setAccessible(true);
			loggerFactoryReset.invoke(null);

			Method staticLoggerBinderReset = StaticLoggerBinder.class.getDeclaredMethod("reset");

			staticLoggerBinderReset.setAccessible(true);
			staticLoggerBinderReset.invoke(null);
		}
		catch (Throwable cause) {
			throw new IllegalStateException("Failed to reset Logback", cause);
		}
	}

	/**
	 * Resolves the SLF4J, Logback {@link LoggerContext}.
	 *
	 * If the {@link LoggerContext} could not be resolve then the returned {@link Optional}
	 * will be {@link Optional#empty() empty}.
	 *
	 * @return an {@link Optional} {@link LoggerContext}.
	 * @see ch.qos.logback.classic.LoggerContext
	 */
	public static Optional<LoggerContext> resolveLoggerContext() {

		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

		LoggerContext resolvedLoggerContext = loggerFactory instanceof LoggerContext
			? (LoggerContext) loggerFactory
			: null;

		return Optional.ofNullable(resolvedLoggerContext);
	}

	/**
	 * Requires a {@link LoggerContext} otherwise throws an {@link IllegalStateException}.
	 *
	 * @return the required {@link LoggerContext}.
	 * @throws IllegalStateException if the {@link LoggerContext} could not be resolved.
	 * @see #resolveLoggerContext()
	 */
	public static LoggerContext requireLoggerContext() {

		return resolveLoggerContext()
			.orElseThrow(() -> new IllegalStateException("LoggerContext is required"));
	}

	/**
	 * Resolves the {@link Logger#ROOT_LOGGER_NAME Root} {@link Logger}.
	 *
	 * @return an {@link Optional} {@link Logger} for the logging provider's {@literal ROOT} {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	public static Optional<Logger> resolveRootLogger() {
		return Optional.ofNullable(LoggerFactory.getLogger(ROOT_LOGGER_NAME));
	}

	/**
	 * Requires the SLF4J Logback {@literal ROOT} {@link Logger} otherwise throws an {@link IllegalStateException}.
	 *
	 * @return the configured SLF4J Logback {@literal ROOT} {@link Logger}.
	 * @throws IllegalStateException if the SLF4J Logback {@literal ROOT} {@link Logger} could not be resolved
	 * or the {@literal ROOT} {@link Logger} is not a SLF4J Logback {@literal ROOT} {@link Logger}.
	 * @see ch.qos.logback.classic.Logger
	 * @see #resolveRootLogger()
	 */
	public static ch.qos.logback.classic.Logger requireLogbackRootLogger() {

		return resolveRootLogger()
			.filter(ch.qos.logback.classic.Logger.class::isInstance)
			.map(ch.qos.logback.classic.Logger.class::cast)
			.orElseThrow(() -> new IllegalStateException(String.format(ILLEGAL_LOGGER_TYPE_EXCEPTION_MESSAGE,
				ROOT_LOGGER_NAME, nullSafeTypeName(resolveRootLogger()))));
	}

	/**
	 * Finds an {@link Appender} with the given {@link String name} from the given {@link Logger}.
	 *
	 * @param <T> {@link Class type} of the {@link Appender}.
	 * @param <E> {@link Class type} of the logging event.
	 * @param logger SLF4J {@link Logger} from which to resolve the {@link Appender}.
	 * @param appenderName a {@link String} containing the name of the {@link Appender} to resolve.
	 * @param appenderType required {@link Class type} of the {@link Appender} to resolve.
	 * @return an {@link Optional} {@link Appender} with the given {@link String name} from the {@link Logger}.
	 * @see ch.qos.logback.core.Appender
	 * @see java.util.Optional
	 * @see org.slf4j.Logger
	 */
	public static <E, T extends Appender<E>> Optional<T> resolveAppender(ch.qos.logback.classic.Logger logger,
			String appenderName, Class<T> appenderType) {

		appenderType = nullSafeAppenderType(appenderType);

		return Optional.ofNullable(logger)
			.map(it -> it.getAppender(appenderName))
			.filter(appenderType::isInstance)
			.map(appenderType::cast);
	}

	/**
	 * Requires an {@link Appender} with the given {@link String name} having the specified {@link Class type}
	 * from the given {@link Logger}.
	 *
	 * @param <T> {@link Class type} of the {@link Appender}.
	 * @param <E> {@link Class type} of the {@link Object Objects} processed by the {@link Appender}.
	 * @param logger {@link Logger} from which to resolve the {@link Appender}.
	 * @param appenderName {@link String} containing the name of the {@link Appender}.
	 * @param appenderType required {@link Class type} of the {@link Appender}.
	 * @return the resolved {@link Appender}.
	 * @throws IllegalStateException if an {@link Appender} with {@link String name} and required {@link Class type}
	 * could not be resolved from the given {@link Logger}.
	 * @see ch.qos.logback.classic.Logger
	 * @see ch.qos.logback.core.Appender
	 */
	public static <E, T extends Appender<E>> T requireAppender(ch.qos.logback.classic.Logger logger,
			String appenderName, Class<T> appenderType) {

		return resolveAppender(logger, appenderName, appenderType)
			.orElseThrow(() -> new IllegalStateException(String.format(UNRESOLVABLE_APPENDER_EXCEPTION_MESSAGE,
				appenderName, nullSafeTypeName(appenderType), nullSafeLoggerName(logger))));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <E, T extends Appender<E>> Class<T> nullSafeAppenderType(Class<T> appenderType) {
		return appenderType != null ? appenderType : (Class) Appender.class;
	}

	/**
	 * Adds the given {@link Appender} to the given {@link Logger}.
	 *
	 * @param logger {@link Logger} to add the {@link Appender} to.
	 * @param appender {@link Appender} to add to the {@link Logger}.
	 * @return a boolean value indicating whether the {@link Appender} was successfully added to the {@link Logger}.
	 * @see ch.qos.logback.classic.Logger
	 * @see ch.qos.logback.core.Appender
	 */
	public static boolean addAppender(ch.qos.logback.classic.Logger logger, Appender<ILoggingEvent> appender) {

		return Optional.ofNullable(logger)
			.filter(it -> Objects.nonNull(appender))
			.map(it -> {
				it.addAppender(appender);
				return logger.getAppender(appender.getName());
			})
			.isPresent();
	}

	/**
	 * Removes the {@link Appender} with the specified {@link String name} from the given {@link Logger}.
	 *
	 * @param logger {@link Logger} from which to remove the {@link Appender}.
	 * @param appenderName {@link String name} of the {@link Appender} to remove from the {@link Logger}.
	 * @return a boolean value indicating whether the targeted {@link Appender} was removed from
	 * the given {@link Logger}.
	 * @see ch.qos.logback.classic.Logger
	 * @see ch.qos.logback.core.Appender
	 */
	@SuppressWarnings("all")
	public static boolean removeAppender(ch.qos.logback.classic.Logger logger, String appenderName) {

		return Optional.ofNullable(logger)
			.map(it -> it.getAppender(appenderName))
			.filter(appender -> appender.getName().equals(appenderName))
			.map(appender -> { appender.stop(); return appender; })
			.map(appender -> logger.detachAppender(appender))
			.orElse(false);
	}

	/**
	 * Removes the {@literal console} {@link Appender} from the given {@link Logger}.
	 *
	 * @param logger {@link Logger} from which to remove the {@literal console} {@link Appender}.
	 * @return {@literal true} if the {@literal console} {@link Appender} was registered with
	 * and successfully remove from the given {@link Logger}.
	 * @see #removeAppender(ch.qos.logback.classic.Logger, String)
	 */
	public static boolean removeConsoleAppender(ch.qos.logback.classic.Logger logger) {
		return removeAppender(logger, CONSOLE_APPENDER_NAME);
	}

	/**
	 * Removes the {@literal delegate} {@link Appender} from the given {@link Logger}.
	 *
	 * @param logger {@link Logger} from which to remove the {@literal delegate} {@link Appender}.
	 * @return {@literal true} if the {@literal delegate} {@link Appender} was registered with
	 * and successfully remove from the given {@link Logger}.
	 * @see #removeAppender(ch.qos.logback.classic.Logger, String)
	 */
	public static boolean removeDelegateAppender(ch.qos.logback.classic.Logger logger) {
		return removeAppender(logger, DELEGATE_APPENDER_NAME);
	}

	/**
	 * Converts an SLF4J {@link Logger} to a Logback {@link ch.qos.logback.classic.Logger}.
	 *
	 * @param logger SLF4J {@link Logger} to convert.
	 * @return an {@link Optional} Logback {@link ch.qos.logback.classic.Logger} for the given SLF4J {@link Logger}
	 * iff the SLF4J {@link Logger} is {@literal not-null} and is a Logback {@link ch.qos.logback.classic.Logger}.
	 * @see java.util.Optional
	 * @see ch.qos.logback.classic.Logger
	 * @see org.slf4j.Logger
	 */
	public static Optional<ch.qos.logback.classic.Logger> toLogbackLogger(Logger logger) {
		return slf4jLoggerToLogbackLoggerConverter.apply(logger);
	}

	private static String nullSafeLoggerName(Logger logger) {
		return logger != null ? logger.getName() : null;
	}

	private static Class<?> nullSafeType(Object obj) {
		return obj != null ? obj.getClass() : null;
	}

	private static String nullSafeTypeName(Class<?> type) {
		return type != null ? type.getName() : null;
	}

	private static String nullSafeTypeName(Object obj) {
		return nullSafeTypeName(nullSafeType(obj));
	}

	private static String nullSafeTypeSimpleName(Class<?> type) {
		return type != null ? type.getSimpleName() : null;
	}

	private static String nullSafeTypeSimpleName(Object obj) {
		return nullSafeTypeSimpleName(nullSafeType(obj));
	}
}
