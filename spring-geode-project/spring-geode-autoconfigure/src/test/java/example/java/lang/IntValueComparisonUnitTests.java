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

import org.junit.Test;

/**
 * Integration Tests testing different comparisons of {@link Integer#TYPE} values.
 *
 * @author John Blum
 * @see java.lang.Integer
 * @see org.junit.Test
 * @since 2.3.0
 */
public class IntValueComparisonUnitTests {

	@Test
	public void zeroIsEqualToNegativeZero() {
		// Of course, this better be always 'true'!
		assertThat(0).isEqualTo(-0);
	}
}
