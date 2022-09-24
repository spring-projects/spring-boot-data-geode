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
package org.springframework.geode.boot.autoconfigure;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.geode.core.env.VcapPropertySource;
import org.springframework.geode.core.env.support.CloudCacheService;
import org.springframework.geode.core.env.support.User;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's Security functionality,
 * and specifically Authentication between a client and server using Spring Data Geode Security annotations.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.AutoConfigureBefore
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.condition.AllNestedConditions
 * @see org.springframework.boot.autoconfigure.condition.AnyNestedCondition
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 * @see org.springframework.boot.cloud.CloudPlatform
 * @see org.springframework.boot.env.EnvironmentPostProcessor
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @see org.springframework.geode.core.env.VcapPropertySource
 * @see org.springframework.geode.core.env.support.CloudCacheService
 * @see org.springframework.geode.core.env.support.Service
 * @see org.springframework.geode.core.env.support.User
 * @since 1.0.0
 */
@SpringBootConfiguration
@AutoConfigureBefore(ClientCacheAutoConfiguration.class)
@Conditional(ClientSecurityAutoConfiguration.EnableSecurityCondition.class)
@ConditionalOnClass({ ClientCacheFactoryBean.class, ClientCache.class })
@ConditionalOnMissingBean(GemFireCache.class)
@EnableSecurity
//@Import(HttpBasicAuthenticationSecurityConfiguration.class)
@SuppressWarnings("unused")
public class ClientSecurityAutoConfiguration {

	public static final String CLOUD_CACHE_SERVICE_INSTANCE_NAME_PROPERTY =
		"spring.boot.data.gemfire.cloud.cloudfoundry.service.cloudcache.name";

	public static final String CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY =
		"spring.boot.data.gemfire.security.auth.environment.post-processor.enabled";

	private static final Logger logger = LoggerFactory.getLogger(ClientSecurityAutoConfiguration.class);

	private static final String CLOUD_CACHE_PROPERTY_SOURCE_NAME = "boot.data.gemfire.cloudcache";

	private static final String MANAGEMENT_HTTP_HOST_PROPERTY = "spring.data.gemfire.management.http.host";
	private static final String MANAGEMENT_HTTP_PORT_PROPERTY = "spring.data.gemfire.management.http.port";
	private static final String MANAGEMENT_REQUIRE_HTTPS_PROPERTY = "spring.data.gemfire.management.require-https";
	private static final String MANAGEMENT_USE_HTTP_PROPERTY = "spring.data.gemfire.management.use-http";

	private static final String POOL_LOCATORS_PROPERTY = "spring.data.gemfire.pool.locators";

	private static final String SECURITY_USERNAME_PROPERTY = "spring.data.gemfire.security.username";
	private static final String SECURITY_PASSWORD_PROPERTY = "spring.data.gemfire.security.password";

	private static final String SSL_USE_DEFAULT_CONTEXT_PROPERTY =
		"spring.data.gemfire.security.ssl.use-default-context";

	private static final String VCAP_PROPERTY_SOURCE_NAME = "vcap";

	public static class AutoConfiguredCloudSecurityEnvironmentPostProcessor implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

			Optional.of(environment)
				.filter(this::isEnabled)
				.filter(this::isCloudFoundryEnvironment)
				.ifPresent(this::configureSecurityContext);
		}

		private boolean isCloudFoundryEnvironment(Environment environment) {
			return CloudPlatform.CLOUD_FOUNDRY.isActive(environment);
		}

		private boolean isEnabled(Environment environment) {

			boolean clientSecurityAutoConfigurationEnabled =
				environment.getProperty(CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY,
					Boolean.class, true);

			logger.debug("{} enabled? [{}]", ClientSecurityAutoConfiguration.class.getSimpleName(),
				clientSecurityAutoConfigurationEnabled);

			return clientSecurityAutoConfigurationEnabled;
		}

		private boolean isSecurityPropertiesSet(Environment environment) {

			boolean securityPropertiesSet = environment.containsProperty(SECURITY_USERNAME_PROPERTY)
				&& environment.containsProperty(SECURITY_PASSWORD_PROPERTY);

			logger.debug("Security Properties set? [{}]", securityPropertiesSet);

			return securityPropertiesSet;
		}

		private boolean isSecurityPropertiesNotSet(Environment environment) {
			return !isSecurityPropertiesSet(environment);
		}

		private void configureAuthentication(Environment environment, VcapPropertySource vcapPropertySource,
				CloudCacheService cloudCacheService, Properties cloudCacheProperties) {

			if (isSecurityPropertiesNotSet(environment)) {
				if (environment.containsProperty(SECURITY_USERNAME_PROPERTY)) {

					String targetUsername = environment.getProperty(SECURITY_USERNAME_PROPERTY);

					vcapPropertySource.findUserByName(cloudCacheService, targetUsername)
						.flatMap(User::getPassword)
						.map(password -> {

							cloudCacheProperties.setProperty(SECURITY_USERNAME_PROPERTY, targetUsername);
							cloudCacheProperties.setProperty(SECURITY_PASSWORD_PROPERTY, password);

							return password;

						})
						.orElseThrow(() -> newIllegalStateException(
							"No User with name [%s] was configured for Cloud Cache service [%s]",
								targetUsername, cloudCacheService.getName()));
				}
				else {
					vcapPropertySource.findFirstUserByRoleClusterOperator(cloudCacheService)
						.ifPresent(user -> {

							cloudCacheProperties.setProperty(SECURITY_USERNAME_PROPERTY, user.getName());

							user.getPassword().ifPresent(password ->
								cloudCacheProperties.setProperty(SECURITY_PASSWORD_PROPERTY, password));
						});
				}
			}
		}

		private void configureLocators(Environment environment, VcapPropertySource vcapPropertySource,
				CloudCacheService cloudCacheService, Properties cloudCacheProperties) {

			cloudCacheService.getLocators().ifPresent(locators ->
				cloudCacheProperties.setProperty(POOL_LOCATORS_PROPERTY, locators));
		}

		private void configureManagementRestApiAccess(Environment environment, VcapPropertySource vcapPropertySource,
				CloudCacheService cloudCacheService, Properties cloudCacheProperties) {

			cloudCacheService.getGfshUrl().ifPresent(url -> {
				cloudCacheProperties.setProperty(MANAGEMENT_HTTP_HOST_PROPERTY, url.getHost());
				cloudCacheProperties.setProperty(MANAGEMENT_HTTP_PORT_PROPERTY, String.valueOf(url.getPort()));
				cloudCacheProperties.setProperty(MANAGEMENT_REQUIRE_HTTPS_PROPERTY, Boolean.TRUE.toString());
				cloudCacheProperties.setProperty(MANAGEMENT_USE_HTTP_PROPERTY, Boolean.TRUE.toString());
			});
		}

		private void configureSsl(Environment environment, VcapPropertySource vcapPropertySource,
				CloudCacheService cloudCacheService, Properties cloudCacheProperties) {

			if (cloudCacheService.isTlsEnabled()) {
				cloudCacheProperties.setProperty(SSL_USE_DEFAULT_CONTEXT_PROPERTY, Boolean.TRUE.toString());
			}
		}

		public void configureSecurityContext(ConfigurableEnvironment environment) {

			String cloudcacheServiceInstanceName = environment.getProperty(CLOUD_CACHE_SERVICE_INSTANCE_NAME_PROPERTY);

			VcapPropertySource vcapPropertySource = StringUtils.hasText(cloudcacheServiceInstanceName)
				? toVcapPropertySource(environment).withVcapServiceName(cloudcacheServiceInstanceName)
				: toVcapPropertySource(environment);

			vcapPropertySource.findFirstCloudCacheService()
				.map(cloudCacheService -> {

					Properties cloudCacheProperties = new Properties();

					configureAuthentication(environment, vcapPropertySource, cloudCacheService, cloudCacheProperties);
					configureLocators(environment, vcapPropertySource, cloudCacheService, cloudCacheProperties);
					configureManagementRestApiAccess(environment, vcapPropertySource, cloudCacheService, cloudCacheProperties);
					configureSsl(environment, vcapPropertySource, cloudCacheService, cloudCacheProperties);

					environment.getPropertySources()
						.addLast(newPropertySource(CLOUD_CACHE_PROPERTY_SOURCE_NAME, cloudCacheProperties));

					return cloudCacheService;
				})
				.orElseGet(() -> {

					if (StringUtils.hasText(cloudcacheServiceInstanceName)) {
						throw newIllegalStateException("No Cloud Cache service instance with name [%s] was found",
							cloudcacheServiceInstanceName);
					}
					else {
						logger.warn("No Cloud Cache service instance was found");
					}

					return null;
				});
		}

		private PropertySource<?> newPropertySource(String name, Properties properties) {
			return new SpringDataGemFirePropertiesPropertySource(name, properties);
		}

		private VcapPropertySource toVcapPropertySource(Environment environment) {
			return VcapPropertySource.from(environment);
		}
	}

	static class EnableSecurityCondition extends AllNestedConditions {

		EnableSecurityCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(name = CLOUD_SECURITY_ENVIRONMENT_POST_PROCESSOR_ENABLED_PROPERTY,
			havingValue = "true", matchIfMissing = true)
		static class SpringBootDataGemFireSecurityAuthEnvironmentPostProcessorEnabled { }

		@Conditional(SecurityTriggersCondition.class)
		static class AnySecurityTriggerCondition { }

	}

	static class SecurityTriggersCondition extends AnyNestedCondition {

		SecurityTriggersCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
		static class CloudPlatformSecurityContextCondition { }

		@ConditionalOnProperty({
			"spring.data.gemfire.security.username",
			"spring.data.gemfire.security.password",
		})
		static class SpringDataGeodeSecurityContextCondition { }

		@ConditionalOnProperty({
			"gemfire.security-username",
			"gemfire.security-password",
		})
		static class UsingApacheGeodeSecurityContextCondition { }

	}

	// This custom PropertySource is required to prevent Pivotal Spring Cloud Services
	// (spring-cloud-services-starter-service-registry) from losing the Apache Geode or Cloud Cache Security Context
	// credentials stored in the Environment.
	static class SpringDataGemFirePropertiesPropertySource extends PropertySource<Properties> {

		private static final String SPRING_DATA_GEMFIRE_PROPERTIES_PROPERTY_SOURCE_NAME =
			"spring.data.gemfire.properties";

		SpringDataGemFirePropertiesPropertySource(Properties springDataGemFireProperties) {
			this(SPRING_DATA_GEMFIRE_PROPERTIES_PROPERTY_SOURCE_NAME, springDataGemFireProperties);
		}

		SpringDataGemFirePropertiesPropertySource(String name, Properties springDataGemFireProperties) {
			super(name, springDataGemFireProperties);
		}

		@Nullable @Override
		public Object getProperty(String name) {
			return getSource().getProperty(name);
		}

		@Override
		public boolean containsProperty(String name) {
			return getSource().containsKey(name);
		}
	}
}
