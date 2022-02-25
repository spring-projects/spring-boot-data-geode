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

import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.distributed.internal.MembershipListener;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;

import org.springframework.geode.distributed.event.support.MemberDepartedEvent;
import org.springframework.geode.distributed.event.support.MemberJoinedEvent;
import org.springframework.geode.distributed.event.support.MemberSuspectEvent;
import org.springframework.geode.distributed.event.support.QuorumLostEvent;

/**
 * An abstract {@link MembershipListener} implementation using the
 * <a href="https://en.wikipedia.org/wiki/Adapter_pattern">Adapter Software Design Pattern</a>
 * to delegate membership event callbacks to event handlers for those membership events.
 *
 * @author John Blum
 * @param <T> specific {@link Class sub-type} of this {@link MembershipListenerAdapter}.
 * @see java.util.EventListener
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.distributed.internal.DistributionManager
 * @see org.apache.geode.distributed.internal.InternalDistributedSystem
 * @see org.apache.geode.distributed.internal.MembershipListener
 * @see org.apache.geode.distributed.internal.membership.InternalDistributedMember
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class MembershipListenerAdapter<T extends MembershipListenerAdapter<T>>
		implements EventListener, MembershipListener {

	/**
	 * @inheritDoc
	 */
	@Override
	public final void memberDeparted(DistributionManager manager, InternalDistributedMember member, boolean crashed) {

		MemberDepartedEvent event = new MemberDepartedEvent(manager)
			.withMember(member)
			.crashed(crashed);

		handleMemberDeparted(event);
	}

	public void handleMemberDeparted(MemberDepartedEvent event) { }

	/**
	 * @inheritDoc
	 */
	@Override
	public final void memberJoined(DistributionManager manager, InternalDistributedMember member) {

		MemberJoinedEvent event = new MemberJoinedEvent(manager)
			.withMember(member);

		handleMemberJoined(event);
	}

	public void handleMemberJoined(MemberJoinedEvent event) { }

	/**
	 * @inheritDoc
	 */
	@Override
	public final void memberSuspect(DistributionManager manager, InternalDistributedMember member,
			InternalDistributedMember suspectMember, String reason) {

		MemberSuspectEvent event = new MemberSuspectEvent(manager)
			.withMember(member)
			.withReason(reason)
			.withSuspect(suspectMember);

		handleMemberSuspect(event);
	}

	public void handleMemberSuspect(MemberSuspectEvent event) { }

	/**
	 * @inheritDoc
	 */
	@Override
	public final void quorumLost(DistributionManager manager, Set<InternalDistributedMember> failedMembers,
			List<InternalDistributedMember> remainingMembers) {

		QuorumLostEvent event = new QuorumLostEvent(manager)
			.withFailedMembers(failedMembers)
			.withRemainingMembers(remainingMembers);

		handleQuorumLost(event);
	}

	public void handleQuorumLost(QuorumLostEvent event) { }

	/**
	 * Registers this {@link MembershipListener} with the given {@literal peer} {@link Cache}.
	 *
	 * @param peerCache {@literal peer} {@link Cache} on which to register this {@link MembershipListener}.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see org.apache.geode.cache.Cache
	 */
	@SuppressWarnings("unchecked")
	public T register(Cache peerCache) {

		Optional.ofNullable(peerCache)
			.map(Cache::getDistributedSystem)
			.filter(InternalDistributedSystem.class::isInstance)
			.map(InternalDistributedSystem.class::cast)
			.map(InternalDistributedSystem::getDistributionManager)
			.ifPresent(distributionManager -> distributionManager
				.addMembershipListener(this));

		return (T) this;
	}
}
