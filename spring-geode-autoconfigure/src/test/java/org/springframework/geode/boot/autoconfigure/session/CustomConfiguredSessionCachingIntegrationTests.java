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
package org.springframework.geode.boot.autoconfigure.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.tests.integration.SpringBootApplicationIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.gemfire.config.annotation.web.http.support.SpringSessionGemFireConfigurer;
import org.springframework.session.data.gemfire.serialization.SessionSerializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Integration Test for the auto-configuration of Spring Session using either Apache Geode
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
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.SpringBootApplicationIntegrationTestsSupport
 * @see org.springframework.session.Session
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.support.SpringSessionGemFireConfigurer
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class CustomConfiguredSessionCachingIntegrationTests extends SpringBootApplicationIntegrationTestsSupport {

	private static final String SPRING_SESSION_DATA_GEMFIRE_PROPERTY = "spring.session.data.gemfire";

	private static Properties singletonProperties(String propertyName, String propertyValue) {

		Properties properties = new Properties();

		properties.setProperty(propertyName, propertyValue);

		return properties;
	}

	private volatile Function<ConfigurableApplicationContext, ConfigurableApplicationContext> applicationContextFunction =
		Function.identity();

	private volatile Function<ConfigurableApplicationContext, ConfigurableApplicationContext> mockServletContextFunction =
		applicationContext -> {

			Optional.ofNullable(applicationContext)
				.filter(ConfigurableWebApplicationContext.class::isInstance)
				.map(ConfigurableWebApplicationContext.class::cast)
				.ifPresent(it -> it.setServletContext(new MockServletContext()));

			return applicationContext;
		};

	private volatile Function<SpringApplicationBuilder, SpringApplicationBuilder> springApplicationBuilderFunction =
		Function.identity();

	private Function<SpringApplicationBuilder, SpringApplicationBuilder> newSpringBootSessionPropertiesConfigurationFunction() {

		return springApplicationBuilder ->
			springApplicationBuilder.properties(singletonProperties("spring.session.timeout", "300s"));
	}

	private Function<ConfigurableApplicationContext, ConfigurableApplicationContext> newSpringSessionGemFirePropertiesConfigurationFunction() {

		return applicationContext -> {

			PropertySource<?> springSessionGemFireProperties = new MockPropertySource("TestSpringSessionGemFireProperties")
				.withProperty(springSessionPropertyName("cache.client.region.shortcut"), "LOCAL")
				.withProperty(springSessionPropertyName("session.attributes.indexable"), "one, two")
				.withProperty(springSessionPropertyName("session.expiration.max-inactive-interval-seconds"), "600")
				.withProperty(springSessionPropertyName("cache.client.pool.name"), "MockPool")
				.withProperty(springSessionPropertyName("session.region.name"), "MockRegion")
				.withProperty(springSessionPropertyName("cache.server.region.shortcut"), "REPLICATE")
				.withProperty(springSessionPropertyName("session.serializer.bean-name"), "MockSessionSerializer");

			applicationContext.getEnvironment().getPropertySources().addFirst(springSessionGemFireProperties);

			return applicationContext;
		};
	}

	private Function<SpringApplicationBuilder, SpringApplicationBuilder> newWebServerSessionPropertiesConfigurationFunction() {

		return springApplicationBuilder ->
			springApplicationBuilder.properties(singletonProperties("server.servlet.session.timeout", "3600s"));
	}

	@Override
	protected SpringApplicationBuilder processBeforeBuild(SpringApplicationBuilder springApplicationBuilder) {

		return this.springApplicationBuilderFunction.apply(springApplicationBuilder)
			.contextFactory(ApplicationContextFactory.ofContextClass(GenericWebApplicationContext.class));
	}

	@Override
	protected ConfigurableApplicationContext processBeforeRefresh(ConfigurableApplicationContext applicationContext) {
		return this.mockServletContextFunction.andThen(this.applicationContextFunction).apply(applicationContext);
	}

	private String springSessionPropertyName(String propertyNameSuffix) {
		return String.format("%1$s.%2$s", SPRING_SESSION_DATA_GEMFIRE_PROPERTY, propertyNameSuffix);
	}

	@Before
	public void setup() {
		GemFireMockObjectsSupport.destroy();
	}

	@Test
	public void springSessionConfigurationCustomizedWithConfigurer() {

		this.applicationContextFunction = newSpringSessionGemFirePropertiesConfigurationFunction();

		this.springApplicationBuilderFunction = newSpringBootSessionPropertiesConfigurationFunction()
			.andThen(newWebServerSessionPropertiesConfigurationFunction());

		newApplicationContext(TestConfiguration.class, SpringSessionGemFireConfigurerTestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(ObjectUtils.<ClientRegionShortcut>invoke(sessionConfiguration, "getClientRegionShortcut"))
			.isEqualTo(ClientRegionShortcut.CACHING_PROXY);

		assertThat(ObjectUtils.<String[]>invoke(sessionConfiguration, "getIndexableSessionAttributes"))
			.contains("two", "four");

		assertThat(ObjectUtils.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(900);

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getPoolName")).isEqualTo("TestPool");

		assertThat(ObjectUtils.<RegionShortcut>invoke(sessionConfiguration, "getServerRegionShortcut"))
			.isEqualTo(RegionShortcut.PARTITION_REDUNDANT_PERSISTENT_OVERFLOW);

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getSessionRegionName"))
			.isEqualTo("TestRegion");

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getSessionSerializerBeanName"))
			.isEqualTo("TestSessionSerializer");
	}

	@Test
	public void springSessionConfigurationCustomizedWithProperties() {

		this.applicationContextFunction = newSpringSessionGemFirePropertiesConfigurationFunction();

		this.springApplicationBuilderFunction = newSpringBootSessionPropertiesConfigurationFunction()
			.andThen(newWebServerSessionPropertiesConfigurationFunction());

		newApplicationContext(TestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(ObjectUtils.<ClientRegionShortcut>invoke(sessionConfiguration, "getClientRegionShortcut"))
			.isEqualTo(ClientRegionShortcut.LOCAL);

		assertThat(ObjectUtils.<String[]>invoke(sessionConfiguration, "getIndexableSessionAttributes"))
			.contains("one", "two");

		assertThat(ObjectUtils.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(600);

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getPoolName")).isEqualTo("MockPool");

		assertThat(ObjectUtils.<RegionShortcut>invoke(sessionConfiguration, "getServerRegionShortcut"))
			.isEqualTo(RegionShortcut.REPLICATE);

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getSessionRegionName"))
			.isEqualTo("MockRegion");

		assertThat(ObjectUtils.<String>invoke(sessionConfiguration, "getSessionSerializerBeanName"))
			.isEqualTo("MockSessionSerializer");
	}

	@Test
	public void springSessionExpirationTimeoutConfiguredWithSpringBootProperties() {

		this.springApplicationBuilderFunction = newSpringBootSessionPropertiesConfigurationFunction()
			.andThen(newWebServerSessionPropertiesConfigurationFunction());

		newApplicationContext(TestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(ObjectUtils.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(300);
	}

	@Test
	public void springSessionExpirationTimeoutConfiguredWithWebContainerProperties() {

		this.springApplicationBuilderFunction = newWebServerSessionPropertiesConfigurationFunction();

		newApplicationContext(TestConfiguration.class);

		GemFireHttpSessionConfiguration sessionConfiguration = getBean(GemFireHttpSessionConfiguration.class);

		assertThat(sessionConfiguration).isNotNull();

		assertThat(ObjectUtils.<Integer>invoke(sessionConfiguration, "getMaxInactiveIntervalInSeconds"))
			.isEqualTo(3600);
	}

	//@SpringBootApplication
	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableGemFireMockObjects
	static class TestConfiguration {

		@Bean("MockSessionSerializer")
		@SuppressWarnings("unchecked")
		SessionSerializer<Session, ?, ?> mockSessionSerializer() {
			return mock(SessionSerializer.class);
		}

		@Bean
		PoolFactoryBean gemfirePool() {
			return new PoolFactoryBean();
		}

		@Bean("MockPool")
		PoolFactoryBean mockPool() {
			return new PoolFactoryBean();
		}

		@Bean("TestPool")
		PoolFactoryBean testPool() {
			return new PoolFactoryBean();
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
