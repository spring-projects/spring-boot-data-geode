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
package org.springframework.geode.util;

import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.management.internal.security.ResourceConstants;

/**
 * Abstract utility class encapsulating common Apache Geode constants used by SBDG.
 *
 * @author John Blum
 * @since 1.3.0
 */
public abstract class GeodeConstants {

	// Logging Constants (referring to Properties)
	public static final String LOG_DISK_SPACE_LIMIT = DistributionConfig.LOG_DISK_SPACE_LIMIT_NAME;
	public static final String LOG_FILE = DistributionConfig.LOG_FILE_NAME;
	public static final String LOG_FILE_SIZE_LIMIT = DistributionConfig.LOG_FILE_SIZE_LIMIT_NAME;
	public static final String LOG_LEVEL = DistributionConfig.LOG_LEVEL_NAME;

	// Security Constants (referring to Properties)
	public static final String PASSWORD = ResourceConstants.PASSWORD;
	public static final String USERNAME = ResourceConstants.USER_NAME;

}
