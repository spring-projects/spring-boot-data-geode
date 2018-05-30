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

package org.springframework.geode.function.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.distributed.DistributedMember;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AbstractResultCollector}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.function.support.AbstractResultCollector
 * @since 1.0.0
 */
public class AbstractResultCollectorUnitTests {

	private AbstractResultCollector<Object, Object> resultCollector;

	private static <T, S> AbstractResultCollector<T, S> newResultCollector() {
		return newResultCollector(() -> {});
	}

	private static <T, S> AbstractResultCollector<T, S> newResultCollector(Runnable runnable) {

		return new AbstractResultCollector<T, S>() {

			@Override
			public synchronized S getResult() throws FunctionException {
				runnable.run();
				return super.getResult();
			}

			@Override
			@SuppressWarnings("unchecked")
			public void addResult(DistributedMember memberID, T resultOfSingleExecution) {
				setResult((S) resultOfSingleExecution);
			}
		};
	}

	@Before
	public void setup() {
		this.resultCollector = newResultCollector();
	}

	@Test
	public void clearResultClearsResult() {

		this.resultCollector.setResult("test");

		assertThat(this.resultCollector.getResult()).isEqualTo("test");

		this.resultCollector.clearResults();

		assertThat(this.resultCollector.getResult()).isNull();
	}

	@Test
	public void getResultReturnsResult() {

		this.resultCollector.setResult("test");

		assertThat(this.resultCollector.getResult()).isEqualTo("test");
	}

	@Test
	public void getResultReturnsResultWithinTimeout() throws Throwable {
		TestFramework.runOnce(new ReturnsResultWithinTimeoutMultithreadedTestCase());
	}

	@Test
	public void resultsHaveEnded() {

		this.resultCollector.endResults();

		assertThat(this.resultCollector.hasResultsEnded()).isTrue();
		assertThat(this.resultCollector.hasResultsNotEnded()).isFalse();
	}

	@Test
	public void resultsHaveNotEnded() {

		assertThat(this.resultCollector.hasResultsEnded()).isFalse();
		assertThat(this.resultCollector.hasResultsNotEnded()).isTrue();
	}

	@SuppressWarnings("unused")
	static class ReturnsResultWithinTimeoutMultithreadedTestCase extends MultithreadedTestCase {

		private long startTimestamp;

		private AbstractResultCollector<Object, Object> resultCollector;

		@Override
		public void initialize() {

			super.initialize();

			this.resultCollector = newResultCollector(() -> waitForTick(1));
			this.startTimestamp = System.currentTimeMillis();
		}

		public void thread1() throws InterruptedException {

			Thread.currentThread().setName("ResultCollector.getResult()");

			assertThat(this.resultCollector.getResult(500, TimeUnit.MILLISECONDS)).isEqualTo("test");
		}

		public void thread2() {

			Thread.currentThread().setName("ResultCollector.setResult(..)");

			waitForTick(1);

			this.resultCollector.setResult("test");
		}

		@Override
		public void finish() {

			long endTimestamp = System.currentTimeMillis();

			assertThat(endTimestamp).isGreaterThan(this.startTimestamp);
			assertThat(endTimestamp - this.startTimestamp).isLessThan(TimeUnit.SECONDS.toMillis(2));
		}
	}
}
