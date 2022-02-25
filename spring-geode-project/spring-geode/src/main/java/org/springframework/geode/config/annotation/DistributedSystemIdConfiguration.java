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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.util.Assert;

/**
 * The {@link DistributedSystemIdConfiguration} class is a Spring {@link Configuration} class used to configure
 * the {@literal distributed-system-id} for a {@link Cache peer Cache member} in a cluster
 * when using the P2P topology.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.geode.config.annotation.UseDistributedSystemId
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class DistributedSystemIdConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	private static final String GEMFIRE_DISTRIBUTED_SYSTEM_ID_PROPERTY = "distributed-system-id";

	private Integer distributedSystemId;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return UseDistributedSystemId.class;
	}

	@Override
	@SuppressWarnings("all")
	public void setImportMetadata(AnnotationMetadata importMetadata) {

		if (isAnnotationPresent(importMetadata)) {

			AnnotationAttributes distributedSystemIdAttributes = getAnnotationAttributes(importMetadata);

			setDistributedSystemId(distributedSystemIdAttributes.containsKey("value")
				? distributedSystemIdAttributes.getNumber("value") : null);

			setDistributedSystemId(distributedSystemIdAttributes.containsKey("id")
				? distributedSystemIdAttributes.getNumber("id") : null);
		}
	}

	protected void setDistributedSystemId(Integer distributedSystemId) {

		this.distributedSystemId = Optional.ofNullable(distributedSystemId)
			.filter(id -> id > -1)
			.orElse(this.distributedSystemId);
	}

	protected Optional<Integer> getDistributedSystemId() {

		return Optional.ofNullable(this.distributedSystemId)
			.filter(id -> id > -1);
	}

	protected Logger getLogger() {
		return this.logger;
	}

	private int validateDistributedSystemId(int distributedSystemId) {

		Assert.isTrue(distributedSystemId >= -1 && distributedSystemId < 256,
			String.format("Distributed System ID [%d] must be between -1 and 255", distributedSystemId));

		return distributedSystemId;
	}

	@Bean
	ClientCacheConfigurer clientCacheDistributedSystemIdConfigurer() {

		return (beanName, clientCacheFactoryBean) -> getDistributedSystemId().ifPresent(distributedSystemId -> {

			Logger logger = getLogger();

			if (logger.isWarnEnabled()) {
				logger.warn("Distributed System Id [{}] was set on the ClientCache instance, which will not have any effect",
					distributedSystemId);
			}
		});
	}

	@Bean
	PeerCacheConfigurer peerCacheDistributedSystemIdConfigurer() {

		return (beanName, cacheFactoryBean) ->
			getDistributedSystemId().ifPresent(id -> cacheFactoryBean.getProperties()
				.setProperty(GEMFIRE_DISTRIBUTED_SYSTEM_ID_PROPERTY,
					String.valueOf(validateDistributedSystemId(id))));
	}
}
