/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.geode.boot.autoconfigure;

import java.util.Optional;

import org.apache.geode.cache.GemFireCache;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.geode.boot.autoconfigure.support.PdxInstanceWrapperRegionAspect;
import org.springframework.geode.cache.SimpleCacheResolver;
import org.springframework.geode.data.json.JsonCacheDataImporterExporter;
import org.springframework.lang.NonNull;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for cache data import/export.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.condition.AnyNestedCondition
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see PdxInstanceWrapperRegionAspect
 * @see org.springframework.geode.cache.SimpleCacheResolver
 * @see org.springframework.geode.data.json.JsonCacheDataImporterExporter
 * @since 1.3.0
 */
@Configuration
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ CacheFactoryBean.class, GemFireCache.class })
@SuppressWarnings("unused")
public class DataImportExportAutoConfiguration {

	protected static final String PDX_READ_SERIALIZED_PROPERTY = "spring.data.gemfire.pdx.read-serialized";
	protected static final String REGION_ADVICE_ENABLED_PROPERTY =
		"spring.boot.data.gemfire.cache.region.advice.enabled";

	@Bean
	JsonCacheDataImporterExporter jsonCacheDataImporterExporter() {
		return new JsonCacheDataImporterExporter();
	}

	@Bean
	@Conditional(RegionAdviceConditions.class)
	PdxInstanceWrapperRegionAspect pdxInstanceWrapperAspect() {
		return new PdxInstanceWrapperRegionAspect();
	}

	static class RegionAdviceConditions extends AnyNestedCondition {

		RegionAdviceConditions() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnProperty(name = REGION_ADVICE_ENABLED_PROPERTY, havingValue = "true")
		static class AdviseRegionOnRegionAdviceEnabledProperty { }

		@Conditional(PdxReadSerializedCondition.class)
		static class AdviceRegionOnPdxReadSerializedCondition { }

	}

	static class PdxReadSerializedCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return isPdxReadSerializedTrue(context.getEnvironment()) || isCachePdxReadSerializedTrue();
		}

		private boolean isCachePdxReadSerializedTrue() {

			return SimpleCacheResolver.getInstance().resolve()
				.filter(GemFireCache::getPdxReadSerialized)
				.isPresent();
		}

		private boolean isPdxReadSerializedTrue(@NonNull Environment environment) {

			return Optional.ofNullable(environment)
				.filter(env -> env.getProperty(PDX_READ_SERIALIZED_PROPERTY, Boolean.class, false))
				.isPresent();
		}
	}
}
