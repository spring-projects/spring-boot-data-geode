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
package example.app.caching.lookaside;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * {@link SpringBootApplication} to configure and bootstrap the example application using the
 * {@literal Look-Aside Caching pattern}, and specifically Spring's Cache Abstraction along with
 * Apache Geode as the caching provider.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @since 1.0.0
 */
// tag::class[]
@SpringBootApplication
public class BootGeodeLookAsideCachingApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeLookAsideCachingApplication.class)
			.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}
}
// end::class[]
