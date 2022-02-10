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
package org.springframework.geode.cache.support;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.CacheRuntimeException;
import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.LoaderHelper;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Abstract base class supporting the implementation of Apache Geode {@link CacheLoader CacheLoaders}
 * and {@link CacheWriter CacheWriters} backed by Spring Data {@link Repository Repositories}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.CacheLoader
 * @see org.apache.geode.cache.CacheWriter
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.data.repository.Repository
 * @see org.springframework.geode.cache.support.CacheLoaderSupport
 * @since 1.1.0
 */
public abstract class RepositoryCacheLoaderWriterSupport<T, ID>
		implements CacheLoaderSupport<ID, T>, CacheWriterSupport<ID, T>, EnvironmentAware {

	public static final String NUKE_AND_PAVE_PROPERTY = "spring.boot.data.gemfire.data.source.nuke-and-pave";

	protected static final String DATA_ACCESS_ERROR =
		"Exception occurred while accessing entity [%s] in external data source";

	private final CrudRepository<T, ID> repository;

	private Environment environment;

	protected RepositoryCacheLoaderWriterSupport(@NonNull CrudRepository<T, ID> repository) {

		Assert.notNull(repository, "Repository is required");

		this.repository = repository;
	}

	protected boolean isNukeAndPaveEnabled() {

		return getEnvironment()
			.map(env -> env.getProperty(NUKE_AND_PAVE_PROPERTY, Boolean.class))
			.orElse(Boolean.getBoolean(NUKE_AND_PAVE_PROPERTY));
	}

	@Override
	public void setEnvironment(@Nullable Environment environment) {
		this.environment = environment;
	}

	protected Optional<Environment> getEnvironment() {
		return Optional.ofNullable(this.environment);
	}

	public @NonNull CrudRepository<T, ID> getRepository() {
		return this.repository;
	}

	protected <S, R> R doRepositoryOp(S entity, Function<S, R> repositoryOperation) {

		try {
			return repositoryOperation.apply(entity);
		}
		catch (Throwable cause) {
			throw newCacheRuntimeException(() -> String.format(DATA_ACCESS_ERROR, entity), cause);
		}
	}

	@Override
	public T load(LoaderHelper<ID, T> helper) throws CacheLoaderException {
		return null;
	}

	protected abstract CacheRuntimeException newCacheRuntimeException(
		Supplier<String> messageSupplier, Throwable cause);

	@SuppressWarnings("unchecked")
	public <U extends RepositoryCacheLoaderWriterSupport<T, ID>> U with(Environment environment) {

		setEnvironment(environment);

		return (U) this;
	}
}
