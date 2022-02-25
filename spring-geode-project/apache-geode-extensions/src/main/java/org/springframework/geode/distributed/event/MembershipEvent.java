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

import java.util.EventObject;
import java.util.Optional;

import org.apache.geode.cache.Cache;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.internal.DistributionManager;

/**
 * {@link EventObject} implementation indicating a membership event in the {@link DistributedSystem}.
 *
 * @author John Blum
 * @param <T> specific {@link Class type} of {@link MembershipEvent}.
 * @see java.util.EventObject
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.apache.geode.distributed.internal.DistributionManager
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public abstract class MembershipEvent<T extends MembershipEvent<T>> extends EventObject {

	private DistributedMember distributedMember;

	/**
	 * Asserts that the given {@link Object target} is not {@literal null}.
	 *
	 * @param <T> {@link Class type} of the {@link Object target}.
	 * @param target {@link Object} to evaluate.
	 * @param message {@link String} containing the message for the {@link IllegalArgumentException}.
	 * @param arguments array of {@link Object arguments} to populate the placeholders in the {@link String message}.
	 * @return the {@link Object target}.
	 * @throws IllegalArgumentException if {@link Object target} is {@literal null}.
	 */
	protected static <T> T assertNotNull(T target, String message, Object... arguments) {

		if (target == null) {
			throw new IllegalArgumentException(String.format(message, arguments));
		}

		return target;
	}

	/**
	 * Constructs a new instance of {@link MembershipEvent} initialized with the given {@link DistributionManager}.
	 *
	 * @param distributionManager {@link DistributionManager} used to acquire the {@link Cache}, which is used
	 * as the {@literal source} of this event.
	 * @throws IllegalArgumentException if {@link DistributionManager} is {@literal null}.
	 * @see org.apache.geode.distributed.internal.DistributionManager
	 */
	public MembershipEvent(DistributionManager distributionManager) {
		super(assertNotNull(distributionManager, "DistributionManager must not be null"));
	}

	/**
	 * Returns an {@link Optional} reference to the {@literal peer} {@link Cache}.
	 *
	 * @return an {@link Optional} reference to the {@literal peer} {@link Cache}.
	 * @see org.apache.geode.cache.Cache
	 * @see #getDistributionManager()
	 * @see java.util.Optional
	 */
	public Optional<Cache> getCache() {
		return Optional.ofNullable(getDistributionManager().getCache());
	}

	/**
	 * Returns an {@link Optional} reference to the {@link DistributedMember} that is the subject
	 * of this {@link MembershipEvent}.
	 *
	 * @return an {@link Optional} reference to the {@link DistributedMember} that is the subject of this event.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getDistributionManager()
	 * @see java.util.Optional
	 */
	public Optional<DistributedMember> getDistributedMember() {
		return Optional.ofNullable(this.distributedMember);
	}

	/**
	 * Returns an {@link Optional} reference to the {@link DistributedSystem} (cluster) to which the {@literal peer}
	 * {@link Cache} is connected.
	 *
	 * @return an {@link Optional} reference to the {@link DistributedSystem}.
	 * @see org.apache.geode.distributed.DistributedSystem
	 * @see #getDistributionManager()
	 * @see java.util.Optional
	 */
	public Optional<DistributedSystem> getDistributedSystem() {
		return Optional.ofNullable(getDistributionManager().getSystem());
	}

	/**
	 * Returns a reference to the configured {@link DistributionManager} which is use as the {@link #getSource() source}
	 * of this event.
	 *
	 * @return a reference to the {@link DistributionManager}.
	 * @see org.apache.geode.distributed.internal.DistributionManager
	 */
	public DistributionManager getDistributionManager() {
		return (DistributionManager) getSource();
	}

	/**
	 * Returns the {@link Type} of this {@link MembershipEvent}, such as {@link Type#MEMBER_JOINED}.
	 *
	 * @return the {@link MembershipEvent.Type}.
	 */
	public Type getType() {
		return Type.UNQUALIFIED;
	}

	/**
	 * Null-safe builder method used to configure the {@link DistributedMember member} that is the subject
	 * of this event.
	 *
	 * @param distributedMember {@link DistributedMember} that is the subject of this event.
	 * @return this {@link MembershipEvent}.
	 * @see org.apache.geode.distributed.DistributedMember
	 * @see #getDistributedMember()
	 */
	@SuppressWarnings("unchecked")
	public T withMember(DistributedMember distributedMember) {

		this.distributedMember = distributedMember;

		return (T) this;
	}

	/**
	 * An {@link Enum enumeration} of different type of {@link MembershipEvent MembershipEvents}.
	 */
	public enum Type {

		MEMBER_DEPARTED,
		MEMBER_JOINED,
		MEMBER_SUSPECT,
		QUORUM_LOST,
		UNQUALIFIED;

	}
}
