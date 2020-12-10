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
package org.springframework.geode.cloud.bindings;

import org.springframework.core.env.Environment;

/**
 * Abstract utility class to assess the Spring {@link Environment}
 *
 * @author John Blum
 * @see org.springframework.core.env.Environment
 * @since 1.4.1
 */
@SuppressWarnings("unused")
public abstract class Guards {

	public static final String SPRING_CLOUD_BOOT_BINDINGS_ENABLED_PROPERTY =
		"org.springframework.cloud.bindings.boot.enable";

	public static final String SPRING_CLOUD_BOOT_BINDINGS_TYPE_ENABLED_PROPERTY =
		"org.springframework.cloud.bindings.boot.%s.enable";

	public static boolean isGlobalEnabled(Environment environment) {
		return environment.getProperty(SPRING_CLOUD_BOOT_BINDINGS_ENABLED_PROPERTY, Boolean.class, false);
	}

	public static boolean isTypeEnabled(Environment environment, String type) {

		String propertyName = String.format(SPRING_CLOUD_BOOT_BINDINGS_TYPE_ENABLED_PROPERTY, type);

		return environment.getProperty(propertyName, Boolean.class, true);
	}
}
