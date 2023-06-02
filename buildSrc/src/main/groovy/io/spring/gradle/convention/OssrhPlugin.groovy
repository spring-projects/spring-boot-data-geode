package io.spring.gradle.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

public class OssrhPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		if(project.hasProperty('ossrhUsername')) {
			project.uploadArchives {
				repositories {
					mavenDeployer {
						repository(url: "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
							authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
						}

						snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
							authentication(userName: project.ossrhUsername, password: project.ossrhPassword)
						}
					}
				}
			}
		}
		if(project.hasProperty('ossrhTokenUsername')) {
			project.uploadArchives {
				repositories {
					mavenDeployer {
						repository(url: "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
							authentication(userName: project.ossrhTokenUsername, password: project.ossrhTokenPassword)
						}

						snapshotRepository(url: "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
							authentication(userName: project.ossrhTokenUsername, password: project.ossrhTokenPassword)
						}
					}
				}
			}
		}
	}
}
