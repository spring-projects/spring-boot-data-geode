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

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.geode.distributed.event.support.MemberDepartedEvent;
import org.springframework.geode.distributed.event.support.MemberJoinedEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * The {@link ApplicationContextMembershipListener} class is an extension of {@link MembershipListenerAdapter} used to
 * adapt the {@link ConfigurableApplicationContext} to handle and process {@link MembershipEvent membership events},
 * and specifically {@link MemberDepartedEvent} and {@link MemberJoinedEvent}, by
 * {@link ConfigurableApplicationContext#close() closing} and {@link ConfigurableApplicationContext#refresh() refreshing}
 * the {@link ConfigurableApplicationContext} when the {@link DistributedMember peer member} departs and joins the
 * {@link DistributedSystem cluster}.
 *
 * @author John Blum
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.geode.distributed.event.support.MemberDepartedEvent
 * @see org.springframework.geode.distributed.event.support.MemberJoinedEvent
 * @since 1.3.0
 */
public class ApplicationContextMembershipListener
		extends MembershipListenerAdapter<ApplicationContextMembershipListener> {

	private final ConfigurableApplicationContext applicationContext;

	/**
	 * Constructs a new instance of {@link ConfigurableApplicationContext} initialized with
	 * the given {@link ConfigurableApplicationContext}.
	 *
	 * @param applicationContext configured {@link ConfigurableApplicationContext}; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link ConfigurableApplicationContext} is {@literal null}.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 */
	public ApplicationContextMembershipListener(@NonNull ConfigurableApplicationContext applicationContext) {

		Assert.notNull(applicationContext, "ConfigurableApplicationContext must not be null");

		this.applicationContext = applicationContext;
	}

	/**
	 * Returns a reference to the configured {@link ConfigurableApplicationContext}.
	 *
	 * @return a reference to the configured {@link ConfigurableApplicationContext}.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 */
	protected @NonNull ConfigurableApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	/**
	 * Handles the {@link MembershipEvent membership event} when a {@link DistributedMember peer member}
	 * departs from the {@link DistributedSystem cluster} by calling {@link ConfigurableApplicationContext#close()}.
	 *
	 * @param event {@link MemberDepartedEvent} to handle.
	 * @see org.springframework.geode.distributed.event.support.MemberDepartedEvent
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	@Override
	public void handleMemberDeparted(MemberDepartedEvent event) {
		getApplicationContext().close();
	}

	/**
	 * Handles the {@link MembershipEvent membership event} when a {@link DistributedMember peer member}
	 * joins the {@link DistributedSystem cluster} by calling {@link ConfigurableApplicationContext#refresh()}.
	 *
	 * @param event {@link MemberJoinedEvent} to handle.
	 * @see org.springframework.geode.distributed.event.support.MemberJoinedEvent
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	@Override
	public void handleMemberJoined(MemberJoinedEvent event) {
		getApplicationContext().refresh();
	}
}
