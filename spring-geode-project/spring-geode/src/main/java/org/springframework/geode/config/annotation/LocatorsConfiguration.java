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
package org.springframework.geode.config.annotation;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.Cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.LocatorConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LocatorsConfiguration} class is a Spring {@link Configuration} class used to configure Apache Geode
 * {@literal locators} and/or {@literal remote-locators} properties used by a {@link Cache peer Cache member}
 * to join a cluster of servers when using the P2P topology.
 *
 * The {@literal remote-locators} property is used to configure the Locators that a cluster will use in order to
 * connect to a remote site in a multi-site (WAN) topology configuration.  To use Locators in a WAN configuration,
 * you must specify a unique distributed system ID ({@literal distributed-system-id}) for the local cluster
 * and remote Locator(s) for the remote clusters to which you will connect.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.geode.config.annotation.UseLocators
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class LocatorsConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	protected static final String DEFAULT_LOCATORS = "localhost[10334]";
	protected static final String DEFAULT_REMOTE_LOCATORS = "";
	protected static final String LOCATORS_PROPERTY = "locators";
	protected static final String REMOTE_LOCATORS_PROPERTY = "remote-locators";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String locators;
	private String remoteLocators;

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return UseLocators.class;
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes useLocatorsAttributes = getAnnotationAttributes(importMetadata);

			setLocators(useLocatorsAttributes.containsKey("locators")
				? useLocatorsAttributes.getString("locators") : null);

			setRemoteLocators(useLocatorsAttributes.containsKey("remoteLocators")
				? useLocatorsAttributes.getString("remoteLocators") : null);
		}
	}

	protected void setLocators(String locators) {
		this.locators = StringUtils.hasText(locators) ? locators : null;
	}

	protected Optional<String> getLocators() {

		return Optional.ofNullable(this.locators)
			.filter(StringUtils::hasText);
	}

	protected Logger getLogger() {
		return this.logger;
	}

	protected void setRemoteLocators(String remoteLocators) {
		this.remoteLocators = StringUtils.hasText(remoteLocators) ? remoteLocators : null;
	}

	protected Optional<String> getRemoteLocators() {

		return Optional.ofNullable(this.remoteLocators)
			.filter(StringUtils::hasText);
	}

	@Bean
	ClientCacheConfigurer clientCacheLocatorsConfigurer() {

		return (beanName, clientCacheFactoryBean) -> {

			Logger logger = getLogger();

			getLocators().ifPresent(locators -> {
				if (logger.isWarnEnabled()) {
					logger.warn("The '{}' property was configured [{}];"
						+ " however, this value does not have any effect for ClientCache instances",
							LOCATORS_PROPERTY, locators);
				}
			});

			getRemoteLocators().ifPresent(remoteLocators -> {
				if (logger.isWarnEnabled()) {
					logger.warn("The '{}' property was configured [{}];"
						+ " however, this value does not have any effect for ClientCache instances",
							REMOTE_LOCATORS_PROPERTY, remoteLocators);
				}
			});
		};
	}

	@Bean
	LocatorConfigurer locatorLocatorsConfigurer() {

		return (beanName, locatorFactoryBean) -> {

			Properties gemfireProperties = locatorFactoryBean.getGemFireProperties();

			getLocators().ifPresent(locators -> gemfireProperties.setProperty(LOCATORS_PROPERTY, locators));

			getRemoteLocators().ifPresent(remoteLocators ->
				gemfireProperties.setProperty(REMOTE_LOCATORS_PROPERTY, remoteLocators));
		};
	}

	@Bean
	PeerCacheConfigurer peerCacheLocatorsConfigurer() {

		return (beanName, cacheFactoryBean) -> {

			Properties gemfireProperties = cacheFactoryBean.getProperties();

			getLocators().ifPresent(locators -> gemfireProperties.setProperty(LOCATORS_PROPERTY, locators));

			getRemoteLocators().ifPresent(remoteLocators ->
				gemfireProperties.setProperty(REMOTE_LOCATORS_PROPERTY, remoteLocators));
		};
	}
}
