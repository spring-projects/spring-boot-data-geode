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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

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
 * @see <a href="https://www.testcontainers.org/features/image_name_substitution/">Image name substitution</a>
 * @see <a href="https://github.com/testcontainers/testcontainers-java/issues/4605">Image Name Substitution not applied for DockerComposeContainers</a>
 * @since 1.7.6
 */
@SuppressWarnings("unused")
public class VmwHarborProxyImageNameSubstitutor extends ImageNameSubstitutor {

	static final String CASSANDRA_KEYWORD = "cassandra";
	static final String JENKINS_KEYWORD = "jenkins";
	static final String TEST_DOCKER_IMAGE = "test_docker_image";

	// VMware Harbor Proxy (DockerHub Proxy Cache) Configuration
	private static final String VMWARE_HARBOR_PROXY_URL = "harbor-repo.vmware.com";

	// Testcontainers Configuration
	private static final String TESTCONTAINERS_REGISTRY = VMWARE_HARBOR_PROXY_URL.concat("/");
	private static final String TESTCONTAINERS_REPOSITORY = "dockerhub-proxy-cache/";
	private static final String TESTCONTAINERS_NAMESPACE = "library/";

	private static final String TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX =
		TESTCONTAINERS_REGISTRY.concat(TESTCONTAINERS_REPOSITORY);

	protected static final String TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.concat("%s");

	private static final String TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_PREFIX =
		TESTCONTAINERS_REGISTRY.concat(TESTCONTAINERS_REPOSITORY).concat(TESTCONTAINERS_NAMESPACE);

	protected static final String TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_PREFIX.concat("%s");

	private static final String TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_PREFIX =
		TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.concat("springci/");

	protected static final String TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_PREFIX.concat("%s");

	// Spring Configuration
	protected static final String SPRING_JAVA_VERSION =
		System.getProperty("spring.java.version", "17.0.6_10-jdk-focal");

	protected static final String SPRING_DATA_CASSANDRA_DOCKER_IMAGE_NAME =
		String.format("spring-data-with-cassandra-3.11:%s", SPRING_JAVA_VERSION);

	// Docker Configuration
	protected static final String DOCKER_IMAGE_NAME_WITH_VERSION_TEMPLATE = "%1$s:%2$s";

	private static final Map<String, String> springManagedDockerImages = new ConcurrentHashMap<>();
	private static final Map<String, String> vmwHarborProxyOfficialDockerImages = new ConcurrentHashMap<>();

	static {
		springManagedDockerImages.put(CASSANDRA_KEYWORD, SPRING_DATA_CASSANDRA_DOCKER_IMAGE_NAME);
		vmwHarborProxyOfficialDockerImages.put(CASSANDRA_KEYWORD, TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_TEMPLATE);
		vmwHarborProxyOfficialDockerImages.put(TEST_DOCKER_IMAGE, TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_TEMPLATE);
	}

	private static final boolean IS_JENKINS_ENVIRONMENT = Boolean.getBoolean(JENKINS_KEYWORD);

	private static final boolean USE_SPRING_MANAGED_DOCKER_IMAGES =
		Boolean.getBoolean("use-spring-managed-docker-images");

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public @NonNull DockerImageName apply(@NonNull DockerImageName originalDockerImageName) {

		DockerImageName resolvedDockerImageName = resolveDockerImageName(originalDockerImageName);

		logInfo("Original Docker Image Name [{}]", () -> asArray(originalDockerImageName.asCanonicalNameString()));
		logInfo("Resolved Docker Image Name [{}]", () -> asArray(resolvedDockerImageName.asCanonicalNameString()));

		return resolveCompatibleSubstituteFor(resolvedDockerImageName);
	}

	protected boolean isJenkinsEnvironment() {
		return IS_JENKINS_ENVIRONMENT;
	}

	protected boolean isSpringManagedDockerImage(DockerImageName dockerImageName) {

		return isUseSpringManagedDockerImages()
			&& (isJenkinsEnvironment() && springManagedDockerImages.containsKey(dockerImageName.getUnversionedPart()));
	}

	protected boolean isUseSpringManagedDockerImages() {
		return USE_SPRING_MANAGED_DOCKER_IMAGES;
	}

	protected boolean isVMwareHarborProxyAvailable() {
		return isJenkinsEnvironment();
	}

	protected boolean isVMwareHarborProxyManagedDockerImage(DockerImageName dockerImageName) {
		return isVMwareHarborProxyAvailable() || dockerImageName.getRegistry().contains(VMWARE_HARBOR_PROXY_URL);
	}

	protected boolean isVmwareHarborProxyOfficialDockerImage(DockerImageName dockerImageName) {

		return isVMwareHarborProxyAvailable() && vmwHarborProxyOfficialDockerImages
			.containsKey(toUnqualifiedDockerImageName(dockerImageName).getUnversionedPart());
	}

	protected DockerImageName resolveCompatibleSubstituteFor(DockerImageName dockerImageName) {

		return isSpringManagedDockerImage(dockerImageName)
			? dockerImageName.asCompatibleSubstituteFor(CASSANDRA_KEYWORD)
			: dockerImageName;
	}

	protected DockerImageName resolveDockerImageName(DockerImageName originalDockerImageName) {

		logInfo("Is Jenkins Environment [{}]", () -> asArray(isJenkinsEnvironment()));

		DockerImageName resolvedDockerImageName = originalDockerImageName;

		if (isVMwareHarborProxyManagedDockerImage(originalDockerImageName)) {
			logInfo("VMware Harbor Proxy detected [{}]", originalDockerImageName);
			originalDockerImageName = toUnqualifiedDockerImageName(originalDockerImageName);
			resolvedDockerImageName = doResolveDockerImageName(originalDockerImageName);
		}

		return resolvedDockerImageName;
	}

	DockerImageName doResolveDockerImageName(DockerImageName originalDockerImageName) {

		return Optional.ofNullable(originalDockerImageName)
			.filter(dockerImageName -> isUseSpringManagedDockerImages())
			.filter(dockerImageName -> springManagedDockerImages.containsKey(dockerImageName.getUnversionedPart()))
			.map(dockerImageName -> springManagedDockerImages.get(dockerImageName.getUnversionedPart()))
			.map(springManagedDockerImageName -> String.format(TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_TEMPLATE, springManagedDockerImageName))
			.map(DockerImageName::parse)
			.orElseGet(() -> {

				DockerImageName unqualifiedDockerImageName = toUnqualifiedDockerImageName(originalDockerImageName);

				return isVmwareHarborProxyOfficialDockerImage(unqualifiedDockerImageName)
					? DockerImageName.parse(String.format(TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_TEMPLATE, originalDockerImageName))
					: DockerImageName.parse(String.format(TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE, originalDockerImageName));
			});
	}

	DockerImageName toUnqualifiedDockerImageName(DockerImageName dockerImageName) {

		String name = dockerImageName.getUnversionedPart();
		String registry = dockerImageName.getRegistry();
		String repository = dockerImageName.getRepository();
		String version = dockerImageName.getVersionPart();

		/*
		return DockerImageName.parse(dockerImageName.asCanonicalNameString()
			.replace(TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX, ""));
		*/

		return DockerImageName.parse(String.format(DOCKER_IMAGE_NAME_WITH_VERSION_TEMPLATE,
			name.replace(TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX, ""),
			version));
	}

	@Override
	protected String getDescription() {
		return "VMware Harbor Proxy Image Name Substitutor";
	}

	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	protected void logInfo(String message, Object... arguments) {
		logInfo(message, () -> arguments);
	}

	protected void logInfo(String message, Supplier<Object[]> argumentsSupplier) {

		Logger logger = getLogger();

		if (logger.isInfoEnabled()) {
			Object[] arguments = argumentsSupplier.get();
			logger.info(String.format(message, arguments), arguments);
		}
	}

	private Object[] asArray(Object... array) {
		return array;
	}
}
