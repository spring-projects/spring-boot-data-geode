/*
 * Copyright 2002-2017 the original author or authors.
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
import org.gradle.api.Task
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.bundling.Zip

/**
 * Aggregates Asciidoc, Javadoc, and deploying of the docs into a single Gradle Plugin.
 *
 * @author Rob Winch
 * @author John Blum
 */
class DocsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		PluginManager pluginManager = project.getPluginManager();

		pluginManager.apply("org.asciidoctor.jvm.convert");
		pluginManager.apply("org.asciidoctor.jvm.pdf");
		pluginManager.apply(AsciidoctorConventionPlugin);
		pluginManager.apply(DeployDocsPlugin);
		pluginManager.apply(JavadocApiPlugin);

		Task docsZip = project.tasks.create('docsZip', Zip) {

			archiveBaseName = project.rootProject.name
			archiveClassifier = 'docs'
			group = 'Distribution'
			description = "Builds -${archiveClassifier} archive containing all Docs for deployment at docs.spring.io."
			dependsOn 'api', 'asciidoctor'

			from(project.tasks.api.outputs) {
				into 'api'
			}

			into 'docs'
			duplicatesStrategy 'exclude'
		}

		Task docs = project.tasks.create("docs") {
			group = 'Documentation'
			description 'Aggregator Task to generate all documentation.'
			dependsOn docsZip
		}

		project.tasks.assemble.dependsOn docs
	}
}
