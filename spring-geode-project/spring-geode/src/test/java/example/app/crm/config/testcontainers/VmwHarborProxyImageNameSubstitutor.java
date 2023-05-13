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
package example.app.crm.config.testcontainers;

import org.springframework.lang.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;

/**
 * Testcontainers {@link ImageNameSubstitutor} for {@literal VMware Harbor Proxy}.
 *
 * @author John Blum
 * @see org.testcontainers.utility.DockerImageName
 * @see org.testcontainers.utility.ImageNameSubstitutor
 * @see <a href="https://github.com/testcontainers/testcontainers-java/issues/4605">Image Name Substitution not applied for DockerComposeContainers</a>
 * @since 1.7.6
 */
@SuppressWarnings("unused")
public class VmwHarborProxyImageNameSubstitutor extends ImageNameSubstitutor {

	private static final String CASSANDRA_KEYWORD = "cassandra";
	private static final String JENKINS_KEYWORD = "jenkins";
	private static final String VMWARE_HARBOR_PROXY_URL = "harbor-repo.vmware.com";

	private static final String TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX =
		VMWARE_HARBOR_PROXY_URL.concat("/dockerhub-proxy-cache/");

	private static final String TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.concat("%s");

	private static final String TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_PREFIX =
		TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.concat("springci/");

	private static final String TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_PREFIX.concat("%s");

	private static final String SPRING_JAVA_VERSION =
		System.getProperty("spring.java.version", "17.0.6_10-jdk-focal");

	private static final String SPRING_DATA_CASSANDRA_DOCKER_IMAGE_NAME =
		String.format("spring-data-with-cassandra-3.11:%s", SPRING_JAVA_VERSION);

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public @NonNull DockerImageName apply(@NonNull DockerImageName original) {

		String originalDockerImageName = original.asCanonicalNameString();
		String resolvedDockerImageName = resolveDockerImageName(originalDockerImageName);;

		logInfo("Original Docker Image Name [%s]", originalDockerImageName);
		logInfo("Resolved Docker Image Name [{}]", resolvedDockerImageName);

		DockerImageName dockerImageName = DockerImageName.parse(resolvedDockerImageName);

		return dockerImageName.asCanonicalNameString().contains(CASSANDRA_KEYWORD)
			? dockerImageName.asCompatibleSubstituteFor(CASSANDRA_KEYWORD)
			: dockerImageName;
	}

	private boolean isJenkinsEnvironment() {
		return Boolean.TRUE.equals(Boolean.getBoolean(JENKINS_KEYWORD));
	}

	private boolean isSpringDockerImageName(String dockerImageName) {
		return dockerImageName.contains("spring");
	}

	private boolean isVMwareHarborProxyAvailable(String dockerImageName) {
		return dockerImageName.contains(VMWARE_HARBOR_PROXY_URL) || isJenkinsEnvironment();
	}

	private String resolveDockerImageName(String originalDockerImageName) {

		logInfo("Is Jenkins Environment [{}]", isJenkinsEnvironment());

		String resolvedDockerImageName = originalDockerImageName;

		if (isVMwareHarborProxyAvailable(originalDockerImageName)) {
			logInfo("VMware Harbor Proxy detected [{}]", originalDockerImageName);
			originalDockerImageName = toUnqualifiedDockerImageName(originalDockerImageName);
			resolvedDockerImageName = doResolveDockerImageName(originalDockerImageName);
		}

		return resolvedDockerImageName;
	}

	private String doResolveDockerImageName(String originalDockerImageName) {

		return isSpringDockerImageName(originalDockerImageName)
			? String.format(TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_TEMPLATE, substituteCassandraDockerImageName(originalDockerImageName))
			: String.format(TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE, originalDockerImageName);
	}

	private String substituteCassandraDockerImageName(String originalDockerImageName) {

		return originalDockerImageName.contains("cassandra")
			? SPRING_DATA_CASSANDRA_DOCKER_IMAGE_NAME
			: originalDockerImageName;
	}

	private String toUnqualifiedDockerImageName(String dockerImageName) {
		return dockerImageName.substring(dockerImageName.lastIndexOf("/") + 1);
	}

	@Override
	protected String getDescription() {
		return "VMware Harbor Proxy Image Name Substitutor";
	}

	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	protected void logInfo(String message, Object... arguments) {

		Logger logger = getLogger();

		if (logger.isInfoEnabled()) {
			logger.info(String.format(message, arguments), arguments);
		}
	}
}
