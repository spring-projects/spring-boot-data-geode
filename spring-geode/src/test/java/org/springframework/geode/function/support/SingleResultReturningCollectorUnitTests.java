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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.geode.distributed.DistributedMember;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link SingleResultReturningCollector}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.geode.function.support.SingleResultReturningCollector
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleResultReturningCollectorUnitTests {

	@Mock
	private DistributedMember mockDistributedMember;

	private SingleResultReturningCollector<Object> resultCollector;

	@Before
	public void setup() {
		this.resultCollector = new SingleResultReturningCollector<>();
	}

	@Test
	public void addResultWithNullIsSafe() {

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(this.mockDistributedMember, null);

		assertThat(this.resultCollector.getResult()).isNull();
	}

	@Test
	public void addResultWithSingleObjectReturnsObject() {

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(this.mockDistributedMember, "test");

		assertThat(this.resultCollector.getResult()).isEqualTo("test");
	}

	@Test
	public void addResultWithIterableReturnsFirstElement() {

		Iterable<String> list = Arrays.asList("one", "two", "three");

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(this.mockDistributedMember, list);

		assertThat(this.resultCollector.getResult()).isEqualTo("one");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void addResultWithIterableReturningNullIteratorIsSafe() {

		Iterable<Object> mockIterable = mock(Iterable.class);

		when(mockIterable.iterator()).thenReturn(null);

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(mockDistributedMember, mockIterable);

		assertThat(this.resultCollector.getResult()).isNull();
	}

	@Test
	public void addResultWithIterableOfListsReturnsFirstElementInListOne() {

		Iterable<String> listOne = Arrays.asList("one", "two", "three");
		Iterable<String> listTwo = Arrays.asList("four", "five", "six");

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(this.mockDistributedMember, Arrays.asList(listOne, listTwo));

		assertThat(this.resultCollector.getResult()).isEqualTo("one");
	}

	@Test
	public void addResultWithIterableOfListsOfListsReturnsFirstElementInListOne() {

		Iterable<String> listOne = Arrays.asList("one", "two", "three");
		Iterable<String> listTwo = Arrays.asList("four", "five", "six");
		Iterable<String> listThree = Arrays.asList("seven", "eight", "nine");

		assertThat(this.resultCollector.getResult()).isNull();

		this.resultCollector.addResult(this.mockDistributedMember,
			Arrays.asList(Arrays.asList(listOne, listTwo), listThree));

		assertThat(this.resultCollector.getResult()).isEqualTo("one");
	}
}
