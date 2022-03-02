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
package example.app.caching.near.client.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import example.app.caching.near.client.model.Person;
import example.app.caching.near.client.service.support.AbstractCacheableService;
import example.app.caching.near.client.service.support.EmailGenerator;
import example.app.caching.near.client.service.support.PhoneNumberGenerator;

/**
 * Spring {@link Service} class implementing the Yellow Pages.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CachePut
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.stereotype.Service
 * @see example.app.caching.near.client.model.Person
 * @see example.app.caching.near.client.service.support.AbstractCacheableService
 * @see example.app.caching.near.client.service.support.EmailGenerator
 * @see example.app.caching.near.client.service.support.PhoneNumberGenerator
 * @since 1.1.0
 */
// tag::class[]
@Service
public class YellowPagesService extends AbstractCacheableService {

	@Cacheable("YellowPages")
	public Person find(String name) {

		this.cacheMiss.set(true);

		Person person = Person.newPerson(name)
			.withEmail(EmailGenerator.generate(name, null))
			.withPhoneNumber(PhoneNumberGenerator.generate(null));

		simulateLatency();

		return person;
	}

	@CachePut(cacheNames = "YellowPages", key = "#person.name")
	public Person save(Person person, String email, String phoneNumber) {

		if (StringUtils.hasText(email)) {
			person.withEmail(email);
		}

		if (StringUtils.hasText(phoneNumber)) {
			person.withPhoneNumber(phoneNumber);
		}

		return person;
	}

	@CacheEvict(cacheNames = "YellowPages")
	public boolean evict(String name) {
		return true;
	}
}
// end::class[]
