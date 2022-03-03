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
package org.springframework.boot.autoconfigure.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * Integration Tests asserting the behavior of Spring Boot's {@link Conditional} configuration.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.boot.autoconfigure.condition.AllNestedConditions
 * @see org.springframework.boot.autoconfigure.condition.AnyNestedCondition
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ComplexConditionalConfigurationIntegrationTests extends IntegrationTestsSupport {

	private static final AtomicBoolean importBeanDefinitionRegistrarCalled = new AtomicBoolean(false);

	@BeforeClass
	public static void setup() {

		//System.setProperty("example.test.condition.properties.one", "true");
		System.setProperty("example.test.condition.properties.two", "true");
		System.setProperty("example.mock.condition.properties.one", "false");
		System.setProperty("example.mock.condition.properties.two", "true");
	}

	@AfterClass
	public static void tearDown() {

		System.getProperties().stringPropertyNames().stream()
			.filter(StringUtils::hasText)
			.filter(propertyName -> propertyName.startsWith("example."))
			.forEach(System::clearProperty);
	}

	@Test
	public void conditionalConfigurationIsCorrect() {
		assertThat(importBeanDefinitionRegistrarCalled.get()).isFalse();
	}

	@Configuration
	@EnableMockConfiguration
	@Conditional(TestConditions.class)
	static class TestConfiguration { }

	static final class TestConditions extends AllNestedConditions {

		TestConditions() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(prefix = "example.test.condition.properties", name = { "one", "two" })
		static class PropertiesCondition { }

		@Conditional(MockConditions.class)
		static class UsingMockConditions { }

	}

	static final class MockConditions extends AnyNestedCondition {

		MockConditions() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@Conditional(MockConditionOne.class)
		static class WithMockConditionOne{ }

		@Conditional(MockConditionTwo.class)
		static class WithMockConditionTwo{ }

	}

	static final class MockConditionOne implements org.springframework.context.annotation.Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return Boolean.getBoolean("example.mock.condition.properties.one");
		}
	}

	static final class MockConditionTwo implements org.springframework.context.annotation.Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return Boolean.getBoolean("example.mock.condition.properties.two");
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	@Import(MockConfiguration.class)
	@interface EnableMockConfiguration { }

	static final class MockConfiguration implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {

			importBeanDefinitionRegistrarCalled.set(true);
		}
	}
}
