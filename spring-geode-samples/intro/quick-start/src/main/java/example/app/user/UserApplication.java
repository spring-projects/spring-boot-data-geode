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
package example.app.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.geode.config.annotation.EnableClusterAware;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Example {@link SpringBootApplication} using Apache Geode to manage Users.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableClusterAware
@EnableEntityDefinedRegions(basePackageClasses = User.class)
public class UserApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

	@Bean
	@SuppressWarnings("unused")
	ApplicationRunner runner(UserRepository userRepository) {

		return args -> {

			long count = userRepository.count();

			assertThat(count).isZero();

			log.info("Number of Users [{}]", count);

			User jonDoe = new User("jonDoe");

			log.info("Created User [{}]", jonDoe);

			userRepository.save(jonDoe);

			log.info("Saved User [{}]", jonDoe);

			count = userRepository.count();

			assertThat(count).isOne();

			log.info("Number of Users [{}]", count);

			User jonDoeFoundById = userRepository.findById(jonDoe.getName()).orElse(null);

			assertThat(jonDoeFoundById).isEqualTo(jonDoe);

			log.info("Found User by ID (name) [{}]", jonDoeFoundById);
		};
	}
}

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Region("Users")
class User {

	@lombok.NonNull @Id
	private final String name;

}

//interface UserRepository extends CrudRepository<User, String> { }
interface UserRepository extends GemfireRepository<User, String> { }
