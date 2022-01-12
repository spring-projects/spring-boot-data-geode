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
package org.springframework.web.servlet.http;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Abstract base class supporting implementations of the {@link HttpSession} interface.
 *
 * @author John Blum
 * @see javax.servlet.http.HttpSession
 * @since 1.4.0
 */
@SuppressWarnings("deprecation")
public abstract class AbstractHttpSession implements HttpSession {

	@Override
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException("Not Implemented");
	}

	@Override
	public @Nullable Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public @NonNull String[] getValueNames() {

		List<String> valueNames = new ArrayList<>();

		Enumeration<String> attributeNames = getAttributeNames();

		if (Objects.nonNull(attributeNames)) {
			while (attributeNames.hasMoreElements()) {
				valueNames.add(attributeNames.nextElement());
			}
		}

		return valueNames.toArray(new String[0]);
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeValue(String name) {
		removeAttribute(name);
	}
}
