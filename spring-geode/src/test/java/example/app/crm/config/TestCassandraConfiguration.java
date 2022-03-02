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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.gemfire.tests.util.IOUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Base test configuration used to configure and bootstrap an Apache Cassandra database with a schema and data.
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.cassandra.config.AbstractCassandraConfiguration
 * @see org.springframework.data.cassandra.config.CqlSessionFactoryBean
 * @since 1.1.0
 */
public abstract class TestCassandraConfiguration extends AbstractCassandraConfiguration {

	protected static final int CASSANDRA_DEFAULT_PORT = CqlSessionFactoryBean.DEFAULT_PORT;

	private static final String CASSANDRA_DATA_CQL = "cassandra-data.cql";
	private static final String CASSANDRA_SCHEMA_CQL = "cassandra-schema.cql";
	private static final String LOCAL_DATA_CENTER = "datacenter1";
	private static final String KEYSPACE_NAME = "CustomerService";
	private static final String SESSION_NAME = "CustomerServiceCluster";

	@NonNull
	@Override
	protected String getKeyspaceName() {
		return KEYSPACE_NAME;
	}

	@Override
	protected String getLocalDataCenter() {
		return LOCAL_DATA_CENTER;
	}

	@Nullable
	@Override
	protected String getSessionName() {
		return SESSION_NAME;
	}

	/*
	@Nullable @Override
	protected KeyspacePopulator keyspacePopulator() {
		return cqlSession -> loadCassandraCqlScripts().forEach(cqlSession::execute);
	}
	*/

	// TODO: Remove use of deprecation after Spring Data for Apache Cassandra issues are resolved!
	@Override
	protected List<String> getStartupScripts() {

		List<String> startupScripts = new ArrayList<>(super.getStartupScripts());

		startupScripts.addAll(readLines(new ClassPathResource(CASSANDRA_SCHEMA_CQL)));
		startupScripts.addAll(readLines(new ClassPathResource(CASSANDRA_DATA_CQL)));

		return startupScripts;
	}

	private @NonNull List<String> readLines(@NonNull Resource resource) {

		BufferedReader resourceReader = null;

		try {

			resourceReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

			return resourceReader.lines()
				.filter(StringUtils::hasText)
				.collect(Collectors.toList());
		}
		catch (IOException cause) {
			throw newRuntimeException(cause, "Failed to read from Resource [%s]", resource);
		}
		finally {
			IOUtils.close(resourceReader);
		}
	}
}
