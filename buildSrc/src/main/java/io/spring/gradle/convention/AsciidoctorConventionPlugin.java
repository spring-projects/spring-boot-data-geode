/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.spring.gradle.convention;

import java.io.File;
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
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Sync;

/**
 * Conventions that are applied in the presence of the {@link AsciidoctorJPlugin}.
 * <p/>
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
 * @author John Blum
 */
public class AsciidoctorConventionPlugin implements Plugin<Project> {

	private static final String ASCIIDOCTORJ_VERSION = "2.4.3";
	private static final String SPRING_ASCIIDOCTOR_BACKENDS_VERSION = "0.0.5";
	private static final String SPRING_DOC_RESOURCES_VERSION = "0.2.5";

	private static final String SPRING_ASCIIDOCTOR_BACKENDS_DEPENDENCY =
		String.format("io.spring.asciidoctor.backends:spring-asciidoctor-backends:%s",
			SPRING_ASCIIDOCTOR_BACKENDS_VERSION);

	@SuppressWarnings("unused")
	private static final String SPRING_DOC_RESOURCES_DEPENDENCY =
		String.format("io.spring.docresources:spring-doc-resources:%s", SPRING_DOC_RESOURCES_VERSION);

	@Override
	public void apply(Project project) {

		project.getPlugins().withType(AsciidoctorJPlugin.class, asciidoctorPlugin -> {

			setAsciidoctorJVersion(project);
			makeAllWarningsFatal(project);
			createAsciidoctorExtensionsConfiguration(project);

			Sync unzipResources = createUnzipDocumentationResourcesTask(project);

			project.getTasks().withType(AbstractAsciidoctorTask.class, asciidoctorTask -> {

				asciidoctorTask.dependsOn(unzipResources);
				configureAsciidoctorTask(project, asciidoctorTask);

				asciidoctorTask.resources(resourcesSpec -> {
					resourcesSpec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
					resourcesSpec.from(unzipResources);
					resourcesSpec.from(asciidoctorTask.getSourceDir(), resourcesSrcDirSpec -> {
						// https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/523
						// For now copy the entire sourceDir over so that include files are
						// available in the intermediateWorkDir
						// resourcesSrcDirSpec.include("images/**");
					});
				});
			});
		});
	}

	private void setAsciidoctorJVersion(Project project) {
		project.getExtensions().getByType(AsciidoctorJExtension.class).setVersion(ASCIIDOCTORJ_VERSION);
	}

	private void makeAllWarningsFatal(Project project) {
		project.getExtensions().getByType(AsciidoctorJExtension.class).fatalWarnings(".*");
	}

	private void createAsciidoctorExtensionsConfiguration(Project project) {

		project.getConfigurations().create("asciidoctorExtensions", configuration -> {

			project.getConfigurations()
				.matching(it -> "dependencyManagement".equals(it.getName()))
				.all(configuration::extendsFrom);

			configuration.getDependencies()
				.add(project.getDependencies().create(SPRING_ASCIIDOCTOR_BACKENDS_DEPENDENCY));

			// TODO: Why is the asiidoctorj-pdf dependency needed?
			configuration.getDependencies()
				.add(project.getDependencies().create("org.asciidoctor:asciidoctorj-pdf:1.5.3"));
		});
	}

	/**
	 * Requests the base Spring Documentation Resources from {@literal Maven Central} and uses it to format
	 * and render documentation.
	 *
	 * @param project {@literal this} Gradle {@link Project}.
	 * @return a {@link Sync} task that copies Spring Documentation Resources to the build directory
	 * used to generate documentation.
	 * @see org.gradle.api.tasks.Sync
	 * @see org.gradle.api.Project
	 */
	@SuppressWarnings("all")
	private Sync createUnzipDocumentationResourcesTask(Project project) {

		Configuration documentationResources = project.getConfigurations().create("documentationResources");

		documentationResources.getDependencies()
			.add(project.getDependencies().create(SPRING_ASCIIDOCTOR_BACKENDS_DEPENDENCY));

		Sync unzipResources = project.getTasks().create("unzipDocumentationResources", Sync.class, sync -> {

			sync.dependsOn(documentationResources);

			Callable<List<FileTree>> source = () -> {
				List<FileTree> result = new ArrayList<>();
				documentationResources.getAsFileTree().forEach(file -> result.add(project.zipTree(file)));
				return result;
			};

			sync.from(source);

			File destination = new File(project.getBuildDir(), "docs/resources");

			sync.into(project.relativePath(destination));

		});

		return unzipResources;
	}

	private void configureAsciidoctorTask(Project project, AbstractAsciidoctorTask asciidoctorTask) {

		asciidoctorTask.baseDirFollowsSourceDir();
		asciidoctorTask.configurations("asciidoctorExtensions");
		//asciidoctorTask.useIntermediateWorkDir();

		configureAttributes(project, asciidoctorTask);
		configureForkOptions(asciidoctorTask);
		configureOptions(asciidoctorTask);

		if (asciidoctorTask instanceof AsciidoctorTask) {
			boolean pdf = asciidoctorTask.getName().toLowerCase().contains("pdf");
			String backend = pdf ? "spring-pdf" : "spring-html";
			((AsciidoctorTask) asciidoctorTask).outputOptions((outputOptions) -> outputOptions.backends(backend));
			configureHtmlOnlyAttributes(asciidoctorTask);
		}
	}

	private void configureAttributes(Project project, AbstractAsciidoctorTask asciidoctorTask) {

		Map<String, Object> attributes = new HashMap<>();

		attributes.put("attribute-missing", "warn");
		attributes.put("docinfo", "shared");
		attributes.put("idprefix", "");
		attributes.put("idseparator", "-");
		attributes.put("sectanchors", "");
		attributes.put("sectnums", "");
		attributes.put("today-year", LocalDate.now().getYear());

		Object version = project.getVersion();

		if (version != null && !Project.DEFAULT_VERSION.equals(version)) {
			attributes.put("revnumber", version);
		}

		asciidoctorTask.attributes(attributes);
	}

	private void configureForkOptions(AbstractAsciidoctorTask asciidoctorTask) {
		if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_16)) {
			asciidoctorTask.forkOptions(options -> options.jvmArgs(
				"--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
				"--add-opens", "java.base/java.io=ALL-UNNAMED")
			);
		}
	}

	private void configureHtmlOnlyAttributes(AbstractAsciidoctorTask asciidoctorTask) {

		Map<String, Object> attributes = new HashMap<>();

		attributes.put("highlightjsdir", "js/highlight");
		attributes.put("highlightjs-theme", "github");
		attributes.put("source-highlighter", "highlight.js");
		attributes.put("icons", "font");
		attributes.put("imagesdir", "./images");
		attributes.put("linkcss", true);
		attributes.put("stylesdir", "css/");
		//attributes.put("stylesheet", "spring.css");

		asciidoctorTask.attributes(attributes);
	}

	private void configureOptions(AbstractAsciidoctorTask asciidoctorTask) {
		asciidoctorTask.options(Collections.singletonMap("doctype", "book"));
	}
}
