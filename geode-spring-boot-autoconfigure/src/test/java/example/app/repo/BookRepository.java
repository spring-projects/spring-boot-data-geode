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

package example.app.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import example.app.model.Author;
import example.app.model.Book;
import example.app.model.ISBN;

/**
 * The {@link BookRepository} interface is a Spring Data {@link CrudRepository} defining basic CRUD
 * and simple query data access operations on {@link Book} objects to the backing data store.
 *
 * @author John Blum
 * @see example.app.model.Book
 * @see example.app.model.ISBN
 * @see org.springframework.data.repository.CrudRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface BookRepository extends CrudRepository<Book, ISBN> {

	List<Book> findByAuthorOrderByAuthorNameAscTitleAsc(Author author);

	Book findByIsbn(ISBN isbn);

	Book findByTitle(String title);

}
