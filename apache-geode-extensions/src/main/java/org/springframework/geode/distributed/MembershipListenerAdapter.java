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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.distributed.internal.InternalDistributedSystem;
import org.apache.geode.distributed.internal.MembershipListener;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;

import org.springframework.geode.util.function.QuadConsumer;
import org.springframework.geode.util.function.TriConsumer;

/**
 * A {@link MembershipListener} implementation using the
 * <a href="https://en.wikipedia.org/wiki/Adapter_pattern">Adapter Software Design Pattern</a>
 * to delegate membership event callbacks to a {@link Consumer} of those membership events.
 *
 * @author John Blum
 * @see java.util.function.BiConsumer
 * @see java.util.function.Consumer
 * @see org.apache.geode.distributed.internal.DistributionManager
 * @see org.apache.geode.distributed.internal.MembershipListener
 * @see org.apache.geode.distributed.internal.membership.InternalDistributedMember
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class MembershipListenerAdapter implements MembershipListener {

	/**
	 * Factory method used to construct a new instance of the {@link MembershipListenerAdapter}.
	 *
	 * @return a new instance of {@link MembershipListenerAdapter}.
	 */
	public static MembershipListenerAdapter create() {
		return new MembershipListenerAdapter();
	}

	private TriConsumer<DistributionManager, InternalDistributedMember, Boolean> memberDepartedConsumer =
		(manager, member, crashed) -> {};

	private BiConsumer<DistributionManager, InternalDistributedMember> memberJoinedConsumer = (manager, member) -> {};

	private QuadConsumer<DistributionManager, InternalDistributedMember, InternalDistributedMember, String> memberSuspectConsumer =
		(manage, member, suspect, reason) -> {};

	private TriConsumer<DistributionManager, Set<InternalDistributedMember>, List<InternalDistributedMember>> quorumLostConsumer =
		(manager, failures, remaining) -> {};

	/**
	 * @inheritDoc
	 */
	@Override
	public void memberDeparted(DistributionManager distributionManager, InternalDistributedMember distributedMember,
			boolean crashed) {

		this.memberDepartedConsumer.accept(distributionManager, distributedMember, crashed);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void memberJoined(DistributionManager distributionManager, InternalDistributedMember distributedMember) {
		this.memberJoinedConsumer.accept(distributionManager, distributedMember);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void memberSuspect(DistributionManager distributionManager, InternalDistributedMember distributedMember,
			InternalDistributedMember whoSuspected, String reason) {

		this.memberSuspectConsumer.accept(distributionManager, distributedMember, whoSuspected, reason);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void quorumLost(DistributionManager distributionManager, Set<InternalDistributedMember> failures,
			List<InternalDistributedMember> remaining) {

		this.quorumLostConsumer.accept(distributionManager, failures, remaining);
	}

	/**
	 * Registers this {@link MembershipListener} with the given {@literal peer} {@link Cache}.
	 *
	 * @param peerCache {@literal peer} {@link Cache} on which to register this {@link MembershipListener}.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see org.apache.geode.cache.Cache
	 */
	public MembershipListenerAdapter register(Cache peerCache) {

		Optional.ofNullable(peerCache)
			.map(Cache::getDistributedSystem)
			.filter(InternalDistributedSystem.class::isInstance)
			.map(InternalDistributedSystem.class::cast)
			.map(InternalDistributedSystem::getDistributionManager)
			.ifPresent(distributionManager -> distributionManager
				.addMembershipListener(this));

		return this;
	}

	/**
	 * Null-safe builder method used to add a {@link #memberDeparted(DistributionManager, InternalDistributedMember, boolean)}
	 * {@link TriConsumer} event handler.
	 *
	 * @param memberDepartedConsumer {@link TriConsumer} handling {@literal memberDeparted} events.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see org.springframework.geode.util.function.TriConsumer
	 */
	public MembershipListenerAdapter withMemberDepartedConsumer(
			TriConsumer<DistributionManager, InternalDistributedMember, Boolean> memberDepartedConsumer) {

		if (memberDepartedConsumer != null) {
			this.memberDepartedConsumer = memberDepartedConsumer;
		}

		return this;
	}

	/**
	 * Null-safe builder method used to add a {@link #memberJoined(DistributionManager, InternalDistributedMember)}
	 * {@link BiConsumer} event handler.
	 *
	 * @param memberJoinedConsumer {@link BiConsumer} handling {@literal memberJoined} events.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see java.util.function.BiConsumer
	 */
	public MembershipListenerAdapter withMemberJoinedConsumer(
			BiConsumer<DistributionManager, InternalDistributedMember> memberJoinedConsumer) {

		if (memberJoinedConsumer != null) {
			this.memberJoinedConsumer = memberJoinedConsumer;
		}

		return this;
	}

	/**
	 * Null-safe builder method used to add a {@link #memberSuspect(DistributionManager, InternalDistributedMember, InternalDistributedMember, String)}
	 * {@link QuadConsumer} event handler.
	 *
	 * @param memberSuspectConsumer {@link QuadConsumer} handling {@literal memberSuspect} events.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see org.springframework.geode.util.function.QuadConsumer
	 */
	public MembershipListenerAdapter withMemberSuspectConsumer(
			QuadConsumer<DistributionManager, InternalDistributedMember, InternalDistributedMember, String> memberSuspectConsumer) {

		if (memberSuspectConsumer != null) {
			this.memberSuspectConsumer = memberSuspectConsumer;
		}

		return this;
	}

	/**
	 * Null-safe build method used to add a {@link #quorumLost(DistributionManager, Set, List)} {@link TriConsumer}
	 * event handler.
	 *
	 * @param quorumLostConsumer {@link TriConsumer} handling {@literal quorumLost} events.
	 * @return this {@link MembershipListenerAdapter}.
	 * @see org.springframework.geode.util.function.TriConsumer
	 */
	public MembershipListenerAdapter withQuorumLostConsumer(
			TriConsumer<DistributionManager, Set<InternalDistributedMember>, List<InternalDistributedMember>> quorumLostConsumer) {

		if (quorumLostConsumer != null) {
			this.quorumLostConsumer = quorumLostConsumer;
		}

		return this;
	}
}
