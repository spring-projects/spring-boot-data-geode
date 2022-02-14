/*
 * Copyright 2022-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.gradle.propdeps

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.javadoc.Javadoc

/**
 * Gradle {@link Plugin} to allow {@literal optional} and {@literal provided} dependency configurations.
 *
 * As stated in the Maven documentation, {@literal provided} scope {@literal "is only available on the compilation
 * and test classpath, and is not transitive"}.
 *
 * This {@link Plugin} creates two new configurations, and each one:
 *
 * <ul>
 * <li>is a parent of the compile configuration</li>
 * <li>is not visible, not transitive</li>
 * <li>all dependencies are excluded from the default configuration</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Brian Clozel
 * @author Rob Winch
 * @author John Blum
 *
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.springframework.gradle.propdeps.PropDepsEclipsePlugin
 * @see org.springframework.gradle.propdeps.PropDepsIdeaPlugin
 * @see <a href="https://www.gradle.org/docs/current/userguide/java_plugin.html#N121CF">Maven documentation</a>
 * @see <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope">Gradle configurations</a>
 */
class PropDepsPlugin implements Plugin<Project> {

	void apply(Project project) {

		project.plugins.apply(JavaPlugin)

		Configuration optional = addConfiguration(project, "optional")
		Configuration provided = addConfiguration(project, "provided")

		Javadoc javadoc = project.tasks.getByName(JavaPlugin.JAVADOC_TASK_NAME)

		javadoc.classpath = javadoc.classpath + provided + optional
	}

	private Configuration addConfiguration(Project project, String name) {

		Configuration configuration = project.configurations.create(name)

		configuration.extendsFrom(project.configurations.implementation)

		project.plugins.withType(JavaLibraryPlugin, {
			configuration.extendsFrom(project.configurations.api)
		})

		project.sourceSets.all {
			compileClasspath += configuration
			runtimeClasspath += configuration
		}

		return configuration
	}
}
