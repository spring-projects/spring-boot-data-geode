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
package org.springframework.geode.boot.autoconfigure.security.auth.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.config.admin.remote.RestHttpGemfireAdminTemplate;
import org.springframework.data.gemfire.config.annotation.ClusterConfigurationConfiguration;
import org.springframework.geode.boot.autoconfigure.support.HttpBasicAuthenticationSecurityConfiguration;
import org.springframework.geode.util.GeodeConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * Unit Tests for {@link HttpBasicAuthenticationSecurityConfiguration}
 *
 * @author John Blum
 * @see java.net.Authenticator
 * @see java.net.PasswordAuthentication
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.springframework.geode.boot.autoconfigure.support.HttpBasicAuthenticationSecurityConfiguration
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HttpBasicAuthenticationSecurityConfigurationUnitTests {

	private final TestHttpBasicAuthenticationSecurityConfiguration httpSecurityConfiguration =
		spy(new TestHttpBasicAuthenticationSecurityConfiguration());

	private <T> T constructInstance(Class<T> type, Class<?>[] parameterTypes, Object... args)
			throws Exception {

		Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);

		constructor.setAccessible(true);

		return constructor.newInstance(args);
	}

	@Test
	public void authenticatorIsConfiguredCorrectly() throws Exception {

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq("spring.data.gemfire.security.username"), anyString()))
			.thenReturn("master");

		when(mockEnvironment.getProperty(eq("spring.data.gemfire.security.password"), anyString()))
			.thenReturn("s3cr3t");

		Authenticator authenticator = this.httpSecurityConfiguration.authenticator(mockEnvironment);

		assertThat(authenticator).isNotNull();

		PasswordAuthentication authentication =
			Authenticator.requestPasswordAuthentication(InetAddress.getLocalHost(),	80, "HTTP",
				"?", "BASIC");

		assertThat(authentication).isNotNull();
		assertThat(authentication.getUserName()).isEqualTo("master");
		assertThat(authentication.getPassword()).isEqualTo("s3cr3t".toCharArray());

		verify(mockEnvironment, times(1))
			.getProperty(eq("spring.data.gemfire.security.username"), eq("test"));

		verify(mockEnvironment, times(1))
			.getProperty(eq("spring.data.gemfire.security.password"), eq("test"));
	}

	@Test
	public void schemaObjectInitializerPostProcessorDoesNotProcessNonClusterSchemaObjectInitializerBeans() {

		Object bean = new Object();

		Environment mockEnvironment = mock(Environment.class);

		BeanPostProcessor beanPostProcessor =
			this.httpSecurityConfiguration.schemaObjectInitializerPostProcessor(mockEnvironment);

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.postProcessBeforeInitialization(bean, "testBean")).isEqualTo(bean);
		assertThat(beanPostProcessor.postProcessAfterInitialization(bean, "testBean")).isEqualTo(bean);

		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void schemaObjectInitializerPostProcessorProcessesClusterSchemaObjectInitializerBeans() throws Exception {

		Environment mockEnvironment = mock(Environment.class);

		ClientCache mockClientCache = mock(ClientCache.class);

		ClusterConfigurationConfiguration.SchemaObjectContext schemaObjectContextSpy =
			spy(constructInstance(ClusterConfigurationConfiguration.SchemaObjectContext.class,
				new Class[] { GemFireCache.class }, mockClientCache));

		assertThat(schemaObjectContextSpy).isNotNull();
		assertThat(schemaObjectContextSpy.<ClientCache>getGemfireCache()).isEqualTo(mockClientCache);

		doReturn(new RestHttpGemfireAdminTemplate(mockClientCache))
			.when(schemaObjectContextSpy).getGemfireAdminOperations();

		ClusterConfigurationConfiguration.ClusterSchemaObjectInitializer clusterSchemaObjectInitializer =
			constructInstance(ClusterConfigurationConfiguration.ClusterSchemaObjectInitializer.class,
				new Class[] { ClusterConfigurationConfiguration.SchemaObjectContext.class }, schemaObjectContextSpy);

		BeanPostProcessor beanPostProcessor =
			this.httpSecurityConfiguration.schemaObjectInitializerPostProcessor(mockEnvironment);

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.postProcessBeforeInitialization(clusterSchemaObjectInitializer,
			"mockClusterSchemaObjectInitializer")).isEqualTo(clusterSchemaObjectInitializer);
		assertThat(beanPostProcessor.postProcessAfterInitialization(clusterSchemaObjectInitializer,
			"mockClusterSchemaObjectInitializer")).isEqualTo(clusterSchemaObjectInitializer);
		assertThat(this.httpSecurityConfiguration.restTemplateReference.get()).isNotNull();
		assertThat(this.httpSecurityConfiguration.restTemplateReference.get().getInterceptors()).isNotEmpty();
		assertThat(this.httpSecurityConfiguration.restTemplateReference.get().getInterceptors().stream()
			.anyMatch(ClientHttpRequestInterceptor.class::isInstance)).isTrue();
	}

	@Test
	public void invokeExceptionThrowingObjectMethod() {

		TestObject testObjectSpy = spy(TestObject.INSTANCE);

		assertThat(this.httpSecurityConfiguration.<Object>invokeMethod(testObjectSpy,
			"exceptionThrowingMethod")).isNull();

		verify(testObjectSpy, times(1)).exceptionThrowingMethod();
	}

	@Test
	public void invokeNonExistingObjectMethod() {

		assertThat(this.httpSecurityConfiguration.<Object>invokeMethod(TestObject.INSTANCE,
			"nonExistingMethod")) .isNull();
	}

	@Test
	public void invokeValueReturningClassMethod() {

		assertThat(this.httpSecurityConfiguration.<Object>invokeMethod(TestObject.INSTANCE,
			"valueReturningClassMethod")).isEqualTo("STATIC");
	}

	@Test
	public void invokeValueReturningObjectMethod() {

		assertThat(this.httpSecurityConfiguration.<Object>invokeMethod(TestObject.INSTANCE,
			"valueReturningObjectMethod")).isEqualTo("test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructSecurityAwareClientHttpRequestInterceptorWithNullEnvironment() {

		try {
			new HttpBasicAuthenticationSecurityConfiguration.SecurityAwareClientHttpRequestInterceptor(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Environment is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void securityAwareClientHttpRequestInterceptorSetsHttpRequestHeaders() throws IOException {

		ClientHttpRequestExecution mockExecution = mock(ClientHttpRequestExecution.class);

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq("spring.data.gemfire.security.username"))).thenReturn("master");
		when(mockEnvironment.getProperty(eq("spring.data.gemfire.security.password"))).thenReturn("s3cr3t");

		HttpHeaders httpHeaders = new HttpHeaders();

		HttpRequest mockHttpRequest = mock(HttpRequest.class);

		when(mockHttpRequest.getHeaders()).thenReturn(httpHeaders);

		ClientHttpRequestInterceptor interceptor =
			new HttpBasicAuthenticationSecurityConfiguration.SecurityAwareClientHttpRequestInterceptor(mockEnvironment);

		byte[] body = new byte[0];

		interceptor.intercept(mockHttpRequest, body, mockExecution);

		assertThat(httpHeaders.getFirst(GeodeConstants.USERNAME)).isEqualTo("master");
		assertThat(httpHeaders.getFirst(GeodeConstants.PASSWORD)).isEqualTo("s3cr3t");

		verify(mockHttpRequest, times(1)).getHeaders();
		verify(mockExecution, times(1)).execute(eq(mockHttpRequest), eq(body));
	}

	@Test
	public void securityAwareClientHttpRequestInterceptorWillNotSetHttpRequestHeadersWhenCredentialsAreIncomplete()
			throws IOException {

		ClientHttpRequestExecution mockExecution = mock(ClientHttpRequestExecution.class);

		Environment mockEnvironment = mock(Environment.class);

		when(mockEnvironment.getProperty(eq("spring.data.gemfire.security.username"))).thenReturn("master");

		HttpHeaders httpHeaders = new HttpHeaders();

		HttpRequest mockHttpRequest = mock(HttpRequest.class);

		when(mockHttpRequest.getHeaders()).thenReturn(httpHeaders);

		ClientHttpRequestInterceptor interceptor =
			new HttpBasicAuthenticationSecurityConfiguration.SecurityAwareClientHttpRequestInterceptor(mockEnvironment);

		byte[] body = new byte[0];

		interceptor.intercept(mockHttpRequest, body, mockExecution);

		assertThat(httpHeaders.containsKey(GeodeConstants.USERNAME)).isFalse();
		assertThat(httpHeaders.containsKey(GeodeConstants.PASSWORD)).isFalse();

		verify(mockHttpRequest, never()).getHeaders();
		verify(mockExecution, times(1)).execute(eq(mockHttpRequest), eq(body));
	}

	@SuppressWarnings("unused")
	public static class TestObject {

		public static final TestObject INSTANCE = new TestObject();

		public Object exceptionThrowingMethod() {
			throw new IllegalStateException("TEST");
		}

		public String valueReturningObjectMethod() {
			return "test";
		}

		public static String valueReturningClassMethod() {
			return "STATIC";
		}
	}

	static class TestHttpBasicAuthenticationSecurityConfiguration extends HttpBasicAuthenticationSecurityConfiguration {

		final AtomicReference<RestTemplate> restTemplateReference = new AtomicReference<>();

		@Nullable @Override
		protected <T> T invokeMethod(@NonNull Object target, @NonNull String methodName, Object... args) {
			return super.invokeMethod(target, methodName, args);
		}

		@Override
		protected RestTemplate registerInterceptor(RestTemplate restTemplate,
				ClientHttpRequestInterceptor clientHttpRequestInterceptor) {

			this.restTemplateReference.set(super.registerInterceptor(restTemplate, clientHttpRequestInterceptor));

			return this.restTemplateReference.get();
		}
	}
}
