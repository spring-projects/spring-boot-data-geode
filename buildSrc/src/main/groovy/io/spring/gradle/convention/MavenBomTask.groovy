package io.spring.gradle.convention

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class MavenBomTask extends DefaultTask {

	@OutputFile
	File bomFile

	@Internal
	Set<Project> projects = []

	@Input
	Set<String> getProjectNames() {
		return projects*.name as Set
	}

	MavenBomTask() {

		this.group = "Generate"
		this.description = "Generates a Maven BOM (Bill of Materials)." +
			" See https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies"
		this.projects = project.subprojects
		this.bomFile = project.file("${->project.buildDir}/maven-bom/${->project.name}-${->project.version}.txt")
		this.outputs.upToDateWhen { false }
	}

	@TaskAction
	void configureBom() {

		bomFile.parentFile.mkdirs()
		bomFile.write("Maven BOM (Bill of Materials)" +
			" See https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Importing_Dependencies")

		// TODO: Shouldn't this be { archives project.mavenBom } according to:
		//  https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#getArtifacts--
		project.artifacts {
			// Workaround GRADLE-2406 by attaching text artifact
			archives(bomFile)
		}

		project.publishing {
			publications {
				mavenJava(MavenPublication) {
					pom {
						packaging = "pom"
						withXml {
							asNode().children().last() + {
								delegate.dependencyManagement {
									delegate.dependencies {
										projects.sort { dep -> "$dep.group:$dep.name" }.each { p ->
											delegate.dependency {
												delegate.groupId(p.group)
												delegate.artifactId(p.name)
												delegate.version(p.version)
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
