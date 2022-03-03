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
package example.app.caching.near.client.config;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.InterestResultPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.data.gemfire.client.RegexInterest;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.cache.AbstractCommonEventProcessingCacheListener;

/**
 * Spring {@link Configuration} class to configure Apache Geode client {@link Region Regions}
 * with interest registration on all keys.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.client.Interest
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @since 1.1.0
 */
// tag::class[]
@Configuration
//@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.CACHING_PROXY)
public class GeodeConfiguration {

	// TODO: Replace with the SDG `@EnableCachingDefineRegions annotation declared above (and currently commented out,
	//  because...) once DATAGEODE-219 is resolved. :(
	// tag::region[]
	@Bean("YellowPages")
	public ClientRegionFactoryBean<Object, Object> yellowPagesRegion(GemFireCache gemfireCache) {

		ClientRegionFactoryBean<Object, Object> clientRegion = new ClientRegionFactoryBean<>();

		clientRegion.setCache(gemfireCache);
		clientRegion.setClose(false);
		clientRegion.setShortcut(ClientRegionShortcut.CACHING_PROXY);

		clientRegion.setRegionConfigurers(
			interestRegisteringRegionConfigurer(),
			subscriptionCacheListenerRegionConfigurer()
		);

		return clientRegion;
	}
	// end::region[]

	// tag::interest-registration[]
	@Bean
	RegionConfigurer interestRegisteringRegionConfigurer() {

		return new RegionConfigurer() {

			@Override
			@SuppressWarnings("unchecked")
			public void configure(String beanName, ClientRegionFactoryBean<?, ?> clientRegion) {

				Interest interest = new RegexInterest(".*", InterestResultPolicy.NONE,
					false, true);

				clientRegion.setInterests(ArrayUtils.asArray(interest));
			}
		};
	}
	// end::interest-registration[]

	// tag::subscription-cache-listener[]
	@Bean
	RegionConfigurer subscriptionCacheListenerRegionConfigurer() {

		return new RegionConfigurer() {

			@Override
			@SuppressWarnings("unchecked")
			public void configure(String beanName, ClientRegionFactoryBean<?, ?> clientRegion) {

				CacheListener subscriptionCacheListener =
						new AbstractCommonEventProcessingCacheListener() {

					@Override
					protected void processEntryEvent(EntryEvent event, EntryEventType eventType) {

						if (event.isOriginRemote()) {
							System.err.printf("[%1$s] EntryEvent for [%2$s] with value [%3$s]%n",
								event.getKey(), event.getOperation(), event.getNewValue());
						}
					}
				};

				clientRegion.setCacheListeners(ArrayUtils.asArray(subscriptionCacheListener));
			}
		};
	}
	// end::subscription-cache-listener[]
}
// end::class[]
