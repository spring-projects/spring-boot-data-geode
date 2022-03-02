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

package example.java.lang;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Unit test for {@link Object#toString()} with {@link Optional} of {@literal null}.
 *
 * @author John Blum
 * @see java.lang.Object
 * @see java.util.Optional
 * @since 1.0.0
 */
public class ObjectToStringWithOptionalUnitTests {

	@Test
	@SuppressWarnings("all")
	public void objectToStringWithOptionalOfNull() {

		assertThat(Optional.ofNullable(null).map(Object::toString).orElse("test"))
			.isEqualTo("test");
	}

	@Test(expected = NullPointerException.class)
	public void objectToStringWithStreamOfElementsContainingNullThrowsNullPointerException() {

		assertThat(Stream.of(1, null, 3).map(Object::toString).collect(Collectors.toList()))
			.containsExactly("1", "null", "3");
	}

	@Test
	public void stringValueOfWithStreamOfElementsContainingNullIsNullSafe() {

		assertThat(Stream.of(1, null, 3).map(String::valueOf).collect(Collectors.toList()))
			.containsExactly("1", "null", "3");
	}
}
