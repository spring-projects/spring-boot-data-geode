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
 * The {@link MemberNameConfiguration} class is a Spring {@link Configuration} class used to set
 * an Apache Geode or Pivotal GemFire's name in the distributed system, whether the member
 * is a {@link ClientCache client} in the client/server topology or a {@link Cache peer} member
 * of the cluster.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.geode.config.annotation.UseMemberName
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class MemberNameConfiguration extends AbstractAnnotationConfigSupport implements ImportAware  {

	private static final String GEMFIRE_NAME_PROPERTY = "name";

	private String memberName;

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return UseMemberName.class;
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes memberNameAttributes = getAnnotationAttributes(importMetadata);

			setMemberNameIfNotSet(memberNameAttributes.containsKey("name")
				? memberNameAttributes.getString("name") : null);

			setMemberNameIfNotSet(memberNameAttributes.containsKey("value")
				? memberNameAttributes.getString("value") : null);
		}
	}

	protected void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	protected void setMemberNameIfNotSet(String memberName) {
		setMemberName(this.memberName != null ? this.memberName : memberName);
	}

	protected Optional<String> getMemberName() {

		return Optional.ofNullable(this.memberName)
			.filter(StringUtils::hasText);
	}

	@Bean
	ClientCacheConfigurer clientCacheMemberNameConfigurer() {
		return (beaName, clientCacheFactoryBean) -> configureMemberName(clientCacheFactoryBean);
	}

	@Bean
	PeerCacheConfigurer peerCacheMemberNameConfigurer() {
		return (beaName, peerCacheFactoryBean) -> configureMemberName(peerCacheFactoryBean);
	}

	private void configureMemberName(CacheFactoryBean cacheFactoryBean) {
		getMemberName().ifPresent(memberName ->
			cacheFactoryBean.getProperties().setProperty(GEMFIRE_NAME_PROPERTY, memberName));
	}
}
