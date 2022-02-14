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
package org.springframework.gradle.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.spring.gradle.convention.Utils;

/**
 * Publishes Gradle {@link Project} artifacts to either Artifactory or Maven Central.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see <a href="https://www.jfrog.com/confluence/display/JFROG/Gradle+Artifactory+Plugin">Artifatory Gradle Plugin</a>
 * @see <a href="https://central.sonatype.org/publish/publish-gradle/">Maven Central Sonatype Gradle Support</a>
 */
public class PublishArtifactsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getTasks().register("publishArtifacts", publishArtifactsTask -> {

			publishArtifactsTask.setGroup("Publishing");
			publishArtifactsTask.setDescription("Publish project artifacts to either Artifactory or Maven Central"
				+ " based on the project version.");

			if (Utils.isRelease(project)) {
				publishArtifactsTask.dependsOn("publishToOssrh");
			}
			else {
				publishArtifactsTask.dependsOn("artifactoryPublish");
			}
		});
	}
}
