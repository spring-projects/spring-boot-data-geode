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
package org.springframework.geode.distributed.event.support;

import java.util.Arrays;
import java.util.Collections;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.internal.DistributionManager;

import org.springframework.geode.distributed.event.MembershipEvent;

/**
 * {@link QuorumLostEvent} is fired for the losing side of the {@link DistributedSystem} when
 * a network partition occurs.
 *
 * @author John Blum
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.geode.distributed.event.MembershipEvent
 * @since 1.3.0
 */
public class QuorumLostEvent extends MembershipEvent<QuorumLostEvent> {

	private Iterable<? extends DistributedMember> remainingMembers = Collections.emptyList();

	private Iterable<? extends DistributedMember> failedMembers = Collections.emptySet();

	/**
	 * Constructs a new instance of {@link QuorumLostEvent} initialized with the given {@link DistributionManager}.
	 *
	 * @param distributionManager {@link DistributionManager} used as the {@link #getSource() source} of this event;
	 * must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link DistributionManager} is {@literal null}.
	 * @see org.apache.geode.distributed.internal.DistributionManager
	 */
	public QuorumLostEvent(DistributionManager distributionManager) {
		super(distributionManager);
	}

	/**
	 * Gets the configured {@link Iterable} of failed {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} that are on the losing side of a network partition.
	 *
	 * @return an {@link Iterable} of failed {@link DistributedMember peer members}; never {@literal null}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getRemainingMembers()
	 * @see java.lang.Iterable
	 */
	public Iterable<? extends DistributedMember> getFailedMembers() {
		return this.failedMembers;
	}

	/**
	 * Gets the configured {@link Iterable} of remaining {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} that are on the winning side of a network partition.
	 *
	 * @return an {@link Iterable} of remaining {@link DistributedMember peer members}; never {@literal null}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getFailedMembers()
	 * @see java.lang.Iterable
	 */
	public Iterable<? extends DistributedMember> getRemainingMembers() {
		return this.remainingMembers;
	}

	/**
	 * Null-safe builder method used to configure an array of failing {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} on the losing side of a network partition.
	 *
	 * @param failedMembers array of failed {@link DistributedMember peer members}; may be {@literal null}.
	 * @return this {@link QuorumLostEvent}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #withFailedMembers(Iterable)
	 * @see #getFailedMembers()
	 */
	public QuorumLostEvent withFailedMembers(DistributedMember... failedMembers) {

		return withFailedMembers(failedMembers != null
			? Arrays.asList(failedMembers)
			: Collections.emptySet());
	}

	/**
	 * Null-safe builder method used to configure an {@link Iterable} of failing {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} on the losing side of a network partition.
	 *
	 * @param failedMembers {@link Iterable} of failed {@link DistributedMember peer members}; may be {@literal null}.
	 * @return this {@link QuorumLostEvent}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getFailedMembers()
	 * @see java.lang.Iterable
	 */
	public QuorumLostEvent withFailedMembers(Iterable<? extends DistributedMember> failedMembers) {

		this.failedMembers = failedMembers != null
			? failedMembers
			: Collections.emptySet();

		return this;
	}

	/**
	 * Null-safe builder method used to configure an array of remaining {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} on the winning side of a network partition.
	 *
	 * @param remainingMembers array of remaining {@link DistributedMember peer members}; may be {@literal null}.
	 * @return this {@link QuorumLostEvent}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #withRemainingMembers(Iterable)
	 * @see #getRemainingMembers()
	 */
	public QuorumLostEvent withRemainingMembers(DistributedMember... remainingMembers) {

		return withRemainingMembers(remainingMembers != null
			? Arrays.asList(remainingMembers)
			: Collections.emptyList());
	}

	/**
	 * Null-safe builder method used to configure an {@link Iterable} of remaining {@link DistributedMember peer members}
	 * in the {@link DistributedSystem} on the winning side of a network partition.
	 *
	 * @param remainingMembers {@link Iterable} of remaining {@link DistributedMember peer members};
	 * may be {@literal null}.
	 * @return this {@link QuorumLostEvent}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getRemainingMembers()
	 * @see java.lang.Iterable
	 */
	public QuorumLostEvent withRemainingMembers(Iterable<? extends DistributedMember> remainingMembers) {

		this.remainingMembers = remainingMembers != null
			? remainingMembers
			: Collections.emptyList();

		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public final Type getType() {
		return Type.QUORUM_LOST;
	}
}
