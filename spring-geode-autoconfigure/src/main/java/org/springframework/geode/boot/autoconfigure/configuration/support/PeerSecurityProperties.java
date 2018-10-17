/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.boot.autoconfigure.configuration.support;

/**
 * The PeerSecurityProperties class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PeerSecurityProperties {

	private static final long DEFAULT_VERIFY_MEMBER_TIMEOUT_IN_MILLISECONDS = 1000;

	private long verifyMemberTimeout = DEFAULT_VERIFY_MEMBER_TIMEOUT_IN_MILLISECONDS;

	private String authenticationInitializer;
	private String authenticator;

	public String getAuthenticationInitializer() {
		return this.authenticationInitializer;
	}

	public void setAuthenticationInitializer(String authenticationInitializer) {
		this.authenticationInitializer = authenticationInitializer;
	}

	public String getAuthenticator() {
		return this.authenticator;
	}

	public void setAuthenticator(String authenticator) {
		this.authenticator = authenticator;
	}

	public long getVerifyMemberTimeout() {
		return this.verifyMemberTimeout;
	}

	public void setVerifyMemberTimeout(long verifyMemberTimeout) {
		this.verifyMemberTimeout = verifyMemberTimeout;
	}
}
