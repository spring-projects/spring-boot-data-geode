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
package example.app.caching.inline.async.client.web;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import example.app.caching.inline.async.client.model.GolfTournament;
import example.app.caching.inline.async.client.model.Golfer;
import example.app.caching.inline.async.client.service.GolfTournamentService;
import example.app.caching.inline.async.client.service.GolferService;

/**
 * Spring Web MVC {@link RestController} used to present a view of a {@link GolfTournament} running.
 *
 * @author John Blum
 * @see example.app.caching.inline.async.client.model.Golfer
 * @see example.app.caching.inline.async.client.model.GolfTournament
 * @see example.app.caching.inline.async.client.service.GolferService
 * @see example.app.caching.inline.async.client.service.GolfTournamentService
 * @see org.springframework.web.bind.annotation.GetMapping
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.RestController
 * @since 1.4.0
 */
@RestController
@RequestMapping("/golf/tournament")
@SuppressWarnings("unused")
public class GolfTournamentController {

	private final GolferService golferService;

	private final GolfTournamentService golfTournamentService;

	public GolfTournamentController(@NonNull GolferService golferService,
			@NonNull GolfTournamentService golfTournamentService) {

		Assert.notNull(golferService, "GolferService must not be null");
		Assert.notNull(golfTournamentService, "GolfTournamentService must not be null");

		this.golferService = golferService;
		this.golfTournamentService = golfTournamentService;
	}

	protected @NonNull GolfTournamentService getGolfTournamentService() {
		return this.golfTournamentService;
	}

	protected @NonNull GolferService getGolferService() {
		return this.golferService;
	}

	@GetMapping("/cache")
	public List<Golfer> getGolfersFromCache() {
		return getGolferService().getAllGolfersFromCache();
	}

	@GetMapping("/database")
	public List<Golfer> getGolfersFromDatabase() {
		return getGolferService().getAllGolfersFromDatabase();
	}
}
