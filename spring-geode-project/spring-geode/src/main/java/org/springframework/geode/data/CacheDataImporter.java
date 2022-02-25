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
package org.springframework.geode.data;

import org.apache.geode.cache.Region;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link CacheDataImporter} interface is a {@link FunctionalInterface} defininig a contract for importing data
 * into a cache {@link Region}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @since 1.3.0
 */
@FunctionalInterface
@SuppressWarnings("rawtypes")
public interface CacheDataImporter extends BeanPostProcessor {

	/**
	 * Imports data from an external data source into a given {@link Region} after initialization.
	 *
	 * @param bean {@link Object} bean to evaluate.
	 * @param beanName {@link String} containing the name of the bean.
	 * @throws BeansException if importing data into a {@link Region} fails!
	 * @see org.apache.geode.cache.Region
	 * @see #importInto(Region)
	 */
	@Nullable @Override
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof Region) {
			bean = importInto((Region) bean);
		}

		return bean;
	}

	/**
	 * Imports data into the given {@link Region}.
	 *
	 * @param region {@link Region} to import data into.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	@NonNull Region importInto(@NonNull Region region);

}
