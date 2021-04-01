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

		stage('Build') {
			options {
				timeout(time: 15, unit: "MINUTES")
			}
			steps {
				script {
					docker.withRegistry('', 'hub.docker.com-springbuildmaster') {
						docker.image('adoptopenjdk/openjdk8:latest').inside('-v $HOME:/tmp/jenkins-home') {

							try {
								sh 'rm -Rf `find . -name "BACKUPDEFAULT*"`'
								sh 'rm -Rf `find . -name "ConfigDiskDir*"`'
								sh 'rm -Rf `find . -name "locator*" | grep -v "src" | grep -v "locator-application"`'
								sh 'rm -Rf `find . -name "newDB"`'
								sh 'rm -Rf `find . -name "server" | grep -v "src"`'
								sh 'rm -Rf `find . -name "*.log"`'
							}
							catch (ignore) { }

							// Run the SBDG project Gradle build using JDK 8
							try {
								sh './gradlew --no-daemon --refresh-dependencies --stacktrace clean check'
							}
							catch (e) {
								currentBuild.result = "FAILED: build"
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
											try {
												sh './gradlew deployArtifacts finalizeDeployArtifacts --no-daemon --refresh-dependencies --stacktrace -Psigning.secretKeyRingFile=$SIGNING_KEYRING_FILE -Psigning.keyId=$SPRING_SIGNING_KEYID -Psigning.password=$SIGNING_PASSWORD -PossrhUsername=$OSSRH_USERNAME -PossrhPassword=$OSSRH_PASSWORD -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD'
											}
											catch (e) {
												currentBuild.result = "FAILED: deploy artifacts"
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
								try {
									sh './gradlew --no-daemon --refresh-dependencies --stacktrace -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME deployDocs'
								}
								catch (e) {
									currentBuild.result = "FAILED: deploy docs"
								}
							}
						}
					}
				}
			}
		}

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
