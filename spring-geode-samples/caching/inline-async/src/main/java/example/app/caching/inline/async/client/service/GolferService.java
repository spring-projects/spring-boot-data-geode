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
package example.app.caching.inline.async.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.caching.inline.async.client.model.Golfer;
import example.app.caching.inline.async.client.repo.GolferRepository;

/**
 * Spring {@link Service} class used to manage {@link Golfer Golfers} on the tour.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.stereotype.Service
 * @see example.app.caching.inline.async.client.model.Golfer
 * @see example.app.caching.inline.async.client.repo.GolferRepository
 * @since 1.4.0
 */
// tag::class[]
@Service
@SuppressWarnings("unused")
public class GolferService {

	private final GemfireTemplate golfersTemplate;

	private final GolferRepository golferRepository;

	public GolferService(@Qualifier("golfersTemplate") GemfireTemplate golfersTemplate,
			GolferRepository golferRepository) {

		Assert.notNull(golfersTemplate, "GolfersTemplate must not be null");
		Assert.notNull(golferRepository, "GolferRepository must not be null");

		this.golfersTemplate = golfersTemplate;
		this.golferRepository = golferRepository;
	}

	// tag::cache-put[]
	@CachePut(cacheNames = "Golfers", key = "#golfer.name")
	public Golfer update(Golfer golfer) {
		return golfer;
	}
	// end::cache-put[]

	public List<Golfer> getAllGolfersFromCache() {

		Map<String, Golfer> golferMap =
			CollectionUtils.nullSafeMap(this.golfersTemplate.getAll(resolveKeys(this.golfersTemplate.getRegion())));

		return CollectionUtils.sort(new ArrayList<>(golferMap.values()));
	}

	public List<Golfer> getAllGolfersFromDatabase() {
		return CollectionUtils.sort(this.golferRepository.findAll());
	}

	private Set<String> resolveKeys(Region<String, ?> region) {
		return RegionUtils.isClient(region) ? region.keySetOnServer() : region.keySet();
	}
}
// end::class[]
