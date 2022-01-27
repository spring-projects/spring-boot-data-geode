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
import org.gradle.api.tasks.javadoc.Javadoc

/**
 * Configures Javadoc (Gradle Task) to disable the DocLint tool by setting the {@literal -Xdoclint} JVM extension option
 * to {@literal none} as well as setting the {@literal -quiet} Javadoc option thereby suppressing the output from
 * the Javadoc tool.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 * @see org.gradle.api.tasks.javadoc.Javadoc
 */
class JavadocOptionsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.getTasks().withType(Javadoc).all { task ->
			task.options.addStringOption('Xdoclint:none', '-quiet')
		}
	}
}
