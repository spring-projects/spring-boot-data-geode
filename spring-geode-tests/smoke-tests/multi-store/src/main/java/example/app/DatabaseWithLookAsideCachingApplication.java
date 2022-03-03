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
package example.app;

import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;

import example.app.model.Contact;

/**
 * The {@link DatabaseWithLookAsideCachingApplication} class is a Spring Boot application testing Spring Data's
 * multi-store support along with using Apache Geode as a caching provider in Spring's Cache Abstraction.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see example.app.model.Contact
 * @since 1.2.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class DatabaseWithLookAsideCachingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatabaseWithLookAsideCachingApplication.class, args);
	}

	@Configuration
	@EntityScan(basePackageClasses = Contact.class)
	@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
	static class ApplicationConfiguration { }

}
