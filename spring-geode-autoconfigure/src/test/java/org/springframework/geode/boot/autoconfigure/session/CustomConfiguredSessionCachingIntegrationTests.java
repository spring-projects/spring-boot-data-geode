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

package org.springframework.geode.boot.autoconfigure.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.gemfire.config.annotation.web.http.support.SpringSessionGemFireConfigurer;
import org.springframework.session.data.gemfire.serialization.SessionSerializer;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Integration Test for the auto-configuration of Spring Session using either Apache Geode or Pivotal GemFire
 * as the {@link Session} state management provider.
 *
 * This test asserts that the Spring Boot auto-configuration can be customized using either {@link Properties}
 * or a {@link SpringSessionGemFireConfigurer}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.session.Session
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.support.SpringSessionGemFireConfigurer
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CustomConfiguredSessionCachingIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

	private static final String SPRING_SESSION_DATA_GEMFIRE_PROPERTY = "spring.session.data.gemfire";

	@SuppressWarnings("unchecked")
	private <T> T invoke(Object obj, String methodName) {

		return (T) Optional.ofNullable(obj)
			.map(Object::getClass)
			.map(type -> ReflectionUtils.findMethod(type, methodName))
			.map(this::makeAccessible)
			.map(method -> ReflectionUtils.invokeMethod(method, obj))
			.orElseThrow(() -> newIllegalArgumentException("Method [%s] not found on Object of type [%s]",
				methodName, ObjectUtils.nullSafeClassName(obj)));
	}

	private Method makeAccessible(Method method) {
		ReflectionUtils.makeAccessible(method);
		return method;
	}

	private PropertySource newSpringSessionGemFireProperties() {

		return new MockPropertySource("TestSpringSessionGemFireProperties")
			.withProperty(springSessionPropertyName("cache.client.region.shortcut"), "LOCAL")
			.withProperty(springSessionPropertyName("session.attributes.indexable"), "one, two")
			.withProperty(springSessionPropertyName("session.expiration.max-inactive-interval-seconds"), "600")
			.withProperty(springSessionPropertyName("cache.client.pool.name"), "MockPool")
			.withProperty(springSessionPropertyName("session.region.name"), "MockRegion")
			.withProperty(springSessionPropertyName("cache.server.region.shortcut"), "REPLICATE")
			.withProperty(springSessionPropertyName("session.serializer.bean-name"), "MockSessionSerializer");
	}

	@Override
	protected ConfigurableApplicationContext processBeforeRefresh(ConfigurableApplicationContext applicationContext) {

		applicationContext.getEnvironment().getPropertySources().addFirst(newSpringSessionGemFireProperties());

		return applicationContext;
	}

	private String springSessionPropertyName(String propertyNameSuffix) {
		return String.format("%1$s.%2$s", SPRING_SESSION_DATA_GEMFIRE_PROPERTY, propertyNameSuffix);
	}

	@Test
	public void springSessionConfigurationCustomizedWithConfigurer() {

		newApplicationContext(TestConfiguration.class, SpringSessionGemFireConfigurerTestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(this.<ClientRegionShortcut>invoke(sessionConfiguration, "getClientRegionShortcut"))
			.isEqualTo(ClientRegionShortcut.CACHING_PROXY);

		assertThat(this.<String[]>invoke(sessionConfiguration, "getIndexableSessionAttributes"))
			.contains("two", "four");

		assertThat(this.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(900);

		assertThat(this.<String>invoke(sessionConfiguration, "getPoolName")).isEqualTo("TestPool");

		assertThat(this.<RegionShortcut>invoke(sessionConfiguration, "getServerRegionShortcut"))
			.isEqualTo(RegionShortcut.PARTITION_REDUNDANT_PERSISTENT_OVERFLOW);

		assertThat(this.<String>invoke(sessionConfiguration, "getSessionRegionName"))
			.isEqualTo("TestRegion");

		assertThat(this.<String>invoke(sessionConfiguration, "getSessionSerializerBeanName"))
			.isEqualTo("TestSessionSerializer");
	}

	@Test
	public void springSessionConfigurationCustomizedWithProperties() {

		newApplicationContext(TestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(this.<ClientRegionShortcut>invoke(sessionConfiguration, "getClientRegionShortcut"))
			.isEqualTo(ClientRegionShortcut.LOCAL);

		assertThat(this.<String[]>invoke(sessionConfiguration, "getIndexableSessionAttributes"))
			.contains("one", "two");

		assertThat(this.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(600);

		assertThat(this.<String>invoke(sessionConfiguration, "getPoolName")).isEqualTo("MockPool");

		assertThat(this.<RegionShortcut>invoke(sessionConfiguration, "getServerRegionShortcut"))
			.isEqualTo(RegionShortcut.REPLICATE);

		assertThat(this.<String>invoke(sessionConfiguration, "getSessionRegionName"))
			.isEqualTo("MockRegion");

		assertThat(this.<String>invoke(sessionConfiguration, "getSessionSerializerBeanName"))
			.isEqualTo("MockSessionSerializer");
	}

	@SpringBootConfiguration
	@EnableGemFireMockObjects
	@EnableAutoConfiguration(exclude = ContinuousQueryAutoConfiguration.class)
	//@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class)
	static class TestConfiguration {

		@Bean("MockSessionSerializer")
		@SuppressWarnings("unchecked")
		SessionSerializer<Session, ?, ?> mockSessionSerializer() {
			return mock(SessionSerializer.class);
		}
	}

	@Configuration
	static class SpringSessionGemFireConfigurerTestConfiguration {

		@Bean
		SpringSessionGemFireConfigurer customSpringSessionGemFireConfiguration() {

			return new SpringSessionGemFireConfigurer() {

				@Override
				public ClientRegionShortcut getClientRegionShortcut() {
					return ClientRegionShortcut.CACHING_PROXY;
				}

				@Override
				public String[] getIndexableSessionAttributes() {
					return new String[] { "two", "four" };
				}

				@Override
				public int getMaxInactiveIntervalInSeconds() {
					return Long.valueOf(TimeUnit.MINUTES.toSeconds(15L)).intValue();
				}

				@Override
				public String getPoolName() {
					return "TestPool";
				}

				@Override
				public String getRegionName() {
					return "TestRegion";
				}

				@Override
				public RegionShortcut getServerRegionShortcut() {
					return RegionShortcut.PARTITION_REDUNDANT_PERSISTENT_OVERFLOW;
				}

				@Override
				public String getSessionSerializerBeanName() {
					return "TestSessionSerializer";
				}
			};
		}

		@Bean("TestSessionSerializer")
		@SuppressWarnings("unchecked")
		SessionSerializer<Session, ?, ?> testSessionSerializer() {
			return mock(SessionSerializer.class);
		}
	}
}
