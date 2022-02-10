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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;

/**
 * Unit Tests for {@link CompositeAppender}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.geode.logging.slf4j.logback.CompositeAppender
 * @see ch.qos.logback.core.Appender
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompositeAppenderUnitTests {

	@Mock
	private Appender mockAppenderOne;

	@Mock
	private Appender mockAppenderTwo;

	@Test
	public void composeIsNullSafe() {
		assertThat(CompositeAppender.compose(null, null)).isNull();
	}

	@Test
	public void composeSingleAppenderReturnsTheAppender() {

		assertThat(CompositeAppender.compose(this.mockAppenderOne, null)).isEqualTo(this.mockAppenderOne);
		assertThat(CompositeAppender.compose(null, this.mockAppenderTwo)).isEqualTo(this.mockAppenderTwo);
	}

	@Test
	public void composeTwoAppendersReturnsCompositeAppender() {

		Appender<?> composite = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(composite).isInstanceOf(CompositeAppender.class);
		assertThat((((CompositeAppender<?>) composite).getAppenderOne())).isEqualTo(this.mockAppenderOne);
		assertThat((((CompositeAppender<?>) composite).getAppenderTwo())).isEqualTo(this.mockAppenderTwo);
		assertThat(composite.getName()).isEqualTo(CompositeAppender.DEFAULT_NAME);
		assertThat(composite.isStarted()).isTrue();
	}

	@Test
	public void composeAppenderArray() {

		Appender mockAppenderOne = mock(Appender.class);
		Appender mockAppenderTwo = mock(Appender.class);
		Appender mockAppenderThree = mock(Appender.class);

		Appender composite = CompositeAppender.compose(mockAppenderOne, mockAppenderTwo, mockAppenderThree);

		assertThat(composite).isInstanceOf(CompositeAppender.class);

		Appender appenderOne = ((CompositeAppender) composite).getAppenderOne();
		Appender appenderTwo = ((CompositeAppender) composite).getAppenderTwo();

		assertThat(appenderOne).isInstanceOf(CompositeAppender.class);
		assertThat(appenderTwo).isEqualTo(mockAppenderThree);
		assertThat(((CompositeAppender) appenderOne).getAppenderOne()).isEqualTo(mockAppenderOne);
		assertThat(((CompositeAppender) appenderOne).getAppenderTwo()).isEqualTo(mockAppenderTwo);
	}

	@Test
	public void composeAppenderArrayWithOneAppender() {
		assertThat(CompositeAppender.compose(this.mockAppenderOne)).isSameAs(this.mockAppenderOne);
	}

	@Test
	public void composeAppenderArrayWithZeroAppenders() {
		assertThat(CompositeAppender.compose()).isNull();
	}

	@Test
	public void composeAppenderArrayIsNullSafe() {
		assertThat(CompositeAppender.compose((Appender[]) null)).isNull();
	}

	@Test
	public void composeAppenderIterable() {

		Appender mockAppenderOne = mock(Appender.class);
		Appender mockAppenderTwo = mock(Appender.class);
		Appender mockAppenderThree = mock(Appender.class);

		Appender composite =
			CompositeAppender.compose(Arrays.asList(mockAppenderOne, mockAppenderTwo, mockAppenderThree));

		assertThat(composite).isInstanceOf(CompositeAppender.class);

		Appender appenderOne = ((CompositeAppender) composite).getAppenderOne();
		Appender appenderTwo = ((CompositeAppender) composite).getAppenderTwo();

		assertThat(appenderOne).isInstanceOf(CompositeAppender.class);
		assertThat(appenderTwo).isEqualTo(mockAppenderThree);
		assertThat(((CompositeAppender) appenderOne).getAppenderOne()).isEqualTo(mockAppenderOne);
		assertThat(((CompositeAppender) appenderOne).getAppenderTwo()).isEqualTo(mockAppenderTwo);
	}

	@Test
	public void composeAppenderIterableWithOneAppender() {
		assertThat(CompositeAppender.compose(Collections.singletonList(this.mockAppenderTwo)))
			.isSameAs(this.mockAppenderTwo);
	}

	@Test
	public void composeAppenderIterableWithZeroAppenders() {
		assertThat(CompositeAppender.compose(Collections.emptyList())).isNull();
	}

	@Test
	public void composeAppenderIterableIsNullSafe() {
		assertThat(CompositeAppender.compose((Iterable<Appender<Object>>) null)).isNull();
	}

	@Test
	public void setContextConfiguresContextOnCompositeAppenderAndComposedAppenders() {

		Context mockContext = mock(Context.class);

		Appender compositeAppender = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);

		compositeAppender.setContext(mockContext);

		assertThat(compositeAppender.getContext()).isEqualTo(mockContext);

		verify(this.mockAppenderOne, times(1)).setContext(eq(mockContext));
		verify(this.mockAppenderTwo, times(1)).setContext(eq(mockContext));
	}

	@Test
	public void getContextReturnsConfiguredContext() {

		Context mockContext = mock(Context.class);

		Appender compositeAppender = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);

		compositeAppender.setContext(mockContext);

		assertThat(compositeAppender.getContext()).isEqualTo(mockContext);

		verify(this.mockAppenderOne, never()).getContext();
		verify(this.mockAppenderTwo, never()).getContext();
	}

	@Test
	public void getContextReturnsAppenderOneContext() {

		Context mockContext = mock(Context.class);

		doReturn(mockContext).when(this.mockAppenderOne).getContext();

		Appender compositeAppender = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);
		assertThat(compositeAppender.getContext()).isEqualTo(mockContext);

		verify(this.mockAppenderOne, times(1)).getContext();
		verify(this.mockAppenderTwo, never()).getContext();
	}

	@Test
	public void getContextReturnsAppenderTwoContext() {

		Context mockContext = mock(Context.class);

		doReturn(mockContext).when(this.mockAppenderTwo).getContext();

		Appender compositeAppender = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);
		assertThat(compositeAppender.getContext()).isEqualTo(mockContext);

		verify(this.mockAppenderOne, times(1)).getContext();
		verify(this.mockAppenderTwo, times(1)).getContext();
	}

	@Test
	public void appendCallsAppenderOneAppendAndAppenderTwoAppend() {

		Appender compositeAppender = CompositeAppender.compose(this.mockAppenderOne, this.mockAppenderTwo);

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);

		((CompositeAppender<Object>) compositeAppender).append("TEST");

		verify(this.mockAppenderOne, times(1)).doAppend(eq("TEST"));
		verify(this.mockAppenderTwo, times(1)).doAppend(eq("TEST"));
	}
}
