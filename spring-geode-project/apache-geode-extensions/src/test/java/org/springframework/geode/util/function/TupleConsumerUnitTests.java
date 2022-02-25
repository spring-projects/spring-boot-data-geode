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
package org.springframework.geode.util.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.InOrder;

/**
 * Unit Tests for {@link TupleConsumer}.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.util.function.TupleConsumer
 * @since 1.3.0
 */
public class TupleConsumerUnitTests {

	@Test
	public void andThenComposesTupleConsumers() {

		TupleConsumer consumerOne = mock(TupleConsumer.class);
		TupleConsumer consumerTwo = mock(TupleConsumer.class);

		doCallRealMethod().when(consumerOne).andThen(any(TupleConsumer.class));

		Consumer<InvocationArguments> composedConsumer = consumerOne.andThen(consumerTwo);

		assertThat(composedConsumer).isNotNull();
		assertThat(composedConsumer).isNotSameAs(consumerOne);
		assertThat(composedConsumer).isNotSameAs(consumerTwo);

		InvocationArguments arguments = InvocationArguments.from("test", 1, true);

		composedConsumer.accept(arguments);

		InOrder order = inOrder(consumerOne, consumerTwo);

		order.verify(consumerOne, times(1)).accept(eq(arguments));
		order.verify(consumerTwo, times(1)).accept(eq(arguments));
	}

	@Test(expected = NullPointerException.class)
	public void andThenWithNull() {

		TupleConsumer consumer = mock(TupleConsumer.class);

		doCallRealMethod().when(consumer).andThen(any());

		consumer.andThen(null);
	}
}
