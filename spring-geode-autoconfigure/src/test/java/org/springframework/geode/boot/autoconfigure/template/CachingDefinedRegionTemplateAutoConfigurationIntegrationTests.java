/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.geode.boot.autoconfigure.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.books.NonBeanType;
import example.app.books.model.Author;
import example.app.books.model.Book;
import example.app.library.service.LibraryService;

/**
 * Integration tests for {@link RegionTemplateAutoConfiguration} using SDG's {@link EnableCachingDefinedRegions}
 * annotation to define {@link Region Regions} and associated Templates.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.boot.autoconfigure.RegionTemplateAutoConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class CachingDefinedRegionTemplateAutoConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private GemFireCache gemfireCache;

	@Autowired
	@Qualifier("booksByAuthorTemplate")
	private GemfireTemplate booksByAuthorTemplate;

	@Autowired
	@Qualifier("cachedBooksTemplate")
	private GemfireTemplate booksByTitleTemplate;

	@Autowired
	@Qualifier("booksByYearTemplate")
	private GemfireTemplate booksByYearTemplate;

	@Resource(name = "BooksByAuthor")
	private Region<Author, List<Book>> booksByAuthor;

	@Resource(name = "CachedBooks")
	private Region<String, Book> booksByTitle;

	@Resource(name = "BooksByYear")
	private Region<Integer, List<Book>> booksByYear;

	@Before
	public void setup() {

		assertThat(this.gemfireCache).isNotNull();

		assertThat(this.gemfireCache.rootRegions().stream()
			.map(Region::getName)
			.sorted()
			.collect(Collectors.toList())).containsExactly("BooksByAuthor", "BooksByYear", "CachedBooks");

		assertThat(this.booksByAuthor).isNotNull();
		assertThat(this.booksByTitle).isNotNull();
		assertThat(this.booksByYear).isNotNull();
	}

	@Test
	public void authorsBooksRegionTemplateIsPresent() {

		assertThat(this.booksByAuthorTemplate).isNotNull();
		assertThat(this.booksByAuthorTemplate.getRegion()).isEqualTo(this.booksByAuthor);
	}

	@Test
	public void booksByTitleRegionTemplateIsPresent() {

		assertThat(this.booksByTitleTemplate).isNotNull();
		assertThat(this.booksByTitleTemplate.getRegion()).isEqualTo(this.booksByTitle);
	}

	@Test
	public void booksByYearRegionTemplateIsPresent() {

		assertThat(this.booksByYearTemplate).isNotNull();
		assertThat(this.booksByYearTemplate.getRegion()).isEqualTo(this.booksByYear);
	}

	@EnableGemFireMockObjects
	@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
	@SpringBootApplication(scanBasePackageClasses = { NonBeanType.class, LibraryService.class })
	static class TestApplicationConfiguration {

		@Bean
		LibraryService libraryService() {
			return new LibraryService();
		}

		//@Bean("TestBean")
		Object testBean(@Qualifier("booksByAuthor") GemfireTemplate booksByAuthorTemplate) {
			return "TEST";
		}
	}
}
