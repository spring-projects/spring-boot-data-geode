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
package org.springframework.geode.docs.example.app.client;

import java.util.Arrays;

import org.apache.geode.cache.client.ClientCache;

import org.apache.shiro.util.Assert;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.config.annotation.UseMemberName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SpringBootApplication Spring Boot application} that configures and bootstraps an Apache Geode
 * {@link ClientCache} instance by default in a JVM process.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @since 1.3.0
 */
// tag::class[]
@SpringBootApplication
@SuppressWarnings("unused")
public class SpringBootApacheGeodeClientCacheApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(SpringBootApacheGeodeClientCacheApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@UseMemberName("SpringBootApacheGeodeClientCacheApplication")
	static class GeodeConfiguration { }

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	ApplicationRunner applicationAssertionRunner(ConfigurableApplicationContext applicationContext) {

		return args -> {

			Assert.notNull(applicationContext, "ApplicationContext is required");

			Environment environment = applicationContext.getEnvironment();

			Assert.notNull(environment, "Environment is required");
			Assert.isTrue(ArrayUtils.isEmpty(ArrayUtils.nullSafeArray(environment.getActiveProfiles(), String.class)),
				"Expected Active Profiles to be empty");
			Assert.isTrue(Arrays.asList(ArrayUtils.nullSafeArray(environment.getDefaultProfiles(), String.class))
					.contains("default"), "Expected Default Profiles to contain 'default'");

			ClientCache clientCache = applicationContext.getBean(ClientCache.class);

			Assert.notNull(clientCache, "ClientCache is expected");
			Assert.isTrue(SpringBootApacheGeodeClientCacheApplication.class.getSimpleName().equals(clientCache.getName()),
				"ClientCache.name is not correct");

			this.logger.info("Application assertions successful!");
		};
	}
}
// end::class[]
