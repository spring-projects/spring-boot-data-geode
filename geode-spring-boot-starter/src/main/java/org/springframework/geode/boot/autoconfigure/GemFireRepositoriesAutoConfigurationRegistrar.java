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

import java.lang.annotation.Annotation;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.repository.config.GemfireRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * Spring {@link ImportBeanDefinitionRegistrar} used to auto-configure Spring Data Geode Repositories.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport
 * @see org.springframework.data.gemfire.repository.config.EnableGemfireRepositories
 * @see org.springframework.data.gemfire.repository.config.GemfireRepositoryConfigurationExtension
 * @see org.springframework.geode.boot.autoconfigure.RepositoriesAutoConfiguration
 * @since 1.0.0
 */
public class GemFireRepositoriesAutoConfigurationRegistrar extends AbstractRepositoryConfigurationSourceSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableGemfireRepositories.class;
	}

	@Override
	protected Class<?> getConfiguration() {
		return EnableGemFireRepositoriesConfiguration.class;
	}

	@Override
	protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
		return new GemfireRepositoryConfigurationExtension();
	}

	@EnableGemfireRepositories
	private static class EnableGemFireRepositoriesConfiguration { }

}
