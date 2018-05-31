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

package example.echo.config;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.util.RegionUtils;

import example.geode.cache.EchoCacheLoader;

/**
 * The {@link EchoClientConfiguration} class is a Spring {@link Configuration} class used to configure
 * a peer {@link Cache} {@link Region} for echo messages.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.PartitionedRegionFactoryBean
 * @see example.geode.cache.EchoCacheLoader
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class EchoServerConfiguration {

	@Bean(EchoClientConfiguration.REGION_NAME)
	public PartitionedRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {

		PartitionedRegionFactoryBean<String, String> echoRegion = new PartitionedRegionFactoryBean<>();

		echoRegion.setCache(gemfireCache);
		echoRegion.setCacheLoader(EchoCacheLoader.INSTANCE);
		echoRegion.setClose(false);
		echoRegion.setPersistent(false);

		return echoRegion;
	}

	@Bean
	public GemfireTemplate echoTemplate(GemFireCache gemfireCache) {

		return new GemfireTemplate(gemfireCache.getRegion(
			RegionUtils.toRegionPath(EchoClientConfiguration.REGION_NAME)));
	}
}
