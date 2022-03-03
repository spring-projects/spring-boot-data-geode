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
package example.app.caching.inline.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.caching.inline.model.Operator;
import example.app.caching.inline.model.ResultHolder;
import example.app.caching.inline.service.support.AbstractCacheableService;

/**
 * Spring {@link Service} class implementing mathematical operators, similar to calculator functions,
 * such as {@literal factorial} and so on.
 *
 * In addition, given that most mathematical functions yield the same result when given the same input, this service
 * also employs caching using Spring's Cache Abstraction and Apache Geode as the caching provider.  This class provides
 * additional functionality to ascertain whether a cache hit/miss occurred.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @see example.app.caching.inline.model.Operator
 * @see example.app.caching.inline.model.ResultHolder
 * @see example.app.caching.inline.service.support.AbstractCacheableService
 * @since 1.1.0
 */
// tag::class[]
@Service
public class CalculatorService extends AbstractCacheableService {

	@Cacheable(value = "Factorials", keyGenerator = "resultKeyGenerator")
	public ResultHolder factorial(int number) {

		this.cacheMiss.set(true);

		Assert.isTrue(number >= 0L,
			String.format("Number [%d] must be greater than equal to 0", number));

		simulateLatency();

		if (number <= 2) {
			return ResultHolder.of(number, Operator.FACTORIAL, number == 2 ? 2 : 1);
		}

		int operand = number;
		int result = number;

		while (--number > 1) {
			result *= number;
		}

		return ResultHolder.of(operand, Operator.FACTORIAL, result);
	}

	@Cacheable(value = "SquareRoots", keyGenerator = "resultKeyGenerator")
	public ResultHolder sqrt(int number) {

		this.cacheMiss.set(true);

		Assert.isTrue(number >= 0,
			String.format("Number [%d] must be greater than equal to 0", number));

		simulateLatency();

		int result = Double.valueOf(Math.sqrt(number)).intValue();

		return ResultHolder.of(number, Operator.SQUARE_ROOT, result);
	}
}
// end::class[]
