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
package org.springframework.session.web.servlet.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.session.SessionRepository;

/**
 * Abstract utility class used to work with {@link HttpSession} objects.
 *
 * @author John Blum
 * @see javax.servlet.http.HttpServletRequest
 * @see javax.servlet.http.HttpSession
 * @since 1.4.0
 */
public abstract class SessionUtils {

	protected static final String CURRENT_SESSION_REQUEST_ATTRIBUTE =
		SessionRepository.class.getName().concat(".CURRENT_SESSION");

	protected static final String GET_CURRENT_SESSION_METHOD_NAME = "getCurrentSession";

	public static @Nullable HttpSession resolveSession(HttpServletRequest servletRequest) {

		try {
			return ObjectUtils.invoke(servletRequest, GET_CURRENT_SESSION_METHOD_NAME);
		}
		catch (IllegalArgumentException ignore) {
			return (HttpSession) servletRequest.getAttribute(CURRENT_SESSION_REQUEST_ATTRIBUTE);
		}
	}
}
