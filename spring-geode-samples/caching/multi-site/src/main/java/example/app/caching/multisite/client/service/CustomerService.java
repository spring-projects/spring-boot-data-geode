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
package example.app.caching.multisite.client.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import example.app.caching.multisite.client.model.Customer;
import example.app.caching.multisite.client.util.ThreadUtils;

/**
 * {@link CustomerService} is a Spring application {@link Service} component used to service interactions with
 * {@link Customer customers}.
 *
 * This service class employs the {@literal Look-Aside Caching} Pattern to lookups of {@link Customer}
 * by {@link String name} since it is not common for the {@link Customer Customer's} {@link String name}
 * to change frequently.
 *
 * Additionally, the {@link #findBy(String)} {@link CustomerService} operations simulates an expensive,
 * or resource-intensive operation by introducing a delay.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.stereotype.Service
 * @see example.app.caching.multisite.client.model.Customer
 * @since 1.3.0
 */
// tag::class[]
@Service
public class CustomerService {

	private static final long SLEEP_IN_SECONDS = 5;

	private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

	private final AtomicLong customerId = new AtomicLong(0L);

	private volatile Long sleepInSeconds;

	// tag::find-by-name[]
	@Cacheable("CustomersByName")
	public Customer findBy(String name) {
		setCacheMiss();
		ThreadUtils.safeSleep(name, Duration.ofSeconds(getSleepInSeconds()));
		return Customer.newCustomer(this.customerId.incrementAndGet(), name);
	}
	// end::find-by-name[]

	public boolean isCacheMiss() {
		return this.cacheMiss.compareAndSet(true, false);
	}

	protected void setCacheMiss() {
		this.cacheMiss.set(true);
	}

	public Long getSleepInSeconds() {

		Long sleepInSeconds = this.sleepInSeconds;

		return sleepInSeconds != null ? sleepInSeconds : SLEEP_IN_SECONDS;
	}

	public void setSleepInSeconds(Long seconds) {
		this.sleepInSeconds = seconds;
	}
}
// end::class[]
