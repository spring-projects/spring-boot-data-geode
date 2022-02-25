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
package example.app.library.service;

import java.util.Collections;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import example.app.books.model.Author;
import example.app.books.model.Book;

/**
 * The {@link LibraryService} class models a Library.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @see example.app.books.model.Author
 * @see example.app.books.model.Book
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class LibraryService {

	@Cacheable("BooksByAuthor")
	public List<Book> findBooksByAuthor(Author author) {
		return Collections.emptyList();
	}

	@Cacheable("BooksByYear")
	public List<Book> findBooksByYear(int year) {
		return Collections.emptyList();
	}
}
