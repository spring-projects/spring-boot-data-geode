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

import java.io.Closeable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import jakarta.annotation.PreDestroy;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.caching.inline.async.client.model.GolfCourse;
import example.app.caching.inline.async.client.model.GolfTournament;
import example.app.caching.inline.async.client.model.Golfer;

/**
 * Spring {@link Service} class used to manage golf tournaments.
 *
 * @author John Blum
 * @see java.io.Closeable
 * @see org.springframework.scheduling.annotation.Scheduled
 * @see org.springframework.stereotype.Service
 * @see example.app.caching.inline.async.client.model.GolfTournament
 * @see example.app.caching.inline.async.client.model.Golfer
 * @since 1.4.0
 */
// tag::class[]
@Service
@SuppressWarnings("unused")
public class PgaTourService implements Closeable {

	protected static final int SCORE_DELTA_BOUND = 2;

	private final GolferService golferService;

	private volatile GolfTournament golfTournament;

	private final Random random = new Random(System.currentTimeMillis());

	public PgaTourService(GolferService golferService) {

		Assert.notNull(golferService, "GolferService must not be null");

		this.golferService = golferService;
	}

	public Optional<GolfTournament> getGolfTournament() {
		return Optional.ofNullable(this.golfTournament);
	}

	@Override @PreDestroy
	public void close() {
		this.golfTournament = null;
	}

	public boolean isFinished(@Nullable GolfTournament golfTournament) {
		return golfTournament == null || golfTournament.isFinished();
	}

	public boolean isNotFinished(@Nullable GolfTournament golfTournament) {
		return !isFinished(golfTournament);
	}

	public PgaTourService manage(GolfTournament golfTournament) {

		GolfTournament currentGolfTournament = this.golfTournament;

		Assert.state(currentGolfTournament == null,
			() -> String.format("Can only manage 1 golf tournament at a time; currently managing [%s]",
				currentGolfTournament));

		this.golfTournament = golfTournament;

		return this;
	}

	// tag::play[]
	@Scheduled(initialDelay = 5000L, fixedDelay = 2500L)
	public void play() {

		GolfTournament golfTournament = this.golfTournament;

		if (isNotFinished(golfTournament)) {
			playHole(golfTournament);
			finish(golfTournament);
		}
	}
	// end::play[]

	private synchronized void playHole(@NonNull GolfTournament golfTournament) {

		GolfCourse golfCourse = golfTournament.getGolfCourse();

		Set<Integer> occupiedHoles = new HashSet<>();

		for (GolfTournament.Pairing pairing : golfTournament) {

			int hole = pairing.nextHole();

			if (!occupiedHoles.contains(hole)) {
				if (golfCourse.isValidHoleNumber(hole)) {
					occupiedHoles.add(hole);
					pairing.setHole(hole);
					updateScore(this::calculateRunningScore, pairing.getPlayerOne());
					updateScore(this::calculateRunningScore, pairing.getPlayerTwo());
				}
			}
		}
	}

	private Golfer updateScore(@NonNull Function<Integer, Integer> scoreFunction, @NonNull Golfer player) {

		player.setScore(scoreFunction.apply(player.getScore()));

		this.golferService.update(player);

		return player;
	}

	private int calculateFinalScore(@Nullable Integer scoreRelativeToPar) {

		int finalScore = scoreRelativeToPar != null ? scoreRelativeToPar : 0;

		int parForCourse = getGolfTournament()
			.map(GolfTournament::getGolfCourse)
			.map(GolfCourse::getParForCourse)
			.orElse(GolfCourse.STANDARD_PAR_FOR_COURSE);

		return parForCourse + finalScore;
	}

	private int calculateRunningScore(@Nullable Integer currentScore) {

		int runningScore = currentScore != null ? currentScore : 0;
		int scoreDelta = this.random.nextInt(SCORE_DELTA_BOUND);

		scoreDelta *= this.random.nextBoolean() ? -1 : 1;

		return runningScore + scoreDelta;
	}

	private void finish(@NonNull GolfTournament golfTournament) {

		for (GolfTournament.Pairing pairing : golfTournament) {
			if (pairing.signScorecard()) {
				updateScore(this::calculateFinalScore, pairing.getPlayerOne());
				updateScore(this::calculateFinalScore, pairing.getPlayerTwo());
			}
		}
	}
}
// end::class[]
