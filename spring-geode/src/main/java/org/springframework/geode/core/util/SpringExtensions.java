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
package org.springframework.geode.core.util;

import java.util.Optional;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link SpringExtensions} is an abstract utility class containing functions to extend the functionality of Spring.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.data.gemfire.util.SpringExtensions
 * @since 1.6.0
 */
@SuppressWarnings("unused")
public abstract class SpringExtensions extends org.springframework.data.gemfire.util.SpringExtensions {

	public static final String BEAN_DEFINITION_METADATA_JSON = "{\n"
		+ "\t'beanName': '%1$s',%n"
		+ "\t'beanClassName': '%2$s',%n"
		+ "\t'description': '%3$s',%n"
		+ "\t'originatingBeanDefinition': '%4$s',%n"
		+ "\t'parentName': '%5$s',%n"
		+ "\t'resourceDescription': '%6$s',%n"
		+ "\t'source': '%7$s',%n"
		+ "}";

	public static final String EMPTY_JSON_OBJECT = "{}";

	public static @NonNull String getBeanDefinitionMetadata(@NonNull String beanName,
			@Nullable ApplicationContext applicationContext) {

		return Optional.ofNullable(applicationContext)
			.filter(ConfigurableApplicationContext.class::isInstance)
			.map(ConfigurableApplicationContext.class::cast)
			.map(ConfigurableApplicationContext::getBeanFactory)
			.map(beanFactory -> getBeanDefinitionMetadata(beanName, beanFactory))
			.orElse(EMPTY_JSON_OBJECT);
	}

	public static @NonNull String getBeanDefinitionMetadata(@NonNull String beanName,
			@Nullable BeanFactory beanFactory) {

		return Optional.ofNullable(beanFactory)
			.filter(BeanDefinitionRegistry.class::isInstance)
			.map(BeanDefinitionRegistry.class::cast)
			.map(registry -> getBeanDefinitionMetadata(beanName, registry))
			.orElse(EMPTY_JSON_OBJECT);
	}

	public static @NonNull String getBeanDefinitionMetadata(@NonNull String beanName,
			@Nullable BeanDefinitionRegistry beanDefinitionRegistry) {

		return Optional.ofNullable(beanDefinitionRegistry)
			.filter(registry -> StringUtils.hasText(beanName))
			.map(registry -> registry.getBeanDefinition(beanName))
			.map(beanDefinition -> getBeanDefinitionMetadata(beanName, beanDefinition))
			.orElse(EMPTY_JSON_OBJECT);
	}

	public static @NonNull String getBeanDefinitionMetadata(@Nullable String beanName,
			@Nullable BeanDefinition beanDefinition) {

		if (beanDefinition != null) {
			return String.format(BEAN_DEFINITION_METADATA_JSON, beanName,
				beanDefinition.getBeanClassName(),
				beanDefinition.getDescription(),
				beanDefinition.getOriginatingBeanDefinition(),
				beanDefinition.getParentName(),
				beanDefinition.getResourceDescription(),
				beanDefinition.getSource());
		}

		return EMPTY_JSON_OBJECT;
	}
}
