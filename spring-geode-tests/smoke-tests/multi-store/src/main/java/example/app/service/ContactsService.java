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
package example.app.service;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.model.Contact;
import example.app.repo.ContactRepository;

/**
 * Spring {@link Service} class for servicing {@link Contact Contacts}.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CachePut
 * @see org.springframework.stereotype.Service
 * @see example.app.model.Contact
 * @see example.app.repo.ContactRepository
 * @since 1.2.0
 */
@Service
public class ContactsService {

	private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

	private final ContactRepository contactRepository;

	public ContactsService(ContactRepository contactRepository) {

		Assert.notNull(contactRepository, "ContactRepository is required");

		this.contactRepository = contactRepository;
	}

	public boolean isCacheMiss() {
		return this.cacheMiss.getAndSet(false);
	}

	@Cacheable(cacheNames = "ContactsByName")
	public Contact findByName(String name) {

		this.cacheMiss.set(true);

		return this.contactRepository.findById(name).orElseThrow(() ->
			newIllegalStateException("No Contact with name [%s] was found", name));
	}

	@CachePut(cacheNames = "ContactsByName", key="#contact.name")
	public Contact save(Contact contact) {
		return this.contactRepository.save(contact);
	}
}
