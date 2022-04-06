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
package io.micrometer.core.instrument.binder.jvm;

import io.micrometer.core.instrument.Tag;

/**
 * Extension of {@link io.micrometer.binder.jvm.JvmMemoryMetrics} for adapting and repackaging purposes
 * as required by Apache Geode 1.x, which is based on Micrometer 1.x.
 *
 * @author John Blum
 * @see io.micrometer.binder.jvm.JvmMemoryMetrics
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class JvmMemoryMetrics extends io.micrometer.binder.jvm.JvmMemoryMetrics {

	/**
	 * @inheritDoc
	 */
	public JvmMemoryMetrics() { }

	/**
	 * @inheritDoc
	 */
	public JvmMemoryMetrics(Iterable<Tag> tags) {
		super(tags);
	}
}
