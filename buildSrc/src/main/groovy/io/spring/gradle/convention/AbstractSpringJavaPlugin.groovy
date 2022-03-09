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
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.springframework.gradle.CopyPropertiesPlugin
import org.springframework.gradle.propdeps.PropDepsEclipsePlugin
import org.springframework.gradle.propdeps.PropDepsIdeaPlugin
import org.springframework.gradle.propdeps.PropDepsPlugin

/**
 * Abstract base Gradle {@link Plugin} for all Spring Java &amp; Groovy Gradle Plugins used by SBDG.
 *
 * This abstract base Gradle {@link Plugin} primarily serves to apply a common set of Gradle {@link Plugin Plugins),
 * such as the {@link JavaPlugin} and {@link GroovyPlugin} for the various SBDG project Spring modules as well as other
 * Spring Gradle {@link Plugin Plugins} to manage builds, IDE integration, releases and so on.
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Plugin
 * @see org.gradle.api.Project
 */
abstract class AbstractSpringJavaPlugin implements Plugin<Project> {

	@Override
	final void apply(Project project) {

		applyPlugins(project)
		setJarManifestAttributes(project)

		project.test {
			useJUnitPlatform()
		}

		applyAdditionalPlugins(project)
	}

	private void applyPlugins(Project project) {

		PluginManager pluginManager = project.getPluginManager()

		applyJavaPlugin(pluginManager)
		applyGroovyPlugin(project)
		applyIdePlugins(pluginManager)
		applySpringPlugins(pluginManager)
	}

	@SuppressWarnings("all")
	private void applyGroovyPlugin(Project project) {

		if (project.file("src/main/groovy").exists()
			|| project.file("src/test/groovy").exists()
			|| project.file("src/integration-test/groovy").exists()) {

			project.getPluginManager().apply(GroovyPlugin.class)
		}
	}

	@SuppressWarnings("all")
	private void applyIdePlugins(PluginManager pluginManager) {

		pluginManager.apply(EclipsePlugin)
		pluginManager.apply(IdeaPlugin)
	}

	@SuppressWarnings("all")
	private void applyJavaPlugin(PluginManager pluginManager) {
		pluginManager.apply(JavaPlugin.class)
	}

	@SuppressWarnings("all")
	private void applySpringPlugins(PluginManager pluginManager) {

		pluginManager.apply(ManagementConfigurationPlugin)
		pluginManager.apply(RepositoryConventionPlugin)
		pluginManager.apply(PropDepsPlugin)
		pluginManager.apply(PropDepsEclipsePlugin)
		pluginManager.apply(PropDepsIdeaPlugin)
		pluginManager.apply(SpringDependencyManagementConventionsPlugin)
		pluginManager.apply(DependencySetPlugin)
		pluginManager.apply(TestsConfigurationPlugin)
		pluginManager.apply(IntegrationTestPlugin)
		pluginManager.apply(JacocoPlugin);
		pluginManager.apply(JavadocOptionsPlugin)
		pluginManager.apply(CheckstylePlugin)
		pluginManager.apply(CopyPropertiesPlugin)
	}

	private void setJarManifestAttributes(Project project) {

		project.jar {
			manifest.attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
			manifest.attributes["Implementation-Title"] = project.name
			manifest.attributes["Implementation-Version"] = project.version
			manifest.attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
		}
	}

	protected abstract void applyAdditionalPlugins(Project project);

}
