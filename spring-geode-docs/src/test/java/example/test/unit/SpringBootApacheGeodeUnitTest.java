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
package example.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Unit Test using Spring Boot with Apache Geode.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.5.0
 */
@ActiveProfiles("spring-geode-docs-unit-test")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class SpringBootApacheGeodeUnitTest extends IntegrationTestsSupport {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void saveAndFindUserIsSuccessful() {

		User jonDoe = User.as("jonDoe");

		assertThat(this.userRepository.save(jonDoe)).isNotNull();

		User jonDoeFoundById = this.userRepository.findById(jonDoe.getName()).orElse(null);

		assertThat(jonDoeFoundById).isEqualTo(jonDoe);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	@EnableEntityDefinedRegions(basePackageClasses = User.class)
	@Profile("spring-geode-docs-unit-test")
	static class TestConfiguration { }

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
