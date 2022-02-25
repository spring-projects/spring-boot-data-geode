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
package example.app.env;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * {@link SpringBootApplication} allowing users to review Spring's property resolution precedence.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.env.Environment
 * @since 1.4.0
 */
@SuppressWarnings("unused")
@SpringBootApplication(exclude = CassandraDataAutoConfiguration.class)
public class EnvironmentUsingSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnvironmentUsingSpringBootApplication.class, args);
	}

	@Value("${example.app.property:FROM-CODE}")
	private String testProperty;

	@Bean
	ApplicationRunner environmentRunner(Environment environment) {

		return args -> {
			//System.err.printf("PROPERTY is [%s]%n", environment.getProperty("example.app.property"));
			System.err.printf("PROPERTY is [%s]%n", this.testProperty);
		};
	}
}
