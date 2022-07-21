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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Arrays;

import com.datastax.oss.driver.api.core.CqlSession;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.lang.NonNull;

import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;

import example.app.crm.model.Customer;

/**
 * Spring {@link @Configuration} for Apache Cassandra using Testcontainers.
 *
 * @author John Blum
 * @see java.net.InetSocketAddress
 * @see com.datastax.oss.driver.api.core.CqlSession
 * @see org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.testcontainers.containers.CassandraContainer
 * @see org.testcontainers.containers.GenericContainer
 * @since 1.1.0
 */
@Configuration
@Profile("inline-caching-cassandra")
@EntityScan(basePackageClasses = Customer.class)
@SuppressWarnings("unused")
public class TestcontainersCassandraConfiguration extends TestCassandraConfiguration {

	private static final String CASSANDRA_DOCKER_IMAGE_NAME = "cassandra:latest";
	private static final String LOCAL_DATACENTER_NAME = "datacenter1";

	@Bean("CassandraContainer")
	GenericContainer<?> cassandraContainer(Environment environment) {

		GenericContainer<?> cassandraContainer = newEnvironmentOptimizedCassandraContainer();

		cassandraContainer.start();

		return withCassandraServer(cassandraContainer, environment);
	}

	private @NonNull GenericContainer<?> newCassandraContainer() {

		return new CassandraContainer<>(CASSANDRA_DOCKER_IMAGE_NAME)
			.withInitScript(CASSANDRA_SCHEMA_CQL)
			//.withInitScript(CASSANDRA_INIT_CQL)
			.withExposedPorts(CASSANDRA_DEFAULT_PORT)
			.withReuse(true);
	}

	// Information (feedback) received from Sergei Egorov.
	private @NonNull GenericContainer<?> newEnvironmentOptimizedCassandraContainer() {

		return newCassandraContainer()
			.withEnv("CASSANDRA_SNITCH", "GossipingPropertyFileSnitch")
			.withEnv("HEAP_NEWSIZE", "128M")
			.withEnv("MAX_HEAP_SIZE", "1024M")
			.withEnv("JVM_OPTS", "-Dcassandra.skip_wait_for_gossip_to_settle=0 -Dcassandra.initial_token=0");
	}

	private @NonNull CassandraTemplate newCassandraTemplate(@NonNull CqlSession session) {
		return new CassandraTemplate(session);
	}

	private @NonNull CqlSession newCqlSession(@NonNull GenericContainer<?> cassandraContainer) {

		return CqlSession.builder()
			.addContactPoint(resolveContactPoint(cassandraContainer))
			.withLocalDatacenter(LOCAL_DATACENTER_NAME)
			.build();
	}

	private @NonNull GenericContainer<?> withCassandraServer(@NonNull GenericContainer<?> cassandraContainer,
			@NonNull Environment environment) {

		if (Arrays.asList(environment.getActiveProfiles()).contains(DEBUGGING_PROFILE)) {
			cassandraContainer = initializeCassandraServer(cassandraContainer);
			cassandraContainer = assertCassandraServerSetup(cassandraContainer);
		}

		return cassandraContainer;
	}

	private @NonNull GenericContainer<?> initializeCassandraServer(@NonNull GenericContainer<?> cassandraContainer) {

		try (CqlSession session = newCqlSession(cassandraContainer)) {
			newKeyspacePopulator(newCassandraSchemaCqlScriptResource()).populate(session);
		}

		return cassandraContainer;
	}

	private @NonNull GenericContainer<?> assertCassandraServerSetup(@NonNull GenericContainer<?> cassandraContainer) {

		try (CqlSession session = newCqlSession(cassandraContainer)) {

			session.getMetadata().getKeyspace(KEYSPACE_NAME)
				.map(keyspaceMetadata -> {

					assertThat(keyspaceMetadata.getName().toString()).isEqualToIgnoringCase(KEYSPACE_NAME);

					keyspaceMetadata.getTable(TABLE_NAME)
						.map(tableMetadata -> {

							assertThat(tableMetadata.getName().toString()).isEqualTo(TABLE_NAME);
							assertThat(tableMetadata.getKeyspace().toString()).isEqualToIgnoringCase(KEYSPACE_NAME);
							//assertCustomersTableHasSizeOne(session);

							return tableMetadata;
						})
						.orElseThrow(() -> new IllegalStateException(String.format("Table [%s] not found", TABLE_NAME)));

					return keyspaceMetadata;
				})
				.orElseThrow(() -> new IllegalStateException(String.format("Keyspace [%s] not found", KEYSPACE_NAME)));
		}

		return cassandraContainer;
	}

	private void assertCustomersTableHasSizeOne(@NonNull CqlSession session) {

		CassandraTemplate template = newCassandraTemplate(session);

		assertThat(template.getCqlOperations().execute(String.format("USE %s;", KEYSPACE_NAME))).isTrue();
		assertThat(template.getCqlOperations().queryForObject("SELECT count(*) FROM \"Customers\"", Long.class)).isOne();
		//assertThat(template.count(Customer.class)).isOne(); // Table Customers not found; needs to use the Keyspace
	}

	@Bean
	CqlSessionBuilderCustomizer cqlSessionBuilderCustomizer(CassandraProperties properties,
			@Qualifier("CassandraContainer") GenericContainer<?> cassandraContainer) {

		return cqlSessionBuilder -> cqlSessionBuilder
			.addContactPoint(resolveContactPoint(cassandraContainer))
			.withLocalDatacenter(properties.getLocalDatacenter())
			.withKeyspace(properties.getKeyspaceName());
	}

	private @NonNull InetSocketAddress resolveContactPoint(@NonNull GenericContainer<?> cassandraContainer) {
		return new InetSocketAddress(cassandraContainer.getHost(),
			cassandraContainer.getMappedPort(CASSANDRA_DEFAULT_PORT));
	}
}
