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
package example.app.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.testcontainers.containers.GenericContainer;

/**
 * Spring {@link @Configuration} for Apache Cassandra using Testcontainers.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.testcontainers.containers.GenericContainer
 * @since 1.1.0
 */
@Configuration
@Profile("inline-caching-cassandra")
@SuppressWarnings("unused")
public class TestcontainersCassandraConfiguration extends TestCassandraConfiguration {

	private static final String CASSANDRA_DOCKER_IMAGE_NAME = "cassandra:latest";

	@Bean
	@SuppressWarnings("rawtypes")
	GenericContainer cassandraContainer() {

		GenericContainer cassandraContainer = newCustomCassandraContainer();

		cassandraContainer.start();

		return cassandraContainer;
	}

	@SuppressWarnings("rawtypes")
	private GenericContainer newCassandraContainer() {
		return new GenericContainer(CASSANDRA_DOCKER_IMAGE_NAME)
			.withExposedPorts(CASSANDRA_DEFAULT_PORT);
	}

	@SuppressWarnings("rawtypes")
	private GenericContainer newCustomCassandraContainer() {

		return newCassandraContainer()
			.withEnv("HEAP_NEWSIZE", "128M")
			.withEnv("MAX_HEAP_SIZE", "1024M")
			.withEnv("JVM_OPTS", "-Dcassandra.skip_wait_for_gossip_to_settle=0 -Dcassandra.initial_token=0")
			.withEnv("CASSANDRA_SNITCH", "GossipingPropertyFileSnitch");
	}

	@Override
	protected String getContactPoints() {
		return cassandraContainer().getContainerIpAddress();
	}

	@Override
	protected int getPort() {
		return cassandraContainer().getFirstMappedPort();
	}
}
