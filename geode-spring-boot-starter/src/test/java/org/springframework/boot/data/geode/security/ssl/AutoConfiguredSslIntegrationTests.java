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

package org.springframework.boot.data.geode.security.ssl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.geode.core.util.ObjectUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.config.annotation.EnableSsl;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import example.geode.cache.EchoCacheLoader;

/**
 * Integration tests testing the auto-configuration of Apache Geode/Pivotal GemFire SSL.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSsl
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
	classes = AutoConfiguredSslIntegrationTests.GemFireClientConfiguration.class)
@SuppressWarnings("unused")
public class AutoConfiguredSslIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";
	private static final String TRUSTED_KEYSTORE_FILENAME = "trusted.keystore";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class, javaxNetSslKeyStorePropertyFromClassPath());
	}

	private static String javaxNetSslKeyStorePropertyFromClassPath() {

		return String.format("-Djavax.net.ssl.keyStore=%s", locateKeyStoreInClassPath()
			.filter(StringUtils::hasText)
			.orElseThrow(() -> newIllegalStateException("KeyStore file [%s] was not found",
				TRUSTED_KEYSTORE_FILENAME)));
	}

	private static String javaxNetSslKeyStorePropertyFromFileSystem() {

		return String.format("-Djavax.net.ssl.keyStore=%s", locateKeyStoreInFileSystem()
			.map(File::getAbsolutePath)
			.filter(StringUtils::hasText)
			.orElseThrow(() -> newIllegalStateException("KeyStore file [%s] was not found",
				TRUSTED_KEYSTORE_FILENAME)));
	}

	private static Optional<String> locateKeyStoreInClassPath() {
		return locateKeyStoreInClassPath(TRUSTED_KEYSTORE_FILENAME);
	}

	@SuppressWarnings("all")
	private static Optional<String> locateKeyStoreInClassPath(String keystoreName) {

		/*
		System.err.printf("KEYSTORE LOCATION [%s]%n", ObjectUtils.doOperationSafely(() ->
			new File(new ClassPathResource(keystoreName).getURL().toURI())).getAbsolutePath());
		*/

		return Optional.of(new ClassPathResource(keystoreName))
			.filter(Resource::exists)
			.map(it -> ObjectUtils.doOperationSafely(() -> it.getURL()))
			.map(url -> ObjectUtils.doOperationSafely(() -> url.toURI()))
			.map(uri -> ObjectUtils.doOperationSafely(() -> new File(uri)))
			.filter(File::isFile)
			.map(File::getAbsolutePath);
	}

	private static Optional<File> locateKeyStoreInFileSystem() {
		return locateKeyStoreInFileSystem(FileSystemUtils.WORKING_DIRECTORY);
	}

	private static Optional<File> locateKeyStoreInFileSystem(File directory) {
		return locateKeyStoreInFileSystem(directory, TRUSTED_KEYSTORE_FILENAME);
	}

	@SuppressWarnings("all")
	private static Optional<File> locateKeyStoreInFileSystem(File directory, String keystoreFilename) {

		Assert.isTrue(directory != null && directory.isDirectory(),
			String.format("[%s] is not a valid directory", directory));

		//System.err.printf("Searching [%s]...%n", directory);

		for (File file : nullSafeArray(directory.listFiles(), File.class)) {

			//System.err.printf("Testing [%s]...%n", file);

			if (file.isDirectory()) {

				Optional<File> theFile = locateKeyStoreInFileSystem(file, keystoreFilename);

				if (theFile.isPresent()) {
					return theFile;
				}
				else {
					continue;
				}
			}

			if (file.getName().equals(keystoreFilename)) {
				return Optional.of(file);
			}
		}

		return Optional.empty();
	}

	@javax.annotation.Resource(name = "Echo")
	private Region<String, String> echo;

	@Test
	public void clientServerCommunicationsSuccessful() {

		assertThat(this.echo).isNotNull();
		assertThat(this.echo.get("Hello")).isEqualTo("Hello");
		assertThat(this.echo.get("Test")).isEqualTo("Test");
		assertThat(this.echo.get("Good-Bye")).isEqualTo("Good-Bye");
	}

	@SpringBootApplication
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration {

		@Bean("Echo")
		public ClientRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setClose(false);
			echoRegion.setShortcut(ClientRegionShortcut.PROXY);

			return echoRegion;
		}
	}

	@EnableSsl
	@CacheServerApplication(name = "AutoConfiguredSslIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration {

		public static void main(String[] args) {
			SpringApplication.run(GemFireServerConfiguration.class, args);
		}

		@Bean
		PeerCacheConfigurer cacheServerSslConfigurer(
			@Value("${javax.net.ssl.keyStore:trusted.keystore}") String keystoreLocation) {

			return (beanName, bean) -> {
				bean.getProperties().setProperty("ssl-keystore", keystoreLocation);
				bean.getProperties().setProperty("ssl-truststore", keystoreLocation);
			};
		}

		@Bean("Echo")
		public PartitionedRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<String, String> echoRegion = new PartitionedRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setCacheLoader(EchoCacheLoader.INSTANCE);
			echoRegion.setClose(false);
			echoRegion.setPersistent(false);

			return echoRegion;
		}
	}
}
