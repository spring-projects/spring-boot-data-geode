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

package org.springframework.geode.boot.autoconfigure.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_PASSWORD_PROPERTY;
import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_USERNAME_PROPERTY;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import example.echo.config.EchoClientConfiguration;
import example.echo.config.EchoServerConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The {@link AbstractAutoConfiguredSecurityContextIntegrationTests} class is an abstract security context integration test class
 * encapsulating configuration and functionality common to both cloud and local security context integration tests.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.security.ResourcePermission
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractAutoConfiguredSecurityContextIntegrationTests
		extends ForkingClientServerIntegrationTestsSupport {

	private static final String SECURITY_CONTEXT_USERNAME_PROPERTY = "security.context.username.property";
	private static final String SECURITY_CONTEXT_PASSWORD_PROPERTY = "security.context.password.property";

	@Autowired
	private GemfireTemplate echoTemplate;

	@Test
	public void clientServerAuthWasSuccessful() {

		assertThat(this.echoTemplate.<String, String>get("Hello")).isEqualTo("Hello");
		assertThat(this.echoTemplate.<String, String>get("Test")).isEqualTo("Test");
		assertThat(this.echoTemplate.<String, String>get("Good-Bye")).isEqualTo("Good-Bye");
	}

	@Configuration
	@Import(EchoClientConfiguration.class)
	protected static abstract class BaseGemFireClientConfiguration { }

	@Configuration
	@Import(EchoServerConfiguration.class)
	protected static abstract class BaseGemFireServerConfiguration {

		@Bean
		TestSecurityManager testSecurityManager(Environment environment) {
			return new TestSecurityManager(environment);
		}
	}

	public static class TestSecurityManager implements org.apache.geode.security.SecurityManager {

		private final String username;
		private final String password;

		public TestSecurityManager(Environment environment) {

			this.username = Optional.ofNullable(environment.getProperty(SECURITY_CONTEXT_USERNAME_PROPERTY))
				.filter(StringUtils::hasText)
				.orElseThrow(() -> newIllegalArgumentException("Username is required"));

			this.password = Optional.ofNullable(environment.getProperty(SECURITY_CONTEXT_PASSWORD_PROPERTY))
				.filter(StringUtils::hasText)
				.orElseThrow(() -> newIllegalArgumentException("Password is required"));
		}

		private ClassPathResource resolveApplicationProperties(Environment environment) {

			Assert.notNull(environment, "Environment must not be null");

			return Arrays.stream(nullSafeArray(environment.getActiveProfiles(), String.class))
				.filter(StringUtils::hasText)
				.filter(it -> !"default".equalsIgnoreCase(it))
				.map(it -> String.format("application-%s.properties", it))
				.map(ClassPathResource::new)
				.filter(ClassPathResource::exists)
				.findFirst()
				.orElseThrow(() ->
					newIllegalStateException("Unable to resolve application.properties from Environment [%s]",
						environment));

		}

		String getUsername() {
			return username;
		}

		String getPassword() {
			return password;
		}

		@Override
		public Object authenticate(Properties credentials) throws AuthenticationFailedException {

			String username = credentials.getProperty(SECURITY_USERNAME_PROPERTY);
			String password = credentials.getProperty(SECURITY_PASSWORD_PROPERTY);

			if (!(getUsername().equals(username) && getPassword().equals(password))) {
				throw new AuthenticationFailedException(String.format("Failed to authenticate user [%s]", username));
			}

			return User.with(username).having(password);
		}

		@Override
		public boolean authorize(Object principal, ResourcePermission permission) {
			return true;
		}
	}

	@Data
	@ToString(of = "name")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "with")
	static class User implements Principal, Serializable {

		@NonNull
		private String name;

		private String password;

		User having(String password) {
			setPassword(password);
			return this;
		}
	}
}
