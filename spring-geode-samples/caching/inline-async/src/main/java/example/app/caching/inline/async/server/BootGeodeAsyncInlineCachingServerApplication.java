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
package example.app.caching.inline.async.server;

import org.apache.geode.cache.RegionShortcut;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;

import example.app.caching.inline.async.client.model.Golfer;
import example.app.caching.inline.async.config.AsyncInlineCachingConfiguration;

/**
 * {@link SpringBootApplication} class implementing the server-side of the golf tournament management application.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see example.app.caching.inline.async.config.AsyncInlineCachingConfiguration
 * @see example.app.caching.inline.async.client.model.Golfer
 * @since 1.4.0
 */
@SpringBootApplication
@Profile("server")
public class BootGeodeAsyncInlineCachingServerApplication {

	private static final String APPLICATION_NAME = "GolfServerApplication";

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeAsyncInlineCachingServerApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Configuration
	@CacheServerApplication(name = APPLICATION_NAME)
	@EnableEntityDefinedRegions(basePackageClasses = Golfer.class, serverRegionShortcut = RegionShortcut.LOCAL)
	@Import(AsyncInlineCachingConfiguration.class)
	@SuppressWarnings("unused")
	static class GeodeConfiguration { }

}
