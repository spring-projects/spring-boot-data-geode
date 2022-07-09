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
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.session.Session;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.SessionFactory;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.SessionFactoryInitializer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import example.app.crm.model.Customer;

/**
 * Base test configuration used to configure and bootstrap an Apache Cassandra database with a schema and data.
 *
 * @author John Blum
 * @see com.datastax.oss.driver.api.core.session.Session
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.cassandra.SessionFactory
 * @see org.springframework.data.cassandra.config.CqlSessionFactoryBean
 * @see org.springframework.data.cassandra.core.CassandraTemplate
 * @see org.springframework.data.cassandra.core.cql.CqlTemplate
 * @see org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator
 * @see org.springframework.data.cassandra.core.cql.session.init.SessionFactoryInitializer
 * @since 1.1.0
 */
@SuppressWarnings("unused")
public abstract class TestCassandraConfiguration {

	protected static final int CASSANDRA_DEFAULT_PORT = CqlSessionFactoryBean.DEFAULT_PORT;

	protected static final String CASSANDRA_DATA_CQL = "cassandra-data.cql";
	protected static final String CASSANDRA_SCHEMA_CQL = "cassandra-schema.cql";

	private static final String COMMENT_LINE_PREFIX = "--";

	protected static final String LOCAL_DATA_CENTER = "datacenter1";
	protected static final String KEYSPACE_NAME = "CustomerService";

	@Bean
	SessionFactoryInitializer sessionFactoryInitializer(SessionFactory sessionFactory) {

		SessionFactoryInitializer sessionFactoryInitializer = new SessionFactoryInitializer();

		KeyspacePopulator keyspacePopulator =
			// cqlSession -> loadCassandraCqlScripts().forEach(cqlSession::execute);
			cqlSession -> loadCassandraDataCqlScript().forEach(cqlSession::execute);

		sessionFactoryInitializer.setKeyspacePopulator(keyspacePopulator);
		sessionFactoryInitializer.setSessionFactory(sessionFactory);

		return sessionFactoryInitializer;
	}

	protected List<String> loadCassandraCqlScripts() {

		List<String> cassandraCqlStatements = new ArrayList<>();

		cassandraCqlStatements.addAll(loadCassandraSchemaCqlScript());
		cassandraCqlStatements.addAll(loadCassandraDataCqlScript());

		return cassandraCqlStatements;
	}

	protected List<String> loadCassandraDataCqlScript() {
		return readLines(new ClassPathResource(CASSANDRA_DATA_CQL));
	}

	protected List<String> loadCassandraSchemaCqlScript() {
		return readLines(new ClassPathResource(CASSANDRA_SCHEMA_CQL));
	}

	private @NonNull List<String> readLines(@NonNull Resource resource) {

		try (BufferedReader resourceReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			return resourceReader.lines()
				.filter(cqlPredicate())
				.collect(Collectors.toList());
		}
		catch (IOException cause) {
			throw newRuntimeException(cause, "Failed to read from Resource [%s]", resource);
		}
	}

	private @NonNull Predicate<String> cqlPredicate() {

		Predicate<String> cqlPredicate = StringUtils::hasText;

		cqlPredicate.and(this::isNotCommentLine);

		return cqlPredicate;
	}

	private boolean isCommentLine(@Nullable String line) {
		return String.valueOf(line).trim().startsWith(COMMENT_LINE_PREFIX);
	}

	private boolean isNotCommentLine(@Nullable String line) {
		return !isCommentLine(line);
	}

	@Bean
	BeanPostProcessor cassandraTemplatePostProcessor() {

		return new BeanPostProcessor() {

			@org.jetbrains.annotations.Nullable @Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof CassandraTemplate cassandraTemplate) {

					Consumer<CassandraTemplate> cassandraTemplateConsumer = noopCassandraTemplateConsumer()
						.andThen(entityObjectInsertingCassandraTemplateConsumer())
						.andThen(entityObjectAssertingCassandraTemplateConsumer());
						//.andThen(entityTableNameAssertingCassandraTemplateConsumer())
						//.andThen(keyspaceNameAssertingCassandraTemplateConsumer());

					cassandraTemplateConsumer.accept(cassandraTemplate);
				}

				return bean;
			}
		};
	}

	private Consumer<CassandraTemplate> entityCountAssertingCassandraTemplateConsumer() {
		return cassandraTemplate -> assertThat(cassandraTemplate.count(Customer.class)).isOne();
	}

	private Consumer<CassandraTemplate> entityObjectAssertingCassandraTemplateConsumer() {
		return cassandraTemplate -> {

			String cql = "SELECT id, name FROM Customers";

			RowMapper<Customer> customerRowMapper = (row, rowNumber) ->
				Customer.newCustomer(row.getLong("id"), row.getString("name"));

			Customer actualCustomer = cassandraTemplate.getCqlOperations().queryForObject(cql, customerRowMapper);
			Customer expectedCustomer = Customer.newCustomer(16L, "Pie Doe");

			assertThat(actualCustomer).isEqualTo(expectedCustomer);
		};
	}

	// TODO: Why does this work and the CQL data script not work!
	private Consumer<CassandraTemplate> entityObjectInsertingCassandraTemplateConsumer() {
		return cassandraTemplate -> cassandraTemplate.insert(Customer.newCustomer(16L, "Pie Doe"));
	}

	private Consumer<CassandraTemplate> entityTableNameAssertingCassandraTemplateConsumer() {

		return cassandraTemplate ->
			assertThat(cassandraTemplate.getTableName(Customer.class).toString()).endsWithIgnoringCase("Customers");
	}

	private Consumer<CassandraTemplate> keyspaceNameAssertingCassandraTemplateConsumer() {

		return cassandraTemplate -> {

			String resolvedKeyspaceName = Optional.of(cassandraTemplate)
				.map(CassandraTemplate::getCqlOperations)
				.filter(CqlTemplate.class::isInstance)
				.map(CqlTemplate.class::cast)
				.map(CqlTemplate::getSessionFactory)
				.map(SessionFactory::getSession)
				.flatMap(Session::getKeyspace)
				.map(CqlIdentifier::toString)
				.orElse(null);

			assertThat(resolvedKeyspaceName).isEqualToIgnoringCase(KEYSPACE_NAME);
		};
	}

	private Consumer<CassandraTemplate> noopCassandraTemplateConsumer() {
		return cassandraTemplate -> {};
	}
}
