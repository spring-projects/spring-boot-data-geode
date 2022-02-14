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
package io.spring.gradle.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Deploys the Spring XML schema (XSD) files to the spring.io server.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class SchemaDeployPlugin implements Plugin<Project> {

	static final String DEFAULT_SPRING_DOCS_HOST = 'docs-ip.spring.io';

	@Override
	void apply(Project project) {

		project.getPluginManager().apply('org.hidetake.ssh')

		project.ssh.settings {
			knownHosts = allowAnyHosts
		}

		project.remotes {
			docs {

				retryCount = 5 // Retry 5 times (default is 0)
				retryWaitSec = 10 // Wait 10 seconds between retries (default is 0)
				role 'docs'

				host = project.hasProperty('deployDocsHost')
					? project.findProperty('deployDocsHost')
					: DEFAULT_SPRING_DOCS_HOST

				user = project.findProperty('deployDocsSshUsername')

				identity = project.hasProperty('deployDocsSshKeyPath')
					? project.file(project.findProperty('deployDocsSshKeyPath'))
					: project.hasProperty('deployDocsSshKey')
					? project.findProperty('deployDocsSshKey')
					: null

				passphrase = project.hasProperty('deployDocsSshPassphrase')
					? project.findProperty('deployDocsSshPassphrase')
					: null
			}
		}

		project.task('deploySchema') {
			dependsOn 'schemaZip'
			doFirst {
				project.ssh.run {
					session(project.remotes.docs) {

						def now = System.currentTimeMillis()
						def name = project.rootProject.name
						def version = project.rootProject.version
						def tempPath = "/tmp/${name}-${now}-schema/".replaceAll(' ', '_')

						execute "mkdir -p $tempPath"

						project.tasks.schemaZip.outputs.each { out ->
							println "Putting $out.files"
							put from: out.files, into: tempPath
						}

						execute "unzip $tempPath*.zip -d $tempPath"

						def extractPath = "/var/www/domains/spring.io/docs/htdocs/autorepo/schema/${name}/${version}/"

						execute "rm -rf $extractPath"
						execute "mkdir -p $extractPath"
						execute "rm -f $tempPath*.zip"
						execute "rm -rf $extractPath*"
						execute "mv $tempPath/* $extractPath"
						execute "chmod -R g+w $extractPath"
					}
				}
			}
		}
	}
}
