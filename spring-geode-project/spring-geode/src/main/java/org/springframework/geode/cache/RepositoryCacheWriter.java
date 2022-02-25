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

import java.util.function.Supplier;

import org.apache.geode.cache.CacheRuntimeException;
import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.RegionEvent;

import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.support.RepositoryCacheLoaderWriterSupport;
import org.springframework.geode.core.util.function.FunctionUtils;

/**
 * A {@link CacheWriter} implementation backed by a Spring Data {@link CrudRepository} used to persist a cache entry
 * (i.e. entity) to a backend, external data source.
 *
 * @author John Blum
 * @see org.apache.geode.cache.CacheWriter
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.support.RepositoryCacheLoaderWriterSupport
 * @since 1.1.0
 */
@SuppressWarnings("unused")
public class RepositoryCacheWriter<T, ID> extends RepositoryCacheLoaderWriterSupport<T, ID> {

  public RepositoryCacheWriter(CrudRepository<T, ID> repository) {
    super(repository);
  }

  @Override
  public void beforeCreate(EntryEvent<ID, T> event) throws CacheWriterException {
    doRepositoryOp(event.getNewValue(), getRepository()::save);
  }

  @Override
  public void beforeUpdate(EntryEvent<ID, T> event) throws CacheWriterException {
    doRepositoryOp(event.getNewValue(), getRepository()::save);
  }

  @Override
  public void beforeDestroy(EntryEvent<ID, T> event) throws CacheWriterException {

    //doRepositoryOp(event.getOldValue(), FunctionUtils.toNullReturningFunction(getRepository()::delete));
    doRepositoryOp(event.getKey(), FunctionUtils.toNullReturningFunction(getRepository()::deleteById));
  }

  @Override
  public void beforeRegionClear(RegionEvent<ID, T> event) throws CacheWriterException {

    if (isNukeAndPaveEnabled()) {
      doRepositoryOp(null, FunctionUtils.toNullReturningFunction(it -> getRepository().deleteAll()));
    }
  }

  @Override
  public void beforeRegionDestroy(RegionEvent<ID, T> event) throws CacheWriterException {
    // TODO: perhaps implement by releasing external data source resources
    //  (i.e. destroy database object(s), e.g. DROP TABLE)
  }

  @Override
  protected CacheRuntimeException newCacheRuntimeException(Supplier<String> messageSupplier, Throwable cause) {
    return new CacheWriterException(messageSupplier.get(), cause);
  }
}
