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
package example.app.caching.multisite.client.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import example.app.caching.multisite.client.model.Customer;
import example.app.caching.multisite.client.service.CustomerService;

/**
 * {@link CustomerController} is a Spring Web application {@link RestController} component used to
 * service user requests for {@link Customer customers} through a REST API (interface) over HTTP.
 *
 * @author John Blum
 * @see org.springframework.core.env.Environment
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @see example.app.caching.multisite.client.model.Customer
 * @see example.app.caching.multisite.client.service.CustomerService
 * @since 1.3.0
 */
// tag::class[]
@RestController
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private Environment environment;

	// tag::rest-api-endpoint[]
	@GetMapping("/customers/{name}")
	public CustomerHolder searchBy(@PathVariable String name) {

		return CustomerHolder.from(this.customerService.findBy(name))
			.setCacheMiss(this.customerService.isCacheMiss());
	}
	// end::rest-api-endpoint[]

	@GetMapping("/ping")
	public String pingPong() {
		return "PONG";
	}

	@GetMapping("/")
	public String home() {

		return String.format("%s is running!",
			environment.getProperty("spring.application.name", "UNKNOWN"));
	}

	public static class CustomerHolder {

		public static CustomerHolder from(Customer customer) {
			return new CustomerHolder(customer);
		}

		private boolean cacheMiss = true;

		private final Customer customer;

		protected CustomerHolder(Customer customer) {

			Assert.notNull(customer, "Customer must not be null");

			this.customer = customer;
		}

		public CustomerHolder setCacheMiss(boolean cacheMiss) {
			this.cacheMiss = cacheMiss;
			return this;

		}

		public boolean isCacheMiss() {
			return this.cacheMiss;
		}

		public Customer getCustomer() {
			return customer;
		}
	}
}
// end::class[]
