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
package org.springframework.geode.boot.autoconfigure.cluster.aware;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.support.RestTemplateConfigurer;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.geode.security.TestSecurityManager;
import org.springframework.geode.util.GeodeConstants;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.books.model.Book;
import example.app.books.model.ISBN;

/**
 * Integration Tests testing the {@link EnableClusterAware} annotation configuration when the Apache Geode cluster
 * (server(s)) are secure (i.e. when both Authentication and TLS/SSL are enabled).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.geode.security.TestSecurityManager
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.4.1
 */
@ActiveProfiles({ "cluster-aware-with-secure-client", "ssl" })
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = SecureClusterAwareConfigurationIntegrationTests.TestGeodeClientConfiguration.class,
	properties = {
		"spring.data.gemfire.management.require-https=true",
		"spring.data.gemfire.security.username=test",
		"spring.data.gemfire.security.password=test"
	},
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class SecureClusterAwareConfigurationIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY =
		"spring.data.gemfire.cache.client.region.shortcut";

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(TestGeodeServerConfiguration.class,
			"-Dspring.profiles.active=cluster-aware-with-secure-server,ssl");
	}

	@BeforeClass @AfterClass
	public static void resetClusterAwareCondition() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Autowired
	@Qualifier("booksTemplate")
	private GemfireTemplate booksTemplate;

	@Before
	public void assertBooksClientRegionIsProxy() {

		assertThat(System.getProperties())
			.doesNotContainKeys(SPRING_DATA_GEMFIRE_CACHE_CLIENT_REGION_SHORTCUT_PROPERTY);

		assertThat(this.booksTemplate).isNotNull();
		assertThat(this.booksTemplate.getRegion()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getName()).isEqualTo("Books");
		assertThat(this.booksTemplate.getRegion().getAttributes()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@Test
	public void clientServerConfigurationAndConfigurationIsSuccessful() {

		Book book = Book.newBook("Book of Job")
			.identifiedBy(ISBN.autoGenerated());

		this.booksTemplate.put(book.getIsbn(), book);

		Book returnedBook = this.booksTemplate.get(book.getIsbn());

		assertThat(returnedBook).isNotNull();
		assertThat(returnedBook).isEqualTo(book);
		assertThat(returnedBook).isNotSameAs(book);
	}

	@SpringBootApplication
	@EnableClusterAware
	@EnableEntityDefinedRegions(basePackageClasses = Book.class)
	@Profile(("cluster-aware-with-secure-client"))
	static class TestGeodeClientConfiguration {

		static final String DEFAULT_TRUSTSTORE_PASSWORD = "unknown";
		static final String SSL_PROFILE = "ssl";
		static final String TEST_TRUSTED_KEYSTORE_FILENAME = "test-trusted.keystore";

		@Bean
		RestTemplateConfigurer secureClientHttpRequestConfigurer(Environment environment)  {

			return restTemplate -> {

				if (areProfilesActive(environment, SSL_PROFILE)) {
					try {

						char[] trustStorePassword =
							environment.getProperty("spring.data.gemfire.security.ssl.truststore.password",
								DEFAULT_TRUSTSTORE_PASSWORD).toCharArray();

						KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

						keyStore.load(new ClassPathResource(TEST_TRUSTED_KEYSTORE_FILENAME).getInputStream(),
							trustStorePassword);

						SSLContext sslContext = SSLContexts.custom()
							.loadTrustMaterial(keyStore, TrustAllStrategy.INSTANCE)
							.build();

						HttpClient httpClient = HttpClients.custom()
							.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
							.build();

						restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
					}
					catch (Exception cause) {
						throw new RuntimeException(cause);
					}
				}
			};
		}

		private boolean areProfilesActive(@NonNull Environment environment, @NonNull String... profiles) {
			return environment.acceptsProfiles(Profiles.of(profiles));
		}
	}

	@SpringBootApplication
	@CacheServerApplication(name = "SecureClusterAwareConfigurationIntegrationTestsServer")
	@EnableManager(start = true)
	@Profile("cluster-aware-with-secure-server")
	static class TestGeodeServerConfiguration {

		private static final String GEODE_HOME_PROPERTY = GeodeConstants.GEMFIRE_PROPERTY_PREFIX + "home";

		public static void main(String[] args) throws IOException {

			resolveAndConfigureGeodeHome();

			new SpringApplicationBuilder(TestGeodeServerConfiguration.class)
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}

		private static void resolveAndConfigureGeodeHome() throws IOException {

			ClassPathResource resource = new ClassPathResource("/geode-home");

			File resourceFile = resource.getFile();

			System.setProperty(GEODE_HOME_PROPERTY, resourceFile.getAbsolutePath());
		}

		@Bean
		TestSecurityManager securityManager() {
			return new TestSecurityManager();
		}

		@Bean
		ApplicationRunner peerCacheVerifier(GemFireCache cache) {

			return args -> {

				assertThat(cache).isNotNull();
				assertThat(GemfireUtils.isPeer(cache)).isTrue();
				assertThat(cache.getName()).isEqualTo("SecureClusterAwareConfigurationIntegrationTestsServer");

				List<String> regionNames = cache.rootRegions().stream()
					.map(Region::getName)
					.collect(Collectors.toList());

				assertThat(regionNames)
					.describedAs("Expected no Regions; but was [%s]", regionNames)
					.isEmpty();
			};
		}
	}
}
