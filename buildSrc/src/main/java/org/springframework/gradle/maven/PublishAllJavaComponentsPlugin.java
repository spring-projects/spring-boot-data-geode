package org.springframework.gradle.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Adds the Java and JavaPlatform based projects to be published via Maven.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
public class PublishAllJavaComponentsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(MavenPublishPlugin.class).all(mavenPublish -> {

			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

			publishing.getPublications().create("mavenJava", MavenPublication.class, maven -> {
				project.getPlugins().withType(JavaPlugin.class,
					plugin -> maven.from(project.getComponents().getByName("java")));

				project.getPlugins().withType(JavaPlatformPlugin.class,
					plugin -> maven.from(project.getComponents().getByName("javaPlatform")));
			});
		});
	}
}
