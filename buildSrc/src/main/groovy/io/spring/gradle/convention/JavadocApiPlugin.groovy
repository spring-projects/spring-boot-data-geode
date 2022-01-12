/*
 * Copyright 2002-2016 the original author or authors.
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

import java.util.regex.Pattern

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.javadoc.Javadoc
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Rob Winch
 * @author John Blum
 */
class JavadocApiPlugin implements Plugin<Project> {

	Logger logger = LoggerFactory.getLogger(getClass())

	Set<Pattern> excludes = Collections.singleton(Pattern.compile("test"))

	@Override
	void apply(Project project) {

		Project rootProject = project.getRootProject()

		Javadoc api = project.tasks.create("api", Javadoc)

		api.setGroup("Documentation")
		api.setDescription("Generates aggregated Javadoc API documentation.")
		api.setDestinationDir(new File(project.getBuildDir(), "api"))
		api.setMaxMemory("1024m")

		api.doLast {
			if (JavaVersion.current().isJava11Compatible()) {
				project.copy({ copy -> copy
					.from(api.destinationDir)
					.into(api.destinationDir)
					.include("element-list")
					.rename("element-list", "package-list")
				})
			}
		}

		Set<Project> subprojects = rootProject.getSubprojects()

		if (subprojects.isEmpty()) {
			addProject(api, project)
		}

		for (Project subproject : subprojects) {
			addProject(api, subproject)
		}

		project.getPluginManager().apply("io.spring.convention.javadoc-options")
	}

	@SuppressWarnings("unused")
	void setExcludes(String... excludes) {
		excludes ?= new String[0]
		this.excludes = new HashSet<>(excludes.length)
		excludes.each {this.excludes.add(Pattern.compile(it)) }
	}

	private void addProject(Javadoc api, Project project) {

		if (isProjectIncluded(project)) {

			logInfo("Add sources for project {}", project)

			project.getPlugins().withType(SpringModulePlugin.class).all { plugin ->

				JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class)
				SourceSet mainSourceSet = java.getSourceSets().getByName("main")

				api.setSource(api.getSource().plus(mainSourceSet.getAllJava()))

				project.getTasks().withType(Javadoc.class).all(new Action<Javadoc>() {

					@Override
					void execute(Javadoc projectJavadoc) {
						api.setClasspath(api.getClasspath().plus(projectJavadoc.getClasspath()))
					}
				})
			}
		}
	}

	private boolean isProjectIncluded(Project project) {

		for (Pattern exclude : this.excludes) {
			if (exclude.matcher(project.getName()).matches()) {
				logInfo("Skipping project {} because it was excluded by {}", project, exclude)
				return false
			}
		}

		return true
	}

	private void logInfo(String message, Object... arguments) {
		this.logger.info(message, arguments)
	}
}
