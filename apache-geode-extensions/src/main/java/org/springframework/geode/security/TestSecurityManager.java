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
package org.springframework.geode.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;

import org.springframework.geode.util.GeodeConstants;

/**
 * Simple, test {@link org.apache.geode.security.SecurityManager}.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see java.util.Properties
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TestSecurityManager implements org.apache.geode.security.SecurityManager {

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException {

		String username = credentials.getProperty(GeodeConstants.USERNAME);
		String password = credentials.getProperty(GeodeConstants.PASSWORD);

		if (!String.valueOf(username).equals(password)) {
			throw new AuthenticationFailedException(String.format("User [%s] could not be authenticated", username));
		}

		return User.create(username);
	}

	public static class User implements Comparable<User>, Principal, Serializable {

		public static User create(String name) {
			return new User(name);
		}

		private final String name;

		public User(String name) {

			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Username is required");
			}

			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(User user) {
			return this.getName().compareTo(user.getName());
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof User)) {
				return false;
			}

			User that = (User) obj;

			return this.getName().equals(that.getName());
		}

		@Override
		public int hashCode() {

			int hashValue = 17;

			hashValue = 37 * hashValue + getName().hashCode();

			return hashValue;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
