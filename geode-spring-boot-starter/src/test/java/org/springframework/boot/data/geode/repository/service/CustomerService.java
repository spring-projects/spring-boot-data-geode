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

package org.springframework.boot.data.geode.repository.service;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.data.geode.repository.model.Customer;
import org.springframework.boot.data.geode.repository.repo.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * The {@link CustomerService} class is an application service for managing {@link Customer Customers}.
 *
 * @author John Blum
 * @see org.springframework.boot.data.geode.repository.model.Customer
 * @see org.springframework.boot.data.geode.repository.repo.CustomerRepository
 * @since 1.0.0
 */
@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	private AtomicLong IDENTIFIER_SEQUENCE = new AtomicLong(0L);

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public final CustomerRepository getCustomerRepository() {

		return Optional.ofNullable(this.customerRepository)
			.orElseThrow(() -> newIllegalStateException("CustomerRepository was not properly configured"));
	}

	public Optional<Customer> findBy(String name) {
		return Optional.ofNullable(getCustomerRepository().findByName(name));
	}

	public Customer save(Customer customer) {

		return Optional.ofNullable(customer)
			.map(it -> {

				if (customer.isNew()) {
					customer.identifiedBy(IDENTIFIER_SEQUENCE.incrementAndGet());
				}

				return getCustomerRepository().save(customer);
			})
			.orElseThrow(() -> newIllegalArgumentException("Customer is required"));
	}
}
