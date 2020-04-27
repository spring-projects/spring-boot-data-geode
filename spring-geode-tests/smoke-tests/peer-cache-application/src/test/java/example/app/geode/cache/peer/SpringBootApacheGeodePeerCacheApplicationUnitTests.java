/*
 * Copyright 2020 the original author or authors.
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
package example.app.geode.cache.peer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.distributed.event.ApplicationContextMembershipListener;
import org.springframework.geode.distributed.event.MembershipListenerAdapter;
import org.springframework.geode.distributed.event.support.MemberDepartedEvent;
import org.springframework.geode.distributed.event.support.MemberJoinedEvent;
import org.springframework.geode.test.context.TestRefreshableApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests for {@link SpringBootApacheGeodePeerCacheApplication}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.support.AbstractRefreshableApplicationContext
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.geode.test.context.TestRefreshableApplicationContextLoader
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@RunWith(SpringRunner.class)
@EnableGemFireMockObjects
@ContextConfiguration(
	classes = SpringBootApacheGeodePeerCacheApplication.class,
	loader = TestRefreshableApplicationContextLoader.class
)
@SuppressWarnings({ "unused" })
public class SpringBootApacheGeodePeerCacheApplicationUnitTests {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	@Qualifier("applicationContextMembershipListener")
	private MembershipListenerAdapter<ApplicationContextMembershipListener> membershipListener;

	@Before
	public void setup() {

		//System.err.printf("ApplicationContext Type [%s]%n", ObjectUtils.nullSafeClassName(this.applicationContext));

		assertThat(this.applicationContext).isInstanceOf(AbstractRefreshableApplicationContext.class);
	}

	@Test
	public void memberFartedAndDepartedThenJoinedAndWasPurloined() {

		MemberDepartedEvent mockMemberDepartedEvent = mock(MemberDepartedEvent.class);
		MemberJoinedEvent mockMemberJoinedEvent = mock(MemberJoinedEvent.class);

		Cache peerCache = this.applicationContext.getBean(Cache.class);

		assertThat(peerCache).isNotNull();

		this.membershipListener.handleMemberDeparted(mockMemberDepartedEvent);

		assertThat(this.applicationContext.isActive()).isFalse();
		assertThat(this.applicationContext.isRunning()).isFalse();

		this.membershipListener.handleMemberJoined(mockMemberJoinedEvent);

		assertThat(this.applicationContext.isActive()).isTrue();
		assertThat(this.applicationContext.isRunning()).isTrue();

		Cache reconnectedPeerCache = this.applicationContext.getBean(Cache.class);

		assertThat(reconnectedPeerCache).isNotNull();
		assertThat(reconnectedPeerCache).isNotSameAs(peerCache);
	}
}
