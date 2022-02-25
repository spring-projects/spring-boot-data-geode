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
package org.springframework.geode.core.io.support;

import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.geode.core.io.ResourceResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * {@link ResourceResolver} that returns a single (i.e. {@literal Singleton}) {@link Resource}
 * regardless of {@link String location}.
 *
 * @author John Blum
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.core.io.ResourceResolver
 * @since 1.3.1
 */
public class SingleResourceResolver implements ResourceResolver {

	@Nullable
	private final Resource resource;

	/**
	 * Constructs a new instance of {@link SingleResourceResolver} initialized with the given {@link Resource}.
	 *
	 * @param resource the {@literal single} {@link Resource} consistently resolved by this resolver.
	 * @see org.springframework.core.io.Resource
	 */
	public SingleResourceResolver(@Nullable Resource resource) {
		this.resource = resource;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<Resource> resolve(@NonNull String location) {
		return Optional.ofNullable(this.resource);
	}
}
