/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.boot.data.geode.cq;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import javax.annotation.Resource;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.test.context.junit4.SpringRunner;

import example.geode.query.cq.event.TemperatureReading;
import example.geode.query.cq.event.TemperatureReadingsContinuousQueriesHandler;

/**
 * The AutoConfiguredContinuousQueryIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
	classes = AutoConfiguredContinuousQueryIntegrationTests.GemFireClientConfiguration.class)
@SuppressWarnings("unused")
public class AutoConfiguredContinuousQueryIntegrationTests extends ClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	private static ProcessWrapper gemfireServer;

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		int availablePort = findAvailablePort();

		gemfireServer = run(GemFireServerConfiguration.class,
			String.format("-D%s=%d", GEMFIRE_CACHE_SERVER_PORT_PROPERTY, availablePort));

		waitForServerToStart("localhost", availablePort);

		System.setProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY, String.valueOf(availablePort));
	}

	@AfterClass
	public static void stopGemFireServer() {
		System.clearProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY);
		stop(gemfireServer);
	}

	@Autowired
	private GemfireTemplate temperatureReadingsTemplate;

	@SuppressWarnings("all")
	@Resource(name = "TemperatureReadings")
	private Region<Long, TemperatureReading> temperatureReadings;

	@Autowired
	private TemperatureReadingsContinuousQueriesHandler temperatureReadingsHandler;

	@Before
	public void setup() {

		assertThat(this.temperatureReadingsTemplate.<Long, TemperatureReading>get(1L))
			.isEqualTo(TemperatureReading.of(99));

		assertThat(this.temperatureReadings.sizeOnServer()).isEqualTo(8);
	}

	@Test
	public void assertTemperatureReadingsAreCorrect() {

		assertThat(this.temperatureReadingsHandler.getTemperatureReadingCount()).isEqualTo(4);
		assertThat(this.temperatureReadingsHandler.getBoilingTemperatures()).contains(300, 242);
		assertThat(this.temperatureReadingsHandler.getFreezingTemperatures()).contains(16, -51);
	}

	@SpringBootApplication
	public static class GemFireClientConfiguration {

		@Bean
		ClientCacheConfigurer clientCachePoolPortConfigurer(
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":40404}") int port) {

			return (bean, clientCacheFactoryBean) -> clientCacheFactoryBean.setServers(
				Collections.singletonList(new ConnectionEndpoint("localhost", port)));
		}

		@Bean("TemperatureReadings")
		public ClientRegionFactoryBean<Long, TemperatureReading> temperatureReadingsRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Long, TemperatureReading> temperatureReadingsRegion =
				new ClientRegionFactoryBean<>();

			temperatureReadingsRegion.setCache(gemfireCache);
			temperatureReadingsRegion.setClose(false);
			temperatureReadingsRegion.setShortcut(ClientRegionShortcut.PROXY);

			return temperatureReadingsRegion;
		}

		@Bean
		@DependsOn("TemperatureReadings")
		GemfireTemplate temperatureReadingsTemplate(GemFireCache gemfireCache) {
			return new GemfireTemplate(gemfireCache.getRegion("/TemperatureReadings"));
		}

		@Bean
		@DependsOn("TemperatureReadings")
		TemperatureReadingsContinuousQueriesHandler temperatureReadingsHandler() {
			return new TemperatureReadingsContinuousQueriesHandler();
		}
	}

	@EnablePdx
	@CacheServerApplication(name = "AutoConfiguredContinuousQueryIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	public static class GemFireServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(GemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("TemperatureReadings")
		public PartitionedRegionFactoryBean<Long, TemperatureReading> temperatureReadingsRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<Long, TemperatureReading> temperatureReadingsRegion =
				new PartitionedRegionFactoryBean<>();

			temperatureReadingsRegion.setCache(gemfireCache);
			temperatureReadingsRegion.setCacheLoader(temperatureReadingsLoader());
			temperatureReadingsRegion.setClose(false);
			temperatureReadingsRegion.setPersistent(false);

			return temperatureReadingsRegion;
		}

		private CacheLoader<Long, TemperatureReading> temperatureReadingsLoader() {

			return new CacheLoader<Long, TemperatureReading>() {

				@Override
				public TemperatureReading load(LoaderHelper<Long, TemperatureReading> helper)
						throws CacheLoaderException {

					long key = helper.getKey();

					Region<Long, TemperatureReading> temperatureReadings = helper.getRegion();

					recordTemperature(temperatureReadings, ++key, 72);
					recordTemperature(temperatureReadings, ++key, 16);
					recordTemperature(temperatureReadings, ++key, 101);
					recordTemperature(temperatureReadings, ++key, 300);
					recordTemperature(temperatureReadings, ++key, -51);
					recordTemperature(temperatureReadings, ++key, 242);
					recordTemperature(temperatureReadings, ++key, 112);

					return TemperatureReading.of(99);
				}

				private void recordTemperature(Region<Long, TemperatureReading> temperatureReadings,
						long key, int temperature) {

					sleep(50);
					temperatureReadings.put(key, TemperatureReading.of(temperature));
				}

				@SuppressWarnings("all")
				private void sleep(long milliseconds) {

					try {
						Thread.sleep(milliseconds);
					}
					catch (InterruptedException ignore) {
					}
				}

				@Override
				public void close() { }

			};
		}
	}
}
