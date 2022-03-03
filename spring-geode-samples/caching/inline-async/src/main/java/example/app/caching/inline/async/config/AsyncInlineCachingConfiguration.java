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
package example.app.caching.inline.async.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer;

import example.app.caching.inline.async.client.model.Golfer;
import example.app.caching.inline.async.client.repo.GolferRepository;

/**
 * Spring {@link Configuration} class used to configure {@literal Async Inline Caching}.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @see example.app.caching.inline.async.client.model.Golfer
 * @see example.app.caching.inline.async.client.repo.GolferRepository
 * @since 1.4.0
 */
// tag::class[]
@Configuration
@SuppressWarnings("unused")
public class AsyncInlineCachingConfiguration {

	protected static final String GOLFERS_REGION_NAME = "Golfers";

	// tag::queue-batch-size[]
	@Bean
	@Profile("queue-batch-size")
	AsyncInlineCachingRegionConfigurer<Golfer, String> batchSizeAsyncInlineCachingConfigurer(
			@Value("${spring.geode.sample.async-inline-caching.queue.batch-size:25}") int queueBatchSize,
			GolferRepository golferRepository) {

		return AsyncInlineCachingRegionConfigurer.create(golferRepository, GOLFERS_REGION_NAME)
			.withQueueBatchConflationEnabled()
			.withQueueBatchSize(queueBatchSize)
			.withQueueBatchTimeInterval(Duration.ofMinutes(15))
			.withQueueDispatcherThreadCount(1);
	}
	// end::queue-batch-size[]

	// tag::queue-batch-time-interval[]
	@Bean
	@Profile("queue-batch-time-interval")
	AsyncInlineCachingRegionConfigurer<Golfer, String> batchTimeIntervalAsyncInlineCachingConfigurer(
			@Value("${spring.geode.sample.async-inline-caching.queue.batch-time-interval-ms:5000}") int queueBatchTimeIntervalMilliseconds,
			GolferRepository golferRepository) {

		return AsyncInlineCachingRegionConfigurer.create(golferRepository, GOLFERS_REGION_NAME)
			.withQueueBatchSize(1000000)
			.withQueueBatchTimeInterval(Duration.ofMillis(queueBatchTimeIntervalMilliseconds))
			.withQueueDispatcherThreadCount(1);
	}
	// end::queue-batch-time-interval[]
}
// end::class[]
