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

import io.spring.nohttp.gradle.NoHttpPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.PluginManager
import org.springframework.gradle.maven.SpringNexusPublishPlugin

/**
 * The Gradle {@link Plugin} applied to the {@literal root} Gradle {@link Project} with functionality inherited by
 * all Gradle {@link Project Projects} ({@literal sub-projects} in a multi-module project.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class RootProjectPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		applyPlugins(project)
		configureMavenCentralRepository(project)
		configureResolutionStrategy(project)
		configureSonarQube(project)
        createDependencyManagementExportTask(project)
		createReleasePublishedArtifactsTask(project)
	}

	@SuppressWarnings("all")
	private void applyPlugins(Project project) {

		PluginManager pluginManager = project.getPluginManager()

		pluginManager.apply(BasePlugin)
		pluginManager.apply(NoHttpPlugin)
		pluginManager.apply(SchemaPlugin)
		pluginManager.apply(SpringNexusPublishPlugin)
		pluginManager.apply("org.sonarqube")
	}

	/**
	 * Adds the Maven Central Repository to the list of repositories used by this Gradle {@link Project} build
	 * to resolve dependencies.
	 *
	 * @param project Gradle {@link Project}.
	 * @see org.gradle.api.Project
	 */
	@SuppressWarnings("all")
	private void configureMavenCentralRepository(Project project) {
		project.repositories.mavenCentral()
	}

	private void configureResolutionStrategy(Project project) {

		project.allprojects {
			configurations.all {
				resolutionStrategy {
					cacheChangingModulesFor 0, 'seconds'
					cacheDynamicVersionsFor 0, 'seconds'
				}
			}
		}
	}

	private void configureSonarQube(Project project) {

		String projectName = Utils.getProjectName(project)

		project.sonarqube {
			properties {
				property "sonar.projectName", projectName
				property "sonar.java.coveragePlugin", "jacoco"
				property "sonar.jacoco.reportPath", "${project.buildDir.name}/jacoco.exec"
				property "sonar.links.homepage", "https://spring.io/${projectName}"
				property "sonar.links.ci", "https://jenkins.spring.io/job/${projectName}/"
				property "sonar.links.issue", "https://github.com/spring-projects/${projectName}/issues"
				property "sonar.links.scm", "https://github.com/spring-projects/${projectName}"
				property "sonar.links.scm_dev", "https://github.com/spring-projects/${projectName}.git"
			}
		}
	}

	@SuppressWarnings("all")
	private void createDependencyManagementExportTask(Project project) {
		project.tasks.create("dependencyManagementExport", DependencyManagementExportTask)
	}

	private void createReleasePublishedArtifactsTask(Project project) {

		project.task("releasePublishedArtifacts", { Task releasePublishedArtifacts ->
			if (isReleasingToMavenCentral(project)) {
				releasePublishedArtifacts.dependsOn project.tasks.closeAndReleaseOssrhStagingRepository
			}
		})
	}

	@SuppressWarnings("all")
	private boolean isReleasingToMavenCentral(Project project) {
		Utils.isRelease(project) && project.hasProperty("ossrhUsername")
	}
}
