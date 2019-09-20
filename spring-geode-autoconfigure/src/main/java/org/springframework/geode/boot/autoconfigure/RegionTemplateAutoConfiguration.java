/*
 * Copyright 2019 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.ResolvableRegionFactoryBean;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.data.gemfire.util.SpringUtils;
import org.springframework.geode.config.annotation.support.TypelessAnnotationConfigSupport;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} class used to configure a {@link GemfireTemplate}
 * for each Apache Geode / Pivotal GemFire {@link Region} declared/defined in
 * the Spring {@link ConfigurableApplicationContext} in order to perform {@link Region} data access operations.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.boot.autoconfigure.AutoConfigureAfter
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.ResolvableRegionFactoryBean
 * @see org.springframework.geode.config.annotation.support.TypelessAnnotationConfigSupport
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass(GemfireTemplate.class)
@SuppressWarnings("unused")
public class RegionTemplateAutoConfiguration extends TypelessAnnotationConfigSupport {

	private static final Object NON_BEAN = new Object();

	private static final String TEMPLATE = "Template";

	private final Set<String> autoConfiguredRegionTemplateBeanNames = Collections.synchronizedSet(new HashSet<>());
	private final Set<String> regionNamesWithTemplates = Collections.synchronizedSet(new HashSet<>());

	@Bean
	BeanFactoryPostProcessor regionTemplateBeanFactoryPostProcessor() {

		return beanFactory -> {

			if (beanFactory instanceof BeanDefinitionRegistry) {

				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

				List<String> beanDefinitionNames =
					Arrays.asList(ArrayUtils.nullSafeArray(registry.getBeanDefinitionNames(), String.class));

				Set<String> userRegionTemplateNames = new HashSet<>();

				for (String beanName : beanDefinitionNames) {

					String regionTemplateBeanName = toRegionTemplateBeanName(beanName);

					if (!beanDefinitionNames.contains(regionTemplateBeanName)) {

						BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);

						Class<?> resolvedBeanType = resolveBeanClass(beanDefinition, registry).orElse(null);

						if (isRegionBeanDefinition(resolvedBeanType)) {
							register(newGemfireTemplateBeanDefinition(beanName), regionTemplateBeanName, registry);
						}
						else if (isGemfireTemplateBeanDefinition(resolvedBeanType)) {
							userRegionTemplateNames.add(beanName);
						}
						else if (isBeanWithGemfireTemplateDependency(beanFactory, beanDefinition)) {
							SpringUtils.addDependsOn(beanDefinition, GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME);
						}
					}
				}

				setAutoConfiguredRegionTemplateDependencies(registry, userRegionTemplateNames);
			}
		};
	}

	private boolean isBeanWithGemfireTemplateDependency(@NonNull BeanFactory beanFactory,
			@NonNull BeanDefinition beanDefinition) {

		Predicate<Object> isGemfireTemplate = value -> value instanceof RuntimeBeanReference
			? beanFactory.isTypeMatch(((RuntimeBeanReference) value).getBeanName(), GemfireOperations.class)
			: value instanceof GemfireOperations;

		boolean match = beanDefinition.getConstructorArgumentValues().getGenericArgumentValues().stream()
			.map(ConstructorArgumentValues.ValueHolder::getValue)
			.anyMatch(isGemfireTemplate);

		match |= match || beanDefinition.getPropertyValues().getPropertyValueList().stream()
			.map(PropertyValue::getValue)
			.anyMatch(isGemfireTemplate);

		match |= match || Optional.of(beanDefinition)
			.filter(AnnotatedBeanDefinition.class::isInstance)
			.map(AnnotatedBeanDefinition.class::cast)
			.map(AnnotatedBeanDefinition::getFactoryMethodMetadata)
			.filter(StandardMethodMetadata.class::isInstance)
			.map(StandardMethodMetadata.class::cast)
			.map(StandardMethodMetadata::getIntrospectedMethod)
			.map(method -> Arrays.stream(ArrayUtils.nullSafeArray(method.getParameterTypes(), Class.class))
				.filter(Objects::nonNull)
				.anyMatch(GemfireOperations.class::isAssignableFrom)
			).orElse(false);

		return match;
	}

	private boolean isGemfireTemplateBeanDefinition(@Nullable Class<?> beanType) {
		return beanType != null && GemfireOperations.class.isAssignableFrom(beanType);
	}

	private boolean isRegionBeanDefinition(@Nullable Class<?> beanType) {
		return beanType != null && ResolvableRegionFactoryBean.class.isAssignableFrom(beanType);
	}

	private BeanDefinition newGemfireTemplateBeanDefinition(String regionBeanName) {

		BeanDefinitionBuilder builder =
			BeanDefinitionBuilder.genericBeanDefinition(GemfireTemplate.class);

		builder.addConstructorArgReference(regionBeanName);

		return builder.getBeanDefinition();
	}

	// Register BeanDefinition with bean name in BeanDefinitionRegistry
	private boolean register(BeanDefinition beanDefinition, String beanName, BeanDefinitionRegistry registry) {

		if (this.autoConfiguredRegionTemplateBeanNames.add(beanName)) {
			registry.registerBeanDefinition(beanName, beanDefinition);
			return true;
		}

		return false;
	}

	private void setAutoConfiguredRegionTemplateDependencies(BeanDefinitionRegistry registry,
			Set<String> dependencyBeanNames) {

		String[] dependencyBeanNamesArray = dependencyBeanNames.toArray(new String[0]);

		this.autoConfiguredRegionTemplateBeanNames.stream()
			.map(registry::getBeanDefinition)
			.forEach(beanDefinition -> SpringUtils.addDependsOn(beanDefinition, dependencyBeanNamesArray));
	}

	// Required by @EnableClusterDefinedRegions & Native-Defined Regions (e.g. Regions defined in "cache.xml").
	@Bean
	BeanPostProcessor regionTemplateBeanPostProcessor(ConfigurableApplicationContext applicationContext) {

		handlePrematureCacheCreation(applicationContext);

		return new BeanPostProcessor() {

			/**
			 * User-defined {@link GemfireTemplate} beans should be post processed before
			 * auto-configured {@link GemfireTemplate} beans!
			 *
			 * @see RegionTemplateAutoConfiguration#setAutoConfiguredRegionTemplateDependencies(BeanDefinitionRegistry, Set)
			 */
			@Nullable @Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof GemfireTemplate) {
					if (autoConfiguredRegionTemplateBeanNames.contains(beanName)) {
						if (regionNamesWithTemplates.contains(((GemfireTemplate) bean).getRegion().getName())) {
							// Returning NO_BEAN means an existing, user-defined GemfireTemplate bean already exists
							// for the target Region and the auto-configured GemfireTemplate bean is not required.
							bean = NON_BEAN;
						}
					}
					else {
						regionNamesWithTemplates.add(((GemfireTemplate) bean).getRegion().getName());
					}
				}

				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof GemFireCache) {

					GemFireCache cache = (GemFireCache) bean;

					registerRegionTemplatesForCacheRegions(applicationContext, cache);
				}

				return bean;
			}
		};
	}

	// TODO: Remove this logic when DATAGEODE-231 is resolved!
	private void handlePrematureCacheCreation(ConfigurableApplicationContext applicationContext) {

		Optional.ofNullable(GemfireUtils.resolveGemFireCache())
			.ifPresent(cache -> registerRegionTemplatesForCacheRegions(applicationContext, cache));
	}

	// Required by @EnableCachingDefinedRegions
	@EventListener({ ContextRefreshedEvent.class })
	public void regionTemplateContextRefreshedEventListener(ContextRefreshedEvent event) {

		this.regionNamesWithTemplates.clear();

		ApplicationContext applicationContext = event.getApplicationContext();

		if (applicationContext instanceof ConfigurableApplicationContext) {

			ConfigurableApplicationContext configurableApplicationContext =
				(ConfigurableApplicationContext) applicationContext;

			GemFireCache cache = configurableApplicationContext.getBean(GemFireCache.class);

			registerRegionTemplatesForCacheRegions(configurableApplicationContext, cache);
		}
	}

	private void registerRegionTemplatesForCacheRegions(@NonNull ConfigurableApplicationContext applicationContext,
			@NonNull GemFireCache cache) {

		for (Region region : CollectionUtils.nullSafeSet(cache.rootRegions())) {

			String regionTemplateBeanName = toRegionTemplateBeanName(region.getName());

			registerRegionTemplateBean(applicationContext, region, regionTemplateBeanName);
		}
	}

	private void registerRegionTemplateBean(@NonNull ConfigurableApplicationContext applicationContext,
			@NonNull Region region, String regionTemplateBeanName) {

		Optional.of(applicationContext)
			.filter(it -> isNotBean(it, regionTemplateBeanName))
			.map(ConfigurableApplicationContext::getBeanFactory)
			.ifPresent(beanFactory -> register(newGemfireTemplate(region), regionTemplateBeanName, beanFactory));
	}

	private boolean isNotBean(@NonNull ApplicationContext applicationContext, @Nullable String beanName) {
		return !(StringUtils.hasText(beanName) && applicationContext.containsBean(beanName));
	}

	@SuppressWarnings("unchecked")
	private GemfireTemplate newGemfireTemplate(@NonNull Region region) {
		return new GemfireTemplate(region);
	}

	// Register Singleton Object with bean name in BeanDefinitionRegistry
	private void register(Object singletonObject, String beanName, ConfigurableBeanFactory beanFactory) {

		if (this.autoConfiguredRegionTemplateBeanNames.add(beanName)) {
			beanFactory.registerSingleton(beanName, singletonObject);
		}
	}

	private String toRegionTemplateBeanName(@NonNull String regionName) {
		return StringUtils.uncapitalize(regionName) + TEMPLATE;
	}
}
