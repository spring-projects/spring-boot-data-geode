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
package org.springframework.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Copies {@literal root} {@link Project} properties to the target ({@literal this}) {@link Project},
 * the {@link Project} for which {@literal this} Gradle {@link Plugin} is applied.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
public class CopyPropertiesPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		copyPropertyFromRootProjectTo("group", project);
		copyPropertyFromRootProjectTo("version", project);
		copyPropertyFromRootProjectTo("description", project);
	}

	private void copyPropertyFromRootProjectTo(String propertyName, Project project) {

		Object propertyValue = project.getRootProject().findProperty(propertyName);

		if (propertyValue != null) {
			project.setProperty(propertyName, propertyValue);
		}
	}
}
