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
package org.springframework.geode.function.config;

import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.function.config.AbstractFunctionExecutionConfigurationSource;
import org.springframework.data.gemfire.function.config.AnnotationFunctionExecutionConfigurationSource;
import org.springframework.data.gemfire.function.config.FunctionExecutionBeanDefinitionRegistrar;
import org.springframework.util.Assert;

/**
 * The {@link AbstractFunctionExecutionAutoConfigurationExtension} class extends SDG's {@link FunctionExecutionBeanDefinitionRegistrar}
 * to redefine the location of application POJO {@link Function} {@link Execution} interfaces.
 *
 * @author John Blum
 * @see org.apache.geode.cache.execute.Execution
 * @see org.apache.geode.cache.execute.Function
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.boot.autoconfigure.AutoConfigurationPackages
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.function.config.FunctionExecutionBeanDefinitionRegistrar
 * @since 1.0.0
 */
public abstract class AbstractFunctionExecutionAutoConfigurationExtension
		extends FunctionExecutionBeanDefinitionRegistrar implements BeanFactoryAware {

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {

		Assert.state(this.beanFactory != null, "BeanFactory was not properly configured");

		return this.beanFactory;
	}

	protected abstract Class<?> getConfiguration();

	@SuppressWarnings("unused")
	@Override
	protected AbstractFunctionExecutionConfigurationSource newAnnotationBasedFunctionExecutionConfigurationSource(
			AnnotationMetadata annotationMetadata) {

		AnnotationMetadata metadata = AnnotationMetadata.introspect(getConfiguration());

		return new AnnotationFunctionExecutionConfigurationSource(metadata) {

			@Override
			public Iterable<String> getBasePackages() {
				return AutoConfigurationPackages.get(getBeanFactory());
			}
		};
	}
}
