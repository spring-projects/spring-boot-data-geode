/*
 * Copyright 2022-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.spring.gradle.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Configures and applies the Checkstyle Gradle {@link Plugin}.
 *
 * @author Vedran Pavic
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class CheckstylePlugin implements Plugin<Project> {

	static final String CHECKSTYLE_PATHNAME = 'etc/checkstyle'
	static final String CHECKSTYLE_VERSION = '8.21'

	@Override
	void apply(Project project) {

		project.plugins.withType(JavaPlugin) {

			def checkstyleDirectory = project.rootProject.file(CHECKSTYLE_PATHNAME)

			if (checkstyleDirectory?.isDirectory()) {

				project.getPluginManager().apply('checkstyle')
				project.dependencies.add('checkstyle', 'io.spring.javaformat:spring-javaformat-checkstyle')
				project.dependencies.add('checkstyle', 'io.spring.nohttp:nohttp-checkstyle')

				project.checkstyle {
					configDirectory = checkstyleDirectory
					toolVersion = CHECKSTYLE_VERSION
				}
			}
		}
	}
}
