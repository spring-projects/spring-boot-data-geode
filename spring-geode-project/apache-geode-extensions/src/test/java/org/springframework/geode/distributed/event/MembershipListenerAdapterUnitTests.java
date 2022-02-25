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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;

import org.springframework.geode.distributed.event.support.MemberDepartedEvent;
import org.springframework.geode.distributed.event.support.MemberJoinedEvent;
import org.springframework.geode.distributed.event.support.MemberSuspectEvent;
import org.springframework.geode.distributed.event.support.QuorumLostEvent;

/**
 * Unit Tests for {@link MembershipListenerAdapter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.apache.geode.distributed.internal.DistributionManager
 * @see org.apache.geode.distributed.internal.membership.InternalDistributedMember
 * @see MembershipListenerAdapter
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MembershipListenerAdapterUnitTests {

	@Mock
	private DistributionManager mockDistributionManager;

	@Mock
	private InternalDistributedMember mockDistributedMember;

	@Mock
	private InternalDistributedSystem mockDistributedSystem;

	@SuppressWarnings("unchecked")
	private <T> Set<T> asSet(T... elements) {
		return new HashSet<>(Arrays.asList(elements));
	}

	@Test
	public void memberDepartedCallsHandleMemberDeparted() {

		MembershipListenerAdapter<?> listener = spy(new TestMembershipListener());

		doAnswer(invocation -> {

			MemberDepartedEvent event = invocation.getArgument(0);

			assertThat(event).isNotNull();
			assertThat(event.isCrashed()).isTrue();
			assertThat(event.getDistributedMember().orElse(null)).isEqualTo(this.mockDistributedMember);
			assertThat(event.getDistributionManager()).isEqualTo(this.mockDistributionManager);
			assertThat(event.getType()).isEqualTo(MembershipEvent.Type.MEMBER_DEPARTED);

			return null;

		}).when(listener).handleMemberDeparted(any(MemberDepartedEvent.class));

		listener.memberDeparted(this.mockDistributionManager, this.mockDistributedMember, true);

		verify(listener, times(1)).handleMemberDeparted(isA(MemberDepartedEvent.class));
	}

	@Test
	public void memberJoinedCallsHandleMemberJoined() {

		MembershipListenerAdapter<?> listener = spy(new TestMembershipListener());

		doAnswer(invocation -> {

			MemberJoinedEvent event = invocation.getArgument(0);

			assertThat(event).isNotNull();
			assertThat(event.getDistributedMember().orElse(null)).isEqualTo(this.mockDistributedMember);
			assertThat(event.getDistributionManager()).isEqualTo(this.mockDistributionManager);
			assertThat(event.getType()).isEqualTo(MembershipEvent.Type.MEMBER_JOINED);

			return null;

		}).when(listener).handleMemberJoined(any(MemberJoinedEvent.class));

		listener.memberJoined(this.mockDistributionManager, this.mockDistributedMember);

		verify(listener, times(1)).handleMemberJoined(isA(MemberJoinedEvent.class));
	}

	@Test
	public void memberSuspectCallsHandleMemberSuspect() {

		InternalDistributedMember suspectMember = mock(InternalDistributedMember.class);

		MembershipListenerAdapter<?> listener = spy(new TestMembershipListener());

		doAnswer(invocation -> {

			MemberSuspectEvent event = invocation.getArgument(0);

			assertThat(event).isNotNull();
			assertThat(event.getDistributedMember().orElse(null)).isEqualTo(this.mockDistributedMember);
			assertThat(event.getDistributionManager()).isEqualTo(this.mockDistributionManager);
			assertThat(event.getReason().orElse(null)).isEqualTo("The system sucks!");
			assertThat(event.getSuspectMember().orElse(null)).isEqualTo(suspectMember);
			assertThat(event.getType()).isEqualTo(MembershipEvent.Type.MEMBER_SUSPECT);

			return null;

		}).when(listener).handleMemberSuspect(any(MemberSuspectEvent.class));

		listener.memberSuspect(this.mockDistributionManager, this.mockDistributedMember, suspectMember,
			"The system sucks!");

		verify(listener, times(1)).handleMemberSuspect(isA(MemberSuspectEvent.class));
	}

	@Test
	public void quorumLostCallsHandleQuorumLost() {

		InternalDistributedMember mockMemberOne = mock(InternalDistributedMember.class);
		InternalDistributedMember mockMemberTwo = mock(InternalDistributedMember.class);

		MembershipListenerAdapter<?> listener = spy(new TestMembershipListener());

		doAnswer(invocation -> {

			QuorumLostEvent event = invocation.getArgument(0);

			assertThat(event).isNotNull();
			assertThat(event.getDistributedMember().orElse(null)).isNull();
			assertThat(event.getDistributionManager()).isEqualTo(this.mockDistributionManager);
			assertThat(event.getFailedMembers()).isEqualTo(asSet(mockMemberOne, mockMemberTwo));
			assertThat(event.getRemainingMembers()).isEqualTo(Collections.singletonList(this.mockDistributedMember));
			assertThat(event.getType()).isEqualTo(MembershipEvent.Type.QUORUM_LOST);

			return null;

		}).when(listener).handleQuorumLost(any(QuorumLostEvent.class));

		listener.quorumLost(this.mockDistributionManager, asSet(mockMemberOne, mockMemberTwo),
			Collections.singletonList(this.mockDistributedMember));

		verify(listener, times(1)).handleQuorumLost(isA(QuorumLostEvent.class));
	}

	@Test
	public void registersListenerWithPeerCache() {

		Cache mockCache = mock(Cache.class);

		doReturn(this.mockDistributedSystem).when(mockCache).getDistributedSystem();
		doReturn(this.mockDistributionManager).when(this.mockDistributedSystem).getDistributionManager();

		MembershipListenerAdapter<?> listener = new TestMembershipListener();

		assertThat(listener.register(mockCache)).isSameAs(listener);

		verify(mockCache, times(1)).getDistributedSystem();
		verify(this.mockDistributedSystem, times(1)).getDistributionManager();
		verify(this.mockDistributionManager, times(1))
			.addMembershipListener(eq(listener));
	}

	@Test
	public void registerListenerWithNullCacheIsNullSafe() {

		MembershipListenerAdapter<?> listener = new TestMembershipListener();

		assertThat(listener.register(null)).isSameAs(listener);
	}

	@Test
	public void registerListenerWithNullDistributedSystemIsNullSafe() {

		Cache mockCache = mock(Cache.class);

		MembershipListenerAdapter<?> listener = new TestMembershipListener();

		assertThat(listener.register(mockCache)).isSameAs(listener);

		verify(mockCache, times(1)).getDistributedSystem();
	}

	@Test
	public void registerListenerWithNonInternalDistributedSystemIsSafe() {

		Cache mockCache = mock(Cache.class);

		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

		doReturn(mockDistributedSystem).when(mockCache).getDistributedSystem();

		MembershipListenerAdapter<?> listener = new TestMembershipListener();

		assertThat(listener.register(mockCache)).isSameAs(listener);

		verify(mockCache, times(1)).getDistributedSystem();
		verifyNoInteractions(mockDistributedSystem);
	}

	@Test
	public void registerListenerWithNullDistributionManagerIsNullSafe() {

		Cache mockCache = mock(Cache.class);

		doReturn(this.mockDistributedSystem).when(mockCache).getDistributedSystem();

		MembershipListenerAdapter<?> listener = new TestMembershipListener();

		assertThat(listener.register(mockCache)).isSameAs(listener);

		verify(mockCache, times(1)).getDistributedSystem();
		verify(this.mockDistributedSystem, times(1)).getDistributionManager();
	}

	static class TestMembershipListener extends MembershipListenerAdapter<TestMembershipListener> { }

}
