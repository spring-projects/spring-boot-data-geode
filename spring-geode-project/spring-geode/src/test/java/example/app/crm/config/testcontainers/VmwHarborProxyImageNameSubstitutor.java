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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;

/**
 * Testcontainers {@link ImageNameSubstitutor} for {@literal VMware Harbor Proxy}.
 *
 * @author John Blum
 * @see org.testcontainers.utility.ImageNameSubstitutor
 * @see <a href="https://github.com/testcontainers/testcontainers-java/issues/4605">Image Name Substitution not applied for DockerComposeContainers</a>
 * @since 1.7.6
 */
@SuppressWarnings("unused")
public class VmwHarborProxyImageNameSubstitutor extends ImageNameSubstitutor {

	private static final String VMWARE_HARBOR_PROXY_URL = "harbor-repo.vmware.com";

	private static final String TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX =
		String.format("%s/dockerhub-proxy-cache/", VMWARE_HARBOR_PROXY_URL);

	private static final String TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE =
		TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.concat("%s");

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public DockerImageName apply(DockerImageName original) {

		String originalDockerImageName = original.asCanonicalNameString();
		String resolvedDockerImageName = originalDockerImageName;

		logInfo("Original Docker Image Name [%s]", originalDockerImageName);

		if (originalDockerImageName.contains(VMWARE_HARBOR_PROXY_URL)) {
			originalDockerImageName = originalDockerImageName.substring(TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX.length() - 1);
			resolvedDockerImageName = String.format(TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE, originalDockerImageName);
		}

		logInfo("Resolved Docker Image Name [{}]", resolvedDockerImageName);

		return DockerImageName.parse(resolvedDockerImageName);
	}

	@Override
	protected String getDescription() {
		return "VMware Harbor Proxy Image Name Substitutor";
	}

	protected Logger getLogger() {
		return this.logger;
	}

	protected void logInfo(String message, Object... arguments) {

		Logger logger = getLogger();

		if (logger.isInfoEnabled()) {
			logger.info(String.format(message, arguments), arguments);
		}
	}
}
