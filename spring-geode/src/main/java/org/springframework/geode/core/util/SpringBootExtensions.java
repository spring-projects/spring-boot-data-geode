/*
 * Copyright 2020 the original author or authors.
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

import java.util.Optional;

import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class used to perform actions on Spring Boot configuration and components.
 *
 * @author John Blum
 * @see org.springframework.geode.core.util.SpringExtensions
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public abstract class SpringBootExtensions extends SpringExtensions {

	/**
	 * Cleans up all resources allocated by the {@link LoggingSystem} loaded, configured and initialized by Spring Boot.
	 *
	 * @see #cleanUpLoggingSystem(ClassLoader)
	 * @see java.lang.ClassLoader
	 */
	public static void cleanUpLoggingSystem() {
		cleanUpLoggingSystem(ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Cleans up all resources allocated by the {@link LoggingSystem} loaded, configured and initialized by Spring Boot.
	 *
	 * @param classLoader Java {@link ClassLoader} used to resolve the Spring Boot {@link LoggingSystem}
	 * representing the logging provider (e.g. Logback).
	 * @see java.lang.ClassLoader
	 */
	public static void cleanUpLoggingSystem(@NonNull ClassLoader classLoader) {

		Optional.ofNullable(LoggingSystemFactory.fromSpringFactories())
			.map(loggingSystemFactory -> loggingSystemFactory.getLoggingSystem(classLoader))
			.ifPresent(LoggingSystem::cleanUp);
	}
}
