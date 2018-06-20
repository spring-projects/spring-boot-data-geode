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

package org.springframework.geode.cache.support;

import org.apache.geode.cache.CacheLoader;

/**
 * The {@link CacheLoaderSupport} interface is an extension of {@link CacheLoader} and a {@link FunctionalInterface}
 * useful in Lambda expressions.
 *
 * @author John Blum
 * @see org.apache.geode.cache.CacheLoader
 * @since 1.0.0
 */
@FunctionalInterface
public interface CacheLoaderSupport<K, V> extends CacheLoader<K, V> {

	/**
	 * Closes any resources opened and used by this {@link CacheLoader}.
	 *
	 * @see org.apache.geode.cache.CacheLoader#close()
	 */
	@Override
	default void close() {}

}
