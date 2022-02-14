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
package io.spring.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaTestFixturesPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import org.springframework.gradle.propdeps.PropDepsPlugin;

/**
 * Creates a {@literal Management} Gradle {@link Configuration} that is appropriate for adding a platform
 * that it is not exposed externally.
 *
 * If the {@link JavaPlugin} is applied, then the {@literal compileClasspath}, {@literal runtimeClasspath},
 * {@literal testCompileClasspath}, and {@literal testRuntimeClasspath} will extend from it.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
public class ManagementConfigurationPlugin implements Plugin<Project> {

	public static final String MANAGEMENT_CONFIGURATION_NAME = "management";

	// TODO: Understand why we don't want certain Configurations to be consumed, resolved or visible???
	@Override
	public void apply(Project project) {

		ConfigurationContainer configurations = project.getConfigurations();

		configurations.create(MANAGEMENT_CONFIGURATION_NAME, management -> {

			management.setCanBeConsumed(false);
			management.setCanBeResolved(false);
			management.setVisible(false);

			PluginContainer plugins = project.getPlugins();

			plugins.withType(JavaPlugin.class, javaPlugin -> {
				configurations.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME).extendsFrom(management);
				configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME).extendsFrom(management);
				configurations.getByName(JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME).extendsFrom(management);
				configurations.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME).extendsFrom(management);
			});

			plugins.withType(JavaTestFixturesPlugin.class, javaTestFixturesPlugin -> {
				configurations.getByName("testFixturesCompileClasspath").extendsFrom(management);
				configurations.getByName("testFixturesRuntimeClasspath").extendsFrom(management);
			});

			plugins.withType(MavenPublishPlugin.class, mavenPublishPlugin -> {

				PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

				publishingExtension.getPublications().withType(MavenPublication.class, mavenPublication ->
					mavenPublication.versionMapping(versions ->
						versions.allVariants(VariantVersionMappingStrategy::fromResolutionResult)));
			});

			plugins.withType(PropDepsPlugin.class, propDepsPlugin -> {
				configurations.getByName("optional").extendsFrom(management);
				configurations.getByName("provided").extendsFrom(management);
			});
		});
	}
}
