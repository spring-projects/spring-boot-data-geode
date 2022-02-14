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

import org.gradle.api.Project
import org.sonarqube.gradle.SonarQubePlugin

/**
 * Utility class encapsulating common operations on Gradle {@link Project Projects}.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Project
 */
class Utils {

	private Utils() {}

	static String getProjectName(Project project) {

		String projectName = project.getRootProject().getName()

		if (projectName.endsWith("-build")) {
			projectName = projectName.substring(0, projectName.length() - "-build".length())
		}

		return projectName
	}

	static boolean isMilestone(Project project) {
		return projectVersion(project).matches('^.*[.-]M\\d+$')
			|| projectVersion(project).matches('^.*[.-]RC\\d+$')
	}

	static boolean isRelease(Project project) {
		return !(isSnapshot(project) || isMilestone(project))
	}

	static boolean isSnapshot(Project project) {
		return projectVersion(project).matches('^.*([.-]BUILD)?-SNAPSHOT$')
	}

	private static String projectVersion(Project project) {
		return String.valueOf(project.version)
	}

	static String findPropertyAsString(Project project, String propertyName) {
		return (String) project.findProperty(propertyName)
	}

	static void skipProjectWithSonarQubePlugin(Project project) {

		project.plugins.withType(SonarQubePlugin) {
			project.sonarqube.skipProject = true
		}
	}
}
