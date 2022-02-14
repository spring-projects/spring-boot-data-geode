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
import org.gradle.api.plugins.JavaPlugin

/**
 * Defines sets of dependencies to make it easy to add a related group of dependencies to a Gradle {@link Project}.
 *
 * The dependencies set defined include:
 *
 * <ul>
 * <li>jstlDependencies</li>
 * <li>seleniumDependencies</li>
 * <li>slf4jDependencies</li>
 * <li>testDependencies</li>
 * </ul>
 *
 *{@literal testDependencies} are automatically added to Java projects
 * ({@lin Project Projects} with the {@link JavaPlugin} applied).
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
class DependencySetPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.ext.jstlDependencies = [
			"jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api",
			"org.glassfish.web:jakarta.servlet.jsp.jstl"
		]

		project.ext.seleniumDependencies = [
			"org.seleniumhq.selenium:htmlunit-driver",
			"org.seleniumhq.selenium:selenium-support"
		]

		project.ext.slf4jDependencies = [
			"org.slf4j:slf4j-api",
			"org.slf4j:jcl-over-slf4j",
			"org.slf4j:jul-over-slf4j",
			"org.slf4j:log4j-over-slf4j",
			"ch.qos.logback:logback-classic"
		]

		project.ext.testDependencies = [
			"junit:junit",
			"org.junit.jupiter:junit-jupiter-api",
			"org.junit.vintage:junit-vintage-engine",
			"org.assertj:assertj-core",
			"org.mockito:mockito-core",
			"org.projectlombok:lombok",
			"org.springframework:spring-test",
			"org.springframework.data:spring-data-geode-test",
			"edu.umd.cs.mtc:multithreadedtc"
		]

		project.plugins.withType(JavaPlugin) {
			project.dependencies {
				testImplementation project.testDependencies
			}
		}
	}
}
