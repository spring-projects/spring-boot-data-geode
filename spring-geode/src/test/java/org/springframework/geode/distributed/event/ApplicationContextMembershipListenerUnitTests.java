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
package org.springframework.geode.distributed.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.geode.distributed.event.support.MemberDepartedEvent;
import org.springframework.geode.distributed.event.support.MemberJoinedEvent;

/**
 * Unit Tests for {@link ApplicationContextMembershipListener}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.geode.distributed.event.ApplicationContextMembershipListener
 * @see org.springframework.geode.distributed.event.support.MemberDepartedEvent
 * @see org.springframework.geode.distributed.event.support.MemberJoinedEvent
 * @since 1.3.0
 */
public class ApplicationContextMembershipListenerUnitTests {

	@Test
	public void constructsApplicationContextMembershipListener() {

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		ApplicationContextMembershipListener listener =
			new ApplicationContextMembershipListener(mockApplicationContext);

		assertThat(listener).isNotNull();
		assertThat(listener.getApplicationContext()).isEqualTo(mockApplicationContext);

		verifyNoInteractions(mockApplicationContext);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructsApplicationContextMembershipListenerWithNullThrowsIllegalArgumentException() {

		try {
			new ApplicationContextMembershipListener(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("ConfigurableApplicationContext must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void handleMemberDepartedCallsApplicationContextClose() {

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		MemberDepartedEvent mockEvent = mock(MemberDepartedEvent.class);

		ApplicationContextMembershipListener listener =
			spy(new ApplicationContextMembershipListener(mockApplicationContext));

		listener.handleMemberDeparted(mockEvent);

		verify(mockApplicationContext, times(1)).close();
		verifyNoMoreInteractions(mockApplicationContext);
	}

	@Test
	public void handleMemberJoinedCallApplicationContextRefresh() {

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		MemberJoinedEvent mockEvent = mock(MemberJoinedEvent.class);

		ApplicationContextMembershipListener listener =
			spy(new ApplicationContextMembershipListener(mockApplicationContext));

		listener.handleMemberJoined(mockEvent);

		verify(mockApplicationContext, times(1)).refresh();
		verifyNoMoreInteractions(mockApplicationContext);
	}
}
