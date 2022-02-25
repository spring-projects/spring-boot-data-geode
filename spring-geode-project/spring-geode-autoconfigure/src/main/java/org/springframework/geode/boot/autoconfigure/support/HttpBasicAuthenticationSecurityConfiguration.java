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
package org.springframework.geode.boot.autoconfigure.support;

import static org.springframework.geode.core.util.ObjectUtils.ExceptionThrowingOperation;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.config.admin.remote.RestHttpGemfireAdminTemplate;
import org.springframework.data.gemfire.config.annotation.ClusterConfigurationConfiguration;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.geode.util.GeodeConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Spring {@link Configuration} class used to configure and initialize a Java Platform {@link Authenticator},
 * which is used by the Java Platform anytime a Java process needs to make a secure network connection.
 *
 * @author John Blum
 * @see java.net.Authenticator
 * @see java.net.PasswordAuthentication
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.Environment
 * @see org.springframework.http.client.ClientHttpRequestInterceptor
 * @see org.springframework.web.client.RestTemplate
 * @since 1.0.0
 * @deprecated
 */
@Deprecated
@Configuration
@SuppressWarnings("unused")
public class HttpBasicAuthenticationSecurityConfiguration {

	private static final String DEFAULT_USERNAME = "test";
	private static final String DEFAULT_PASSWORD = DEFAULT_USERNAME;

	private static final String SPRING_DATA_GEMFIRE_SECURITY_USERNAME_PROPERTY =
		"spring.data.gemfire.security.username";

	private static final String SPRING_DATA_GEMFIRE_SECURITY_PASSWORD_PROPERTY =
		"spring.data.gemfire.security.password";

	@Bean
	public Authenticator authenticator(Environment environment) {

		Authenticator authenticator = new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				String username =
					environment.getProperty(SPRING_DATA_GEMFIRE_SECURITY_USERNAME_PROPERTY, DEFAULT_USERNAME);

				String password =
					environment.getProperty(SPRING_DATA_GEMFIRE_SECURITY_PASSWORD_PROPERTY, DEFAULT_PASSWORD);

				return new PasswordAuthentication(username, password.toCharArray());
			}
		};

		Authenticator.setDefault(authenticator);

		return authenticator;
	}

	@Bean
	public BeanPostProcessor schemaObjectInitializerPostProcessor(Environment environment) {

		return new BeanPostProcessor() {

			@Nullable @Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof ClusterConfigurationConfiguration.ClusterSchemaObjectInitializer) {

					Optional.of(bean)
						.map(schemaObjectInitializer -> invokeMethod(schemaObjectInitializer, "getSchemaObjectContext"))
						.filter(ClusterConfigurationConfiguration.SchemaObjectContext.class::isInstance)
						.map(schemaObjectContext -> invokeMethod(schemaObjectContext, "getGemfireAdminOperations"))
						.filter(RestHttpGemfireAdminTemplate.class::isInstance)
						.map(gemfireAdminTemplate -> invokeMethod(gemfireAdminTemplate, "getRestOperations"))
						.filter(RestTemplate.class::isInstance)
						.map(RestTemplate.class::cast)
						.ifPresent(restTemplate -> registerInterceptor(restTemplate,
							new SecurityAwareClientHttpRequestInterceptor(environment)));
				}

				return bean;
			}
		};
	}

	@SuppressWarnings("unchecked")
	protected @Nullable <T> T invokeMethod(@NonNull Object target, @NonNull String methodName, Object... args) {

		ExceptionThrowingOperation<T> operation = () ->

			ObjectUtils.findMethod(target.getClass(), methodName, args)
				.map(ObjectUtils::makeAccessible)
				.map(method -> (T) ReflectionUtils.invokeMethod(method,
					ObjectUtils.resolveInvocationTarget(target, method), args))
				.orElse(null);

		Function<Throwable, T> exceptionHandlingFunction = cause -> null;

		return ObjectUtils.doOperationSafely(operation, exceptionHandlingFunction);
	}

	protected RestTemplate registerInterceptor(RestTemplate restTemplate,
			ClientHttpRequestInterceptor clientHttpRequestInterceptor) {

		restTemplate.getInterceptors().add(clientHttpRequestInterceptor);

		return restTemplate;
	}

	public static class SecurityAwareClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

		private final Environment environment;

		public SecurityAwareClientHttpRequestInterceptor(Environment environment) {

			Assert.notNull(environment, "Environment is required");

			this.environment = environment;
		}

		protected boolean isAuthenticationEnabled() {
			return StringUtils.hasText(getUsername()) && StringUtils.hasText(getPassword());
		}

		protected String getUsername() {
			return this.environment.getProperty(SPRING_DATA_GEMFIRE_SECURITY_USERNAME_PROPERTY);
		}

		protected String getPassword() {
			return this.environment.getProperty(SPRING_DATA_GEMFIRE_SECURITY_PASSWORD_PROPERTY);
		}

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
				ClientHttpRequestExecution execution) throws IOException {

			if (isAuthenticationEnabled()) {

				HttpHeaders requestHeaders = request.getHeaders();

				requestHeaders.add(GeodeConstants.USERNAME, getUsername());
				requestHeaders.add(GeodeConstants.PASSWORD, getPassword());
			}

			return execution.execute(request, body);
		}
	}
}
