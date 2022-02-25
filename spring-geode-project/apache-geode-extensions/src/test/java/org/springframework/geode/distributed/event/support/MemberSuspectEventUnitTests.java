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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.internal.DistributionManager;

import org.springframework.geode.distributed.event.MembershipEvent;

/**
 * Unit Tests for {@link MemberSuspectEvent}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.springframework.geode.distributed.event.MembershipEvent
 * @see org.springframework.geode.distributed.event.support.MemberSuspectEvent
 * @since 1.3.0
 */
public class MemberSuspectEventUnitTests {

	@Test
	public void constructsMemberSuspectEvent() {

		DistributionManager mockDistributionManager = mock(DistributionManager.class);

		MemberSuspectEvent event = new MemberSuspectEvent(mockDistributionManager);

		assertThat(event).isNotNull();
		assertThat(event.getDistributionManager()).isEqualTo(mockDistributionManager);
		assertThat(event.getReason().orElse(null)).isNull();
		assertThat(event.getSuspectMember().orElse(null)).isNull();
		assertThat(event.getType()).isEqualTo(MembershipEvent.Type.MEMBER_SUSPECT);

		verifyNoInteractions(mockDistributionManager);
	}

	@Test
	public void setAndGetReason() {

		DistributionManager mockDistributionManager = mock(DistributionManager.class);

		MemberSuspectEvent event = new MemberSuspectEvent(mockDistributionManager);

		assertThat(event).isNotNull();
		assertThat(event.getReason().orElse(null)).isNull();
		assertThat(event.withReason("TEST")).isSameAs(event);
		assertThat(event.getReason().orElse(null)).isEqualTo("TEST");
		assertThat(event.withReason(null)).isSameAs(event);
		assertThat(event.getReason().orElse(null)).isNull();
		assertThat(event.withReason("  ")).isSameAs(event);
		assertThat(event.getReason().orElse(null)).isEqualTo("  ");
		assertThat(event.withReason(null)).isSameAs(event);
		assertThat(event.getReason().orElse(null)).isNull();
		assertThat(event.withReason("")).isSameAs(event);
		assertThat(event.getReason().orElse(null)).isEqualTo("");
	}

	@Test
	public void setAndGetSuspectMember() {

		DistributedMember mockDistributedMember = mock(DistributedMember.class);

		DistributionManager mockDistributionManager = mock(DistributionManager.class);

		MemberSuspectEvent event = new MemberSuspectEvent(mockDistributionManager);

		assertThat(event).isNotNull();
		assertThat(event.getSuspectMember().orElse(null)).isNull();
		assertThat(event.withSuspect(mockDistributedMember)).isSameAs(event);
		assertThat(event.getSuspectMember().orElse(null)).isEqualTo(mockDistributedMember);
		assertThat(event.withSuspect(null)).isSameAs(event);
		assertThat(event.getSuspectMember().orElse(null)).isNull();
	}
}
