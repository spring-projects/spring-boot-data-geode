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
package org.springframework.geode.context.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.mockito.InOrder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import org.slf4j.Logger;

/**
 * Unit Tests for {@link EnvironmentLoggingApplicationListener}.
 *
 * @author John Blum
 * @see java.io.PrintStream
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.slf4j.Logger
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.event.ContextRefreshedEvent
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.geode.context.logging.EnvironmentLoggingApplicationListener
 * @see org.springframework.mock.env.MockEnvironment
 * @see org.springframework.mock.env.MockPropertySource
 * @since 1.4.0
 */
public class EnvironmentLoggingApplicationListenerUnitTests {

	@Test
	@SuppressWarnings("unchecked")
	public void onApplicationEventLogsEnvironmentStateCorrectly() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		ConfigurableEnvironment mockEnvironment = new MockEnvironment();

		ContextRefreshedEvent mockEvent = mock(ContextRefreshedEvent.class);

		Logger mockLogger = mock(Logger.class);

		MutablePropertySources propertySources = mockEnvironment.getPropertySources();

		propertySources.remove(MockPropertySource.MOCK_PROPERTIES_PROPERTY_SOURCE_NAME);

		mockEnvironment = spy(mockEnvironment);

		doReturn(mockApplicationContext).when(mockEvent).getApplicationContext();
		doReturn(mockEnvironment).when(mockApplicationContext).getEnvironment();

		doReturn(ArrayUtils.asArray("activeOne","activeTwo", "activeThree"))
			.when(mockEnvironment).getActiveProfiles();

		doReturn(ArrayUtils.asArray("defaultOne","defaultTwo", "defaultThree"))
			.when(mockEnvironment).getDefaultProfiles();

		MockPropertySource mockPropertySourceOne = spy(new MockPropertySource("MockPropertySourceOne")
			.withProperty("one", 1)
			.withProperty("two", 2));

		PropertySource<Map<String, Object>> mockPropertySourceTwo =
			mock(PropertySource.class, "MockPropertySourceTwo");

		doReturn("MockPropertySourceTwo").when(mockPropertySourceTwo).getName();
		doReturn(Collections.singletonMap("three", 3)).when(mockPropertySourceTwo).getSource();

		PropertySource<?> mockPropertySourceThree = mock(PropertySource.class, "MockPropertySourceThree");

		doReturn("MockPropertySourceThree").when(mockPropertySourceThree).getName();

		propertySources.addLast(mockPropertySourceOne);
		propertySources.addLast(mockPropertySourceTwo);
		propertySources.addLast(mockPropertySourceThree);

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		doReturn(mockLogger).when(listener).getLogger();
		doNothing().when(listener).logToSystemErr(anyString(), any());

		listener.onApplicationEvent(mockEvent);

		InOrder order = inOrder(mockApplicationContext, mockEnvironment, mockEvent, mockLogger,
			mockPropertySourceOne, mockPropertySourceTwo, mockPropertySourceThree);

		order.verify(mockEvent, times(1)).getApplicationContext();
		order.verify(mockApplicationContext, times(1)).getEnvironment();
		//order.verify(listener, times(1)).log(eq("ENV: [%s]"), eq(mockEnvironment.getClass().getName()));
		order.verify(mockLogger, times(1))
			.debug(eq("ENV: [" + mockEnvironment.getClass().getName() + "]"));
		order.verify(mockEnvironment, times(1)).getActiveProfiles();
		order.verify(mockLogger, times(1))
			.debug(eq("ENV: Active Profiles [activeOne, activeTwo, activeThree]"));
		order.verify(mockEnvironment, times(1)).getDefaultProfiles();
		order.verify(mockLogger, times(1))
			.debug(eq("ENV: Default Profiles [defaultOne, defaultTwo, defaultThree]"));
		order.verify(mockEnvironment, times(1)).getPropertySources();
		order.verify(mockPropertySourceOne, times(1)).getName();
		order.verify(mockLogger, times(1))
			.debug(eq("ENV: PropertySource [MockPropertySourceOne]"));
		order.verify(mockPropertySourceOne, times(1)).getPropertyNames();
		order.verify(mockLogger, times(1)).debug(eq("Properties ["));
		order.verify(mockPropertySourceOne, times(1)).getProperty(eq("one"));
		order.verify(mockLogger, times(1)).debug(eq("\tone = 1"));
		order.verify(mockPropertySourceOne, times(1)).getProperty(eq("two"));
		order.verify(mockLogger, times(1)).debug(eq("\ttwo = 2"));
		order.verify(mockLogger, times(1)).debug(eq("]"));
		order.verify(mockPropertySourceTwo, times(1)).getName();
		order.verify(mockLogger, times(1))
			.debug(eq("ENV: PropertySource [MockPropertySourceTwo]"));
		order.verify(mockPropertySourceTwo, times(1)).getSource();
		order.verify(mockLogger, times(1)).debug(eq("Properties ["));
		order.verify(mockLogger, times(1)).debug(eq("\tthree = 3"));
		order.verify(mockLogger, times(1)).debug(eq("]"));
		order.verify(mockPropertySourceThree, times(1)).getName();
		order.verify(mockLogger, times(1))
			.debug("ENV: PropertySource [MockPropertySourceThree]");
		order.verify(mockPropertySourceThree, times(1)).getSource();

		verifyNoMoreInteractions(mockEvent, mockApplicationContext, mockEnvironment,
			mockPropertySourceOne, mockPropertySourceTwo, mockPropertySourceThree, mockLogger);
	}

	@Test
	public void onApplicationEventWithNonConfigurableEnvironmentLogsState() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		ContextRefreshedEvent mockEvent = mock(ContextRefreshedEvent.class);

		Environment mockEnvironment = mock(Environment.class);

		doReturn(mockApplicationContext).when(mockEvent).getApplicationContext();
		doReturn(mockEnvironment).when(mockApplicationContext).getEnvironment();
		doReturn(ArrayUtils.asArray("mockProfile")).when(mockEnvironment).getActiveProfiles();
		doReturn(ArrayUtils.asArray("testProfile")).when(mockEnvironment).getDefaultProfiles();

		Logger mockLogger = mock(Logger.class);

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		doReturn(mockLogger).when(listener).getLogger();
		doNothing().when(listener).logToSystemErr(anyString(), any());

		listener.onApplicationEvent(mockEvent);

		verify(mockEvent, times(1)).getApplicationContext();
		verify(mockApplicationContext, times(1)).getEnvironment();
		verify(mockLogger, times(1))
			.debug(eq("ENV: [" + mockEnvironment.getClass().getName() + "]"));
		verify(mockEnvironment, times(1)).getActiveProfiles();
		verify(mockLogger, times(1)).debug(eq("ENV: Active Profiles [mockProfile]"));
		verify(mockEnvironment, times(1)).getDefaultProfiles();
		verify(mockLogger, times(1)).debug(eq("ENV: Default Profiles [testProfile]"));

		verifyNoMoreInteractions(mockApplicationContext, mockEnvironment, mockEvent, mockLogger);
	}

	@Test
	public void logCallsLogToSlf4jLoggerAndThenLogToSystemErr() {

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		doNothing().when(listener).logToSlf4jLogger(anyString(), any());
		doNothing().when(listener).logToSystemErr(anyString(), any());

		listener.log("test", 1, 2);

		InOrder order = inOrder(listener);

		order.verify(listener, times(1))
			.logToSlf4jLogger(eq("test"), eq(1), eq(2));

		order.verify(listener, times(1))
			.logToSystemErr(eq("test"), eq(1), eq(2));
	}

	@Test
	public void logDoesNothingWhenLogMessageIsEmpty() {

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		doNothing().when(listener).logToSlf4jLogger(anyString(), any());
		doNothing().when(listener).logToSystemErr(anyString(), any());

		listener.log("  ", 1, 2);

		InOrder order = inOrder(listener);

		order.verify(listener, never()).logToSlf4jLogger(anyString(), any());
		order.verify(listener, never()).logToSystemErr(anyString(), any());
	}

	@Test
	public void logToSystemErrorDoesNothingWhenNotEnabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(false)
			.when(mockEnvironment).getProperty(eq(EnvironmentLoggingApplicationListener.SYSTEM_ERR_ENABLED_PROPERTY),
			eq(Boolean.class), eq(false));

		PrintStream mockPrintStream = mock(PrintStream.class);
		PrintStream sysErr = System.err;

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		try {
			EnvironmentLoggingApplicationListener.threadLocalEnvironmentReference.set(mockEnvironment);
			System.setErr(mockPrintStream);

			listener.logToSystemErr("test", 1);

			verify(mockEnvironment, times(1))
				.getProperty(eq(EnvironmentLoggingApplicationListener.SYSTEM_ERR_ENABLED_PROPERTY),
					eq(Boolean.class), eq(false));
			verifyNoMoreInteractions(mockEnvironment);
			verifyNoInteractions(mockPrintStream);
		}
		finally {
			EnvironmentLoggingApplicationListener.threadLocalEnvironmentReference.remove();
			System.setErr(sysErr);
		}
	}

	@Test
	public void logsToSystemErrOnlyWhenEnabled() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true)
			.when(mockEnvironment).getProperty(eq(EnvironmentLoggingApplicationListener.SYSTEM_ERR_ENABLED_PROPERTY),
				eq(Boolean.class), eq(false));

		PrintStream mockPrintStream = mock(PrintStream.class);
		PrintStream sysErr = System.err;

		EnvironmentLoggingApplicationListener listener = spy(new EnvironmentLoggingApplicationListener());

		try {
			EnvironmentLoggingApplicationListener.threadLocalEnvironmentReference.set(mockEnvironment);
			System.setErr(mockPrintStream);

			listener.logToSystemErr("test", 1);

			verify(mockEnvironment, times(1))
				.getProperty(eq(EnvironmentLoggingApplicationListener.SYSTEM_ERR_ENABLED_PROPERTY),
					eq(Boolean.class), eq(false));

			verify(mockEnvironment, times(1))
				.getProperty(eq(EnvironmentLoggingApplicationListener.SYSTEM_ERR_ENABLED_PROPERTY),
					eq(Boolean.class), eq(false));
			verify(mockPrintStream, times(1)).printf(eq("test%n"), eq(1));
			verify(mockPrintStream, times(1)).flush();

			verifyNoMoreInteractions(mockEnvironment, mockPrintStream);
		}
		finally {
			EnvironmentLoggingApplicationListener.threadLocalEnvironmentReference.remove();
			System.setErr(sysErr);
		}
	}
}
