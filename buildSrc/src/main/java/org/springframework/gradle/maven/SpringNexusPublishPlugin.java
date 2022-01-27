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
package org.springframework.gradle.maven;

import java.net.URI;
import java.time.Duration;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;

/**
 * Enables a Gradle {@link Project} to publish to Maven Central through OSSRH using Sonatype's Nexus Repository Manager.
 *
 * @author Rob Winch
 * @author John Blum
 * @see <a href="https://github.com/gradle-nexus/publish-plugin">Nexus Publish Gradle Plugin</a>
 */
public class SpringNexusPublishPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getPlugins().apply(NexusPublishPlugin.class);

		NexusPublishExtension nexusPublishing = project.getExtensions().findByType(NexusPublishExtension.class);

		// TODO: Why did we not simply use/configure the 'sonatype' repository and instead add a repo ('ossrh')?
		//  See here: https://github.com/gradle-nexus/publish-plugin#publishing-to-maven-central-via-sonatype-ossrh
		// NOTE: Careful, the keyword 'ossrh' is refered to in names in the Spring Build Conventions Gradle Plugins,
		// such as, but not limited to:
		// * 'ossrhUsername'
		// * 'publishToOssrh'
		// * 'closeAndReleaseOssrhStagingRepository'
		nexusPublishing.getRepositories().create("ossrh", nexusRepository -> {
			nexusRepository.getNexusUrl().set(URI.create("https://s01.oss.sonatype.org/service/local/"));
			nexusRepository.getSnapshotRepositoryUrl()
				.set(URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/"));
		});

		nexusPublishing.getClientTimeout().set(Duration.ofMinutes(3));
		nexusPublishing.getConnectTimeout().set(Duration.ofMinutes(3));
	}
}
