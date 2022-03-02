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
package org.springframework.geode.docs.example.app.locator;

import java.util.Scanner;

import org.apache.geode.distributed.Locator;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.LocatorApplication;
import org.springframework.geode.config.annotation.UseLocators;

/**
 * The {@link SpringBootApacheGeodeLocatorApplication} class is a Spring Boot application that configures and bootstraps
 * an Apache Geode {@link Locator} application JVM process.
 *
 * @author John Blum
 * @see org.apache.geode.distributed.Locator
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.LocatorApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see org.springframework.geode.config.annotation.UseLocators
 * @since 1.2.0
 */
// tag::class[]
@UseLocators
@SpringBootApplication
@LocatorApplication(name = "SpringBootApacheGeodeLocatorApplication")
public class SpringBootApacheGeodeLocatorApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(SpringBootApacheGeodeLocatorApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);

		System.err.println("Press <enter> to exit!");

		new Scanner(System.in).nextLine();
	}

	@Configuration
	@EnableManager(start = true)
	@Profile("manager")
	@SuppressWarnings("unused")
	static class ManagerConfiguration { }

}
// end::class[]
