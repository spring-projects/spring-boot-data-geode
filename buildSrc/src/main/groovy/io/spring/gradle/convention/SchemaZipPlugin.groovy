/*
 * Copyright 2016-2019 the original author or authors.
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
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Zip

/**
 * Zips all Spring XML schemas (XSD) files.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class SchemaZipPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		Zip schemaZip = project.tasks.create('schemaZip', Zip)

		schemaZip.archiveBaseName = project.rootProject.name
		schemaZip.archiveClassifier = 'schema'
		schemaZip.description = "Builds -${schemaZip.archiveClassifier} archive containing all XSDs" +
			" for deployment to static.springframework.org/schema."
		schemaZip.group = 'Distribution'

		project.rootProject.subprojects.each { module ->

			module.getPlugins().withType(JavaPlugin.class).all {

				Properties schemas = new Properties();

				module.sourceSets.main.resources
					.find { it.path.endsWith('META-INF/spring.schemas') }
					?.withInputStream { schemas.load(it) }

				for (def key : schemas.keySet()) {

					def zipEntryName = key.replaceAll(/http.*schema.(.*).spring-.*/, '$1')

					assert zipEntryName != key

					File xsdFile = module.sourceSets.main.resources.find {
						it.path.endsWith(schemas.get(key))
					}

					assert xsdFile != null

					schemaZip.into(zipEntryName) {
						duplicatesStrategy DuplicatesStrategy.EXCLUDE
						from xsdFile.path
					}
				}
			}
		}
	}
}
