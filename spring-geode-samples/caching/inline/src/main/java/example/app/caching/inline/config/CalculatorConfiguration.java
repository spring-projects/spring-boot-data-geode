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
package example.app.caching.inline.config;

import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.geode.cache.InlineCachingRegionConfigurer;

import example.app.caching.inline.model.Operator;
import example.app.caching.inline.model.ResultHolder;
import example.app.caching.inline.repo.CalculatorRepository;

/**
 * Spring {@link Configuration} class used to configure Apache Geode as a caching provider as well as configure
 * the target of JPA entity scan for application persistent entities.
 *
 * Additionally, a custom {@link KeyGenerator} bean definition is declared and used in caching to sync the keys
 * used by both the cache and the database.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.cache.interceptor.KeyGenerator
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.geode.cache.InlineCachingRegionConfigurer
 * @see example.app.caching.inline.model.ResultHolder
 * @see example.app.caching.inline.repo.CalculatorRepository
 * @since 1.1.0
 */
// tag::class[]
@Configuration
@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EntityScan(basePackageClasses = ResultHolder.class)
@SuppressWarnings("unused")
public class CalculatorConfiguration {

	@Bean
	InlineCachingRegionConfigurer<ResultHolder, ResultHolder.ResultKey> inlineCachingForCalculatorApplicationRegionsConfigurer(
			CalculatorRepository calculatorRepository) {

		Predicate<String> regionBeanNamePredicate = regionBeanName ->
			Arrays.asList("Factorials", "SquareRoots").contains(regionBeanName);

		return new InlineCachingRegionConfigurer<>(calculatorRepository, regionBeanNamePredicate);
	}

	// tag::key-generator[]
	@Bean
	KeyGenerator resultKeyGenerator() {

		return (target, method, arguments) -> {

			int operand = Integer.parseInt(String.valueOf(arguments[0]));

			Operator operator = "sqrt".equals(method.getName())
				? Operator.SQUARE_ROOT
				: Operator.FACTORIAL;

			return ResultHolder.ResultKey.of(operand, operator);
		};
	}
	// end::key-generator[]
}
// end::class[]
