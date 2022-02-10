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
package example.app.caching.lookaside.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * A Spring Cacheable {@link Service} class used to maintain a collection of named counters and provide counter
 * operations to increment the count, access the current, cached count and rest the count.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.CachePut
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
// tag::class[]
@Service
public class CounterService {

	private ConcurrentMap<String, AtomicLong> namedCounterMap = new ConcurrentHashMap<>();

	@Cacheable("Counters")
	public long getCachedCount(String counterName) {
		return getCount(counterName);
	}

	@CachePut("Counters")
	public long getCount(String counterName) {

		AtomicLong counter = this.namedCounterMap.get(counterName);

		if (counter == null) {

			counter = new AtomicLong(0L);

			AtomicLong existingCounter = this.namedCounterMap.putIfAbsent(counterName, counter);

			counter = existingCounter != null ? existingCounter : counter;
		}

		return counter.incrementAndGet();
	}

	@CacheEvict("Counters")
	public void resetCounter(String counterName) {
		this.namedCounterMap.remove(counterName);
	}
}
// end::class[]
