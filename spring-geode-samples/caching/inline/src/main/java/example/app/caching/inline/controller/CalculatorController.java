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
package example.app.caching.inline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import example.app.caching.inline.model.ResultHolder;
import example.app.caching.inline.service.CalculatorService;

/**
 * The CalculatorController class...
 *
 * @author John Blum
 * @since 1.0.0
 */
// tag::class[]
@RestController
public class CalculatorController {

	@Autowired
	private CalculatorService calculatorService;

	@GetMapping("/")
	public String home() {
		return format("Inline Caching Example");
	}

	@GetMapping("/ping")
	public String ping() {
		return format("PONG");
	}

	@GetMapping("/calculator/factorial/{number}")
	public String factorial(@PathVariable("number") int number) {

		long t0 = System.currentTimeMillis();

		ResultHolder result = this.calculatorService.factorial(number);

		return toJson(result, System.currentTimeMillis() - t0, this.calculatorService.isCacheMiss());
	}

	@GetMapping("/calculator/sqrt/{number}")
	public String sqrt(@PathVariable("number") int number) {

		long t0 = System.currentTimeMillis();

		ResultHolder result = this.calculatorService.sqrt(number);

		return toJson(result, System.currentTimeMillis() - t0, this.calculatorService.isCacheMiss());
	}

	private String format(Object output) {
		return String.format("<h1>%s</h1>", output);
	}

	private String toJson(ResultHolder result, long latency, boolean cacheMiss) {
		return format(String.format("{ math: %s, latency: %d ms, cacheMiss: %s }", result, latency, cacheMiss));
	}
}
// end::class[]
