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

package org.springframework.geode.boot.actuate.health.support;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.server.ServerLoad;
import org.apache.geode.cache.server.ServerLoadProbe;
import org.apache.geode.cache.server.ServerMetrics;
import org.springframework.util.Assert;

/**
 * The ActuatorServerLoadProbeWrapper class is an implementation of Apache Geode's {@link ServerLoadProbe} interface
 * used to capture the current {@link ServerMetrics} and access the latest {@link ServerLoad} details.
 *
 * @author John Blum
 * @see org.apache.geode.cache.server.ServerLoad
 * @see org.apache.geode.cache.server.ServerLoadProbe
 * @see org.apache.geode.cache.server.ServerMetrics
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ActuatorServerLoadProbeWrapper implements ServerLoadProbe {

	private AtomicReference<ServerMetrics> currentServerMetrics = new AtomicReference<>(null);

	private final ServerLoadProbe delegate;

	/**
	 * Constructs a new instance of {@link ActuatorServerLoadProbeWrapper} initialized with the required
	 * {@link ServerLoadProbe} used as the delegate.
	 *
	 * @param serverLoadProbe required {@link ServerLoadProbe}.
	 * @throws IllegalArgumentException if {@link ServerLoadProbe} is {@literal null}.
	 * @see org.apache.geode.cache.server.ServerLoadProbe
	 */
	public ActuatorServerLoadProbeWrapper(ServerLoadProbe serverLoadProbe) {

		Assert.notNull(serverLoadProbe, "ServerLoaderProbe is required");

		this.delegate = serverLoadProbe;
	}

	/**
	 * Returns the current, most up-to-date details on the {@link ServerLoad} if possible.
	 *
	 * @return the current {@link ServerLoad}.
	 * @see org.apache.geode.cache.server.ServerLoad
	 * @see #getCurrentServerMetrics()
	 * @see java.util.Optional
	 */
	public Optional<ServerLoad> getCurrentServerLoad() {
		return getCurrentServerMetrics().map(getDelegate()::getLoad);
	}

	/**
	 * Returns the current, provided {@link ServerMetrics} if available.
	 *
	 * @return the current, provided {@link ServerMetrics} if available.
	 */
	public Optional<ServerMetrics> getCurrentServerMetrics() {
		return Optional.ofNullable(this.currentServerMetrics.get());
	}

	/**
	 * Returns the underlying, wrapped {@link ServerLoadProbe} backing this instance.
	 *
	 * @return the underlying, wrapped {@link ServerLoadProbe}.
	 * @see org.apache.geode.cache.server.ServerLoadProbe
	 */
	protected ServerLoadProbe getDelegate() {
		return this.delegate;
	}

	@Override
	public ServerLoad getLoad(ServerMetrics metrics) {

		this.currentServerMetrics.set(metrics);

		return getDelegate().getLoad(metrics);
	}

	@Override
	public void open() {
		getDelegate().open();
	}

	@Override
	public void close() {
		getDelegate().close();
	}
}
