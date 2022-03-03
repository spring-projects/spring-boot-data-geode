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
package example.app.caching.inline.async.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a golf tournament.
 *
 * @author John Blum
 * @see example.app.caching.inline.async.client.model.Golfer
 * @see example.app.caching.inline.async.client.model.GolfCourse
 * @since 1.4.0
 */
@Getter
@ToString(of = "name")
@RequiredArgsConstructor(staticName = "newGolfTournament")
@SuppressWarnings("unused")
public class GolfTournament implements Iterable<GolfTournament.Pairing> {

	@NonNull
	private final String name;

	private GolfCourse golfCourse;

	private final List<Pairing> pairings = Collections.synchronizedList(new ArrayList<>());

	private final Set<Golfer> players = Collections.synchronizedSet(new HashSet<>());

	public Iterable<Golfer> getPlayers() {
		return Collections.unmodifiableSet(this.players);
	}

	public boolean isFinished() {

		for (Pairing pairing : this) {
			if (pairing.getHole() < 18) {
				return false;
			}
		}

		return true;
	}

	public GolfTournament at(GolfCourse golfCourse) {

		Assert.notNull(golfCourse, "Golf Course must not be null");

		this.golfCourse = golfCourse;

		return this;
	}

	public GolfTournament buildPairings() {

		Assert.notEmpty(this.players,
			() -> String.format("No players are registered for this golf tournament [%s]", getName()));

		Assert.isTrue(this.players.size() % 2 == 0,
			() -> String.format("An even number of players must register to play this golf tournament [%s]; currently at [%d]",
				getName(), this.players.size()));

		List<Golfer> playersToPair = new ArrayList<>(this.players);

		Collections.shuffle(playersToPair);

		for (int index = 0, size = playersToPair.size(); index < size; index += 2) {
			this.pairings.add(Pairing.of(playersToPair.get(index), playersToPair.get(index + 1)));
		}

		return this;
	}

	@Override
	public Iterator<GolfTournament.Pairing> iterator() {
		return Collections.unmodifiableList(this.pairings).iterator();
	}

	public GolfTournament play() {

		Assert.state(this.golfCourse != null, "No golf course was declared");
		Assert.state(!this.players.isEmpty(), "Golfers must register to play before the golf tournament is played");
		Assert.state(!this.pairings.isEmpty(), "Pairings must be formed before the golf tournament is played");
		Assert.state(!isFinished(), () -> String.format("Golf tournament [%s] has already been played", getName()));

		return this;
	}

	public GolfTournament register(Golfer... players) {
		return register(Arrays.asList(ArrayUtils.nullSafeArray(players, Golfer.class)));
	}

	public GolfTournament register(Iterable<Golfer> players) {

		StreamSupport.stream(CollectionUtils.nullSafeIterable(players).spliterator(), false)
			.filter(Objects::nonNull)
			.forEach(this.players::add);

		return this;
	}

	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "of")
	public static class Pairing {

		private final AtomicBoolean signedScorecard = new AtomicBoolean(false);

		@NonNull
		private final Golfer playerOne;

		@NonNull
		private final Golfer playerTwo;

		public synchronized void setHole(int hole) {
			this.playerOne.setHole(hole);
			this.playerTwo.setHole(hole);
		}

		public synchronized int getHole() {
			return getPlayerOne().getHole();
		}

		public boolean in(@NonNull Golfer golfer) {
			return this.playerOne.equals(golfer) || this.playerTwo.equals(golfer);
		}

		public synchronized int nextHole() {
			return getHole() + 1;
		}

		public synchronized boolean signScorecard() {

			return getHole() >= 18
				&& this.signedScorecard.compareAndSet(false, true);
		}
	}
}
