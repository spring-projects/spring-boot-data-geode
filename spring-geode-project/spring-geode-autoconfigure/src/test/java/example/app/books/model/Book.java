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
package example.app.books.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link Book} class is an Abstract Data Type (ADT) modeling a book.
 *
 * @author John Blum
 * @see lombok
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @see example.app.books.model.Author
 * @see example.app.books.model.ISBN
 * @since 1.0.0
 */
@Data
@Region("Books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(staticName = "newBook")
public class Book {

	private Author author;

	@Id
	private ISBN isbn;

	private LocalDate publishedDate;

	@NonNull
	private String title;

	@Transient
	public boolean isNew() {
		return getIsbn() == null;
	}

	public Book identifiedBy(ISBN isbn) {
		setIsbn(isbn);
		return this;
	}
}
