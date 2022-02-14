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

import java.util.concurrent.Callable;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

import io.spring.gradle.convention.Utils;

/**
 * Signs all Gradle {@link Project} artifacts.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.gradle.plugins.signing.SigningPlugin
 */
public class SpringSigningPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		if (isSigningRequired(project)) {
			project.getPluginManager().apply(SigningPlugin.class);
			sign(project);
		}
	}

	private boolean isSigningRequired(Project project) {
		return isSigningKeyPresent(project) && isRelease(project);
	}

	private boolean isSigningKeyPresent(Project project) {

		return project.hasProperty("signing.keyId")
			|| project.hasProperty("signingKeyId")
			|| project.hasProperty("signingKey");
	}

	private boolean isRelease(Project project) {
		return Utils.isRelease(project);
	}

	private void sign(Project project) {

		SigningExtension signing = findAndConfigureSigningExtension(project);

		project.getPlugins().withType(PublishAllJavaComponentsPlugin.class).all(publishJavaComponentsPlugin -> {
			PublishingExtension publishing = project.getExtensions().findByType(PublishingExtension.class);
			Publication maven = publishing.getPublications().getByName("mavenJava");
			signing.sign(maven);
		});
	}

	private SigningExtension findAndConfigureSigningExtension(Project project) {

		SigningExtension signingExtension = project.getExtensions().findByType(SigningExtension.class);

		return configurePgpKeys(project, configureSigningRequired(project, signingExtension));
	}

	private SigningExtension configureSigningRequired(Project project, SigningExtension signing) {

		Callable<Boolean> signingRequired =
			() -> project.getGradle().getTaskGraph().hasTask("publishArtifacts");

		signing.setRequired(signingRequired);

		return signing;
	}

	private SigningExtension configurePgpKeys(Project project, SigningExtension signing) {

		String signingKey = Utils.findPropertyAsString(project, "signingKey");
		String signingKeyId = resolveSigningKeyId(project);
		String signingPassword = Utils.findPropertyAsString(project, "signingPassword");

		if (signingKeyId != null) {
			signing.useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword);
		}
		else {
			signing.useInMemoryPgpKeys(signingKey, signingPassword);
		}

		return signing;
	}

	private String resolveSigningKeyId(Project project) {

		String signingKeyId = Utils.findPropertyAsString(project, "signingKeyId");

		return signingKeyId != null ? signingKeyId
			: Utils.findPropertyAsString(project, "signing.keyId");
	}
}
