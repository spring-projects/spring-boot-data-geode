/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.geode.cloud.bindings;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Maps a source set of properties to a target set of properties.
 *
 * @author John Blum
 * @see java.util.Map
 * @see java.util.function.Function
 * @since 1.4.1
 */
@SuppressWarnings("unused")
public class MapMapper {

	private final Map<String, String> source;
	private final Map<String, Object> target;

	public MapMapper(@NonNull Map<String, String> source, @NonNull Map<String, Object> target) {

		Assert.notNull(source, "Source Map must not be null");
		Assert.notNull(target, "Target Map must not be null");

		this.source = source;
		this.target = target;
	}

	protected @NonNull Map<String, String> getSource() {
		return source;
	}

	protected @NonNull Map<String, Object> getTarget() {
		return target;
	}

	@SuppressWarnings("all")
	public Source from(@NonNull String... keys) {

		String[] resolvedKeys = Arrays.stream(ArrayUtils.nullSafeArray(keys, String.class))
			.filter(StringUtils::hasText)
			.collect(Collectors.toList())
			.toArray(new String[0]);

		return new Source(resolvedKeys);
	}

	protected interface TriFunction<T, U, V, R> {
		R apply(T t, U u, V v);
	}

	public class Source {

		private final String[] keys;

		private Source(@NonNull String[] keys) {

			Assert.notNull(keys, "The String array of keys must not be null");

			this.keys = keys;
		}

		public void to(String key) {
			to(key, v -> v);
		}

		public void to(@NonNull String key, @NonNull Function<String, Object> function) {

			String[] keys = this.keys;

			Assert.state(keys.length == 1,
				String.format("Source size [%d] cannot be transformed as one argument", keys.length));

			Map<String, String> source = getSource();

			if (Arrays.stream(keys).allMatch(source::containsKey)) {
				getTarget().put(key, function.apply(source.get(keys[0])));
			}
		}

		public void to(String key, TriFunction<String, String, String, Object> function) {

			String[] keys = this.keys;

			Assert.state(keys.length == 3,
				String.format("Source size [%d] cannot be consumed as three arguments", keys.length));

			Map<String, String> source = getSource();

			if (Arrays.stream(keys).allMatch(source::containsKey)) {
				getTarget().put(key, function.apply(source.get(keys[0]), source.get(keys[1]), source.get(keys[2])));
			}
		}

	}
}
