/*
 * Copyright 2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link SpringBootApplication Spring Boot application} generated from {@literal Spring Initializer}
 * at <a href="https://start.spring.io">start.spring.io</a>.
 *
 * Putting data into and getting data back out of an Apache Geode (client) cache {@link Region}
 * is the most basic function.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.WebApplicationType
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.web.context.WebApplicationContext
 * @since 1.2.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class InitializerApplication {

	public static void main(String[] args) {

		SpringApplication springApplication =
			new SpringApplicationBuilder(InitializerApplication.class).build();

		assertThat(springApplication).isNotNull();
		assertThat(springApplication.getWebApplicationType()).isEqualTo(WebApplicationType.NONE);

		ConfigurableApplicationContext applicationContext = springApplication.run(args);

		assertThat(applicationContext).isNotNull();
		assertThat(applicationContext).isNotInstanceOf(WebApplicationContext.class);
		assertThat(springApplication.getWebApplicationType()).isNotEqualTo(WebApplicationType.SERVLET);
	}

	@Configuration
	static class GeodeConfiguration {

		@Bean("Example")
		public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setShortcut(ClientRegionShortcut.LOCAL);

			return exampleRegion;
		}
	}
}
