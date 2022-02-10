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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;

/**
 * Unit Tests for {@link StringAppender}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.geode.logging.slf4j.logback.StringAppender
 * @see ch.qos.logback.classic.Logger
 * @see ch.qos.logback.core.Appender
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class StringAppenderUnitTests {

	@Mock
	private StringAppender.StringAppenderWrapper mockWrapper;

	@After
	public void tearDown() {
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).detachAndStopAllAppenders();
	}

	@Test
	public void buildStringAppender() {

		StringAppender stringAppender = new StringAppender.Builder().build();

		assertThat(stringAppender).isNotNull();
		assertThat(stringAppender.isStarted()).isFalse();
		assertThat(stringAppender.getContext()).isEqualTo(LoggerFactory.getILoggerFactory());
		assertThat(stringAppender.getName()).isEqualTo(StringAppender.DEFAULT_NAME);
		assertThat(stringAppender.getStringAppenderWrapper())
			.isInstanceOf(StringAppender.StringBuilderAppenderWrapper.class);
	}

	@Test
	public void buildStringAppenderApplyToDelegateUsingReplace() {

		DelegatingAppender delegate = spy(new DelegatingAppender());

		StringAppender stringAppender = new StringAppender.Builder()
			.applyTo(delegate, true)
			.build();

		assertThat(stringAppender).isNotNull();
		assertThat(stringAppender.isStarted()).isFalse();
		assertThat(stringAppender.getName()).isEqualTo(StringAppender.DEFAULT_NAME);

		verify(delegate, times(1)).setAppender(eq(stringAppender));

		assertThat(delegate.getAppender()).isEqualTo(stringAppender);
	}

	@Test
	public void buildAndStartStringAppender() {

		Context mockContext = mock(Context.class);

		DelegatingAppender delegate = spy(new DelegatingAppender());

		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		StringAppender stringAppender = new StringAppender.Builder()
			.applyTo(delegate)
			.applyTo(rootLogger)
			.setContext(mockContext)
			.setName("TestStringAppender")
			.useSynchronization()
			.buildAndStart();

		assertThat(stringAppender).isNotNull();
		assertThat(stringAppender.isStarted()).isTrue();
		assertThat(stringAppender.getContext()).isEqualTo(mockContext);
		assertThat(stringAppender.getName()).isEqualTo("TestStringAppender");
		assertThat(stringAppender.getStringAppenderWrapper())
			.isInstanceOf(StringAppender.StringBufferAppenderWrapper.class);
		assertThat(rootLogger.getAppender("TestStringAppender")).isEqualTo(stringAppender);

		verify(delegate, times(1)).setAppender(isA(CompositeAppender.class));

		Appender compositeAppender = delegate.getAppender();

		assertThat(compositeAppender).isInstanceOf(CompositeAppender.class);
		assertThat(((CompositeAppender) compositeAppender).getAppenderOne()).isEqualTo(DelegatingAppender.DEFAULT_APPENDER);
		assertThat(((CompositeAppender) compositeAppender).getAppenderTwo()).isEqualTo(stringAppender);
	}

	@Test
	public void constructStringAppender() {

		StringAppender.StringAppenderWrapper mockWrapper = mock(StringAppender.StringAppenderWrapper.class);

		StringAppender appender = new StringAppender(mockWrapper);

		assertThat(appender).isNotNull();
		assertThat(appender.getStringAppenderWrapper()).isEqualTo(mockWrapper);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructStringAppenderWithNullWrapper() {

		try {
			new StringAppender(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("StringAppenderWrapper must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void appendCallsConfiguredStringAppenderWrapper() {

		ILoggingEvent mockEvent = mock(ILoggingEvent.class);

		StringAppender appender = spy(new StringAppender(this.mockWrapper));

		doReturn(this.mockWrapper).when(appender).getStringAppenderWrapper();
		doReturn("TEST").when(appender).toString(eq(mockEvent));

		appender.append(mockEvent);

		verify(this.mockWrapper, times(1)).append(eq("TEST"));
	}

	@Test
	public void appendIgnoresBlankEmptyAndNullStrings() {

		ILoggingEvent mockEvent = mock(ILoggingEvent.class);

		StringAppender appender = spy(new StringAppender(this.mockWrapper));

		doReturn(this.mockWrapper).when(appender).getStringAppenderWrapper();
		doReturn("  ").doReturn("").doReturn(null)
			.when(appender).toString(eq(mockEvent));

		appender.append(mockEvent);
		appender.append(mockEvent);
		appender.append(mockEvent);

		verify(appender, times(3)).toString(eq(mockEvent));
		verify(appender, times(1)).preProcessLogMessage(eq("  "));
		verify(appender, times(1)).preProcessLogMessage(eq(""));
		verify(appender, times(1)).preProcessLogMessage(eq(null));
		verify(this.mockWrapper, never()).append(any());
	}

	@Test
	public void appendIsNullSafe() {

		StringAppender appender = spy(new StringAppender(this.mockWrapper));

		doReturn(this.mockWrapper).when(appender).getStringAppenderWrapper();

		appender.append(null);

		verify(appender, never()).toString(any(ILoggingEvent.class));
		verify(this.mockWrapper, never()).append(any());
	}

	@Test
	public void emptyStringIsNotValidLogMessage() {

		StringAppender appender = new StringAppender(this.mockWrapper);

		assertThat(appender.isValidLogMessage("")).isFalse();
		assertThat(appender.isValidLogMessage(null)).isFalse();	}

	@Test
	public void nonEmptyStringIsValidLogMessage() {

		StringAppender appender = new StringAppender(this.mockWrapper);

		assertThat(appender.isValidLogMessage("TEST")).isTrue();
		assertThat(appender.isValidLogMessage("_")).isTrue();
		assertThat(appender.isValidLogMessage("  ")).isTrue();
	}

	@Test
	public void preProcessLogMessageIsNullSafe() {
		assertThat(new StringAppender(this.mockWrapper).preProcessLogMessage(null)).isNull();
	}

	@Test
	public void preProcessLogMessageTrimsMessage() {

		StringAppender appender = new StringAppender(this.mockWrapper);

		assertThat(appender.preProcessLogMessage("TEST")).isEqualTo("TEST");
		assertThat(appender.preProcessLogMessage("  MOCK ")).isEqualTo("MOCK");
		assertThat(appender.preProcessLogMessage("J UNK ")).isEqualTo("J UNK");
	}

	@Test
	public void toStringCallsLoggingEventFormattedMessage() {

		ILoggingEvent mockEvent = mock(ILoggingEvent.class);

		when(mockEvent.getFormattedMessage()).thenReturn("TEST");

		assertThat(new StringAppender(this.mockWrapper).toString(mockEvent)).isEqualTo("TEST");

		verify(mockEvent, times(1)).getFormattedMessage();
	}

	@Test
	public void toStringIsNullSafe() {
		assertThat(new StringAppender(this.mockWrapper).toString(null)).isNull();
	}

	@Test
	public void getLogOutputCallsStringAppenderWrapperToString() {

		doReturn("TEST").when(this.mockWrapper).toString();

		assertThat(new StringAppender(this.mockWrapper).getLogOutput()).isEqualTo("TEST");
	}
}
