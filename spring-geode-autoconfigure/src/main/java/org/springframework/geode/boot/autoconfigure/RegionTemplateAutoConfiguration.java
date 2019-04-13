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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.internal.concurrent.ConcurrentHashSet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
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
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} class used to configure a {@link GemfireTemplate}
 * for each Apache Geode / Pivotal GemFire {@link Region} declared/defined in
 * the Spring {@link ConfigurableApplicationContext} in order to perform {@link Region} data access operations.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.beans.factory.config.SingletonBeanRegistry
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @since 1.0.0
 */
@Configuration
@AutoConfigureAfter(ClientCacheAutoConfiguration.class)
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnClass(GemfireTemplate.class)
@SuppressWarnings("unused")
public class RegionTemplateAutoConfiguration {

	private static final Set<String> regionTemplateNames = new ConcurrentHashSet<>();

	private String toRegionTemplateName(String regionName) {
		return StringUtils.uncapitalize(regionName) + "Template";
	}

	@Bean
	BeanPostProcessor regionTemplateBeanPostProcessor(ConfigurableApplicationContext applicationContext) {

		return new BeanPostProcessor() {

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof Region) {

					String regionTemplateName = toRegionTemplateName(beanName);

					registerRegionTemplateBean(regionTemplateName, bean);
				}

				return bean;
			}

			@SuppressWarnings("all")
			private void registerRegionTemplateBean(String regionTemplateName, Object bean) {

				Optional.ofNullable(applicationContext)
					.filter(it -> bean instanceof Region)
					.filter(it -> !it.containsBean(regionTemplateName))
					.filter(it -> isGemfireTemplateWithRegionNotPresent(it, (Region) bean))
					.map(ConfigurableApplicationContext::getBeanFactory)
					.filter(SingletonBeanRegistry.class::isInstance)
					.map(SingletonBeanRegistry.class::cast)
					.ifPresent(beanFactory -> {
						beanFactory.registerSingleton(regionTemplateName, new GemfireTemplate((Region) bean));
						regionTemplateNames.add(regionTemplateName);
					});
			}
		};
	}

	@EventListener({ ContextRefreshedEvent.class })
	public void registerRemainingRegionTemplatesOnContextRefresh(ContextRefreshedEvent event) {

		ApplicationContext applicationContext = event.getApplicationContext();

		if (applicationContext instanceof ConfigurableApplicationContext) {

			ConfigurableListableBeanFactory beanFactory =
				((ConfigurableApplicationContext) applicationContext).getBeanFactory();

			Optional.ofNullable(applicationContext.getBean(GemFireCache.class))
				.map(GemFireCache::rootRegions)
				.ifPresent(rootRegions -> rootRegions.stream()
					.filter(Objects::nonNull)
					.filter(region -> !regionTemplateNames.contains(toRegionTemplateName(region.getName())))
					.filter(region -> !applicationContext.containsBean(toRegionTemplateName(region.getName())))
					.filter(region -> isGemfireTemplateWithRegionNotPresent(applicationContext, region))
					.forEach(region -> beanFactory.registerSingleton(toRegionTemplateName(region.getName()),
						new GemfireTemplate(region))));
		}
	}

	private boolean isGemfireTemplateWithRegionNotPresent(ApplicationContext applicationContext, Region region) {

		Map<String, GemfireTemplate> gemfireTemplateBeans =
			applicationContext.getBeansOfType(GemfireTemplate.class, false, false);

		return CollectionUtils.nullSafeMap(gemfireTemplateBeans).values().stream()
			.map(GemfireTemplate::getRegion)
			.noneMatch(templateRegion -> templateRegion.equals(region));
	}
}
