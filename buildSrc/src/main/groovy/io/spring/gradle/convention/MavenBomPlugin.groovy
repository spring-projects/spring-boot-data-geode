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
package io.spring.gradle.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.springframework.gradle.maven.SpringMavenPlugin

/**
 * @author Rob Winch
 * @author John Blum
 */
class MavenBomPlugin implements Plugin<Project> {

	static String MAVEN_BOM_TASK_NAME = "mavenBom"

	@Override
	void apply(Project project) {

		// Declares a new configuration that will be used to associate with Project artifacts
		// (namely, the Maven BOM file).
		project.configurations {
			archives
		}

		project.group = project.rootProject.group
		project.plugins.apply(SpringMavenPlugin)

		Utils.skipProjectWithSonarQubePlugin(project)

		project.task(MAVEN_BOM_TASK_NAME, type: MavenBomTask, group: 'Generate',
			description: 'Configures the Maven POM as a Maven BOM (Bill of Materials)')

		project.tasks.artifactoryPublish.dependsOn project.mavenBom
		project.tasks.publishToOssrh.dependsOn project.mavenBom

		project.rootProject.allprojects.each { p ->
			p.plugins.withType(SpringMavenPlugin) {
				if (!project.name.equals(p.name)) {
					project.mavenBom.projects.add(p)
				}
			}
		}

		// TODO: Shouldn't this be { archives project.mavenBom } according to:
		//  https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#getArtifacts--
		// TODO: Is this even necessary since this block is defined in MavenBomTask?
		project.artifacts {
			archives project.mavenBom.bomFile
		}

		// TODO: Why?
		Utils.configureDeployArtifactsTask(project)
	}
}
