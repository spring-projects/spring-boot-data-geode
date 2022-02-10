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
package org.springframework.web.servlet.http;

import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link HttpSession} implementation wrapping and proxying for an existing {@link HttpSession} instance.
 *
 * @author John Blum
 * @see javax.servlet.ServletContext
 * @see javax.servlet.http.HttpSession
 * @see org.springframework.web.servlet.http.AbstractHttpSession
 * @since 1.4.0
 */
public class HttpSessionProxy extends AbstractHttpSession {

	public static HttpSessionProxy from(HttpSession session) {
		return new HttpSessionProxy(session);
	}

	private final HttpSession session;

	private HttpSessionProxy(@NonNull HttpSession session) {

		Assert.notNull(session, "HttpSession must not be null");

		this.session = session;
	}

	protected @NonNull HttpSession getSession() {
		return this.session;
	}

	@Override
	public String getId() {
		return getSession().getId();
	}

	@Override
	public long getCreationTime() {
		return getSession().getCreationTime();
	}

	@Override
	public long getLastAccessedTime() {
		return getSession().getLastAccessedTime();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		getSession().setMaxInactiveInterval(interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		return getSession().getMaxInactiveInterval();
	}

	@Override
	public @NonNull ServletContext getServletContext() {
		return getSession().getServletContext();
	}

	@Override
	@SuppressWarnings("deprecation")
	public HttpSessionContext getSessionContext() {
		return getSession().getSessionContext();
	}

	@Override
	public void setAttribute(String name, Object value) {
		getSession().setAttribute(name, value);
	}

	@Override
	public @Nullable Object getAttribute(String name) {
		return getSession().getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return getSession().getAttributeNames();
	}

	@Override
	public void removeAttribute(String name) {
		getSession().removeAttribute(name);
	}

	@Override
	public void invalidate() {
		getSession().invalidate();
	}

	@Override
	public boolean isNew() {
		return getSession().isNew();
	}
}
