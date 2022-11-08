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
package org.springframework.web.servlet.http;

import java.time.Duration;
import java.time.Instant;

import jakarta.servlet.http.HttpSession;

import org.springframework.util.StringUtils;

/**
 * Abstract base class supporting implementations of the {@link HttpSession} interface.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see java.time.Instant
 * @see jakarta.servlet.http.HttpSession
 * @since 1.4.0
 */
public abstract class AbstractHttpSession implements HttpSession {

	private Duration maxInactiveInterval = Duration.ofMinutes(30);

	private final Instant creationTime = Instant.now();

	@Override
	public long getCreationTime() {
		return this.creationTime.toEpochMilli();
	}

	@Override
	public int getMaxInactiveInterval() {
		return Long.valueOf(this.maxInactiveInterval.toSeconds()).intValue();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		int resolvedInterval = interval > 0 ? interval : Integer.MAX_VALUE;
		this.maxInactiveInterval = Duration.ofSeconds(resolvedInterval);
	}

	@Override
	public boolean isNew() {
		return !StringUtils.hasText(getId());
	}
}
