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

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Gradle Plugin used to publish all {@link Project} artifacts locally
 * under {@literal rootProject/buildDir/publications/repos}.
 *
 * This is useful for inspecting the generated {@link Project} artifacts to ensure they are correct
 * before publishing the {@link Project} artifacts to Artifactory or Maven Central.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @since 2.0.0
 */
public class PublishLocalPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(MavenPublishPlugin.class).all(mavenPublish -> {

			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

			publishing.getRepositories().maven(maven -> {
				maven.setName("local");
				maven.setUrl(new File(project.getRootProject().getBuildDir(), "publications/repos"));
			});
		});
	}
}
