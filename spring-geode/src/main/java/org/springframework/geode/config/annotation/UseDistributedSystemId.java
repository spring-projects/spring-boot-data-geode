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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * The {@link UseDistributedSystemId} annotation configures the {@literal distributed-system-id} property
 * of a {@link Cache peer Cache member} in an Apache Geode P2P topology.
 *
 * This configuration annotation is only applicable on {@link Cache peer Cache members}
 * and has no effect on {@link ClientCache} instances.
 *
 * @author John Blum
 * @see java.lang.annotation.Documented
 * @see java.lang.annotation.Inherited
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.core.annotation.AliasFor
 * @see org.springframework.geode.config.annotation.DistributedSystemIdConfiguration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(DistributedSystemIdConfiguration.class)
@SuppressWarnings("unused")
public @interface UseDistributedSystemId {

	/**
	 * Configures the identifier used to distinguish messages from different distributed systems.
	 *
	 * This is required for Portable Data eXchange (PDX) data serialization.
	 *
	 * Set distributed-system-id to different values for different systems in a multi-site (WAN) configuration,
	 * and to different values for production vs. development environments. This setting must be the same
	 * for every member of a given distributed system and unique to each distributed system within a WAN installation.
	 *
	 * Valid values are integers in the range -1…255. -1 means no setting.
	 *
	 * Defaults to {@literal -1}.
	 */
	@AliasFor("id")
	int value() default -1;

	/**
	 * Configures the identifier used to distinguish messages from different distributed systems.
	 *
	 * Alias for {@literal #value()}.
	 */
	@AliasFor("value")
	int id() default -1;

}
