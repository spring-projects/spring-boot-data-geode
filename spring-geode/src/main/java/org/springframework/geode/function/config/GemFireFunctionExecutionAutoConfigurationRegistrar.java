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

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;

/**
 * The {@link GemFireFunctionExecutionAutoConfigurationRegistrar} class is a Spring {@link ImportBeanDefinitionRegistrar}
 * used to register SDG POJO interfaces defining Apache Geode {@link Function} {@link Execution Executions}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.execute.Execution
 * @see org.apache.geode.cache.execute.Function
 * @see org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions
 * @see org.springframework.geode.function.config.AbstractFunctionExecutionAutoConfigurationExtension
 * @since 1.0.0
 */
public class GemFireFunctionExecutionAutoConfigurationRegistrar
		extends AbstractFunctionExecutionAutoConfigurationExtension {

	@Override
	protected Class<?> getConfiguration() {
		return EnableGemfireFunctionExecutionsConfiguration.class;
	}

	@EnableGemfireFunctionExecutions
	private static class EnableGemfireFunctionExecutionsConfiguration { }

}
