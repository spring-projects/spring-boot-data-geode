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
package org.springframework.geode.core.io.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.springframework.core.io.ResourceLoader;

/**
 * Unit Tests for {@link ResourcePrefix}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.3.1
 */
public class ResourcePrefixUnitTests {

	@Test
	public void fromReturnsResourcePrefix() {

		assertThat(ResourcePrefix.from(ResourceLoader.CLASSPATH_URL_PREFIX))
			.isEqualTo(ResourcePrefix.CLASSPATH_URL_PREFIX);

		for (ResourcePrefix prefix : ResourcePrefix.values()) {
			assertThat(ResourcePrefix.from(prefix.toString())).isEqualTo(prefix);
		}
	}

	@Test
	public void fromInvalidPrefixReturnsNull() {

		assertThat(ResourcePrefix.from("ftp:")).isNull();
		assertThat(ResourcePrefix.from("scp:")).isNull();
		assertThat(ResourcePrefix.from("smtp:")).isNull();
		assertThat(ResourcePrefix.from("  ")).isNull();
		assertThat(ResourcePrefix.from("")).isNull();
		assertThat(ResourcePrefix.from(null)).isNull();
	}

	@Test
	public void getProtocolIsCorrect() {

		assertThat(ResourcePrefix.CLASSPATH_URL_PREFIX.getProtocol()).isEqualTo("classpath");
		assertThat(ResourcePrefix.FILESYSTEM_URL_PREFIX.getProtocol()).isEqualTo("file");
		assertThat(ResourcePrefix.HTTP_URL_PREFIX.getProtocol()).isEqualTo("http");
	}

	@Test
	public void toUrlPrefix() {

		assertThat(ResourcePrefix.CLASSPATH_URL_PREFIX.toUrlPrefix()).isEqualTo("classpath:");
		assertThat(ResourcePrefix.FILESYSTEM_URL_PREFIX.toUrlPrefix()).isEqualTo("file://");
		assertThat(ResourcePrefix.HTTP_URL_PREFIX.toUrlPrefix()).isEqualTo("http://");
	}
}
