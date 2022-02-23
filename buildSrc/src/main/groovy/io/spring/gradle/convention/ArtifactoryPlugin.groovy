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

/**
 * Applies and configures the JFrag Artifactory Gradle {@link Plugin} to publish Gradle {@link Project} artifacts
 * to the Spring {@literal snapshot}, {@literal milestone} and {@literal release} repositories in Artifactory.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class ArtifactoryPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.plugins.apply('com.jfrog.artifactory')

		// (Externally-defined) Methods cannot be invoked inside the Groovy/Gradle DSL.
		def artifactoryRepoKey = resolveRepositoryKey(project)
		def authRequired = isAuthRequired(project)

		project.artifactory {
			contextUrl = 'https://repo.spring.io'
			publish {
				repository {
					repoKey = artifactoryRepoKey
					if (authRequired) {
						username = artifactoryUsername
						password = artifactoryPassword
					}
				}
				defaults {
					publications('mavenJava')
				}
			}
		}
	}

	@SuppressWarnings("all")
	private boolean isAuthRequired(Project project) {
		project?.hasProperty('artifactoryUsername')
	}

	@SuppressWarnings("all")
	private String resolveRepositoryKey(Project project) {

		boolean isSnapshot = Utils.isSnapshot(project);
		boolean isMilestone = Utils.isMilestone(project);

		return isSnapshot ? 'libs-snapshot-local'
			: isMilestone ? 'libs-milestone-local'
			: 'libs-release-local'
	}
}
