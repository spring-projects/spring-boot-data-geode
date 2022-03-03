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
package org.springframework.geode.boot.autoconfigure.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.golf.model.Golfer;

/**
 * Integration Tests for {@link DataImportExportAutoConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("IMPORT-LOCAL")
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = LocalClientCacheDataImportAutoConfigurationIntegrationTests.TestGeodeClientConfiguration.class,
	properties = {
		"spring.application.name=LocalClientCacheDataImportAutoConfigurationIntegrationTests",
		"spring.boot.data.gemfire.cache.data.import.active-profiles=IMPORT-LOCAL"
	}
)
public class LocalClientCacheDataImportAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	@SuppressWarnings("unused")
	private GemfireTemplate golfersTemplate;

	@Test
	public void golfersWereLoaded() {

		assertThat(this.golfersTemplate).isNotNull();
		assertThat(this.golfersTemplate.getRegion()).isNotNull();
		assertThat(this.golfersTemplate.getRegion().getName()).isEqualTo("Golfers");
		assertThat(this.golfersTemplate.getRegion()).hasSize(1);

		Object value = this.golfersTemplate.get(1L);

		assertThat(value).isInstanceOf(Golfer.class);

		Golfer golfer = (Golfer) value;

		assertThat(golfer).isNotNull();
		assertThat(golfer.getId()).isEqualTo(1L);
		assertThat(golfer.getName()).isEqualTo("John Blum");
		assertThat(golfer.getHandicap()).isEqualTo(9);
	}

	@Profile("IMPORT-LOCAL")
	@SpringBootApplication
	@EnableEntityDefinedRegions(basePackageClasses = Golfer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
	static class TestGeodeClientConfiguration { }

}
