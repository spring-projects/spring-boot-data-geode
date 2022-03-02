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
package org.springframework.geode.boot.autoconfigure;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.geode.cache.GemFireCache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.geode.boot.autoconfigure.support.PdxInstanceWrapperRegionAspect;
import org.springframework.geode.cache.SimpleCacheResolver;
import org.springframework.geode.data.AbstractCacheDataImporterExporter;
import org.springframework.geode.data.CacheDataImporterExporter;
import org.springframework.geode.data.json.JsonCacheDataImporterExporter;
import org.springframework.geode.data.support.LifecycleAwareCacheDataImporterExporter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} for cache data import/export.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.condition.AnyNestedCondition
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.Environment
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.geode.boot.autoconfigure.support.PdxInstanceWrapperRegionAspect
 * @see org.springframework.geode.data.CacheDataImporterExporter
 * @see org.springframework.geode.data.json.JsonCacheDataImporterExporter
 * @see org.springframework.geode.data.support.LifecycleAwareCacheDataImporterExporter
 * @since 1.3.0
 */
@SpringBootConfiguration
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass({ CacheFactoryBean.class, GemFireCache.class })
@SuppressWarnings("unused")
public class DataImportExportAutoConfiguration {

	protected static final String GEMFIRE_DISABLE_SHUTDOWN_HOOK = "gemfire.disableShutdownHook";
	protected static final String PDX_READ_SERIALIZED_PROPERTY = "spring.data.gemfire.pdx.read-serialized";
	protected static final String REGION_ADVICE_ENABLED_PROPERTY =
		"spring.boot.data.gemfire.cache.region.advice.enabled";

	@Bean
	CacheDataImporterExporter jsonCacheDataImporterExporter() {
		return new LifecycleAwareCacheDataImporterExporter(newCacheDataImporterExporter());
	}

	protected CacheDataImporterExporter newCacheDataImporterExporter() {
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
		static class AdviseRegionOnPdxReadSerializedCondition { }

	}

	static class PdxReadSerializedCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return isPdxReadSerializedEnabled(context.getEnvironment()) || isCachePdxReadSerializedEnabled();
		}

		private boolean isCachePdxReadSerializedEnabled() {

			return SimpleCacheResolver.getInstance().resolve()
				.filter(GemFireCache::getPdxReadSerialized)
				.isPresent();
		}

		private boolean isPdxReadSerializedEnabled(@NonNull Environment environment) {

			return Optional.ofNullable(environment)
				.filter(env -> env.getProperty(PDX_READ_SERIALIZED_PROPERTY, Boolean.class, false))
				.isPresent();
		}
	}

	private static final boolean DEFAULT_EXPORT_ENABLED = false;

	private static final Predicate<Environment> disableGemFireShutdownHookPredicate = environment ->
		Optional.ofNullable(environment)
			.filter(env -> env.getProperty(CacheDataImporterExporterReference.EXPORT_ENABLED_PROPERTY_NAME,
				Boolean.class, DEFAULT_EXPORT_ENABLED))
			.isPresent();

	static abstract class AbstractDisableGemFireShutdownHookSupport {

		boolean shouldDisableGemFireShutdownHook(@Nullable Environment environment) {
			return disableGemFireShutdownHookPredicate.test(environment);
		}

		/**
		 * If we do not disable Apache Geode's {@link org.apache.geode.distributed.DistributedSystem} JRE/JVM runtime
		 * shutdown hook then the {@link org.apache.geode.cache.Region} is prematurely closed by the JRE/JVM shutdown hook
		 * before Spring's {@link org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor}s can do
		 * their work of exporting data from the {@link org.apache.geode.cache.Region} as JSON.
		 */
		void disableGemFireShutdownHook(@Nullable Environment environment) {
			System.setProperty(GEMFIRE_DISABLE_SHUTDOWN_HOOK, Boolean.TRUE.toString());
		}
	}

	static abstract class CacheDataImporterExporterReference extends AbstractCacheDataImporterExporter {
		static final String EXPORT_ENABLED_PROPERTY_NAME =
			AbstractCacheDataImporterExporter.CACHE_DATA_EXPORT_ENABLED_PROPERTY_NAME;
	}

	static class DisableGemFireShutdownHookCondition extends AbstractDisableGemFireShutdownHookSupport
			implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return shouldDisableGemFireShutdownHook(context.getEnvironment());
		}
	}

	public static class DisableGemFireShutdownHookEnvironmentPostProcessor
			extends AbstractDisableGemFireShutdownHookSupport implements EnvironmentPostProcessor {

		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

			if (shouldDisableGemFireShutdownHook(environment)) {
				disableGemFireShutdownHook(environment);
			}
		}
	}
}
