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

package org.springframework.gradle.github.milestones;
public class RepositoryRef {
	private String owner;

	private String name;

	RepositoryRef() {
	}

	public RepositoryRef(String owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "RepositoryRef{" +
				"owner='" + owner + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	public static RepositoryRefBuilder owner(String owner) {
		return new RepositoryRefBuilder().owner(owner);
	}

	public static final class RepositoryRefBuilder {
		private String owner;
		private String repository;

		private RepositoryRefBuilder() {
		}

		private RepositoryRefBuilder owner(String owner) {
			this.owner = owner;
			return this;
		}

		public RepositoryRefBuilder repository(String repository) {
			this.repository = repository;
			return this;
		}

		public RepositoryRef build() {
			return new RepositoryRef(owner, repository);
		}
	}
}

