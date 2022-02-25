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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.util.StringUtils;

/**
 * The {@link GroupsConfiguration} class is a Spring {@link Configuration} class used to configure the {@literal groups}
 * in which is member belongs in an Apache Geode distributed system, whether the member is a {@link ClientCache} in a
 * client/server topology or a {@link Cache peer Cache} in a cluster using the P2P topology.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.geode.config.annotation.UseGroups
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class GroupsConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	private static final String GEMFIRE_GROUPS_PROPERTY = "groups";

	private String[] groups = {};

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return UseGroups.class;
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes inGroupsAttributes = getAnnotationAttributes(importMetadata);

			setGroups(inGroupsAttributes.containsKey("value")
				? inGroupsAttributes.getStringArray("value") : null);

			setGroups(inGroupsAttributes.containsKey("groups")
				? inGroupsAttributes.getStringArray("groups") : null);
		}
	}

	protected void setGroups(String[] groups) {

		this.groups = Optional.ofNullable(groups)
			.filter(it -> it.length > 0)
			.orElse(this.groups);
	}

	protected Optional<String[]> getGroups() {

		return Optional.ofNullable(this.groups)
			.filter(it -> it.length > 0);
	}

	@Bean
	ClientCacheConfigurer clientCacheGroupsConfigurer() {
		return (beaName, clientCacheFactoryBean) -> configureGroups(clientCacheFactoryBean);
	}

	@Bean
	PeerCacheConfigurer peerCacheGroupsConfigurer() {
		return (beaName, peerCacheFactoryBean) -> configureGroups(peerCacheFactoryBean);
	}

	private void configureGroups(CacheFactoryBean cacheFactoryBean) {
		getGroups().ifPresent(groups -> cacheFactoryBean.getProperties()
			.setProperty(GEMFIRE_GROUPS_PROPERTY, StringUtils.arrayToCommaDelimitedString(groups)));
	}
}
