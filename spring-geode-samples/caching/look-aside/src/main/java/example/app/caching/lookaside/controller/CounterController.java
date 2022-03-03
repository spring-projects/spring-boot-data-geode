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
package example.app.caching.lookaside.controller;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import example.app.caching.lookaside.service.CounterService;

/**
 * A Spring Web MVC {@link RestController} used to expose the {@link CounterService} operations via HTTP
 * in a REST-ful interface.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @since 1.0.0
 */
@SuppressWarnings("unused")
// tag::class[]
@RestController
public class CounterController {

	private static final String HEADER_ONE = "<h1>%s</h1>";

	private final CounterService counterService;

	public CounterController(CounterService counterService) {

		Assert.notNull(counterService, "CounterService is required");

		this.counterService = counterService;
	}

	@GetMapping("/")
	public String home() {
		return String.format(HEADER_ONE, "Look-Aside Caching Example");
	}

	@GetMapping("/ping")
	public String ping() {
		return String.format(HEADER_ONE, "PONG");
	}

	@GetMapping("counter/{name}")
	public String getCount(@PathVariable("name") String counterName) {
		return String.format(HEADER_ONE, this.counterService.getCount(counterName));
	}

	@GetMapping("counter/{name}/cached")
	public String getCachedCount(@PathVariable("name") String counterName) {
		return String.format(HEADER_ONE, this.counterService.getCachedCount(counterName));
	}

	@GetMapping("counter/{name}/reset")
	public String resetCounter(@PathVariable("name") String counterName) {
		this.counterService.resetCounter(counterName);
		return String.format(HEADER_ONE, "0");
	}
}
// end::class[]
