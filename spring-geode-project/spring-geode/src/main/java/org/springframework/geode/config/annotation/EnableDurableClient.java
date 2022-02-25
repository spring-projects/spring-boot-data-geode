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

package org.springframework.geode.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.geode.cache.client.ClientCache;
import org.springframework.context.annotation.Import;

/**
 * The {@link EnableDurableClient} annotation configures a {@link ClientCache} instance as a {@literal Durable Client}.
 *
 * @author John Blum
 * @see java.lang.annotation.Documented
 * @see java.lang.annotation.Inherited
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.geode.config.annotation.DurableClientConfiguration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(DurableClientConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableDurableClient {

	/**
	 * Used only for clients in a client/server installation. If set, this indicates that the client is durable
	 * and identifies the client. The ID is used by servers to reestablish any messaging that was interrupted
	 * by client downtime.
	 */
	String id();

	/**
	 * Configure whether the server should keep the durable client's queues alive for the timeout period.
	 *
	 * Defaults to {@literal true}.
	 */
	boolean keepAlive() default DurableClientConfiguration.DEFAULT_KEEP_ALIVE;

	/**
	 * Configures whether the {@link ClientCache} is ready to recieve events on startup.
	 *
	 * Defaults to {@literal true}.
	 */
	boolean readyForEvents() default DurableClientConfiguration.DEFAULT_READY_FOR_EVENTS;

	/**
	 * Used only for clients in a client/server installation. Number of seconds this client can remain disconnected
	 * from its server and have the server continue to accumulate durable events for it.
	 *
	 * Defaults to {@literal 300 seconds}, or {@literal 5 minutes}.
	 */
	int timeout() default DurableClientConfiguration.DEFAULT_DURABLE_CLIENT_TIMEOUT;

}
