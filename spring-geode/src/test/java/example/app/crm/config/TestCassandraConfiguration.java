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

import java.util.Optional;
import java.util.function.Consumer;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.session.Session;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.cassandra.SessionFactory;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.SessionFactoryInitializer;
import org.springframework.lang.NonNull;

import example.app.crm.model.Customer;
import example.app.crm.repo.CustomerRepository;

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

	private static final boolean CONTINUE_ON_ERROR = false;
	private static final boolean IGNORE_FAILED_DROPS = true;

	private static final Customer pieDoe = Customer.newCustomer(16L, "Pie Doe");

	private static final String CQL_SCRIPT_ENCODING = null;

	protected static final int CASSANDRA_DEFAULT_PORT = CqlSessionFactoryBean.DEFAULT_PORT;

	protected static final String CASSANDRA_DATA_CQL = "cassandra-data.cql";
	protected static final String CASSANDRA_INIT_CQL = "cassandra-init.cql";
	protected static final String CASSANDRA_SCHEMA_CQL = "cassandra-schema.cql";
	protected static final String DEBUGGING_PROFILE = "debugging";
	protected static final String KEYSPACE_NAME = "CustomerService";
	protected static final String TABLE_NAME = "Customers";

	protected @NonNull Resource newCassandraDataCqlScriptResource() {
		return new ClassPathResource(CASSANDRA_DATA_CQL);
	}

	protected @NonNull Resource newCassandraInitCqlScriptResource() {
		return new ClassPathResource(CASSANDRA_INIT_CQL);
	}

	protected @NonNull Resource newCassandraSchemaCqlScriptResource() {
		return new ClassPathResource(CASSANDRA_SCHEMA_CQL);
	}

	@Bean
	SessionFactoryInitializer sessionFactoryInitializer(SessionFactory sessionFactory) {

		SessionFactoryInitializer sessionFactoryInitializer = new SessionFactoryInitializer();

		KeyspacePopulator keyspacePopulator = newKeyspacePopulator(newCassandraDataCqlScriptResource());

		sessionFactoryInitializer.setKeyspacePopulator(keyspacePopulator);
		sessionFactoryInitializer.setSessionFactory(sessionFactory);

		return sessionFactoryInitializer;
	}

	protected @NonNull KeyspacePopulator newKeyspacePopulator(Resource... cqlScripts) {
		return new ResourceKeyspacePopulator(CONTINUE_ON_ERROR, IGNORE_FAILED_DROPS, CQL_SCRIPT_ENCODING, cqlScripts);
	}

	@Bean
	@Profile(DEBUGGING_PROFILE)
	BeanPostProcessor cassandraTemplatePostProcessor() {

		return new BeanPostProcessor() {

			@Override
			public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {

				if (bean instanceof CassandraTemplate) {

					CassandraTemplate cassandraTemplate = (CassandraTemplate) bean;

					Consumer<CassandraTemplate> cassandraTemplateConsumer = noopCassandraTemplateConsumer()
						.andThen(insertEntityObjectCassandraTemplateConsumer())
						.andThen(assertEntityCountCassandraTemplateConsumer())
						.andThen(assertEntityObjectCassandraTemplateConsumer())
						.andThen(assertKeyspaceNameCassandraTemplateConsumer())
						.andThen(assertTableNameCassandraTemplateConsumer());

					cassandraTemplateConsumer.accept(cassandraTemplate);
				}

				return bean;
			}
		};
	}

	private Consumer<CassandraTemplate> noopCassandraTemplateConsumer() {
		return cassandraTemplate -> {};
	}

	private Consumer<CassandraTemplate> assertEntityCountCassandraTemplateConsumer() {
		return cassandraTemplate -> assertThat(cassandraTemplate.count(Customer.class)).isOne();
	}

	private Consumer<CassandraTemplate> assertEntityObjectCassandraTemplateConsumer() {

		return cassandraTemplate -> {

			String cql = "SELECT id, name FROM \"Customers\"";

			RowMapper<Customer> customerRowMapper = (row, rowNumber) ->
				Customer.newCustomer(row.getLong("id"), row.getString("name"));

			Customer actualCustomer = cassandraTemplate.getCqlOperations().queryForObject(cql, customerRowMapper);

			assertThat(actualCustomer).isEqualTo(pieDoe);
			assertThat(cassandraTemplate.selectOneById(16L, Customer.class)).isEqualTo(pieDoe);
			assertThat(cassandraTemplate.query(Customer.class).stream().findFirst().orElse(null)).isEqualTo(pieDoe);
		};
	}

	private Consumer<CassandraTemplate> insertEntityObjectCassandraTemplateConsumer() {
		return cassandraTemplate -> cassandraTemplate.insert(pieDoe);
	}

	private Consumer<CassandraTemplate> assertKeyspaceNameCassandraTemplateConsumer() {

		return cassandraTemplate -> {

			String resolvedKeyspaceName = Optional.of(cassandraTemplate.getCqlOperations())
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

	private Consumer<CassandraTemplate> assertTableNameCassandraTemplateConsumer() {

		return cassandraTemplate -> {

			String entityTableName = cassandraTemplate.getTableName(Customer.class).toString();

			assertThat(entityTableName).endsWith(TABLE_NAME);

			Optional.of(cassandraTemplate.getCqlOperations())
				.filter(CqlTemplate.class::isInstance)
				.map(CqlTemplate.class::cast)
				.map(CqlTemplate::getSessionFactory)
				.map(SessionFactory::getSession)
				.map(Session::getMetadata)
				.flatMap(metadata -> metadata.getKeyspace(KEYSPACE_NAME))
				.map(keyspaceMetadata -> keyspaceMetadata.getTable(entityTableName))
				.orElseThrow(() -> new IllegalStateException(String.format("Table [%s] not found", entityTableName)));
		};
	}

	@Bean
	@Profile(DEBUGGING_PROFILE)
	ApplicationListener<ContextRefreshedEvent> populateCassandraDatabaseUsingRepository(
			CustomerRepository customerRepository) {

		return event -> customerRepository.save(pieDoe);
	}
}
