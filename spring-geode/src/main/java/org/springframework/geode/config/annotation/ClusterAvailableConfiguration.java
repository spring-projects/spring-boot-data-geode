/*
 * Copyright 2020 the original author or authors.
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

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;

/**
 * The {@link ClusterAvailableConfiguration} class is a Spring {@link Configuration} class that enables configuration
 * when an Apache Geode cluster of servers are available.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.condition.AnyNestedCondition
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @since 1.2.0
 */
@Configuration
@Conditional(ClusterAvailableConfiguration.AnyClusterAvailableCondition.class)
@EnableClusterConfiguration(requireHttps = false, useHttp = true)
@SuppressWarnings("unused")
public class ClusterAvailableConfiguration {

	public static final class AnyClusterAvailableCondition extends AnyNestedCondition {

		public AnyClusterAvailableCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@Conditional(ClusterAvailableCondition.class)
		static class IsClusterAvailableCondition { }

		@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
		static class IsCloudFoundryEnvironmentCondition { }

		@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
		static class IsKubernetesEnvironmentCondition { }

	}

	public static final class ClusterAvailableCondition extends ClusterAwareConfiguration.ClusterAwareCondition { }

}
