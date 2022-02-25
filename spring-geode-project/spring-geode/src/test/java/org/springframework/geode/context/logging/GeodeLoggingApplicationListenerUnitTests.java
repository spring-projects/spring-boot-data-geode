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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.web.context.WebApplicationContext;

/**
 * Unit Tests for {@link GeodeLoggingApplicationListener}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.context.logging.GeodeLoggingApplicationListener
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class GeodeLoggingApplicationListenerUnitTests extends IntegrationTestsSupport {

	@Mock
	private ApplicationEnvironmentPreparedEvent mockEvent;

	@Mock
	private ConfigurableEnvironment mockEnvironment;

	@Spy
	private GeodeLoggingApplicationListener listener;

	@Before
	public void setup() {
		doReturn(this.mockEnvironment).when(this.mockEvent).getEnvironment();
	}

	@After
	public void tearDown() {

		System.getProperties().stringPropertyNames().stream()
			.filter(propertyName -> propertyName.startsWith("spring"))
			.forEach(System::clearProperty);
	}

	@Test
	public void getOrderReturnsHigherPrecedenceThanSpringBootLoggingApplicationListenerOrderByDefault() {
		assertThat(this.listener.getOrder()).isLessThan(new LoggingApplicationListener().getOrder());
	}

	@Test
	public void onApplicationEventIsNullSafe() {

		this.listener.onApplicationEvent(null);

		verify(this.listener, never()).onApplicationEnvironmentPreparedEvent(any());
	}

	@Test
	public void onApplicationEventWithApplicationEnvironmentPreparedEventProcessesEvent() {

		doNothing().when(this.listener)
			.onApplicationEnvironmentPreparedEvent(any(ApplicationEnvironmentPreparedEvent.class));

		this.listener.onApplicationEvent(this.mockEvent);

		verify(this.listener, times(1))
			.onApplicationEnvironmentPreparedEvent(eq(this.mockEvent));
	}

	@Test
	public void onApplicationEventWithNonApplicationEnvironmentPreparedEventWillNotProcessEvent() {

		ApplicationEvent mockEvent = mock(ApplicationEvent.class);

		this.listener.onApplicationEvent(mockEvent);

		verify(this.listener, never()).onApplicationEnvironmentPreparedEvent(any());
	}

	@Test
	public void onApplicationEnvironmentPreparedEventConfiguresApacheGeodeLoggingFromSpringBootDataGemFireLogLevelProperty() {

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isNullOrEmpty();

		doReturn("DEBUG").when(this.mockEnvironment)
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY),
				ArgumentMatchers.<String>any());

		this.listener.onApplicationEnvironmentPreparedEvent(this.mockEvent);

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isEqualTo("DEBUG");

		verify(this.mockEnvironment, times(1))
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_DATA_GEMFIRE_LOGGING_LOG_LEVEL),
				ArgumentMatchers.<String>eq(null));
	}

	@Test
	public void onApplicationEnvironmentPreparedEventConfiguresApacheGeodeLoggingFromSpringDataGemFireCacheLogLevelProperty() {

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isNullOrEmpty();

		doReturn("DEBUG").when(this.mockEnvironment)
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_DATA_GEMFIRE_CACHE_LOG_LEVEL));

		Arrays.asList(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY,
			GeodeLoggingApplicationListener.SPRING_DATA_GEMFIRE_LOGGING_LOG_LEVEL).forEach(propertyName ->
				doAnswer(invocation -> invocation.getArgument(1, String.class))
					.when(this.mockEnvironment).getProperty(eq(propertyName), anyString()));

		this.listener.onApplicationEnvironmentPreparedEvent(this.mockEvent);

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isEqualTo("DEBUG");

		verify(this.mockEnvironment, times(1))
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY),
				eq("DEBUG"));
	}

	@Test
	public void onApplicationEnvironmentPreparedEventConfiguresApacheGeodeLoggingFromSpringDataGemFireLoggingLevelProperty() {

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isNullOrEmpty();

		doReturn("DEBUG").when(this.mockEnvironment)
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_DATA_GEMFIRE_LOGGING_LOG_LEVEL),
				ArgumentMatchers.<String>any());

		doAnswer(invocation -> invocation.getArgument(1, String.class)).when(this.mockEnvironment)
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY), anyString());

		this.listener.onApplicationEnvironmentPreparedEvent(this.mockEvent);

		assertThat(System.getProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY))
			.isEqualTo("DEBUG");

		verify(this.mockEnvironment, times(1))
			.getProperty(eq(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY),
				eq("DEBUG"));
	}

	@Test
	public void onApplicationEnvironmentPreparedEventWillNotConfigureApacheGeodeLoggingWhenLogLevelIsExplicitlyConfigured() {

		System.setProperty(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY, "OFF");

		this.listener.onApplicationEnvironmentPreparedEvent(this.mockEvent);

		verify(this.listener, never())
			.setSystemProperty(eq(GeodeLoggingApplicationListener.SPRING_BOOT_DATA_GEMFIRE_LOG_LEVEL_PROPERTY), any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void onApplicationEnvironmentPreparedEventWithNullEventThrowsIllegalArgumentException() {

		try {
			this.listener.onApplicationEnvironmentPreparedEvent(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("ApplicationEnvironmentPreparedEvent must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void isSystemPropertySetIsNullSafe() {
		assertThat(this.listener.isSystemPropertySet(null)).isFalse();
	}

	@Test
	public void isSystemPropertySetReturnsTrue() {

		System.setProperty("spring.test.property", "TEST");

		assertThat(this.listener.isSystemPropertySet("spring.test.property")).isTrue();
	}

	@Test
	public void isSystemPropertyNotSetIsNullSafe() {
		assertThat(this.listener.isSystemPropertyNotSet(null)).isTrue();
	}

	@Test
	public void isSystemPropertySetReturnsFalse() {

		System.setProperty("spring.test.property.one", "  ");
		System.setProperty("spring.test.property.two", "");

		assertThat(this.listener.isSystemPropertySet("spring.non-existing.property")).isFalse();
		assertThat(this.listener.isSystemPropertySet("spring.test.property.one")).isFalse();
		assertThat(this.listener.isSystemPropertySet("spring.test.property.two")).isFalse();
	}

	@Test
	public void isSystemPropertyNotSetReturnsTrue() {

		System.setProperty("spring.test.property.one", "  ");
		System.setProperty("spring.test.property.two", "");

		assertThat(this.listener.isSystemPropertyNotSet("spring.non-existing.property")).isTrue();
		assertThat(this.listener.isSystemPropertyNotSet("spring.test.property.one")).isTrue();
		assertThat(this.listener.isSystemPropertyNotSet("spring.test.property.two")).isTrue();
	}

	@Test
	public void isSystemPropertyNotSetReturnFalse() {

		System.setProperty("spring.test.property", "TEST");

		assertThat(this.listener.isSystemPropertyNotSet("spring.test.property")).isFalse();
	}

	@Test
	public void setSystemPropertyWithValue() {

		assertThat(System.getProperty("spring.test.property")).isNull();

		this.listener.setSystemProperty("spring.test.property", "TEST");

		assertThat(System.getProperty("spring.test.property")).isEqualTo("TEST");
	}

	@Test
	public void setSystemPropertyWithNoValue() {

		this.listener.setSystemProperty("spring.test.property.one", null);
		this.listener.setSystemProperty("spring.test.property.two", "");
		this.listener.setSystemProperty("spring.test.property.three", "  ");

		assertThat(System.getProperty("spring.test.property.one")).isNull();
	}

	public void testSetSystemPropertyWithNoPropertyNameThrowsIllegalArgumentException(String propertyName) {

		try {
			this.listener.setSystemProperty(propertyName, "TEST");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("PropertyName [%s] is required", propertyName);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void setSystemPropertyWithBlankPropertyNameThrowsIllegalArgumentException() {
		testSetSystemPropertyWithNoPropertyNameThrowsIllegalArgumentException("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setSystemPropertyWithEmptyPropertyNameThrowsIllegalArgumentException() {
		testSetSystemPropertyWithNoPropertyNameThrowsIllegalArgumentException("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setSystemPropertyWithNullPropertyNameThrowsIllegalArgumentException() {
		testSetSystemPropertyWithNoPropertyNameThrowsIllegalArgumentException(null);
	}

	@Test
	public void supportsEventTypeReturnsTrue() {
		assertThat(this.listener.supportsEventType(ResolvableType.forClass(ApplicationEnvironmentPreparedEvent.class)))
			.isTrue();
	}

	@Test
	public void supportsEventTypeReturnsFalse() {

		assertThat(this.listener.supportsEventType(ResolvableType.NONE)).isFalse();
		assertThat(this.listener.supportsEventType(ResolvableType.forClass(ApplicationEvent.class))).isFalse();
		assertThat(this.listener.supportsEventType(ResolvableType.forClass(Object.class))).isFalse();
	}

	@Test
	public void supportsSourceTypeReturnsTrue() {

		assertThat(this.listener.supportsSourceType(ApplicationContext.class)).isTrue();
		assertThat(this.listener.supportsSourceType(SpringApplication.class)).isTrue();
		assertThat(this.listener.supportsSourceType(WebApplicationContext.class)).isTrue();
	}

	@Test
	public void supportSourceTypeReturnsFalse() {

		assertThat(this.listener.supportsSourceType(null)).isFalse();
		assertThat(this.listener.supportsSourceType(Object.class)).isFalse();
		assertThat(this.listener.supportsSourceType(BeanFactory.class)).isFalse();
		assertThat(this.listener.supportsSourceType(SpringApplicationBuilder.class)).isFalse();
	}
}
