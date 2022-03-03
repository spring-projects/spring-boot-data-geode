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
package org.springframework.geode.boot.autoconfigure.cluster.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.geode.security.TestSecurityManager;
import org.springframework.geode.util.GeodeConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.books.model.Book;
import example.app.books.model.ISBN;

/**
 * Integration Tests testing the SDG {@link EnableClusterConfiguration} annotation functionality when the Apache Geode
 * server is configured with Security (Authentication).
 *
 * @author John Blum
 * @see java.io.File
 * @see java.net.URI
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("cluster-configuration-with-auth-client")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = ClusterConfigurationWithAuthenticationIntegrationTests.GeodeClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE,
	properties = {
		"spring.data.gemfire.security.username=test",
		"spring.data.gemfire.security.password=test"
	}
)
@SuppressWarnings("unused")
public class ClusterConfigurationWithAuthenticationIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final AtomicBoolean REDIRECTING_CLIENT_HTTP_REQUEST_INTERCEPTOR_INVOKED =
		new AtomicBoolean(false);

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GeodeServerConfiguration.class,
			"-Dspring.profiles.active=cluster-configuration-with-auth-server");
	}

	@Autowired
	@Qualifier("booksTemplate")
	private GemfireTemplate booksTemplate;

	@Before
	public void setup() {

		assertThat(this.booksTemplate).isNotNull();
		assertThat(this.booksTemplate.getRegion()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getName()).isEqualTo("Books");
		assertThat(this.booksTemplate.getRegion().getAttributes()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@After
	public void tearDown() {
		assertThat(REDIRECTING_CLIENT_HTTP_REQUEST_INTERCEPTOR_INVOKED.get()).isFalse();
	}

	@Test
	public void clusterConfigurationAndRegionDataAccessOperationsAreSuccessful() {

		Book expectedSeriesOfUnfortunateEvents = Book
			.newBook("A Series of Unfortunate Events")
			.identifiedBy(ISBN.autoGenerated());

		this.booksTemplate.put(expectedSeriesOfUnfortunateEvents.getIsbn(), expectedSeriesOfUnfortunateEvents);

		Book actualSeriesOfUnfortunateEvents = this.booksTemplate.get(expectedSeriesOfUnfortunateEvents.getIsbn());

		assertThat(actualSeriesOfUnfortunateEvents).isNotNull();
		assertThat(actualSeriesOfUnfortunateEvents).isEqualTo(expectedSeriesOfUnfortunateEvents);
		assertThat(actualSeriesOfUnfortunateEvents).isNotSameAs(expectedSeriesOfUnfortunateEvents);
	}

	@SpringBootApplication
	@Profile("cluster-configuration-with-auth-client")
	@EnableClusterConfiguration(useHttp = true, requireHttps = false)
	@EnableEntityDefinedRegions(basePackageClasses = Book.class)
	static class GeodeClientConfiguration {

		// NOTE: This ClientHttpRequestInterceptor bean should no longer be picked up by SDG's Cluster Configuration
		// infrastructure as of SD Moore-SR1
		@Bean
		ClientHttpRequestInterceptor testRedirectingClientHttpRequestInterceptor() {

			return (request, body, execution) -> {

				REDIRECTING_CLIENT_HTTP_REQUEST_INTERCEPTOR_INVOKED.set(true);

				String urlPattern = "%1$s://%2$s:%3$d%4$s";

				URI originalUri = request.getURI();
				URI redirectedUri = URI.create(String.format(urlPattern, originalUri.getScheme(), "nonExistingHost",
					originalUri.getPort(), originalUri.getPath()));

				HttpMethod httpMethod = request.getMethod();

				httpMethod = httpMethod != null ? httpMethod : HttpMethod.GET;

				ClientHttpRequest newRequest =
					new SimpleClientHttpRequestFactory().createRequest(redirectedUri, httpMethod);

				return execution.execute(newRequest, body);
			};
		}
	}

	@SpringBootApplication
	@Profile("cluster-configuration-with-auth-server")
	@CacheServerApplication(name = "ClusterConfigurationWithAuthenticationIntegrationTests")
	@EnableManager(start = true)
	static class GeodeServerConfiguration {

		private static final String GEODE_HOME_PROPERTY = GeodeConstants.GEMFIRE_PROPERTY_PREFIX + "home";

		public static void main(String[] args) throws IOException {

			resolveAndConfigureGeodeHome();

			//System.err.printf("%s [%s]%n", GEODE_HOME_PROPERTY, System.getProperty(GEODE_HOME_PROPERTY));

			new SpringApplicationBuilder(GeodeServerConfiguration.class)
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
		org.apache.geode.security.SecurityManager testSecurityManager() {
			return new TestSecurityManager();
		}

		@Bean
		ApplicationRunner peerCacheVerifier(GemFireCache cache) {

			return args -> {

				assertThat(cache).isNotNull();
				assertThat(GemfireUtils.isPeer(cache)).isTrue();
				assertThat(cache.getName())
					.isEqualTo(ClusterConfigurationWithAuthenticationIntegrationTests.class.getSimpleName());

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
