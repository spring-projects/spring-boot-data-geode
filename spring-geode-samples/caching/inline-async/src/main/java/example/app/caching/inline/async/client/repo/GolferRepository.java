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
package example.app.caching.inline.async.client.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import example.app.caching.inline.async.client.model.Golfer;

/**
 * Spring Data {@link JpaRepository} and Data Access Object (DAO) used to perform basic CRUD and simple SQL query
 * data access operations on {@link Golfer Golfers} stored in an RDBMS (database) with JPA (Hibernate).
 *
 * @author John Blum
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see example.app.caching.inline.async.client.model.Golfer
 * @since 1.4.0
 */
public interface GolferRepository extends JpaRepository<Golfer, String> {

}
