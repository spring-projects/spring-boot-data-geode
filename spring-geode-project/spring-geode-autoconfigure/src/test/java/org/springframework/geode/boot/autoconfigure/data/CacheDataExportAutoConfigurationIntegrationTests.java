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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.geode.cache.Region;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.data.gemfire.tests.util.FileUtils;
import org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration;
import org.springframework.geode.config.annotation.EnableClusterAware;

import example.app.golf.model.Golfer;

/**
 * Integration Tests for {@link DataImportExportAutoConfiguration}, which specifically tests the export of
 * {@link Region} values (data) to JSON on Spring Boot application (JVM) shutdown.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration
 * @since 1.3.0
 */
public class CacheDataExportAutoConfigurationIntegrationTests extends ClientServerIntegrationTestsSupport {

	private static final File GEODE_WORKING_DIRECTORY =
		new File(String.format("cache-data-export-%d", System.currentTimeMillis()));

	private static ProcessWrapper process;

	private static final String DATA_GOLFERS_JSON = "data-golfers.json";

	@BeforeClass @AfterClass
	public static void resetClusterAwareCondition() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@BeforeClass
	public static void runGeodeProcess() throws IOException {

		System.setProperty(DIRECTORY_DELETE_ON_EXIT_PROPERTY, Boolean.FALSE.toString());

		process = run(GEODE_WORKING_DIRECTORY, TestGeodeConfiguration.class,
			"-Dspring.profiles.active=EXPORT", "-Dspring.boot.data.gemfire.cache.data.export.enabled=true");

		assertThat(process).isNotNull();

		waitOn(() -> !process.isRunning(), Duration.ofSeconds(20).toMillis(), Duration.ofSeconds(2).toMillis());
	}

	@AfterClass
	public static void cleanup() {
		System.clearProperty(DIRECTORY_DELETE_ON_EXIT_PROPERTY);
		FileSystemUtils.deleteRecursive(GEODE_WORKING_DIRECTORY);
		stop(process);
	}

	@Test
	public void exportedJsonIsCorrect() throws Exception {

		File dataGolferJson = new File(GEODE_WORKING_DIRECTORY, DATA_GOLFERS_JSON);

		assertThat(dataGolferJson).isFile();

		String actualJson = FileUtils.read(dataGolferJson);

		/*
		String expectedJson = "["
			+ "{\"@type\":\"example.app.golf.model.Golfer\",\"handicap\":9,\"id\":1,\"name\":\"John Blum\"},"
			+ "{\"@type\":\"example.app.golf.model.Golfer\",\"handicap\":10,\"id\":2,\"name\":\"Moe Haroon\"}"
			+ "]";

		assertThat(actualJson).isEqualTo(expectedJson);
		*/

		Set<Golfer> expectedGolfers = mapFromJsonToGolfers(actualJson);

		assertThat(expectedGolfers).isNotNull();
		assertThat(expectedGolfers).hasSize(2);
		assertContains(expectedGolfers, Golfer.newGolfer(1L, "John Blum").withHandicap(9));
		assertContains(expectedGolfers, Golfer.newGolfer(2L, "Moe Haroon").withHandicap(10));
	}

	private void assertContains(Iterable<Golfer> golfers, Golfer golfer) {

		assertThat(StreamSupport.stream(golfers.spliterator(), false)
			.anyMatch(it -> it.getId().equals(golfer.getId())
				&& it.getName().equals(golfer.getName())
				&& it.getHandicap().equals(golfer.getHandicap())))
			.isTrue();
	}

	private Set<Golfer> mapFromJsonToGolfers(String json) throws Exception {
		return new HashSet<>(newObjectMapper().readerForListOf(Golfer.class).readValue(json));
	}

	private ObjectMapper newObjectMapper() {

		return new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Profile("EXPORT")
	@SpringBootApplication
	@EnableClusterAware
	@EnableEntityDefinedRegions(basePackageClasses = Golfer.class)
	@SuppressWarnings("unused")
	static class TestGeodeConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(TestGeodeConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}

		private static void log(String message, Object... args) {
			System.out.printf(String.format("%s%n", message), args);
			System.out.flush();
		}

		@Bean
		ApplicationRunner runner(GemfireTemplate golfersTemplate) {

			return args -> {

				save(golfersTemplate, Golfer.newGolfer(1L, "John Blum").withHandicap(9));
				save(golfersTemplate, Golfer.newGolfer(2L, "Moe Haroon").withHandicap(10));

				log("FORE!");
			};
		}

		private Golfer save(GemfireTemplate golfersTemplate, Golfer golfer) {
			golfersTemplate.put(golfer.getId(), golfer);
			return golfer;
		}
	}
}
