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

import static org.springframework.data.gemfire.util.CollectionUtils.asSet;

import java.time.Duration;
import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.GemFireCache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.geode.boot.autoconfigure.support.EnableSubscriptionConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for configuring Apache Geode
 * as an (HTTP) {@link Session} state management provider in Spring Session.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.AutoConfigureAfter
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.geode.boot.autoconfigure.support.EnableSubscriptionConfiguration
 * @see org.springframework.session.Session
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.session.web.http.SessionRepositoryFilter
 * @since 1.0.0
 */
@SpringBootConfiguration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@Conditional(SpringSessionAutoConfiguration.SpringSessionStoreTypeCondition.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ GemFireCache.class, GemFireHttpSessionConfiguration.class })
@ConditionalOnMissingBean(SessionRepositoryFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableGemFireHttpSession(poolName = "DEFAULT")
@Import(EnableSubscriptionConfiguration.class)
@SuppressWarnings("unused")
public class SpringSessionAutoConfiguration {

	protected static final Set<String> SPRING_SESSION_STORE_TYPES = asSet("gemfire", "geode");

	protected static final String SERVER_SERVLET_SESSION_TIMEOUT_PROPERTY = "server.servlet.session.timeout";
	protected static final String SPRING_SESSION_DATA_GEMFIRE_SESSION_EXPIRATION_TIMEOUT =
		"spring.session.data.gemfire.session.expiration.max-inactive-interval-seconds";
	protected static final String SPRING_SESSION_PROPERTY_SOURCE_NAME = "SpringSessionProperties";
	protected static final String SPRING_SESSION_STORE_TYPE_PROPERTY = "spring.session.store-type";
	protected static final String SPRING_SESSION_TIMEOUT_PROPERTY = "spring.session.timeout";

	public static class SpringSessionPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

			if (isNotSet(environment, SPRING_SESSION_DATA_GEMFIRE_SESSION_EXPIRATION_TIMEOUT)) {

				Properties springSessionProperties = new Properties();

				if (isSet(environment, SPRING_SESSION_TIMEOUT_PROPERTY)) {
					springSessionProperties.setProperty(SPRING_SESSION_DATA_GEMFIRE_SESSION_EXPIRATION_TIMEOUT,
						toSecondsAsString(environment.getProperty(SPRING_SESSION_TIMEOUT_PROPERTY, Duration.class,
							getDefaultSessionTimeout())));
				}
				else if (isSet(environment, SERVER_SERVLET_SESSION_TIMEOUT_PROPERTY)) {
					springSessionProperties.setProperty(SPRING_SESSION_DATA_GEMFIRE_SESSION_EXPIRATION_TIMEOUT,
						toSecondsAsString(environment.getProperty(SERVER_SERVLET_SESSION_TIMEOUT_PROPERTY,
							Duration.class, getDefaultSessionTimeout())));
				}

				if (!springSessionProperties.isEmpty()) {
					environment.getPropertySources()
						.addFirst(newPropertySource(SPRING_SESSION_PROPERTY_SOURCE_NAME, springSessionProperties));
				}
			}
		}

		private PropertySource<?> newPropertySource(String name, Properties properties) {
			return new PropertiesPropertySource(name, properties);
		}
	}

	protected static @NonNull Duration getDefaultSessionTimeout() {
		return Duration.ofSeconds(GemFireHttpSessionConfiguration.DEFAULT_MAX_INACTIVE_INTERVAL_IN_SECONDS);
	}

	protected static int toSeconds(@Nullable Duration duration) {

		return duration != null ? Long.valueOf(duration.getSeconds()).intValue()
			: GemFireHttpSessionConfiguration.DEFAULT_MAX_INACTIVE_INTERVAL_IN_SECONDS;
	}

	protected static @NonNull String toSecondsAsString(@Nullable Duration duration) {
		return String.valueOf(toSeconds(duration));
	}

	protected static boolean isNotSet(ConfigurableEnvironment environment, String propertyName) {
		return !isSet(environment, propertyName);
	}

	protected static boolean isSet(ConfigurableEnvironment environment, String propertyName) {

		return environment.containsProperty(propertyName)
			&& StringUtils.hasText(environment.getProperty(propertyName));
	}

	protected static class SpringSessionStoreTypeCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			String springSessionStoreTypeValue =
				context.getEnvironment().getProperty(SPRING_SESSION_STORE_TYPE_PROPERTY);

			return !StringUtils.hasText(springSessionStoreTypeValue)
				|| SPRING_SESSION_STORE_TYPES.contains(springSessionStoreTypeValue.trim().toLowerCase());
		}
	}
}
