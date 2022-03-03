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
package org.springframework.geode.core.util.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

/**
 * Unit Tests for {@link FunctionUtils}.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @see java.util.function.Function
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @since 1.1.0
 */
public class FunctionUtilsUnitTests {

	@Test
	@SuppressWarnings("unchecked")
	public void callsConsumerReturnsNull() {

		Consumer<Object> mockConsumer = mock(Consumer.class);

		Function<Object, Object> function = FunctionUtils.toNullReturningFunction(mockConsumer);

		assertThat(function).isNotNull();
		assertThat(function.apply("test")).isNull();

		verify(mockConsumer, times(1)).accept(eq("test"));
	}
}
