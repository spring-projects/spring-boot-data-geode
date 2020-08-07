/*
 * Copyright 2020 the original author or authors.
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
package example.app.security.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import example.app.security.client.BootGeodeSecurityClientApplication;

/**
 * A Spring {@link RestController} used by {@link BootGeodeSecurityClientApplication}.
 *
 * @author Patrick Johnson
 * @see org.springframework.web.bind.annotation.RestController
 * @since 1.4.0
 */
// tag::class[]
@RestController
public class SecurityController {

	@Autowired
	private Environment environment;

	@GetMapping("/message")
	public String getMessage() {
		return String.format("I'm using SSL with this Keystore: %s",
			this.environment.getProperty("spring.data.gemfire.security.ssl.keystore"));
	}
}
// end::class[]
