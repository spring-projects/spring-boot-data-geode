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
package org.springframework.geode.logging.slf4j.logback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.helpers.NOPAppender;

/**
 * Unit Tests for {@link DelegatingAppender}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.springframework.geode.logging.slf4j.logback.DelegatingAppender
 * @since 1.3.0
 */
public class DelegatingAppenderUnitTests {

	@Test
	public void delegatingAppenderDefaultsNameToDelegate() {
		assertThat(new DelegatingAppender<>().getName()).isEqualTo(DelegatingAppender.DEFAULT_NAME);
	}

	@Test
	public void delegatingAppenderDefaultsToNoOpAppender() {
		assertThat(new DelegatingAppender<>().getAppender()).isInstanceOf(NOPAppender.class);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void delegatingAppenderDelegatesToMockAppender() {

		Appender mockAppender = mock(Appender.class);

		DelegatingAppender delegatingAppender = new DelegatingAppender<>();

		delegatingAppender.setAppender(mockAppender);

		assertThat(delegatingAppender.getAppender()).isSameAs(mockAppender);

		delegatingAppender.append("TEST");

		verify(mockAppender, times(1)).doAppend(eq("TEST"));
	}
}
