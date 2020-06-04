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
package org.springframework.geode.docs.example.app.docker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.geode.cache.client.ClientCache;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.mapping.MappingPdxSerializer;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.geode.config.annotation.UseMemberName;
import org.springframework.geode.docs.example.app.docker.model.Customer;
import org.springframework.geode.docs.example.app.docker.repo.CustomerRepository;

/**
 * A Spring Boot, Apache Geode {@link ClientCache} application used to connect to an Apache Geode cluster running in a
 * Docker Container.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @see org.springframework.geode.docs.example.app.docker.model.Customer
 * @see org.springframework.geode.docs.example.app.docker.repo.CustomerRepository
 * @since 1.3.0
 */
// tag::class[]
@SpringBootApplication
@EnableClusterAware
@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
@UseMemberName("SpringBootApacheGeodeDockerClientCacheApplication")
public class SpringBootApacheGeodeDockerClientCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApacheGeodeDockerClientCacheApplication.class, args);
	}

	@Bean
	@SuppressWarnings("unused")
	ApplicationRunner runner(ClientCache clientCache, CustomerRepository customerRepository) {

		return args -> {

			assertClientCacheAndConfigureMappingPdxSerializer(clientCache);
			assertThat(customerRepository.count()).isEqualTo(0);

			Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

			log("Saving Customer [%s]...%n", jonDoe);

			jonDoe = customerRepository.save(jonDoe);

			assertThat(jonDoe).isNotNull();
			assertThat(jonDoe.getId()).isEqualTo(1L);
			assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
			assertThat(customerRepository.count()).isEqualTo(1);

			log("Querying for Customer [SELECT * FROM /Customers WHERE name LIKE '%s']...%n", "%Doe");

			Customer queriedJonDoe = customerRepository.findByNameLike("%Doe");

			assertThat(queriedJonDoe).isEqualTo(jonDoe);

			log("Customer was [%s]%n", queriedJonDoe);
		};
	}

	private void assertClientCacheAndConfigureMappingPdxSerializer(ClientCache clientCache) {

		assertThat(clientCache).isNotNull();
		assertThat(clientCache.getName())
			.isEqualTo(SpringBootApacheGeodeDockerClientCacheApplication.class.getSimpleName());
		assertThat(clientCache.getPdxSerializer()).isInstanceOf(MappingPdxSerializer.class);

		MappingPdxSerializer serializer = (MappingPdxSerializer) clientCache.getPdxSerializer();

		serializer.setIncludeTypeFilters(type -> Optional.ofNullable(type)
			.map(Class::getPackage)
			.map(Package::getName)
			.filter(packageName -> packageName.startsWith(this.getClass().getPackage().getName()))
			.isPresent());
	}

	private void log(String message, Object... args) {
		System.err.printf(message, args);
		System.err.flush();
	}
}
// end::class[]
