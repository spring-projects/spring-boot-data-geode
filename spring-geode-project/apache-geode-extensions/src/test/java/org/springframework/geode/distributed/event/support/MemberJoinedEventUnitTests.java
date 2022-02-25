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

import org.junit.Test;

import org.apache.geode.distributed.internal.DistributionManager;

import org.springframework.geode.distributed.event.MembershipEvent;

/**
 * Unit Tests for {@link MemberJoinedEvent}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.distributed.event.MembershipEvent
 * @see org.springframework.geode.distributed.event.support.MemberJoinedEvent
 * @since 1.3.0
 */
public class MemberJoinedEventUnitTests {

	@Test
	public void constructsMemberJoinedEvent() {

		DistributionManager mockDistributionManager = mock(DistributionManager.class);

		MemberJoinedEvent event = new MemberJoinedEvent(mockDistributionManager);

		assertThat(event).isNotNull();
		assertThat(event.getDistributionManager()).isEqualTo(mockDistributionManager);
		assertThat(event.getType()).isEqualTo(MembershipEvent.Type.MEMBER_JOINED);
	}
}
