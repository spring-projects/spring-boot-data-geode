/*
 * Copyright 2019 the original author or authors.
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

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;

/**
 * The {@link ClusterAvailableConfiguration} class is a Spring {@link Configuration @Configuration} class that enables
 * configuration when an Apache Geode or Pivotal GemFire cluster of servers are available.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @since 1.2.0
 */
@Configuration
@Conditional(ClusterAvailableConfiguration.ClusterAvailableCondition.class)
@EnableClusterConfiguration(requireHttps = false, useHttp = true)
class ClusterAvailableConfiguration {

	static final class ClusterAvailableCondition extends ClusterAwareConfiguration.ClusterAwareCondition { }

}
