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
package example.app.security.client;

import org.apache.geode.cache.client.ClientCache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.geode.config.annotation.EnableClusterAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.app.security.client.model.Customer;

/**
 * A Spring Boot, Apache Geode {@link ClientCache} application that configures security.
 *
 * @author Patrick Johnson
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.geode.config.annotation.EnableClusterAware
 * @since 1.4.0
 */
// tag::class[]
@SpringBootApplication
@EnableClusterAware
@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
public class BootGeodeSecurityClientApplication {

	private static final Logger logger = LoggerFactory.getLogger("example.app.security");

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeSecurityClientApplication.class)
			.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}

	// tag::runner[]
	@Bean
	ApplicationRunner runner(@Qualifier("customersTemplate") GemfireTemplate customersTemplate) {

		return args -> {

			Customer williamEvans = Customer.newCustomer(2L, "William Evans");

			customersTemplate.put(williamEvans.getId(), williamEvans);

			logger.info("Successfully put [{}] in Region [{}]",
				williamEvans, customersTemplate.getRegion().getName());

			try {
				logger.info("Attempting to read from Region [{}]...", customersTemplate.getRegion().getName());
				customersTemplate.get(2L);
			}
			catch (Exception cause) {
				logger.info("Read failed because \"{}\"", cause.getCause().getCause().getMessage());
			}
		};
	}
	// end::runner[]
}
// end::class[]
