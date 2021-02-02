/*
 * Copyright 2020 the original author or authors.
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
package example.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Test using Spring Boot with Apache Geode.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.5.0
 */
@ActiveProfiles("spring-geode-docs-integration-test-client")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = "spring.data.gemfire.management.use-http=false",
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@SuppressWarnings("unused")
public class SpringBootApacheGeodeIntegrationTest extends ForkingClientServerIntegrationTestsSupport {

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(TestGeodeServerConfiguration.class);
	}

	@Resource(name = "Users")
	private org.apache.geode.cache.Region<String, User> users;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void assertRegionConfiguration() {

		assertThat(this.users).isNotNull();
		assertThat(this.users.getName()).isEqualTo("Users");
		assertThat(this.users.getAttributes()).isNotNull();
		assertThat(this.users.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	@Test
	public void saveAndFindUserIsSuccessful() {

		User jonDoe = User.as("jonDoe");

		assertThat(this.userRepository.save(jonDoe)).isNotNull();

		User jonDoeFoundById = this.userRepository.findById(jonDoe.getName()).orElse(null);

		assertThat(jonDoeFoundById).isEqualTo(jonDoe);
		assertThat(jonDoeFoundById).isNotSameAs(jonDoe);
	}

	@SpringBootApplication
	@EnableClusterAware
	@EnableEntityDefinedRegions(basePackageClasses = User.class)
	@Profile("spring-geode-docs-integration-test-client")
	static class TestGeodeClientConfiguration { }

	@CacheServerApplication
	@Profile("spring-geode-docs-integration-test-server")
	static class TestGeodeServerConfiguration {

		public static void main(String[] args) {

			new SpringApplicationBuilder(TestGeodeServerConfiguration.class)
				.profiles("spring-geode-docs-integration-test-server")
				.web(WebApplicationType.NONE)
				.build()
				.run(args);
		}
	}
}

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "as")
@Region("Users")
class User {

	@Id
	@lombok.NonNull
	private String name;

}

interface UserRepository extends CrudRepository<User, String> { }
