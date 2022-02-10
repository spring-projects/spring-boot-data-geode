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
package example.app.caching.inline;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.caching.inline.model.Operator;
import example.app.caching.inline.model.ResultHolder;
import example.app.caching.inline.repo.CalculatorRepository;
import example.app.caching.inline.service.CalculatorService;

/**
 * Integration Tests for Calculator Application.
 *
 * @author John Blum
 * @see java.util.concurrent.atomic.AtomicBoolean
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.1.0
 */
// tag::class[]
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
	"spring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=false"
})
@SuppressWarnings("unused")
public class CalculatorApplicationIntegrationTests extends IntegrationTestsSupport {

	private static final AtomicBoolean runOnce = new AtomicBoolean(false);

	@Autowired
	private CalculatorRepository calculatorRepository;

	@Autowired
	private CalculatorService calculatorService;

	@Resource(name = "Factorials")
	private Region<Object, Object> factorials;

	@Resource(name = "SquareRoots")
	private Region<Object, Object> squareRoots;

	@Before
	public void setup() {

		if (runOnce.compareAndSet(false, true)) {

			assertThat(this.factorials).isNotNull();
			assertThat(this.factorials.getName()).isEqualTo("Factorials");
			assertThat(this.squareRoots).isNotNull();
			assertThat(this.squareRoots.getName()).isEqualTo("SquareRoots");

			Iterable<ResultHolder> calculations = calculatorRepository.findAll();

			assertThat(calculations).isNotNull();
			assertThat(calculations).hasSize(6);
			assertThat(calculations).containsExactly(
				ResultHolder.of(5, Operator.FACTORIAL, 120),
				ResultHolder.of(7, Operator.FACTORIAL, 5040),
				ResultHolder.of(9, Operator.FACTORIAL, 362880),
				ResultHolder.of(16, Operator.SQUARE_ROOT, 4),
				ResultHolder.of(64, Operator.SQUARE_ROOT, 8),
				ResultHolder.of(256, Operator.SQUARE_ROOT, 16)
			);
		}
	}

	@Test
	public void cacheMissForExistingValueLoadsFromDatabase() {

		Object key = ResultHolder.ResultKey.of(7, Operator.FACTORIAL);

		assertThat(this.factorials).doesNotContainKey(key);
		assertThat(this.calculatorRepository.findByOperandEqualsAndOperatorEquals(7, Operator.FACTORIAL)
			.isPresent()).isTrue();
		assertThat(this.calculatorService.isCacheMiss()).isFalse();

		ResultHolder factorialOfSeven = this.calculatorService.factorial(7);

		assertThat(factorialOfSeven).isNotNull();
		assertThat(factorialOfSeven.getResult()).isEqualTo(5040);
		assertThat(this.factorials).containsKey(key);
		assertThat(this.calculatorService.isCacheHit()).isTrue();
	}

	@Test
	public void cacheMissForNonExistingValueInvokesServiceMethodPutsIntoCacheAndSavesToDatabase() {

		Object key = ResultHolder.ResultKey.of(25, Operator.SQUARE_ROOT);

		assertThat(this.squareRoots).doesNotContainKey(key);
		assertThat(this.calculatorRepository.findByOperandEqualsAndOperatorEquals(25, Operator.SQUARE_ROOT)
			.isPresent()).isFalse();
		assertThat(this.calculatorService.isCacheMiss()).isFalse();

		ResultHolder squareRootOfTwentyFive = this.calculatorService.sqrt(25);

		assertThat(squareRootOfTwentyFive.getResult()).isEqualTo(5);
		assertThat(this.squareRoots).containsKey(key);
		assertThat(this.calculatorRepository.findByOperandEqualsAndOperatorEquals(25, Operator.SQUARE_ROOT)
			.isPresent()).isTrue();
		assertThat(this.calculatorService.isCacheMiss()).isTrue();
	}
}
// end::class[]
