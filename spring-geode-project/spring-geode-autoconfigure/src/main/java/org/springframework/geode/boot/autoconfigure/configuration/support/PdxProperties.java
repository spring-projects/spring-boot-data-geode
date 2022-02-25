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
package org.springframework.geode.boot.autoconfigure.configuration.support;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure Apache Geode {@literal PDX} serialization.
 *
 * PDX serialization is an alternative serialization format to Java Serialization provided by Apache Geode. PDX enables
 * interoperability with native language clients (e.g. C++), enables objects stored in Apache Geode to be queried
 * without causing deserialization and is a more efficient format than Java Serialization.  While PDX is more robust in
 * some ways, it is less robust in others.  For example, PDX does not handle cyclic references in the object graph.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.distributed.Locator
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PdxProperties {

	private static final boolean DEFAULT_IGNORE_UNREAD_FIELDS = false;
	private static final boolean DEFAULT_PERSISTENT = false;
	private static final boolean DEFAULT_READ_SERIALIZED = false;

	private boolean ignoreUnreadFields = DEFAULT_IGNORE_UNREAD_FIELDS;
	private boolean persistent = DEFAULT_PERSISTENT;
	private boolean readSerialized = DEFAULT_READ_SERIALIZED;

	private String diskStoreName;
	private String serializerBeanName;

	public String getDiskStoreName() {
		return this.diskStoreName;
	}

	public void setDiskStoreName(String diskStoreName) {
		this.diskStoreName = diskStoreName;
	}

	public boolean isIgnoreUnreadFields() {
		return this.ignoreUnreadFields;
	}

	public void setIgnoreUnreadFields(boolean ignoreUnreadFields) {
		this.ignoreUnreadFields = ignoreUnreadFields;
	}

	public boolean isPersistent() {
		return this.persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public boolean isReadSerialized() {
		return this.readSerialized;
	}

	public void setReadSerialized(boolean readSerialized) {
		this.readSerialized = readSerialized;
	}

	public String getSerializerBeanName() {
		return this.serializerBeanName;
	}

	public void setSerializerBeanName(String serializerBeanName) {
		this.serializerBeanName = serializerBeanName;
	}
}
