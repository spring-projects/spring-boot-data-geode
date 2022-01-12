/*
 * Copyright 2016-2021 the original author or authors.
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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Gradle API Task to output all the configured project & subproject (runtime) dependencies.
 *
 * @author Rob Winch
 * @author John Blum
 */
class DependencyManagementExportTask extends DefaultTask {

	@Internal
	def projects;

	@Input
	String getProjectNames() {
		return this.projects*.name
	}

	@TaskAction
	void dependencyManagementExport() throws IOException {

		def projects = this.projects ?: project.subprojects + project

		def configurations = projects*.configurations*.findAll {
			[ 'testRuntime', 'integrationTestRuntime', 'grettyRunnerTomcat10', 'ajtools' ].contains(it.name)
		}

		def dependencyResults = configurations*.incoming*.resolutionResult*.allDependencies.flatten()

		def moduleVersionVersions = dependencyResults
			.findAll { r -> r.requested instanceof ModuleComponentSelector }
			.collect { r -> r.selected.moduleVersion }

		def projectDependencies = projects.collect { p ->
			"${p.group}:${p.name}:${p.version}".toString()
		} as Set

		def dependencies = moduleVersionVersions
			.collect { d -> "${d.group}:${d.name}:${d.version}".toString() }
			.sort() as Set

		println ''
		println ''
		println 'dependencyManagement {'
		println '\tdependencies {'

		dependencies
			.findAll { d -> !projectDependencies.contains(d) }
			.each { println "\t\tdependency '$it'" }

		println '\t}'
		println '}'
		println ''
		println ''
		println 'TIP Use this to find duplicates:\n$ sort gradle/dependency-management.gradle| uniq -c | grep -v \'^\\s*1\''
		println ''
		println ''
	}
}
