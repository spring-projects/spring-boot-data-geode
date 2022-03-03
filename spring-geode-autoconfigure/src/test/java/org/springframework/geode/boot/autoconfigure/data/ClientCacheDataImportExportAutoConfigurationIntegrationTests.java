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
package org.springframework.geode.boot.autoconfigure.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration;
import org.springframework.geode.config.annotation.ClusterAwareConfiguration;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.geode.util.CacheUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.books.model.Author;
import example.app.books.model.Book;
import example.app.books.model.ISBN;

/**
 * Integration Tests for {@link DataImportExportAutoConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.DataImportExportAutoConfiguration
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("IMPORT-CLIENT")
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = ClientCacheDataImportExportAutoConfigurationIntegrationTests.TestGeodeClientConfiguration.class,
	properties = {
		"spring.application.name=ClientCacheDataImportExportAutoConfigurationIntegrationTestsClient",
		"spring.data.gemfire.management.use-http=false",
		"spring.boot.data.gemfire.cache.data.import.active-profiles=IMPORT-CLIENT",
		"spring.boot.data.gemfire.cache.region.advice.enabled=true"
	}
)
@SuppressWarnings("unused")
public class ClientCacheDataImportExportAutoConfigurationIntegrationTests
		extends ForkingClientServerIntegrationTestsSupport {

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(TestGeodeServerConfiguration.class,"-Dspring.profiles.active=IMPORT-SERVER");
	}

	@BeforeClass @AfterClass
	public static void resetClusterAwareCondition() {
		ClusterAwareConfiguration.ClusterAwareCondition.reset();
	}

	@Autowired
	private GemfireTemplate booksTemplate;

	@Before
	public void assertBooksTemplate() {

		assertThat(this.booksTemplate).isNotNull();
		assertThat(this.booksTemplate.getRegion()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getName()).isEqualTo("Books");
		assertThat(this.booksTemplate.getRegion().getAttributes()).isNotNull();
		assertThat(this.booksTemplate.getRegion().getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);
	}

	private void assertBook(Book book, String title, LocalDate publishedDate, ISBN isbn, Author author) {

		assertThat(book).isNotNull();
		assertThat(book.getTitle()).isEqualTo(title);
		assertThat(book.getPublishedDate()).isEqualTo(publishedDate);
		assertThat(book.getIsbn()).isEqualTo(isbn);
		assertThat(book.getAuthor()).isEqualTo(author);
	}

	private Book findBy(Iterable<Book> books, String title) {

		return StreamSupport.stream(books.spliterator(), false)
			.filter(book -> book.getTitle().equals(title))
			.findFirst()
			.orElse(null);
	}

	private Collection<Object> getRegionValues(GemfireTemplate template) {

		return Optional.ofNullable(template)
			.map(GemfireTemplate::getRegion)
			.filter(region -> DataPolicy.EMPTY.equals(region.getAttributes().getDataPolicy()))
			.map(CacheUtils::collectValues)
			.orElseGet(() -> template.getRegion().values());
	}

	private Object log(Object value) {

		System.err.printf("%s%n", value);
		System.err.flush();

		return value;
	}

	@Test
	public void booksWereLoaded() {

		Collection<Object> bookValues = getRegionValues(this.booksTemplate);

		assertThat(bookValues).isNotNull();
		assertThat(bookValues).hasSize(2);

		Set<Book> books = bookValues.stream()
			//.peek(this::log)
			.map(value -> ObjectUtils.asType(value, Book.class))
			.collect(Collectors.toSet());

		Book cloudNativeJava = findBy(books, "Cloud Native Java");

		assertBook(cloudNativeJava, "Cloud Native Java", LocalDate.of(2017, Month.AUGUST, 1),
			ISBN.of("978-1-449-374640-8"), Author.newAuthor("Josh Long").identifiedBy(1L));

		Book databaseInternals = findBy(books, "Database Internals");

		assertBook(databaseInternals, "Database Internals", LocalDate.of(2019, Month.OCTOBER, 1),
			ISBN.of("978-1-492-04034-7"), Author.newAuthor("Alex Petrov").identifiedBy(2L));
	}

	@Profile("IMPORT-CLIENT")
	@SpringBootApplication
	@EnableClusterAware
	@EnableEntityDefinedRegions(basePackageClasses = Book.class)
	static class TestGeodeClientConfiguration { }

	@Profile("IMPORT-SERVER")
	@CacheServerApplication(name = "ClientCacheDataImportExportAutoConfigurationIntegrationTestsServer")
	static class TestGeodeServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(TestGeodeServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}
	}
}
