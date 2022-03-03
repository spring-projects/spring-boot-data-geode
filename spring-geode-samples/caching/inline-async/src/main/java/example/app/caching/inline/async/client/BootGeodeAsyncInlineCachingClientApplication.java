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
package example.app.caching.inline.async.client;

import org.apache.geode.cache.RegionShortcut;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.geode.config.annotation.UseMemberName;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.app.caching.inline.async.client.model.GolfTournament;
import example.app.caching.inline.async.client.model.support.GolfCourseBuilder;
import example.app.caching.inline.async.client.model.support.GolferBuilder;
import example.app.caching.inline.async.client.service.PgaTourService;
import example.app.caching.inline.async.config.AsyncInlineCachingConfiguration;
import example.app.caching.inline.async.config.AsyncInlineCachingRegionConfiguration;

/**
 * {@link SpringBootApplication} class simulating a golf tournament management application.
 *
 * @author John Blum
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @see org.springframework.geode.config.annotation.UseMemberName
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see example.app.caching.inline.async.client.model.Golfer
 * @see example.app.caching.inline.async.client.model.GolfCourse
 * @see example.app.caching.inline.async.client.model.GolfTournament
 * @see example.app.caching.inline.async.client.service.PgaTourService
 * @see example.app.caching.inline.async.config.AsyncInlineCachingConfiguration
 * @see example.app.caching.inline.async.config.AsyncInlineCachingRegionConfiguration
 * @since 1.4.0
 */
// tag::class[]
@SpringBootApplication
@SuppressWarnings("unused")
public class BootGeodeAsyncInlineCachingClientApplication {

	private static final String APPLICATION_NAME = "GolfClientApplication";

	public static void main(String[] args) {
		SpringApplication.run(BootGeodeAsyncInlineCachingClientApplication.class, args);
	}

	// tag::application-configuration[]
	@Configuration
	@EnableScheduling
	static class GolfApplicationConfiguration {

		// tag::application-runner[]
		@Bean
		ApplicationRunner runGolfTournament(PgaTourService pgaTourService) {

			return args -> {

				GolfTournament golfTournament = GolfTournament.newGolfTournament("The Masters")
					.at(GolfCourseBuilder.buildAugustaNational())
					.register(GolferBuilder.buildGolfers(GolferBuilder.FAVORITE_GOLFER_NAMES))
					.buildPairings()
					.play();

				pgaTourService.manage(golfTournament);

			};
		}
		// end::application-runner[]
	}
	// end::application-configuration[]

	// tag::geode-configuration[]
	@Configuration
	@UseMemberName(APPLICATION_NAME)
	@EnableCachingDefinedRegions(serverRegionShortcut = RegionShortcut.REPLICATE)
	static class GeodeConfiguration { }
	// end::geode-configuration[]

	// tag::peer-cache-configuration[]
	@PeerCacheApplication
	@Profile("peer-cache")
	@Import({ AsyncInlineCachingConfiguration.class, AsyncInlineCachingRegionConfiguration.class })
	static class PeerCacheApplicationConfiguration { }
	// end::peer-cache-configuration[]

}
// end::class[]
