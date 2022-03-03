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
package example.app.caching.near.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import example.app.caching.near.client.model.Person;
import example.app.caching.near.client.service.YellowPagesService;

/**
 * Spring {@link RestController} class for implementing the UI to the Yellow Pages application.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.PathVariable
 * @see org.springframework.web.bind.annotation.RequestParam
 * @see org.springframework.web.bind.annotation.RestController
 * @see example.app.caching.near.client.model.Person
 * @see example.app.caching.near.client.service.YellowPagesService
 * @since 1.1.0
 */
// tag::class[]
@RestController
public class YellowPagesController {

	private static final String HTML =
		"<html><title>Yellow Pages</title><body bgcolor=\"#F5FC1D\" text=\"black\"><h1>%s</h1><body><html>";

	@Autowired
	private YellowPagesService yellowPages;

	@GetMapping("/")
	public String home() {
		return format("Near Caching Example");
	}

	@GetMapping("/ping")
	public String ping() {
		return format("PONG");
	}

	@GetMapping("/yellow-pages/{name}")
	public String find(@PathVariable("name") String name) {

		long t0 = System.currentTimeMillis();

		Person person = this.yellowPages.find(name);

		return format(String.format("{ person: %s, cacheMiss: %s, latency: %d ms }",
			person, this.yellowPages.isCacheMiss(), (System.currentTimeMillis() - t0)));
	}

	@GetMapping("/yellow-pages/{name}/update")
	public String update(@PathVariable("name") String name,
			@RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "phoneNumber", required = false) String phoneNumber) {

		Person person = this.yellowPages.save(this.yellowPages.find(name), email, phoneNumber);

		return format(String.format("{ person: %s }", person));
	}

	@GetMapping("/yellow-pages/{name}/evict")
	public String evict(@PathVariable("name") String name) {

		this.yellowPages.evict(name);

		return format(String.format("Evicted %s", name));
	}

	private String format(String value) {
		return String.format(HTML, value);
	}
}
// end::class[]
