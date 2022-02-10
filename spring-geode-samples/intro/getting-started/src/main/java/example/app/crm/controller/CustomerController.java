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
package example.app.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import example.app.crm.model.Customer;
import example.app.crm.repo.CustomerRepository;

/**
 * Spring Web MVC {@link RestController} used to implement a crude CRM application REST interface.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.PostMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @see example.app.crm.model.Customer
 * @see example.app.crm.repo.CustomerRepository
 * @since 1.2.0
 */
// tag::class[]
@RestController
public class CustomerController {

	private static final String HTML = "<H1>%s</H1>";

	@Autowired
	private CustomerRepository customerRepository;

	@GetMapping("/customers")
	public Iterable<Customer> findAll() {
		return this.customerRepository.findAll();
	}

	@PostMapping("/customers")
	public Customer save(Customer customer) {
		return this.customerRepository.save(customer);
	}

	@GetMapping("/customers/{name}")
	public Customer findByName(@PathVariable("name") String name) {
		return this.customerRepository.findByNameLike(name);
	}

	@GetMapping("/")
	public String home() {
		return format("Customer Relationship Management");
	}

	@GetMapping("/ping")
	public String ping() {
		return format("PONG");
	}

	private String format(String value) {
		return String.format(HTML, value);
	}
}
// end::class[]
