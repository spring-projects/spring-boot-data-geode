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

import example.app.security.client.model.Customer;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;

/**
 * The {@link BootGeodeSecurityClientApplication} class is a Spring Boot, Apache Geode {@link ClientCache}
 * application that configures security.
 *
 * @author Patrick Johnson
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @since 1.3.0
 */
// tag::class[]
@SpringBootApplication
@EnableClusterConfiguration
@EnableEntityDefinedRegions
public class BootGeodeSecurityClientApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(BootGeodeSecurityClientApplication.class)
				.web(WebApplicationType.SERVLET)
				.build()
				.run(args);
	}

	@Bean
	ApplicationRunner runner(@Qualifier("Customers") Region<Long, Customer> customers) {
		return args -> {
			customers.put(2L, Customer.newCustomer(2L, "William Evans"));
			System.out.println(String.format("Successfully wrote data to region %s", customers.getName()));

			try {
				System.out.println(String.format("Attempting to read data from region %s", customers.getName()));
				customers.get(2L);
			} catch (Exception e) {
				System.out.println(String.format("Read failed because \"%s\"", e.getCause().getMessage()));
			}
		};
	}
}
// end::class[]
