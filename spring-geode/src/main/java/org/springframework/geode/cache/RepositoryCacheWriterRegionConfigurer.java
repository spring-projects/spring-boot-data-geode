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
package org.springframework.geode.cache;

import java.util.function.Predicate;

import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.Region;

import org.springframework.data.gemfire.PeerRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Spring Data {@link RegionConfigurer} implementation used to adapt and register a Spring Data {@link CrudRepository}
 * as a {@link CacheWriter} for a targeted {@link Region}.
 *
 * @author John Blum
 * @param <T> {@link Class type} of the persistent entity.
 * @param <ID> {@link Class type} of the persistent entity identifier (ID).
 * @see java.util.function.Predicate
 * @see org.apache.geode.cache.CacheWriter
 * @see org.apache.geode.cache.Region
 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @see org.springframework.data.repository.CrudRepository
 * @since 1.1.0
 */
public class RepositoryCacheWriterRegionConfigurer<T, ID> implements RegionConfigurer {

	/**
	 * Factory method used to construct a new instance of {@link RepositoryCacheWriterRegionConfigurer} initialized with
	 * the given Spring Data {@link CrudRepository} used to write {@link Region} values to a backend data source
	 * /data store along with a given {@link Predicate} to identify/qualify the {@link Region} on which
	 * the {@link CrudRepository} will be registered and used as a {@link CacheWriter}.
	 *
	 * @param repository {@link CrudRepository} used to write {@link Region} values to a backend data source.
	 * @param regionBeanName {@link Predicate} used to identify/qualify the {@link Region} on which
	 * the {@link CrudRepository} will be registered and used as a {@link CacheWriter}.
	 * @return a new instance of {@link RepositoryCacheWriterRegionConfigurer}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 * @see java.util.function.Predicate
	 * @see #RepositoryCacheWriterRegionConfigurer(CrudRepository, Predicate)
	 */
	public static <T, ID> RepositoryCacheWriterRegionConfigurer<T, ID> create(@NonNull CrudRepository<T, ID> repository,
			@Nullable Predicate<String> regionBeanName) {

		return new RepositoryCacheWriterRegionConfigurer<>(repository, regionBeanName);
	}

	/**
	 * Factory method used to construct a new instance of {@link RepositoryCacheWriterRegionConfigurer} initialized with
	 * the given Spring Data {@link CrudRepository} used to write {@link Region} values to a backend data source
	 * /data store along with a given {@link Predicate} to identify/qualify the {@link Region} on which
	 * the {@link CrudRepository} will be registered and used as a {@link CacheWriter}.
	 *
	 * @param repository {@link CrudRepository} used to write {@link Region} values to a backend data source.
	 * @param regionBeanName {@link String} containing the bean name identifying/qualifying the {@link Region}
	 * on which the {@link CrudRepository} will be registered and used as a {@link CacheWriter}.
	 * @return a new instance of {@link RepositoryCacheWriterRegionConfigurer}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 * @see java.lang.String
	 * @see #create(CrudRepository, Predicate)
	 */
	public static <T, ID> RepositoryCacheWriterRegionConfigurer<T, ID> create(@NonNull CrudRepository<T, ID> repository,
			@Nullable String regionBeanName) {

		return create(repository, Predicate.isEqual(regionBeanName));
	}

	private final CrudRepository<T, ID> repository;

	private final Predicate<String> regionBeanName;

	/**
	 * Constructs a new instance of {@link RepositoryCacheWriterRegionConfigurer} initialized with the given Spring Data
	 * {@link CrudRepository} used to write {@link Region} values to a backend data source/data store along with
	 * a given {@link Predicate} to identify/qualify the {@link Region} on which the {@link CrudRepository} will be
	 * registered and used as a {@link CacheWriter}.
	 *
	 * @param repository {@link CrudRepository} used to write {@link Region} values to a backend data source.
	 * @param regionBeanName {@link Predicate} used to identify/qualify the {@link Region} on which
	 * the {@link CrudRepository} will be registered and used as a {@link CacheWriter}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 * @see java.util.function.Predicate
	 */
	public RepositoryCacheWriterRegionConfigurer(@NonNull CrudRepository<T, ID> repository,
			@Nullable Predicate<String> regionBeanName) {

		Assert.notNull(repository, "CrudRepository is required");

		this.repository = repository;
		this.regionBeanName = regionBeanName != null ? regionBeanName : beanName -> false;
	}

	/**
	 * Returns the configured {@link Predicate} used to identify/qualify the {@link Region}
	 * on which the {@link CrudRepository} will be registered as a {@link CacheWriter} for write through.
	 *
	 * @return the configured {@link Predicate} used to identify/qualify the {@link Region}
	 * targeted for the {@link CacheWriter} registration.
	 * @see java.util.function.Predicate
	 */
	protected @NonNull Predicate<String> getRegionBeanName() {
		return regionBeanName;
	}

	/**
	 * Returns the configured Spring Data {@link CrudRepository} adapted/wrapped as a {@link CacheWriter}
	 * and used to write {@link Region} values to a backend data source/data store.
	 *
	 * @return the configured {@link CrudRepository} used to write {@link Region} values to a backend data source.
	 * @see org.springframework.data.repository.CrudRepository
	 */
	protected @NonNull CrudRepository<T, ID> getRepository() {
		return this.repository;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void configure(String beanName, ClientRegionFactoryBean<?, ?> bean) {

		if (getRegionBeanName().test(beanName)) {
			bean.setCacheWriter(newRepositoryCacheWriter());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void configure(String beanName, PeerRegionFactoryBean<?, ?> bean) {

		if (getRegionBeanName().test(beanName)) {
			bean.setCacheWriter(newRepositoryCacheWriter());
		}
	}

	/**
	 * Constructs a new instance of {@link RepositoryCacheWriter} adapting the {@link CrudRepository}
	 * as an instance of a {@link CacheWriter}.
	 *
	 * @return a new {@link RepositoryCacheWriter}.
	 * @see org.springframework.geode.cache.RepositoryCacheWriter
	 * @see org.springframework.data.repository.CrudRepository
	 * @see org.apache.geode.cache.CacheWriter
	 * @see #getRepository()
	 */
	@SuppressWarnings("rawtypes")
	protected RepositoryCacheWriter newRepositoryCacheWriter() {
		return new RepositoryCacheWriter<>(getRepository());
	}
}
