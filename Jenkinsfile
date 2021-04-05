pipeline {

	agent {
		label "geode"
	}

/*
	environment {
		JAVA_HOME = "${tool 'jdk8'}"
	}
 */

	options {
		buildDiscarder(logRotator(numToKeepStr: '10'))
		disableConcurrentBuilds()
	}

	triggers {
		cron('@daily')
	}

	stages {

		try {
			stage('Build') {
				options {
					timeout(time: 15, unit: "MINUTES")
				}
				steps {
					script {
						docker.withRegistry('', 'hub.docker.com-springbuildmaster') {
							docker.image('adoptopenjdk/openjdk8:latest').inside('-u root -v /var/run/docker.sock:/var/run/docker.sock -v /usr/bin/docker:/usr/bin/docker -v $HOME:/tmp/jenkins-home') {

								// Cleanup any prior build system resources
								try {
									sh "ci/cleanupGemFiles.sh"
									sh "ci/cleanupArtifacts.sh"
								}
								catch (ignore) { }

								// Run the SBDG project Gradle build using JDK 8 inside Docker
								try {
									sh "docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}"
									sh "ci/check.sh"
								}
								catch (e) {
									currentBuild.result = "FAILED: build"
									throw e
								}
								finally {
									junit '**/build/test-results/*/*.xml'
								}
							}
						}
					}
				}
			}

			stage ('Deploy') {
				parallel {
					stage ('Deploy Artifacts') {
						steps {
							script {
								withCredentials([file(credentialsId: 'spring-signing-secring.gpg', variable: 'SIGNING_KEYRING_FILE')]) {
									withCredentials([string(credentialsId: 'spring-gpg-passphrase', variable: 'SIGNING_PASSWORD')]) {
										withCredentials([usernamePassword(credentialsId: 'oss-token', passwordVariable: 'OSSRH_PASSWORD', usernameVariable: 'OSSRH_USERNAME')]) {
											withCredentials([usernamePassword(credentialsId: '02bd1690-b54f-4c9f-819d-a77cb7a9822c', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD')]) {
												withEnv(["JAVA_HOME=${tool 'jdk8'}"]) {
													try {
														sh "ci/deployArtifacts.sh"
													}
													catch (e) {
														currentBuild.result = "FAILED: deploy artifacts"
														throw e
													}
												}
											}
										}
									}
								}
							}
						}
					}
					stage ('Deploy Docs') {
						steps {
							script {
								withCredentials([file(credentialsId: 'docs.spring.io-jenkins_private_ssh_key', variable: 'DEPLOY_SSH_KEY')]) {
									withEnv(["JAVA_HOME=${tool 'jdk8'}"]) {
										try {
											sh "ci/deployDocs.sh"
										}
										catch (e) {
											currentBuild.result = "FAILED: deploy docs"
											throw e
										}
									}
								}
							}
						}
					}
				}
			}
		}
		finally {
			stage ('Notify') {
				steps {
					script {

						def BUILD_SUCCESS = hudson.model.Result.SUCCESS.toString()
						def buildStatus = currentBuild.result
						def buildNotSuccess = !BUILD_SUCCESS.equals(buildStatus)
						def previousBuildStatus = currentBuild.previousBuild?.result
						def previousBuildNotSuccess = !BUILD_SUCCESS.equals(previousBuildStatus)

						if (buildNotSuccess || previousBuildNotSuccess) {

							def RECIPIENTS = [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
							def subject = "${buildStatus}: Build ${env.JOB_NAME} ${env.BUILD_NUMBER} status is now ${buildStatus}"
							def details = "The build status changed to ${buildStatus}. For details see ${env.BUILD_URL}"

							emailext(subject: subject, body: details, recipientProviders: RECIPIENTS, to: "$GEODE_TEAM_EMAILS")
						}
					}
				}
			}
		}
	}
}
