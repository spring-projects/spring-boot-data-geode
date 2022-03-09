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

import io.spring.gradle.convention.ArtifactoryPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Enables publishing to Maven for a Spring module Gradle {@link Project}.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.gradle.api.publish.maven.plugins.MavenPublishPlugin
 */
public class SpringMavenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		PluginManager pluginManager = project.getPluginManager();

		pluginManager.apply(MavenPublishPlugin.class);
		pluginManager.apply(MavenPublishConventionsPlugin.class);
		pluginManager.apply(PublishAllJavaComponentsPlugin.class);
		pluginManager.apply(PublishArtifactsPlugin.class);
		pluginManager.apply(PublishLocalPlugin.class);
		pluginManager.apply(SpringSigningPlugin.class);
		pluginManager.apply(ArtifactoryPlugin.class);
	}
}
