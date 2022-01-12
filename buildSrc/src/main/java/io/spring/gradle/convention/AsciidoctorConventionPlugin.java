/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.spring.gradle.convention;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask;
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension;
import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin;
import org.asciidoctor.gradle.jvm.AsciidoctorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Sync;

/**
 * Conventions that are applied in the presence of the {@link AsciidoctorJPlugin}.
 *
 * When the plugin is applied:
 *
 * <ul>
 * <li>All warnings are made fatal.
 * <li>A task is created to resolve and unzip our documentation resources (CSS and Javascript).
 * <li>For each {@link AsciidoctorTask} (HTML only):
 * <ul>
 * <li>A configuration named asciidoctorExtensions is ued to add the
 * <a href="https://github.com/spring-io/spring-asciidoctor-extensions#block-switch">block switch</a> extension
 * <li>{@code doctype} {@link AsciidoctorTask#options(Map) option} is configured.
 * <li>{@link AsciidoctorTask#attributes(Map) Attributes} are configured for syntax highlighting, CSS styling,
 * docinfo, etc.
 * </ul>
 * <li>For each {@link AbstractAsciidoctorTask} (HTML and PDF):
 * <ul>
 * <li>{@link AsciidoctorTask#attributes(Map) Attributes} are configured to enable warnings for references to
 * missing attributes, the year is added as @{code today-year}, etc
 * <li>{@link AbstractAsciidoctorTask#baseDirFollowsSourceDir() baseDirFollowsSourceDir()} is enabled.
 * </ul>
 * </ul>
 *
 * @author Andy Wilkinson
 * @author Rob Winch
 */
public class AsciidoctorConventionPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(AsciidoctorJPlugin.class, asciidoctorPlugin -> {

			createDefaultAsciidoctorRepository(project);
			makeAllWarningsFatal(project);

			Sync unzipResources = createUnzipDocumentationResourcesTask(project);

			project.getTasks().withType(AbstractAsciidoctorTask.class, asciidoctorTask -> {

				asciidoctorTask.dependsOn(unzipResources);
				configureExtensions(project, asciidoctorTask);
				configureCommonAttributes(project, asciidoctorTask);
				configureOptions(asciidoctorTask);
				asciidoctorTask.baseDirFollowsSourceDir();
				asciidoctorTask.useIntermediateWorkDir();

				asciidoctorTask.resources(resourcesSpec -> {
					resourcesSpec.from(unzipResources);
					resourcesSpec.from(asciidoctorTask.getSourceDir(), resourcesSrcDirSpec -> {
						// https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/523
						// For now copy the entire sourceDir over so that include files are
						// available in the intermediateWorkDir
						// resourcesSrcDirSpec.include("images/**");
					});
				});

				if (asciidoctorTask instanceof AsciidoctorTask) {
					configureHtmlOnlyAttributes(project, asciidoctorTask);
				}
			});
		});
	}

	private void createDefaultAsciidoctorRepository(Project project) {

		project.getGradle().afterProject(it -> {

			RepositoryHandler repositories = it.getRepositories();

			if (repositories.isEmpty()) {
				repositories.mavenCentral();
				repositories.maven(repo -> repo.setUrl(URI.create("https://repo.spring.io/release")));
			}
		});
	}

	@SuppressWarnings("all")
	private Sync createUnzipDocumentationResourcesTask(Project project) {

		Configuration documentationResources = project.getConfigurations().maybeCreate("documentationResources");

		documentationResources.getDependencies()
			.add(project.getDependencies().create("io.spring.docresources:spring-doc-resources:0.2.5"));

		Sync unzipResources = project.getTasks().create("unzipDocumentationResources", Sync.class, sync -> {

			sync.dependsOn(documentationResources);

			sync.from((Callable<List<FileTree>>) () -> {
				List<FileTree> result = new ArrayList<>();
				documentationResources.getAsFileTree().forEach(file -> result.add(project.zipTree(file)));
				return result;
			});

			File destination = new File(project.getBuildDir(), "docs/resources");

			sync.into(project.relativePath(destination));

		});

		return unzipResources;
	}

	private void configureCommonAttributes(Project project, AbstractAsciidoctorTask asciidoctorTask) {

		Map<String, Object> attributes = new HashMap<>();

		attributes.put("attribute-missing", "warn");
		attributes.put("icons", "font");
		attributes.put("idprefix", "");
		attributes.put("idseparator", "-");
		attributes.put("docinfo", "shared");
		attributes.put("sectanchors", "");
		attributes.put("sectnums", "");
		attributes.put("today-year", LocalDate.now().getYear());

		asciidoctorTask.attributes(attributes);
	}

	private void configureHtmlOnlyAttributes(Project project, AbstractAsciidoctorTask asciidoctorTask) {

		Map<String, Object> attributes = new HashMap<>();

		attributes.put("source-highlighter", "highlight.js");
		attributes.put("highlightjsdir", "js/highlight");
		attributes.put("highlightjs-theme", "github");
		attributes.put("linkcss", true);
		attributes.put("icons", "font");
		attributes.put("stylesheet", "css/spring.css");

		asciidoctorTask.getAttributeProviders().add(() -> {

			Object version = project.getVersion();

			Map<String, Object> localAttributes = new HashMap<>();

			if (version != null && !Project.DEFAULT_VERSION.equals(version)) {
				localAttributes.put("revnumber", version);
			}

			return localAttributes;
		});

		asciidoctorTask.attributes(attributes);
	}

	private void configureExtensions(Project project, AbstractAsciidoctorTask asciidoctorTask) {

		Configuration extensionsConfiguration = project.getConfigurations().maybeCreate("asciidoctorExtensions");

		extensionsConfiguration.defaultDependencies(dependencies -> dependencies.add(project.getDependencies()
			.create("io.spring.asciidoctor:spring-asciidoctor-extensions-block-switch:0.4.2.RELEASE")));

		asciidoctorTask.configurations(extensionsConfiguration);
	}

	private void configureOptions(AbstractAsciidoctorTask asciidoctorTask) {
		asciidoctorTask.options(Collections.singletonMap("doctype", "book"));
	}

	private void makeAllWarningsFatal(Project project) {
		project.getExtensions().getByType(AsciidoctorJExtension.class).fatalWarnings(".*");
	}
}
