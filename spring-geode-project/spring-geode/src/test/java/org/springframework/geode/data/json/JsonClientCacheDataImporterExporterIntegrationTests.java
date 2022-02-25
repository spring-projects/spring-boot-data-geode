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
package org.springframework.geode.data.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.util.PropertiesBuilder;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.util.FileCopyUtils;

import example.app.crm.model.Customer;

/**
 * Integration Tests for {@link JsonCacheDataImporterExporter} using an Apache Geode {@link ClientCache}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.util.PropertiesBuilder
 * @see org.springframework.geode.data.json.JsonCacheDataImporterExporter
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class JsonClientCacheDataImporterExporterIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final AtomicBoolean applicationContextClosed = new AtomicBoolean(false);

	private static ConfigurableApplicationContext applicationContext = null;

	private static final String CUSTOMERS_JSON_RESOURCE_PATH = "data-customers.json";

	private static final StringWriter writer = new StringWriter();

	@AfterClass
	public static void assetClientCacheDataExportWorksAsExpected() throws IOException {

		assertThat(applicationContextClosed.get()).isTrue();

		String actualJson = trimJson(writer.toString());
		String expectedJson = trimJson(loadJson(CUSTOMERS_JSON_RESOURCE_PATH));

		assertThat(actualJson).isEqualTo(expectedJson);
	}

	private static String loadJson(String resourcePath) throws IOException {
		return FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource(resourcePath).getInputStream()));
	}

	private static String trimJson(String json) {

		StringBuilder buffer = new StringBuilder();

		for (char character : String.valueOf(json).toCharArray()) {
			if (!Character.isWhitespace(character)) {
				buffer.append(character);
			}
		}

		return buffer.toString();
	}

	private static void closeApplicationContext() {
		SpringApplication.exit(applicationContext);
	}

	private static ApplicationContext newApplicationContext() {

		Properties configuration = PropertiesBuilder.create()
			.setProperty("spring.boot.data.gemfire.cache.data.export.enabled", Boolean.TRUE.toString())
			.setProperty("spring.boot.data.gemfire.cache.data.import.active-profiles", "DEV")
			.build();

		ConfigurableApplicationContext applicationContext =
			new SpringApplicationBuilder(TestGeodeClientConfiguration.class)
				.profiles("DEV")
				.properties(configuration)
				.web(WebApplicationType.NONE)
				.build()
				.run();

		JsonClientCacheDataImporterExporterIntegrationTests.applicationContext = applicationContext;

		return applicationContext;
	}

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(TestGeodeServerConfiguration.class);
	}

	public void assertCustomersRegion(Region<?, ?> customers) {

		assertThat(customers).isNotNull();
		assertThat(customers.getName()).isEqualTo("Customers");
		assertThat(customers.getAttributes()).isNotNull();
		assertThat(customers.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
		assertThat(customers).hasSize(0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void clientCacheDataImportWorksAsExpected() {

		try {
			Region<Byte, Customer> customers =
				newApplicationContext().getBean("Customers", Region.class);

			assertCustomersRegion(customers);

			Set<Byte> keys = customers.keySetOnServer();

			assertThat(keys).hasSize(2);
			assertThat(keys).containsExactlyInAnyOrder((byte) 1, (byte) 2);

			Object jonDoeValue = customers.get((byte) 1);

			assertThat(jonDoeValue).isInstanceOf(PdxInstance.class);
			assertThat(((PdxInstance) jonDoeValue).getObject())
				.isEqualTo(Customer.newCustomer(1L, "Jon Doe"));

			Object janeDoeValue = customers.get((byte) 2);

			assertThat(janeDoeValue).isInstanceOf(PdxInstance.class);
			assertThat(((PdxInstance) janeDoeValue).getObject())
				.isEqualTo(Customer.newCustomer(2L, "Jane Doe"));
		}
		finally {
			closeApplicationContext();
		}
	}

	@ClientCacheApplication(name = "JsonClientCacheDataImporterExporterIntegrationTestsClient")
	static class TestGeodeClientConfiguration {

		@Bean("Customers")
		ClientRegionFactoryBean<Long, Customer> customersRegion(GemFireCache cache) {

			ClientRegionFactoryBean<Long, Customer> customersRegion = new ClientRegionFactoryBean<>();

			customersRegion.setCache(cache);
			customersRegion.setShortcut(ClientRegionShortcut.PROXY);

			return customersRegion;
		}

		@Bean
		JsonCacheDataImporterExporter cacheDataImporterExporter() {
			return new JsonCacheDataImporterExporter();
		}

		@Bean
		ResourceWriter testResourceWriter() {

			return (resource, data) -> {

				String json = new String(data);

				writer.write(json);
				writer.flush();
			};
		}

		@EventListener(classes = ContextClosedEvent.class)
		void applicationContextClosedListener(ContextClosedEvent event) {
			applicationContextClosed.set(true);
		}
	}

	@CacheServerApplication(name = "JsonClientCacheDataImporterExporterIntegrationTestsServer")
	static class TestGeodeServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(TestGeodeServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("Customers")
		LocalRegionFactoryBean<Long, Customer> customersRegion(GemFireCache cache) {

			LocalRegionFactoryBean<Long, Customer> customersRegion = new LocalRegionFactoryBean<>();

			customersRegion.setCache(cache);
			customersRegion.setPersistent(false);

			return customersRegion;
		}
	}
}
