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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpSession;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.session.Session;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.servlet.http.HttpSessionProxy;

/**
 * Spring Test Mock Web MVC framework {@link RequestPostProcessor} that substitutes the Spring Session {@link Session}
 * for the {@link MockHttpSession}.
 *
 * @author John Blum
 * @see javax.servlet.http.HttpSession
 * @see org.springframework.mock.web.MockHttpServletRequest
 * @see org.springframework.mock.web.MockHttpSession
 * @see org.springframework.session.Session
 * @see org.springframework.test.web.servlet.request.RequestPostProcessor
 * @see org.springframework.web.servlet.http.HttpSessionProxy
 * @since 1.4.0
 */
public class SpringSessionSubstitutingSpyRequestPostProcessor implements RequestPostProcessor {

	private static final AtomicReference<SpringSessionSubstitutingSpyRequestPostProcessor> instance = new AtomicReference<>(null);

	public static SpringSessionSubstitutingSpyRequestPostProcessor create() {
		return instance.updateAndGet(instance -> instance != null ? instance
			: new SpringSessionSubstitutingSpyRequestPostProcessor());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {

		MockHttpServletRequest requestSpy = spy(request);

		doAnswer(invocation -> {

			HttpSession session = SessionUtils.resolveSession(request);

			return session != null
				? HttpSessionProxy.from(session)
				: request.getSession();

		}).when(requestSpy).getSession();

		return requestSpy;
	}
}
