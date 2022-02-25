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

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.internal.DistributionManager;

import org.springframework.geode.distributed.event.MembershipEvent;

/**
 * {@link MembershipEvent} fired when a {@link DistributedMember} departs from the {@link DistributedSystem}.
 *
 * @author John Blum
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.geode.distributed.event.MembershipEvent
 * @since 1.3.0
 */
public class MemberDepartedEvent extends MembershipEvent<MemberDepartedEvent> {

	private boolean crashed = false;

	/**
	 * Constructs a new instance of {@link MemberDepartedEvent} initialized with the given {@link DistributionManager}.
	 *
	 * @param distributionManager {@link DistributionManager} used as the {@link #getSource() source} of this event;
	 * must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link DistributionManager} is {@literal null}.
	 * @see org.apache.geode.distributed.internal.DistributionManager
	 */
	public MemberDepartedEvent(DistributionManager distributionManager) {
		super(distributionManager);
	}

	/**
	 * Determines whether the peer member crashed when it departed from the {@link DistributedSystem} (cluster).
	 *
	 * @return a boolean value indicating whether the peer member crashed when it departed
	 * from the {@link DistributedSystem} (cluster).
	 */
	public boolean isCrashed() {
		return this.crashed;
	}

	/**
	 * Builder method used to configure the {@link #isCrashed()} property indicating whether the peer member crashed
	 * when it departed from the {@link DistributedSystem}.
	 *
	 * @param crashed boolean value indicating whether the peer member crashed.
	 * @return this {@link MemberDepartedEvent}.
	 * @see #isCrashed()
	 */
	public MemberDepartedEvent crashed(boolean crashed) {

		this.crashed = crashed;

		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public final Type getType() {
		return Type.MEMBER_DEPARTED;
	}
}
