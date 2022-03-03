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
package org.springframework.geode.boot.autoconfigure.pdx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.pdx.PdxSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.PdxSerializationAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests for {@link PdxSerializationAutoConfiguration} asserting that user-defined PDX configuration
 * {@literal overrides} SBDG's PDX {@literal auto-configuration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.pdx.PdxSerializer
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.PdxSerializationAutoConfiguration
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("MANUALLY_CONFIGURED_PDX_SERIALIZER")
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = { "spring.application.name=ManuallyConfiguredPdxSerializerWithPdxSerializationAutoConfigurationIntegrationTests" },
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class ManuallyConfiguredPdxSerializerWithPdxSerializationAutoConfigurationIntegrationTests
		extends IntegrationTestsSupport {

	@Autowired
	private GemFireCache cache;

	@Autowired
	@Qualifier("mockPdxSerializer")
	private PdxSerializer mockPdxSerializer;

	@Test
	public void cacheIsManuallyConfiguredWithPdxSerializer() {

		assertThat(this.cache).isNotNull();
		assertThat(this.cache.getName())
			.isEqualTo(ManuallyConfiguredPdxSerializerWithPdxSerializationAutoConfigurationIntegrationTests.class.getSimpleName());
		assertThat(this.cache.getPdxSerializer()).isEqualTo(this.mockPdxSerializer);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	@EnablePdx(serializerBeanName = "mockPdxSerializer")
	@Profile("MANUALLY_CONFIGURED_PDX_SERIALIZER")
	static class TestGeodeConfiguration {

		@Bean
		PdxSerializer mockPdxSerializer() {
			return mock(PdxSerializer.class, "MockPdxSerializer");
		}
	}
}
