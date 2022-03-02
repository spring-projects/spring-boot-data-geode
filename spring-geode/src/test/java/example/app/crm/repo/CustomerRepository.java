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
package example.app.crm.repo;

import org.springframework.data.repository.CrudRepository;

import example.app.crm.model.Customer;

/**
 * {@link CustomerRepository} is a Spring Data {@link CrudRepository} and Data Access Object (DAO) defining basic CRUD
 * and simple query data access operations on {@link Customer} objects.
 *
 * @author John Blum
 * @see java.lang.Long
 * @see org.springframework.data.repository.CrudRepository
 * @see example.app.crm.model.Customer
 * @since 1.1.0
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

	Customer findByName(String name);

}
