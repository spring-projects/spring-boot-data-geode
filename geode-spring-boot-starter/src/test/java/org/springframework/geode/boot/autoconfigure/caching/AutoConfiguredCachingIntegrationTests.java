/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.caching;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.NonBeanType;
import example.app.model.Book;
import example.app.service.support.CachingBookService;

/**
 * Integration tests testing the auto-configuration of Spring's Cache Abstraction with Apache Geode
 * or Pivotal GemFire as the caching provider.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.CachingProviderAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredCachingIntegrationTests extends IntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	private CachingBookService bookService;

	@Resource(name = "CachedBooks")
	private Region<String, Book> cachedBooks;

	private void assertBook(Book book, String title) {

		assertThat(book).isNotNull();
		assertThat(book.isNew()).isFalse();
		assertThat(book.getTitle()).isEqualTo(title);
	}

	@Test
	public void bookServiceWasConfiguredCorrectly() {

		assertThat(this.bookService).isNotNull();
		assertThat(this.bookService.isCacheMiss()).isFalse();
	}

	@Test
	public void cachedBooksRegionWasConfiguredCorrectly() {

		assertThat(this.cachedBooks).isNotNull();
		assertThat(this.cachedBooks.getName()).isEqualTo("CachedBooks");
		assertThat(this.cachedBooks.getFullPath()).isEqualTo(RegionUtils.toRegionPath("CachedBooks"));
		assertThat(this.cachedBooks).isEmpty();
	}

	@Test
	public void geodeAsTheCachingProviderWasAutoConfiguredCorrectly() {

		assertThat(this.cachedBooks).isEmpty();

		Book bookOne = this.bookService.findByTitle("Star Wars 3 - Revenge of the Sith");

		assertBook(bookOne, "Star Wars 3 - Revenge of the Sith");
		assertThat(this.bookService.isCacheMiss()).isTrue();
		assertThat(this.cachedBooks).hasSize(1);
		assertThat(this.cachedBooks.get(bookOne.getTitle())).isEqualTo(bookOne);

		Book bookOneAgain = this.bookService.findByTitle(bookOne.getTitle());

		assertThat(bookOneAgain).isEqualTo(bookOne);
		assertThat(this.bookService.isCacheMiss()).isFalse();
		assertThat(this.cachedBooks).hasSize(1);
		assertThat(this.cachedBooks.get(bookOne.getTitle())).isEqualTo(bookOne);

		Book bookTwo = this.bookService.findByTitle("Star Wars 6 - Return of the Jedi");

		assertBook(bookTwo, "Star Wars 6 - Return of the Jedi");
		assertThat(this.bookService.isCacheMiss()).isTrue();
		assertThat(this.cachedBooks).hasSize(2);
		assertThat(this.cachedBooks.get(bookOne.getTitle())).isEqualTo(bookOne);
		assertThat(this.cachedBooks.get(bookTwo.getTitle())).isEqualTo(bookTwo);
	}

	@SpringBootApplication(scanBasePackageClasses = NonBeanType.class)
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
	static class TestConfiguration {  }

}
