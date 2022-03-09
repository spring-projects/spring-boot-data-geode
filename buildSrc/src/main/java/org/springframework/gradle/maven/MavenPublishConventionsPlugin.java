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
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPomDeveloperSpec;
import org.gradle.api.publish.maven.MavenPomIssueManagement;
import org.gradle.api.publish.maven.MavenPomLicenseSpec;
import org.gradle.api.publish.maven.MavenPomOrganization;
import org.gradle.api.publish.maven.MavenPomScm;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Customizes the Maven POM generated from the Gradle {@link Project}.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.gradle.api.publish.PublishingExtension
 * @see org.gradle.api.publish.maven.MavenPom
 * @see org.gradle.api.publish.maven.MavenPublication
 * @see org.gradle.api.publish.maven.plugins.MavenPublishPlugin
 */
public class MavenPublishConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(MavenPublishPlugin.class).all(mavenPublishPlugin -> {

			customizeJavaPlugin(project);

			PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

			publishingExtension.getPublications().withType(MavenPublication.class).all(mavenPublication ->
				customizeMavenPom(project, mavenPublication.getPom()));
		});
	}

	private void customizeJavaPlugin(Project project) {

		project.getPlugins().withType(JavaPlugin.class).all(javaPlugin -> {

			JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);

			extension.withJavadocJar();
			extension.withSourcesJar();
		});
	}

	private void customizeMavenPom(Project project, MavenPom pom) {

		pom.getName().set(project.provider(project::getName));
		pom.getDescription().set(project.provider(project::getDescription));
		pom.getUrl().set("https://github.com/spring-projects/spring-boot-data-geode");
		pom.licenses(this::customizeLicences);
		pom.organization(this::customizeOrganization);
		pom.developers(this::customizeDevelopers);
		pom.scm(this::customizeScm);
		pom.issueManagement(this::customizeIssueManagement);
	}

	private void customizeDevelopers(MavenPomDeveloperSpec developers) {

		developers.developer(developer -> {
			developer.getName().set("VMware");
			developer.getEmail().set("info@vmware.com");
			developer.getOrganization().set("VMware, Inc.");
			developer.getOrganizationUrl().set("https://www.spring.io");
		});
	}

	private void customizeIssueManagement(MavenPomIssueManagement issueManagement) {

		issueManagement.getSystem().set("GitHub");
		issueManagement.getUrl().set("https://github.com/spring-projects/spring-boot-data-geode/issues");
	}

	private void customizeLicences(MavenPomLicenseSpec licences) {

		licences.license(licence -> {
			licence.getName().set("Apache License, Version 2.0");
			licence.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
		});
	}

	private void customizeOrganization(MavenPomOrganization organization) {

		organization.getName().set("VMware, Inc.");
		organization.getUrl().set("https://spring.io");
	}

	private void customizeScm(MavenPomScm scm) {

		scm.getConnection().set("scm:git:git://github.com/spring-projects/spring-boot-data-geode.git");
		scm.getDeveloperConnection().set("scm:git:ssh://git@github.com/spring-projects/spring-boot-data-geode.git");
		scm.getUrl().set("https://github.com/spring-projects/spring-boot-data-geode");
	}
}
