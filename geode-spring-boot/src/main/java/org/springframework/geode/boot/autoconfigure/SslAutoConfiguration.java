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

package org.springframework.geode.boot.autoconfigure;

import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.GemFireCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableSsl;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's SSL transport
 * between client and servers when using the client/server topology.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.net.URL
 * @see java.util.Properties
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.ClientCacheAutoConfiguration
 * @see org.springframework.boot.env.EnvironmentPostProcessor
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnableSsl
 * @since 1.0.0
 */
@Configuration
@AutoConfigureBefore(ClientCacheAutoConfiguration.class)
@Conditional(SslAutoConfiguration.EnableSslCondition.class)
@ConditionalOnClass({ CacheFactoryBean.class, GemFireCache.class })
@EnableSsl
@SuppressWarnings("unused")
public class SslAutoConfiguration {

	public static final String SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_DISABLED_PROPERTY =
		"spring.boot.data.gemfire.security.ssl.environment.post-processor.disabled";

	private static final String CURRENT_WORKING_DIRECTORY = System.getProperty("user.dir");
	private static final String GEMFIRE_SSL_KEYSTORE_PROPERTY = "gemfire.ssl-keystore";
	private static final String GEMFIRE_SSL_PROPERTY_SOURCE_NAME = "gemfire-ssl";
	private static final String GEMFIRE_SSL_TRUSTSTORE_PROPERTY = "gemfire.ssl-truststore";
	private static final String SECURITY_SSL_KEYSTORE_PROPERTY = "spring.data.gemfire.security.ssl.keystore";
	private static final String SECURITY_SSL_TRUSTSTORE_PROPERTY = "spring.data.gemfire.security.ssl.truststore";
	private static final String SSL_KEYSTORE_PROPERTY = "ssl-keystore";
	private static final String SSL_TRUSTSTORE_PROPERTY = "ssl-truststore";
	private static final String TRUSTED_KEYSTORE_FILENAME = "trusted.keystore";
	private static final String TRUSTED_KEYSTORE_FILENAME_PROPERTY = "spring.boot.data.gemfire.security.ssl.keystore.name";
	private static final String USER_HOME_DIRECTORY = System.getProperty("user.home");

	private static final Logger logger = LoggerFactory.getLogger(SslAutoConfiguration.class);

	private static boolean isSslConfigured(Environment environment) {

		return (environment.containsProperty(SECURITY_SSL_KEYSTORE_PROPERTY)
				&& environment.containsProperty(SECURITY_SSL_TRUSTSTORE_PROPERTY))
			|| (environment.containsProperty(GEMFIRE_SSL_KEYSTORE_PROPERTY)
				&& environment.containsProperty(GEMFIRE_SSL_TRUSTSTORE_PROPERTY))
			|| (environment.containsProperty(SSL_KEYSTORE_PROPERTY)
				&& environment.containsProperty(SSL_TRUSTSTORE_PROPERTY));
	}

	private static boolean isSslNotConfigured(Environment environment) {
		return !isSslConfigured(environment);
	}

	private static String resolveTrustedKeyStore(Environment environment) {

		return locateKeyStoreInFileSystem(environment)
			.map(File::getAbsolutePath)
			.orElseGet(() -> locateKeyStoreInUserHome(environment)
				.map(File::getAbsolutePath)
				.orElseGet(() -> resolveKeyStoreFromClassPathAsPathname(environment)
					.orElse(null)));
	}

	private static String resolveTrustedKeystoreName(Environment environment) {

		return Optional.ofNullable(environment)
			.filter(it -> environment.containsProperty(TRUSTED_KEYSTORE_FILENAME_PROPERTY))
			.map(it -> environment.getProperty(TRUSTED_KEYSTORE_FILENAME_PROPERTY))
			.orElse(TRUSTED_KEYSTORE_FILENAME);
	}

	private static Optional<String> resolveKeyStoreFromClassPathAsPathname(Environment environment) {

		return resolveKeyStoreFromClassPath(environment)
			.filter(File::isFile)
			.map(File::getAbsolutePath)
			.filter(StringUtils::hasText);
	}

	private static Optional<File> resolveKeyStoreFromClassPath(Environment environment) {

		/*
		System.err.printf("KEYSTORE LOCATION [%s]%n", ObjectUtils.doOperationSafely(() ->
			new File(new ClassPathResource(keystoreName).getURL().toURI())).getAbsolutePath());
		*/

		return locateKeyStoreInClassPath(environment)
			.map(resource -> {

				File trustedKeyStore = null;

				try {

					URL url = resource.getURL();

					if (ResourceUtils.isFileURL(url)) {
						trustedKeyStore = new File(url.toURI());
					}
					else if (ResourceUtils.isJarURL(url)) {
						trustedKeyStore = new File(CURRENT_WORKING_DIRECTORY, resolveTrustedKeystoreName(environment));
						FileCopyUtils.copy(url.openStream(), new FileOutputStream(trustedKeyStore));
					}
				}
				catch (IOException | URISyntaxException cause) {

					if (logger.isWarnEnabled()) {

						logger.warn("Trusted KeyStore {} found in Class Path but is not resolvable as a File: {}",
							resource, cause.getMessage());

						if (logger.isTraceEnabled()) {
							logger.trace("Caused by:", cause);
						}
					}
				}

				return trustedKeyStore;
			});
	}

	private static Optional<ClassPathResource> locateKeyStoreInClassPath(Environment environment) {
		return locateKeyStoreInClassPath(resolveTrustedKeystoreName(environment));
	}

	@SuppressWarnings("all")
	private static Optional<ClassPathResource> locateKeyStoreInClassPath(String keystoreName) {

		return Optional.of(new ClassPathResource(keystoreName))
			.filter(Resource::exists);
	}

	private static Optional<File> locateKeyStoreInFileSystem(Environment environment) {
		return locateKeyStoreInFileSystem(environment, new File(CURRENT_WORKING_DIRECTORY));
	}

	private static Optional<File> locateKeyStoreInFileSystem(Environment environment, File directory) {
		return locateKeyStoreInFileSystem(directory, resolveTrustedKeystoreName(environment));
	}

	private static Optional<File> locateKeyStoreInFileSystem(String keystoreName) {
		return locateKeyStoreInFileSystem(new File(CURRENT_WORKING_DIRECTORY), keystoreName);
	}

	@SuppressWarnings("all")
	private static Optional<File> locateKeyStoreInFileSystem(File directory, String keystoreFilename) {

		assertDirectory(directory);

		//System.err.printf("Searching [%s]...%n", directory);

		for (File file : nullSafeListFiles(directory)) {

			//System.err.printf("Testing [%s]...%n", file);

			if (isDirectory(file)) {

				Optional<File> theFile = locateKeyStoreInFileSystem(file, keystoreFilename);

				if (theFile.isPresent()) {
					return theFile;
				}
				else {
					continue;
				}
			}

			if (file.getName().equals(keystoreFilename)) {
				return Optional.of(file);
			}
		}

		return Optional.empty();
	}

	private static Optional<File> locateKeyStoreInUserHome(Environment environment) {
		return locateKeyStoreInUserHome(resolveTrustedKeystoreName(environment));
	}

	private static Optional<File> locateKeyStoreInUserHome(String keystoreFilename) {

		return Optional.of(new File(USER_HOME_DIRECTORY, keystoreFilename))
			.filter(File::isFile);
	}

	private static void assertDirectory(File path) {
		Assert.isTrue(isDirectory(path), String.format("[%s] is not a valid directory", path));
	}

	private static boolean isDirectory(File path) {
		return path != null && path.isDirectory();
	}

	private static File[] nullSafeListFiles(File directory) {
		return nullSafeArray(directory.listFiles(), File.class);
	}

	@SuppressWarnings("unused")
	static class EnableSslCondition extends AnyNestedCondition {

		public EnableSslCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@Conditional(TrustedKeyStoreIsPresentCondition.class)
		static class TrustedKeyStoreCondition {}

		@ConditionalOnProperty(prefix = "spring.data.gemfire.security.ssl", name = { "keystore", "truststore", })
		static class SpringDataGeodeSslContextCondition {}

		// TODO: ;-)
		@ConditionalOnProperty({
			GEMFIRE_SSL_KEYSTORE_PROPERTY,
			GEMFIRE_SSL_TRUSTSTORE_PROPERTY,
			SSL_KEYSTORE_PROPERTY,
			SSL_TRUSTSTORE_PROPERTY,
		})
		static class StandaloneApacheGeodeSslContextCondition {}

	}

	static class SslEnvironmentPostProcessor implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

			Optional.of(environment)
				.filter(this::isEnabled)
				.filter(SslAutoConfiguration::isSslNotConfigured)
				.map(SslAutoConfiguration::resolveTrustedKeyStore)
				.filter(StringUtils::hasText)
				.ifPresent(trustedKeyStore -> {

					Properties gemfireSslProperties = new Properties();

					gemfireSslProperties.setProperty(SECURITY_SSL_KEYSTORE_PROPERTY, trustedKeyStore);
					gemfireSslProperties.setProperty(SECURITY_SSL_TRUSTSTORE_PROPERTY, trustedKeyStore);

					environment.getPropertySources()
						.addFirst(new PropertiesPropertySource(GEMFIRE_SSL_PROPERTY_SOURCE_NAME, gemfireSslProperties));
				});
		}

		private boolean isEnabled(Environment environment) {
			return !isDisabled(environment);
		}

		private boolean isDisabled(Environment environment) {
			return Boolean.getBoolean(SECURITY_SSL_ENVIRONMENT_POST_PROCESSOR_DISABLED_PROPERTY);
		}
	}

	static class TrustedKeyStoreIsPresentCondition implements Condition {

		@Override
		@SuppressWarnings("all")
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			Environment environment = context.getEnvironment();

			return locateKeyStoreInClassPath(environment).isPresent()
				|| locateKeyStoreInUserHome(environment).isPresent()
				|| locateKeyStoreInFileSystem(environment).isPresent();
		}
	}
}
