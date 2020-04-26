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
package org.springframework.geode.distributed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;

import org.springframework.geode.util.function.QuadConsumer;
import org.springframework.geode.util.function.TriConsumer;

/**
 * Unit Tests for {@link MembershipListenerAdapter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.apache.geode.distributed.internal.DistributionManager
 * @see org.apache.geode.distributed.internal.membership.InternalDistributedMember
 * @see org.springframework.geode.distributed.MembershipListenerAdapter
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MembershipListenerAdapterUnitTests {

	@Mock
	private DistributionManager mockDistributionManager;

	@Mock
	private InternalDistributedMember mockDistributedMember;

	@Test
	public void createConstructsNewMembershipListenerAdapterWithNoOpEventHandlers() {

		InternalDistributedMember mockSuspectMember = mock(InternalDistributedMember.class);

		MembershipListenerAdapter membershipListener = MembershipListenerAdapter.create();

		assertThat(membershipListener).isNotNull();

		membershipListener.memberDeparted(this.mockDistributionManager, this.mockDistributedMember, true);
		membershipListener.memberJoined(this.mockDistributionManager, this.mockDistributedMember);
		membershipListener.memberSuspect(this.mockDistributionManager, this.mockDistributedMember, mockSuspectMember,
			"System is unstable!!");
		membershipListener.quorumLost(this.mockDistributionManager, Collections.singleton(mockSuspectMember),
			Collections.singletonList(this.mockDistributedMember));

		verifyNoInteractions(this.mockDistributedMember);
		verifyNoInteractions(this.mockDistributedMember);
		verifyNoInteractions(mockSuspectMember);
	}

	@Test
	public void registersListenerWithPeerCache() {

		Cache mockCache = mock(Cache.class);

		DistributionManager mockDistributionManager = mock(DistributionManager.class);

		InternalDistributedSystem mockDistributedSystem = mock(InternalDistributedSystem.class);

		doReturn(mockDistributedSystem).when(mockCache).getDistributedSystem();
		doReturn(mockDistributionManager).when(mockDistributedSystem).getDistributionManager();

		MembershipListenerAdapter membershipListener = new MembershipListenerAdapter();

		assertThat(membershipListener.register(mockCache)).isSameAs(membershipListener);

		verify(mockCache, times(1)).getDistributedSystem();
		verify(mockDistributedSystem, times(1)).getDistributionManager();
		verify(mockDistributionManager, times(1)).addMembershipListener(eq(membershipListener));
	}

	@Test
	public void registerListenerWithNullCacheIsNullSafe() {

		MembershipListenerAdapter membershipListener = new MembershipListenerAdapter();

		assertThat(membershipListener.register(null)).isSameAs(membershipListener);
	}

	@Test
	public void registerListenerWithNullDistributedSystemIsNullSafe() {

		Cache mockCache = mock(Cache.class);

		MembershipListenerAdapter membershipListener = new MembershipListenerAdapter();

		assertThat(membershipListener.register(mockCache)).isSameAs(membershipListener);

		verify(mockCache, times(1)).getDistributedSystem();
	}

	@Test
	public void registerListenerWithNonInternalDistributedSystemIsSafe() {

		Cache mockCache = mock(Cache.class);

		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

		MembershipListenerAdapter membershipListener = new MembershipListenerAdapter();

		assertThat(membershipListener.register(mockCache)).isSameAs(membershipListener);

		verify(mockCache, times(1)).getDistributedSystem();
		verifyNoInteractions(mockDistributedSystem);
	}

	@Test
	public void registerListenerWithNullDistributionManagerIsNullSafe() {

		Cache mockCache = mock(Cache.class);

		InternalDistributedSystem mockDistributedSystem = mock(InternalDistributedSystem.class);

		doReturn(mockDistributedSystem).when(mockCache).getDistributedSystem();

		MembershipListenerAdapter membershipListener = new MembershipListenerAdapter();

		assertThat(membershipListener.register(mockCache)).isSameAs(membershipListener);

		verify(mockCache, times(1)).getDistributedSystem();
		verify(mockDistributedSystem, times(1)).getDistributionManager();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withMemberDepartedEventHandler() {

		TriConsumer<DistributionManager, InternalDistributedMember, Boolean> mockConsumer = mock(TriConsumer.class);

		MembershipListenerAdapter membershipListener = MembershipListenerAdapter.create()
			.withMemberDepartedConsumer(mockConsumer);

		assertThat(membershipListener).isNotNull();

		membershipListener.memberDeparted(this.mockDistributionManager, this.mockDistributedMember, true);

		verify(mockConsumer, times(1))
			.accept(eq(this.mockDistributionManager), eq(this.mockDistributedMember), eq(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withMemberJoinedEventHandler() {

		BiConsumer<DistributionManager, InternalDistributedMember> mockConsumer = mock(BiConsumer.class);

		MembershipListenerAdapter membershipListener = MembershipListenerAdapter.create()
			.withMemberJoinedConsumer(mockConsumer);

		assertThat(membershipListener).isNotNull();

		membershipListener.memberJoined(this.mockDistributionManager, this.mockDistributedMember);

		verify(mockConsumer, times(1))
			.accept(eq(this.mockDistributionManager), eq(this.mockDistributedMember));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withMemberSuspectEventHandler() {

		QuadConsumer<DistributionManager, InternalDistributedMember, InternalDistributedMember, String> mockConsumer =
			mock(QuadConsumer.class);

		InternalDistributedMember mockSuspectMember = mock(InternalDistributedMember.class);

		MembershipListenerAdapter membershipListener = MembershipListenerAdapter.create()
			.withMemberSuspectConsumer(mockConsumer);

		assertThat(membershipListener).isNotNull();

		membershipListener.memberSuspect(this.mockDistributionManager, this.mockDistributedMember, mockSuspectMember,
			"System is a lost cause!!");

		verify(mockConsumer, times(1))
			.accept(eq(this.mockDistributionManager), eq(this.mockDistributedMember), eq(mockSuspectMember),
				eq("System is a lost cause!!"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void withQuorumLostEventHandler() {

		TriConsumer<DistributionManager, Set<InternalDistributedMember>, List<InternalDistributedMember>> mockConsumer
			= mock(TriConsumer.class);

		MembershipListenerAdapter membershipListener = MembershipListenerAdapter.create()
			.withQuorumLostConsumer(mockConsumer);

		assertThat(membershipListener).isNotNull();

		membershipListener.quorumLost(this.mockDistributionManager, Collections.singleton(this.mockDistributedMember),
			Collections.singletonList(this.mockDistributedMember));

		verify(mockConsumer, times(1)).accept(eq(this.mockDistributionManager),
			eq(Collections.singleton(this.mockDistributedMember)),
				eq(Collections.singletonList(this.mockDistributedMember)));
	}
}
