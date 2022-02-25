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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.gemfire.config.annotation.EnablePdx;

/**
 * Spring Boot {@link EnableAutoConfiguration auto-configuration} enabling Apache Geode's PDX Serialization
 * functionality in a either a {@link Cache peer cache} or {@link ClientCache} application.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.SpringBootConfiguration
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @since 1.0.0
 */
@SpringBootConfiguration
@ConditionalOnBean(GemFireCache.class)
@ConditionalOnMissingBean(
	name = { "clientCachePdxConfigurer", "peerCachePdxConfigurer" },
	type = "org.springframework.data.gemfire.config.support.PdxDiskStoreAwareBeanFactoryPostProcessor"
)
@EnablePdx
public class PdxSerializationAutoConfiguration {

}
