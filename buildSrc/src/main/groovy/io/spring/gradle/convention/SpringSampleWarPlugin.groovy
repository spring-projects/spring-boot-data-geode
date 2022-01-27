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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.testing.Test

/**
 * Spring Sample Gradle Plugin used to build Samples as a Java Web Application (WAR archive).
 *
 * @author Rob Winch
 * @author John Blum
 * @see org.gradle.api.Project
 * @see org.gradle.api.Task
 */
class SpringSampleWarPlugin extends SpringSamplePlugin {

    @Override
    void applyAdditionalPlugins(Project project) {

        super.applyAdditionalPlugins(project);

        PluginManager pluginManager = project.getPluginManager();

        pluginManager.apply("war");
        pluginManager.apply("org.gretty");

        project.gretty {
            servletContainer = 'tomcat10'
            contextPath = '/'
            fileLogEnabled = false
        }

        Task prepareAppServerForIntegrationTests = project.tasks.create('prepareAppServerForIntegrationTests') {
            group = 'Verification'
            description = 'Prepares the Web application server for Integration Testing'
            doFirst {
                project.gretty {
                    httpPort = getRandomPort()
                    httpsPort = getRandomPort()
                }
            }
        }

        project.tasks.matching { it.name == "appBeforeIntegrationTest" }.all { task ->
            task.dependsOn prepareAppServerForIntegrationTests
        }

        project.tasks.withType(Test).all { task ->
            if ("integrationTest".equals(task.name)) {
                applyForIntegrationTest(project, task)
            }
        }
    }

    def applyForIntegrationTest(Project project, Task integrationTest) {

        project.gretty.integrationTestTask = integrationTest.name

        integrationTest.doFirst {

            def gretty = project.gretty

            boolean isHttps = gretty.httpsEnabled

            Integer httpPort = integrationTest.systemProperties['gretty.httpPort']
            Integer httpsPort = integrationTest.systemProperties['gretty.httpsPort']

            int port = isHttps ? httpsPort : httpPort

            String host = gretty.host ?: 'localhost'
            String contextPath = gretty.contextPath
            String httpBaseUrl = "http://${host}:${httpPort}${contextPath}"
            String httpsBaseUrl = "https://${host}:${httpsPort}${contextPath}"
            String baseUrl = isHttps ? httpsBaseUrl : httpBaseUrl

            integrationTest.systemProperty 'app.port', port
            integrationTest.systemProperty 'app.httpPort', httpPort
            integrationTest.systemProperty 'app.httpsPort', httpsPort
            integrationTest.systemProperty 'app.baseURI', baseUrl
            integrationTest.systemProperty 'app.httpBaseURI', httpBaseUrl
            integrationTest.systemProperty 'app.httpsBaseURI', httpsBaseUrl
            integrationTest.systemProperty 'geb.build.baseUrl', baseUrl
            integrationTest.systemProperty 'geb.build.reportsDir', 'build/geb-reports'
        }
    }

    def getRandomPort() {
        ServerSocket serverSocket = new ServerSocket(0)
        int port = serverSocket.localPort
        serverSocket.close()
        return port
    }
}
