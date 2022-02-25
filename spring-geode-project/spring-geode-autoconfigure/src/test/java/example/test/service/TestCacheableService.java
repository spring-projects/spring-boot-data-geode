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
package example.test.service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * {@link TestCacheableService} is a Spring {@link Service} bean with caching applied.
 *
 * @author John Blum
 * @see java.util.Random
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @since 1.2.1
 */
@Service
public class TestCacheableService {

	private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

	private final Random random = new Random(System.currentTimeMillis());

	public boolean isCacheMiss() {
		return this.cacheMiss.getAndSet(false);
	}

	@Cacheable("RandomNumbers")
	public Number getRandomNumber(@SuppressWarnings("unused") String key) {

		this.cacheMiss.set(true);

		return this.random.nextInt();
	}
}
