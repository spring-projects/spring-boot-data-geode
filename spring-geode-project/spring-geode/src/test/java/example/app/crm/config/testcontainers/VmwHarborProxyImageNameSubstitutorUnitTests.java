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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;

import org.testcontainers.utility.DockerImageName;

/**
 * Unit Tests for {@link VmwHarborProxyImageNameSubstitutor}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see example.app.crm.config.testcontainers.VmwHarborProxyImageNameSubstitutor
 * @since 1.7.6
 */
public class VmwHarborProxyImageNameSubstitutorUnitTests {

	@Test
	public void doResolveDockerImageNameForSpringManagedDockerImage() {

		DockerImageName cassandraDockerImage = DockerImageName.parse("cassandra:3.11.14");

		VmwHarborProxyImageNameSubstitutor imageNameSubstitutor = spy(new VmwHarborProxyImageNameSubstitutor());

		doReturn(true).when(imageNameSubstitutor).isJenkinsEnvironment();
		doReturn(true).when(imageNameSubstitutor).isUseSpringManagedDockerImages();

		assertThat(imageNameSubstitutor.doResolveDockerImageName(cassandraDockerImage)
			.asCanonicalNameString())
			.isEqualTo(String.format(VmwHarborProxyImageNameSubstitutor.TESTCONTAINERS_SPRINGCI_HUB_IMAGE_NAME_TEMPLATE,
				VmwHarborProxyImageNameSubstitutor.SPRING_DATA_CASSANDRA_DOCKER_IMAGE_NAME));
	}

	@Test
	public void doResolveDockerImageNameForNonSpringManagedNonOfficialDockerImage() {

		DockerImageName cassandraDockerImage = DockerImageName.parse("testcontainers/ryuk:0.4.0");

		VmwHarborProxyImageNameSubstitutor imageNameSubstitutor = spy(new VmwHarborProxyImageNameSubstitutor());

		doReturn(true).when(imageNameSubstitutor).isJenkinsEnvironment();
		doReturn(true).when(imageNameSubstitutor).isUseSpringManagedDockerImages();

		assertThat(imageNameSubstitutor.doResolveDockerImageName(cassandraDockerImage)
			.asCanonicalNameString())
			.isEqualTo(String.format(VmwHarborProxyImageNameSubstitutor.TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE,
				"testcontainers/ryuk:0.4.0"));
	}

	@Test
	public void doResolveDockerImageNameForNonSpringManagedOfficialDockerImage() {

		DockerImageName cassandraDockerImage = DockerImageName.parse(VmwHarborProxyImageNameSubstitutor.TEST_DOCKER_IMAGE);

		VmwHarborProxyImageNameSubstitutor imageNameSubstitutor = spy(new VmwHarborProxyImageNameSubstitutor());

		doReturn(true).when(imageNameSubstitutor).isJenkinsEnvironment();
		doReturn(true).when(imageNameSubstitutor).isUseSpringManagedDockerImages();

		assertThat(imageNameSubstitutor.doResolveDockerImageName(cassandraDockerImage)
			.asCanonicalNameString())
			.isEqualTo(String.format(VmwHarborProxyImageNameSubstitutor.TESTCONTAINERS_OFFICIAL_HUB_IMAGE_NAME_TEMPLATE,
				VmwHarborProxyImageNameSubstitutor.TEST_DOCKER_IMAGE.concat(":latest")));
	}

	@Test
	public void toUnqualifiedDockerImageNameFromQualifiedName() {

		DockerImageName qualifiedDockerImageName = DockerImageName.parse(String.format(
			VmwHarborProxyImageNameSubstitutor.TESTCONTAINERS_HUB_IMAGE_NAME_TEMPLATE, "testcontainers/ryuk:0.4.0"));

		VmwHarborProxyImageNameSubstitutor imageNameSubstitutor = new VmwHarborProxyImageNameSubstitutor();

		assertThat(imageNameSubstitutor.toUnqualifiedDockerImageName(qualifiedDockerImageName)
			.asCanonicalNameString())
			.isEqualTo("testcontainers/ryuk:0.4.0");
	}

	@Test
	public void toUnqualifiedDockerImageNameFromUnqualifiedName() {

		DockerImageName unqualifiedDockerImageName = DockerImageName.parse("testcontainers/ryuk:0.4.0");

		VmwHarborProxyImageNameSubstitutor imageNameSubstitutor = new VmwHarborProxyImageNameSubstitutor();

		assertThat(imageNameSubstitutor.toUnqualifiedDockerImageName(unqualifiedDockerImageName)
			.asCanonicalNameString())
			.isEqualTo("testcontainers/ryuk:0.4.0");
	}
}
