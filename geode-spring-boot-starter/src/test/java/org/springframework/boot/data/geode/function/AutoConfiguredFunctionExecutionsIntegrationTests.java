/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.boot.data.geode.function;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.shiro.util.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.geode.function.executions.Calculator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableGemFireProperties;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The AutoConfiguredFunctionExecutionsIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredFunctionExecutionsIntegrationTests {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@Autowired
	private Calculator calculator;

	@Test
	public void firstFunctionsMustBeRegistered() {

		Arrays.stream(CalculatorFunctions.class.getMethods())
			.filter(method -> !Object.class.equals(method.getDeclaringClass()))
			.map(Method::getName)
			.forEach(methodName -> assertThat(FunctionService.isRegistered(methodName))
				.describedAs("Function [%s] was not registered", methodName)
				.isTrue());
	}

	@Test
	public void thenCalculationsAreCorrect() {

		assertThat(this.calculator).isNotNull();
		assertThat(extractResult(this.calculator.add(8.0d, 8.0d))).isEqualTo(16.0d);
		assertThat(extractResult(this.calculator.divide(16.0d, 4.0d))).isEqualTo(4.0d);
		assertThat(extractResult(this.calculator.factorial(5L))).isEqualTo(120L);
		assertThat(extractResult(this.calculator.multiply(4.0d, 4.0d))).isEqualTo(16.0d);
		assertThat(extractResult(this.calculator.squared(4.0d))).isEqualTo(16.0d);
		assertThat(extractResult(this.calculator.squareRoot(16.0d))).isEqualTo(4.0d);
		assertThat(extractResult(this.calculator.subtract(16.0d, 8.0d))).isEqualTo(8.0d);
	}

	private Object extractResult(Object result) {

		return Optional.ofNullable(result)
			.filter(it -> it instanceof Iterable)
			.map(it -> ((Iterable) it).iterator())
			.filter(Iterator::hasNext)
			.map(Iterator::next)
			.map(this::extractResult)
			.orElse(result);
	}

	@SpringBootApplication
	@EnableGemFireProperties(groups = "test")
	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	static class TestConfiguration {

		@Bean
		public CalculatorFunctions calculatorFunctions(GemFireCache gemfireCache) {
			return new CalculatorFunctions();
		}
	}

	public static class CalculatorFunctions {

		@GemfireFunction(id = "add", hasResult = true)
		public double add(double operandOne, double operandTwo) {
			return operandOne + operandTwo;
		}

		@GemfireFunction(id = "divide", hasResult = true)
		public double divide(double numerator, double divisor) {
			return numerator / divisor;
		}

		@GemfireFunction(id = "factorial", hasResult = true)
		public long factorial(long number) {

			Assert.isTrue(number > -1, "Number be greater than -1");

			long result = number == 2 ? 2 : 1;

			while (number > 1) {
				result *= number--;
			}

			return result;
		}

		@GemfireFunction(id = "multiply", hasResult = true)
		public double multiply(double  operandOne, double operandTwo) {
			return operandOne * operandTwo;
		}

		@GemfireFunction(id = "squareRoot", hasResult = true)
		public double squareRoot(double number) {
			return Math.sqrt(number);
		}

		@GemfireFunction(id = "squared", hasResult = true)
		public double squared(double number) {
			return number * number;
		}

		@GemfireFunction(id = "subtract", hasResult = true)
		public double subtract(double operandOne, double operandTwo) {
			return operandOne - operandTwo;
		}
	}
}
