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
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Adds Java and JavaPlatform based Gradle {@link Project Pojects} to be published by Maven.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.gradle.api.plugins.JavaPlatformPlugin
 * @see org.gradle.api.plugins.JavaPlugin
 * @see org.gradle.api.publish.PublishingExtension
 * @see org.gradle.api.publish.maven.MavenPublication
 * @see org.gradle.api.publish.maven.plugins.MavenPublishPlugin
 */
public class PublishAllJavaComponentsPlugin implements Plugin<Project> {

	private static final String JAVA_COMPONENT_NAME = "java";
	private static final String JAVA_PLATFORM_COMPONENT_NAME = "javaPlatform";

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(MavenPublishPlugin.class).all(mavenPublish -> {

			PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

			publishingExtension.getPublications().create("mavenJava", MavenPublication.class, mavenPublication -> {

				project.getPlugins().withType(JavaPlugin.class, javaPlugin ->
					mavenPublication.from(project.getComponents().getByName(JAVA_COMPONENT_NAME)));

				project.getPlugins().withType(JavaPlatformPlugin.class, javaPlatformPlugin ->
					mavenPublication.from(project.getComponents().getByName(JAVA_PLATFORM_COMPONENT_NAME)));
			});
		});
	}
}
