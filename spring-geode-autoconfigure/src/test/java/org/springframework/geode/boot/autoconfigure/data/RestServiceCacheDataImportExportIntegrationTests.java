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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.core.io.ResourceReadException;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceWriteException;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.data.CacheDataImporterExporter;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.AbstractExportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.AbstractImportResourceResolver;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;
import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import example.app.golf.model.Golfer;

/**
 * Integration Tests testing custom {@link ResourceReader} and {@link ResourceWriter} to import/export data from/to
 * a web service in a cloud environment.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.boot.web.server.LocalServerPort
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.geode.core.io.ResourceReader
 * @see org.springframework.geode.core.io.ResourceWriter
 * @see org.springframework.geode.data.CacheDataImporterExporter
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.springframework.test.web.servlet.MockMvc
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @since 1.3.1
 */
@ActiveProfiles("NET-IMPORT-EXPORT")
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = {
		"spring.boot.data.gemfire.cache.data.export.enabled=true",
		"spring.boot.data.gemfire.cache.data.export.resource.location=/cache/#{#regionName}/data/export",
		"spring.boot.data.gemfire.cache.data.import.active-profiles=NET-IMPORT-EXPORT",
		"spring.boot.data.gemfire.cache.data.import.resource.location=/cache/#{#regionName}/data/import",
		"spring.session.store-type=NONE",
		//"spring.boot.data.gemfire.cache.data.import.phase=2147483647",
		//"spring.boot.data.gemfire.cache.data.export.resource.location=http://localhost:#{#env['local.server.port']}/cache/#{#regionName}/data/export",
		//"spring.boot.data.gemfire.cache.data.import.resource.location=http://localhost:#{#env['local.server.port']}/cache/#{#regionName}/data/import",
	},
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@SuppressWarnings("unused")
public class RestServiceCacheDataImportExportIntegrationTests extends IntegrationTestsSupport {

	private static final boolean DEBUG = false;

	//@LocalServerPort
	//private int httpServerPort;

	@Autowired
	private CacheDataImporterExporter importerExporter;

	@Autowired
	@Qualifier("golfersTemplate")
	private GemfireTemplate golfersTemplate;

	private static void assertGolfer(Golfer golfer, long id, String name, int handicap) {

		assertThat(golfer).isNotNull();
		assertThat(golfer.getId()).isEqualTo(id);
		assertThat(golfer.getName()).isEqualTo(name);
		assertThat(golfer.getHandicap()).isEqualTo(handicap);
	}

	private static Golfer findById(Iterable<Golfer> golfers, long id) {

		return StreamSupport.stream(CollectionUtils.nullSafeIterable(golfers).spliterator(), false)
			.filter(golfer -> golfer.getId().equals(id))
			.findFirst()
			.orElse(null);
	}

	private static void log(String message, Object... args) {

		if (DEBUG) {
			System.err.printf(message, args);
			System.err.flush();
		}
	}

	private Golfer findById(long id) {
		return this.golfersTemplate.get(id);
	}

	private Golfer save(Golfer golfer) {
		this.golfersTemplate.put(golfer.getId(), golfer);
		return golfer;
	}

	@Before
	public void setup() {

		//log("Local Server Port [%d]%n", this.httpServerPort);
		//log("environment['local.server.port'] = %s%n", this.environment.getProperty("local.server.port"));

		assertThat(this.golfersTemplate).isNotNull();
		assertThat(this.golfersTemplate.getRegion()).isNotNull();
		assertThat(this.golfersTemplate.getRegion().getName()).isEqualTo("Golfers");
		assertThat(this.importerExporter).isNotNull();

		//assertThat(this.httpServerPort).isNotZero();
		//assertThat(this.environment.containsProperty("local.server.port")).isTrue();
		//assertThat(this.environment.getProperty("local.server.port", Integer.class, -80))
		//	.isEqualTo(this.httpServerPort);
	}

	@After
	public void tearDown() {

		this.importerExporter.exportFrom(this.golfersTemplate.getRegion());

		assertThat(CacheDataRestService.exportCalled.get()).isTrue();
	}

	@Test
	public void golfersRegionContainsGolfers() {

		assertThat(this.golfersTemplate.getRegion()).hasSize(2);

		Golfer johnBlum = findById(1L);

		assertGolfer(johnBlum, 1L, "John Blum", 12);

		save(Golfer.newGolfer(johnBlum.getId(), "Jon Bloom").withHandicap(21));

		Golfer moeHaroon = findById(2L);

		assertGolfer(moeHaroon, 2L, "Moe Haroon", 10);

		save(Golfer.newGolfer(moeHaroon.getId(), "Moiz Harpoon").withHandicap(1));
	}

	@Profile("NET-IMPORT-EXPORT")
	@SpringBootApplication
	@EnableGemFireMockObjects
	@EnableEntityDefinedRegions(basePackageClasses = Golfer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
	static class TestGeodeConfiguration implements WebMvcConfigurer {

		@Override
		public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

			CollectionUtils.nullSafeList(converters).stream()
				.filter(AbstractJackson2HttpMessageConverter.class::isInstance)
				.map(AbstractJackson2HttpMessageConverter.class::cast)
				.map(AbstractJackson2HttpMessageConverter::getObjectMapper)
				.forEach(objectMapper -> objectMapper.addMixIn(Golfer.class, ObjectTypeMetadataMixin.class));
		}

		@Bean
		BeanPostProcessor mockGolfersRegionBeanPostProcessor() {

			return new BeanPostProcessor() {

				@Override
				public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) {

					if (bean instanceof Region && "Golfers".equals(beanName)) {

						Region<?, ?> golfers = (Region<?, ?>) bean;

						doAnswer(invocation -> Arrays.asList(golfers.get(1L), golfers.get(2L))).when(golfers).values();
					}

					return bean;
				}
			};
		}

		@Bean
		CacheDataRestService cacheDataRestService() {
			return new CacheDataRestService();
		}

		@Bean
		ImportResourceResolver restServiceImportResourceResolver() {
			return new RestServiceImportResourceResolver();
		}

		@Bean
		ResourceReader restServiceResourceReader(MockMvc mvc) {
			return new RestServiceResourceReader(mvc);
		}

		@Bean
		ResourceWriter restServiceResourceWriter(MockMvc mvc) {
			return new RestServiceResourceWriter(mvc);
		}
	}

	@RestController
	static class CacheDataRestService {

		static final AtomicBoolean exportCalled = new AtomicBoolean(false);

		/* /cache/#{#regionName}/data/import */
		@GetMapping("/cache/{regionName}/data/import")
		public List<Golfer> doOnImport(@PathVariable("regionName") String regionName) {

			assertThat(regionName).isEqualTo("golfers");

			return Arrays.asList(
				Golfer.newGolfer(1L, "John Blum").withHandicap(12),
				Golfer.newGolfer(2L, "Moe Haroon").withHandicap(10)
			);
		}

		@PostMapping("/cache/{regionName}/data/export")
		public void doOnExport(@PathVariable("regionName") String regionName, @RequestBody List<Golfer> golfers) {

			assertThat(regionName).isEqualTo("golfers");
			assertThat(golfers).hasSize(2);

			Golfer jonBloom = findById(golfers, 1L);

			assertGolfer(jonBloom, 1L, "Jon Bloom", 21);

			Golfer moizHarpoon = findById(golfers, 2L);

			assertGolfer(moizHarpoon, 2L, "Moiz Harpoon", 1);

			exportCalled.set(true);
		}
	}

	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = PdxInstanceWrapper.AT_TYPE_FIELD_NAME
	)
	@SuppressWarnings("all")
	interface ObjectTypeMetadataMixin { }

	static class RestServiceExportResourceResolver extends AbstractExportResourceResolver {

		@Override
		protected @NonNull String getResourcePath() {
			throw new UnsupportedOperationException("Not Implemented");
		}
	}

	static class RestServiceImportResourceResolver extends AbstractImportResourceResolver {

		@Override
		protected Resource postProcess(Resource resource) {

			Resource resourceSpy = spy(super.postProcess(resource));

			doReturn(true).when(resourceSpy).exists();
			doReturn(true).when(resourceSpy).isReadable();

			return resourceSpy;
		}

		@Override
		protected @NonNull String getResourcePath() {
			throw new UnsupportedOperationException("Not Implemented");
		}
	}

	static class RestServiceResourceReader implements ResourceReader {

		private final MockMvc mvc;

		RestServiceResourceReader(MockMvc mvc) {

			Assert.notNull(mvc, "MockMvc must not be null");

			this.mvc = mvc;
		}

		@Override
		public @NonNull byte[] read(@NonNull Resource resource) {

			try {

				//String json = this.mvc.perform(get(resource.getURI()).accept(MediaType.APPLICATION_JSON))
				String json = this.mvc.perform(get("/cache/{regionName}/data/import", "golfers")
					.accept(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getContentAsString();

				return json.getBytes();
			}
			catch (Exception cause) {
				throw new ResourceReadException(String.format("Failed to read from resource at location [%s]",
					resource.getDescription()), cause);
			}
		}
	}

	static class RestServiceResourceWriter implements ResourceWriter {

		private final MockMvc mvc;

		RestServiceResourceWriter(MockMvc mvc) {

			Assert.notNull(mvc, "MockMvc must not be null");

			this.mvc = mvc;
		}

		@Override
		public void write(@NonNull Resource resource, byte[] data) {

			try {

				String json = new String(data);

				//this.mvc.perform(post(resource.getURI()).content(data).contentType(MediaType.APPLICATION_JSON))
				this.mvc.perform(post("/cache/{regionName}/data/export", "golfers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(data))
					.andExpect(status().isOk());
			}
			catch (Exception cause) {
				throw new ResourceWriteException(String.format("Failed to write to resource at location [%s]",
					resource.getDescription()), cause);
			}
		}
	}
}
