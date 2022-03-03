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

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit Tests for {@link InvocationArguments).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.util.function.InvocationArguments
 * @since 1.3.0
 */
public class InvocationArgumentsUnitTests {

	@Test
	public void constructsInvocationArgumentsWithArguments() {

		Object[] arguments = { true, 'c', 1, Math.PI, "test" };

		InvocationArguments invocationArguments = new InvocationArguments(arguments);

		assertThat(invocationArguments).isNotNull();
		assertThat(invocationArguments.size()).isEqualTo(arguments.length);
		assertThat(Arrays.equals(invocationArguments.getArguments(), arguments)).isTrue();
		assertThat(invocationArguments.<Boolean>getArgumentAt(0)).isEqualTo(true);
		assertThat(invocationArguments.<Character>getArgumentAt(1)).isEqualTo('c');
		assertThat(invocationArguments.<Integer>getArgumentAt(2)).isEqualTo(1);
		assertThat(invocationArguments.<Double>getArgumentAt(3)).isEqualTo(Math.PI);
		assertThat(invocationArguments.<String>getArgumentAt(4)).isEqualTo("test");
	}

	@Test
	public void fromConstructsNewInvocationArguments() {

		InvocationArguments arguments = InvocationArguments.from("test", 1, false);

		assertThat(arguments).isNotNull();
		assertThat(arguments).hasSize(3);
		assertThat(arguments.size()).isEqualTo(3);
		assertThat(arguments).containsExactly("test", 1, false);
		assertThat(arguments.<String>getArgumentAt(0)).isEqualTo("test");
		assertThat(arguments.<Integer>getArgumentAt(1)).isEqualTo(1);
		assertThat(arguments.<Boolean>getArgumentAt(2)).isEqualTo(false);
	}

	@Test
	public void constructInvocationArgumentsWithNull() {

		InvocationArguments arguments = new InvocationArguments(null);

		assertThat(arguments).isNotNull();
		assertThat(arguments).hasSize(0);
		assertThat(arguments.getArguments()).isNotNull();
	}

	@Test
	public void iteratesArguments() {

		Object[] arguments = { true, 1, "test" };

		InvocationArguments invocationArguments = new InvocationArguments(arguments);

		int index = 0;

		for (Object argument : invocationArguments) {
			assertThat(argument).isEqualTo(arguments[index++]);
		}
	}

	@Test
	public void toStringIsCorrect() {

		InvocationArguments arguments = InvocationArguments.from("one", "two", "three");

		assertThat(arguments).isNotNull();
		assertThat(arguments.toString()).isEqualTo("[one, two, three]");
	}
}
