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
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.data.gemfire.ResolvableRegionFactoryBean;
import org.springframework.lang.NonNull;

/**
 * The {@link CacheDataExporter} interface is a {@link FunctionalInterface} defining a contract for exporting data
 * from a cache {@link Region}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
 * @see org.springframework.data.gemfire.ResolvableRegionFactoryBean
 * @since 1.3.0
 */
@FunctionalInterface
@SuppressWarnings("rawtypes")
public interface CacheDataExporter extends DestructionAwareBeanPostProcessor {

	/**
	 * Exports any data contained in a {@link Region} on destruction.
	 *
	 * @param bean {@link Object} bean to evaluate.
	 * @param beanName {@link String} containing the name of the bean.
	 * @throws BeansException if exporting data from a {@link Region} fails!
	 * @see org.apache.geode.cache.Region
	 * @see #exportFrom(Region)
	 */
	@Override
	default void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {

		if (bean instanceof Region) {
			exportFrom((Region) bean);
		}
		else if (bean instanceof ResolvableRegionFactoryBean) {
			exportFrom(((ResolvableRegionFactoryBean) bean).getRegion());
		}
	}

	/**
	 * Exports data contained in the given {@link Region}.
	 *
	 * @param region {@link Region} to export data from.
	 * @return the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	@NonNull Region exportFrom(@NonNull Region region);

}
