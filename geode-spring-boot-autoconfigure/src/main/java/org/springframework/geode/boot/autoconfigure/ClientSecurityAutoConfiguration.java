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

package org.springframework.geode.boot.autoconfigure;

import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer;
import org.springframework.geode.core.env.VcapPropertySource;
import org.springframework.geode.core.env.support.CloudCacheService;
import org.springframework.geode.core.env.support.Service;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's Security functionality,
 * and specifically Authentication between a client and server using Spring Data Geode Security annotations.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.boot.env.EnvironmentPostProcessor
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @see org.springframework.geode.core.env.VcapPropertySource
 * @see org.springframework.geode.core.env.support.CloudCacheService
 * @see org.springframework.geode.core.env.support.Service
 * @since 1.0.0
 */
@Configuration
@AutoConfigureBefore(ClientCacheAutoConfiguration.class)
@Conditional(ClientSecurityAutoConfiguration.EnableSecurityCondition.class)
@ConditionalOnClass({ ClientCacheFactoryBean.class, ClientCache.class })
@ConditionalOnMissingBean(GemFireCache.class)
@EnableSecurity
@SuppressWarnings("unused")
public class ClientSecurityAutoConfiguration {

	public static final String SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_DISABLED_PROPERTY =
		"spring.boot.data.gemfire.security.auth.environment.post-processor.disabled";

	private static final String CLOUD_CACHE_PROPERTY_SOURCE_NAME = "cloudcache-configuration";

	private static final String MANAGEMENT_HTTP_HOST_PROPERTY = "spring.data.gemfire.management.http.host";
	private static final String MANAGEMENT_HTTP_PORT_PROPERTY = "spring.data.gemfire.management.http.port";
	private static final String MANAGEMENT_USE_HTTP_PROPERTY = "spring.data.gemfire.management.use-http";

	private static final String POOL_LOCATORS_PROPERTY = "spring.data.gemfire.pool.locators";

	private static final String SECURITY_USERNAME_PROPERTY =
		AutoConfiguredAuthenticationInitializer.SDG_SECURITY_USERNAME_PROPERTY;

	private static final String SECURITY_PASSWORD_PROPERTY =
		AutoConfiguredAuthenticationInitializer.SDG_SECURITY_PASSWORD_PROPERTY;

	private static final String VCAP_PROPERTY_SOURCE_NAME = "vcap";

	static class AutoConfiguredCloudSecurityEnvironmentPostProcessor implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

			Optional.of(environment)
				.filter(this::isEnabled)
				.filter(this::isCloudFoundryEnvironment)
				.ifPresent(env -> {

					VcapPropertySource propertySource = VcapPropertySource.from(env);

					Properties cloudCacheProperties = new Properties();

					CloudCacheService cloudCache = propertySource.findFirstCloudCacheService();

					configureAuthentication(env, cloudCacheProperties, propertySource, cloudCache);
					configureLocators(env, cloudCacheProperties, propertySource, cloudCache);
					configureManagementRestApiAccess(env, cloudCacheProperties, propertySource, cloudCache);

					environment.getPropertySources()
						.addFirst(new PropertiesPropertySource(CLOUD_CACHE_PROPERTY_SOURCE_NAME, cloudCacheProperties));
				});
		}

		private boolean isCloudFoundryEnvironment(Environment environment) {
			return Optional.ofNullable(environment).filter(CloudPlatform.CLOUD_FOUNDRY::isActive).isPresent();
		}

		private boolean isDisabled(Environment environment) {
			return Boolean.getBoolean(SECURITY_CLOUD_ENVIRONMENT_POST_PROCESSOR_DISABLED_PROPERTY);
		}

		private boolean isEnabled(Environment environment) {
			return !isDisabled(environment);
		}

		private boolean isSecurityPropertiesSet(Environment environment) {
			return environment.containsProperty(SECURITY_USERNAME_PROPERTY)
				&& environment.containsProperty(SECURITY_PASSWORD_PROPERTY);
		}

		private boolean isSecurityPropertiesNotSet(Environment environment) {
			return !isSecurityPropertiesSet(environment);
		}

		private void configureAuthentication(Environment environment, Properties cloudCacheProperties,
				VcapPropertySource propertySource, Service cloudCache) {

			propertySource.findFirstUserByRoleClusterOperator(cloudCache)
				.filter(user -> isSecurityPropertiesNotSet(environment))
				.ifPresent(user -> {
					cloudCacheProperties.setProperty(SECURITY_USERNAME_PROPERTY, user.getName());
					user.getPassword().ifPresent(password ->
						cloudCacheProperties.setProperty(SECURITY_PASSWORD_PROPERTY, password));
				});
		}

		private void configureLocators(Environment environment, Properties cloudCacheProperties,
				VcapPropertySource propertySource, CloudCacheService cloudCache) {

			cloudCache.getLocators().ifPresent(locators ->
				cloudCacheProperties.setProperty(POOL_LOCATORS_PROPERTY, locators));
		}

		private void configureManagementRestApiAccess(Environment environment, Properties cloudCacheProperties,
				VcapPropertySource propertySource, CloudCacheService cloudCache) {

			cloudCache.getGfshUrl().ifPresent(url -> {
				cloudCacheProperties.setProperty(MANAGEMENT_USE_HTTP_PROPERTY, Boolean.TRUE.toString());
				cloudCacheProperties.setProperty(MANAGEMENT_HTTP_HOST_PROPERTY, url.getHost());
				cloudCacheProperties.setProperty(MANAGEMENT_HTTP_PORT_PROPERTY, String.valueOf(url.getPort()));
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
		})
		static class StandaloneApacheGeodeSecurityContextCondition { }

	}
}
