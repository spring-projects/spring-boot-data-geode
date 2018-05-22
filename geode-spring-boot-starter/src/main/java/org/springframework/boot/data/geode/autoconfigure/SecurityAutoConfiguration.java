/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.boot.data.geode.autoconfigure;

import java.util.Optional;

import org.apache.geode.cache.client.ClientCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.data.geode.core.env.VcapPropertySource;
import org.springframework.boot.data.geode.core.env.support.CloudCacheService;
import org.springframework.boot.data.geode.core.env.support.Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer;
import org.springframework.util.Assert;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's Security functionality,
 * and specifically Authentication between a client and server using Spring Data Geode Security annotations.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.data.geode.autoconfigure.ClientCacheAutoConfiguration
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({ ClientCacheFactoryBean.class, ClientCache.class })
@Conditional(SecurityAutoConfiguration.EnableSecurityCondition.class)
@AutoConfigureBefore(ClientCacheAutoConfiguration.class)
@EnableSecurity
@SuppressWarnings("unused")
public class SecurityAutoConfiguration {

	private static final String MANAGEMENT_HTTP_HOST_PROPERTY = "spring.data.gemfire.management.http.host";
	private static final String MANAGEMENT_HTTP_PORT_PROPERTY = "spring.data.gemfire.management.http.port";
	private static final String MANAGEMENT_USE_HTTP_PROPERTY = "spring.data.gemfire.management.use-http";

	private static final String POOL_LOCATORS_PROPERTY = "spring.data.gemfire.pool.locators";

	private static final String SECURITY_USERNAME_PROPERTY =
		AutoConfiguredAuthenticationInitializer.SDG_SECURITY_USERNAME_PROPERTY;

	private static final String SECURITY_PASSWORD_PROPERTY =
		AutoConfiguredAuthenticationInitializer.SDG_SECURITY_PASSWORD_PROPERTY;

	private static final String VCAP_PROPERTY_SOURCE_NAME = "vcap";

	@Bean
	@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
	AutoConfiguredCloudSecurityEnvironmentProcessor securityEnvironmentProcessor(Environment environment) {
		return AutoConfiguredCloudSecurityEnvironmentProcessor.with(environment);
	}

	static class AutoConfiguredCloudSecurityEnvironmentProcessor implements InitializingBean {

		private final Environment environment;

		/**
		 * Factory method used to construct a new instance of {@link AutoConfiguredCloudSecurityEnvironmentProcessor}
		 * initialized with the given {@link Environment} from which to extract environment and contextual-based
		 * meta-data used to configure Apache Geode/Pivotal GemFire client Security (specifically Authentication).
		 *
		 * @param environment {@link Environment} to evaluate.
		 * @return a new {@link AutoConfiguredCloudSecurityEnvironmentProcessor} initialized with
		 * the given {@link Environment}.
		 * @throws IllegalArgumentException if {@link Environment} is {@literal null}.
		 * @see #AutoConfiguredCloudSecurityEnvironmentProcessor(Environment)
		 * @see org.springframework.core.env.Environment
		 */
		static AutoConfiguredCloudSecurityEnvironmentProcessor with(Environment environment) {
			return new AutoConfiguredCloudSecurityEnvironmentProcessor(environment);
		}

		/**
		 * Constructs a new instance of {@link AutoConfiguredCloudSecurityEnvironmentProcessor} initialized with
		 * the given {@link Environment} from which to extract environment and contextual-based meta-data
		 * used to configure Apache Geode/Pivotal GemFire client Security (specifically Authentication).
		 *
		 * @param environment {@link Environment} to evaluate.
		 * @throws IllegalArgumentException if {@link Environment} is {@literal null}.
		 * @see org.springframework.core.env.Environment
		 */
		AutoConfiguredCloudSecurityEnvironmentProcessor(Environment environment) {

			Assert.notNull(environment, "Environment must not be null");

			this.environment = environment;
		}

		/**
		 * Returns a reference to the configured {@link Environment}.
		 *
		 * @return a reference to the configured {@link Environment}.
		 * @see org.springframework.core.env.Environment
		 */
		Environment getEnvironment() {
			return this.environment;
		}

		@Override
		public void afterPropertiesSet() throws Exception {

			Optional.of(getEnvironment())
				.filter(this::isCloudFoundryEnvironment)
				.ifPresent(environment -> {

					VcapPropertySource propertySource = VcapPropertySource.from(environment);

					CloudCacheService cloudCache = propertySource.findFirstCloudCacheService();

					configureAuthentication(environment, propertySource, cloudCache);
					configureLocators(environment, propertySource, cloudCache);
					configureManagementRestApiAccess(environment, propertySource, cloudCache);
				});
		}

		private boolean isCloudFoundryEnvironment(Environment environment) {
			return Optional.ofNullable(environment).filter(CloudPlatform.CLOUD_FOUNDRY::isActive).isPresent();
		}

		private boolean isSecurityPropertiesSet(Environment environment) {
			return environment.containsProperty(SECURITY_USERNAME_PROPERTY)
				&& environment.containsProperty(SECURITY_PASSWORD_PROPERTY);
		}

		private boolean isSecurityPropertiesNotSet(Environment environment) {
			return !isSecurityPropertiesSet(environment);
		}

		private void configureAuthentication(Environment environment, VcapPropertySource propertySource,
				Service cloudCache) {

			propertySource.findFirstUserByRoleClusterOperator(cloudCache)
				.filter(user -> isSecurityPropertiesNotSet(environment))
				.ifPresent(user -> {
					System.setProperty(SECURITY_USERNAME_PROPERTY, user.getName());
					user.getPassword().ifPresent(password -> System.setProperty(SECURITY_PASSWORD_PROPERTY, password));
				});
		}

		private void configureLocators(Environment environment, VcapPropertySource propertySource,
				CloudCacheService cloudCache) {

			cloudCache.getLocators().ifPresent(locators -> System.setProperty(POOL_LOCATORS_PROPERTY, locators));
		}

		private void configureManagementRestApiAccess(Environment environment, VcapPropertySource propertySource,
				CloudCacheService cloudCache) {

			cloudCache.getGfshUrl().ifPresent(url -> {
				System.setProperty(MANAGEMENT_USE_HTTP_PROPERTY, Boolean.TRUE.toString());
				System.setProperty(MANAGEMENT_HTTP_HOST_PROPERTY, url.getHost());
				System.setProperty(MANAGEMENT_HTTP_PORT_PROPERTY, String.valueOf(url.getPort()));
			});
		}
	}

	static class EnableSecurityCondition extends AnyNestedCondition {

		public EnableSecurityCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
		static class CloudSecurityContextCondition { }

		@ConditionalOnProperty({
			"spring.data.gemfire.security.username",
			"spring.data.gemfire.security.password",
		})
		static class SpringDataGeodeSecurityContextCondition { }

		@ConditionalOnProperty({
			"gemfire.security-username",
			"gemfire.security-password",
			"security-username",
			"security-password"
		})
		static class StandaloneApacheGeodeSecurityContextCondition { }

	}
}
