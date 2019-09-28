/*
 * Copyright 2019 the original author or authors.
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
package example.app.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling contact information for a person.
 *
 * @author John Blum
 * @see javax.persistence.Entity
 * @see org.springframework.data.annotation.Id
 * @since 1.2.0
 */
@Data
@Entity
//@Document
//@Region("Contacts")
@Table(name = "Contacts")
@ToString(of = "name")
@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor(staticName = "newContact")
@SuppressWarnings("unused")
public class Contact {

	@javax.persistence.Id @Id @NonNull
	private String name;

	@Column(name = "email_address")
	private String emailAddress;

	@Column(name = "phone_number")
	private String phoneNumber;

	protected Contact() { }

	public Contact withEmailAddress(String emailAddress) {
		setEmailAddress(emailAddress);
		return this;
	}

	public Contact withPhoneNumber(String phoneNumber) {
		setPhoneNumber(phoneNumber);
		return this;
	}
}
