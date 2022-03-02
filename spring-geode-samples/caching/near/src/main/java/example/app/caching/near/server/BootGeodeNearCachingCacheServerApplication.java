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
package example.app.caching.near.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.server.CacheServer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;

import example.app.caching.near.client.model.Person;

/**
 * Spring Boot application that configures and bootstraps an Apache Geode {@link CacheServer} application.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.RegionAttributesFactoryBean
 * @see org.springframework.data.gemfire.ReplicatedRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @since 1.1.0
 */
// tag::class[]
@SpringBootApplication
@CacheServerApplication
@SuppressWarnings("unused")
public class BootGeodeNearCachingCacheServerApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeNearCachingCacheServerApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	// tag::application-runner[]
	@Bean
	ApplicationRunner runner(@Qualifier("YellowPages") Region<String, Person> yellowPages) {

		return args -> {

			assertThat(yellowPages).isNotNull();
			assertThat(yellowPages.getName()).isEqualTo("YellowPages");
			assertThat(yellowPages.getAttributes()).isNotNull();
			assertThat(yellowPages.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.REPLICATE);
			assertThat(yellowPages.getAttributes().getEnableSubscriptionConflation()).isTrue();

		};
	}
	// end::application-runner[]

	// tag::geode-configuration[]
	@Configuration
	static class GeodeConfiguration {

		@Bean("YellowPages")
		public ReplicatedRegionFactoryBean<String, Person> yellowPagesRegion(GemFireCache gemfireCache,
				@Qualifier("YellowPagesAttributes") RegionAttributes<String, Person> exampleAttributes) {

			ReplicatedRegionFactoryBean<String, Person> yellowPagesRegion =
				new ReplicatedRegionFactoryBean<>();

			yellowPagesRegion.setAttributes(exampleAttributes);
			yellowPagesRegion.setCache(gemfireCache);
			yellowPagesRegion.setClose(false);
			yellowPagesRegion.setPersistent(false);

			return yellowPagesRegion;
		}

		@Bean("YellowPagesAttributes")
		public RegionAttributesFactoryBean<String, Person> exampleRegionAttributes() {

			RegionAttributesFactoryBean<String, Person> yellowPagesRegionAttributes =
				new RegionAttributesFactoryBean<>();

			yellowPagesRegionAttributes.setEnableSubscriptionConflation(true);

			return yellowPagesRegionAttributes;
		}
	}
	// end::geode-configuration[]

	// tag::locator-manager[]
	@Configuration
	@EnableLocator
	@EnableManager(start = true)
	@Profile("locator-manager")
	static class LocatorManagerConfiguration { }
	// end::locator-manager[]

}
// end::class[]
