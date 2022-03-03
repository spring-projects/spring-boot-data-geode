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
package example.app.caching.near.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import example.app.caching.near.client.model.Person;

/**
 * Spring Boot application demonstrating Spring's Cache Abstraction with Apache Geode as the caching provider
 * for {@literal Near Caching}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.Pool
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see example.app.caching.near.client.model.Person
 * @since 1.1.0
 */
// tag::class[]
@SpringBootApplication
public class BootGeodeNearCachingClientCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootGeodeNearCachingClientCacheApplication.class, args);
	}

	// tag::application-runner[]
	@Bean
	public ApplicationRunner runner(@Qualifier("YellowPages") Region<String, Person> yellowPages) {

		return args -> {

			assertThat(yellowPages).isNotNull();
			assertThat(yellowPages.getName()).isEqualTo("YellowPages");
			assertThat(yellowPages.getInterestListRegex()).containsAnyOf(".*");
			assertThat(yellowPages.getAttributes()).isNotNull();
			assertThat(yellowPages.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.NORMAL);
			assertThat(yellowPages.getAttributes().getPoolName()).isEqualTo("DEFAULT");

			Pool defaultPool = PoolManager.find("DEFAULT");

			assertThat(defaultPool).isNotNull();
			assertThat(defaultPool.getSubscriptionEnabled()).isTrue();

		};
	}
	// end::application-runner[]
}
// end::class[]
