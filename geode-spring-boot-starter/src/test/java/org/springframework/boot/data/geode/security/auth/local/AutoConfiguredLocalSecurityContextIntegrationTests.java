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

package org.springframework.boot.data.geode.security.auth.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_PASSWORD_PROPERTY;
import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_USERNAME_PROPERTY;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Properties;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.geode.autoconfigure.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.geode.cache.EchoCacheLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration test testing the auto-configuration of Apache Geode/Pivotal GemFire Security
 * authentication/authorization in a local, non-managed context.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableSecurity
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("security-local")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AutoConfiguredLocalSecurityContextIntegrationTests.GemFireClientConfiguration.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class AutoConfiguredLocalSecurityContextIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEMFIRE_LOG_LEVEL = "error";

	@BeforeClass
	public static void startGemFireServer() throws IOException {
		startGemFireServer(GemFireServerConfiguration.class);
	}

	@Autowired
	private GemfireTemplate echoTemplate;

	@Test
	public void clientServerAuthIsSuccessful() {

		assertThat(this.echoTemplate.<String, String>get("Hello")).isEqualTo("Hello");
		assertThat(this.echoTemplate.<String, String>get("Test")).isEqualTo("Test");
		assertThat(this.echoTemplate.<String, String>get("Good-Bye")).isEqualTo("Good-Bye");
	}

	@EnableLogging(logLevel = GEMFIRE_LOG_LEVEL)
	@SpringBootApplication(exclude = SslAutoConfiguration.class)
	static class GemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration {

		@Bean("Echo")
		public ClientRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setClose(false);
			echoRegion.setShortcut(ClientRegionShortcut.PROXY);

			return echoRegion;
		}

		@Bean
		GemfireTemplate echoTemplate(GemFireCache gemfireCache) {
			return new GemfireTemplate(gemfireCache.getRegion("/Echo"));
		}
	}

	@EnablePdx
	@EnableSecurity(securityManagerClassName =
		"org.springframework.boot.data.geode.security.auth.local.AutoConfiguredLocalSecurityContextIntegrationTests$TestSecurityManager")
	@CacheServerApplication(name = "AutoConfiguredLocalSecurityContextIntegrationTests", logLevel = GEMFIRE_LOG_LEVEL)
	static class GemFireServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(GemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("Echo")
		public PartitionedRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<String, String> echoRegion = new PartitionedRegionFactoryBean<>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setCacheLoader(EchoCacheLoader.INSTANCE);
			echoRegion.setClose(false);
			echoRegion.setPersistent(false);

			return echoRegion;
		}
	}

	public static class TestSecurityManager implements org.apache.geode.security.SecurityManager {

		private final String username;
		private final String password;

		public TestSecurityManager() throws IOException {

			this.username = "ghostrider";
			this.password = "p@55w0rd";

			/*
			Properties securityProperties = new Properties();

			securityProperties.load(new ClassPathResource("application-security-local.properties").getInputStream());

			this.username = Optional.ofNullable(securityProperties.getProperty(SDG_SECURITY_PASSWORD_PROPERTY))
				.filter(StringUtils::hasText)
				.orElseThrow(() -> newIllegalArgumentException("Username is required"));

			this.password = Optional.ofNullable(securityProperties.getProperty(SDG_SECURITY_PASSWORD_PROPERTY))
				.filter(StringUtils::hasText)
				.orElseThrow(() -> newIllegalArgumentException("Password is required"));
			*/
		}

		@Override
		public Object authenticate(Properties credentials) throws AuthenticationFailedException {

			String username = credentials.getProperty(SECURITY_USERNAME_PROPERTY);
			String password = credentials.getProperty(SECURITY_PASSWORD_PROPERTY);

			if (!(this.username.equals(username) && this.password.equals(password))) {
				throw new AuthenticationFailedException(String.format("Failed to authenticate user [%s]", username));
			}

			return User.with(username).having(password);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean authorize(Object principal, ResourcePermission permission) {
			return true;
		}
	}

	@Data
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "with")
	public static class User implements Principal, Serializable {

		@NonNull
		private String name;

		private String password;

		User having(String password) {
			setPassword(password);
			return this;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
