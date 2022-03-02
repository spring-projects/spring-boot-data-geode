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

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.annotation.Id;
import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a person who plays golf.
 *
 * In addition to the {@link Golfer Golfer's} {@link String name}, this class tracks the current {@link Integer hole}
 * and {@link Integer score} of the {@link Golfer} when s/he competes/plays in a golf tournament.
 *
 * @author John Blum
 * @see javax.persistence.Entity
 * @see javax.persistence.Table
 * @see org.springframework.data.annotation.Id
 * @since 1.4.0
 */
@Entity
@Getter
@ToString(of = "name")
@Table(name = "golfers")
@EqualsAndHashCode(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(staticName = "newGolfer")
@SuppressWarnings("unused")
public class Golfer implements Comparable<Golfer> {

	@javax.persistence.Id @Id @NonNull
	private String name;

	@Setter
	private Integer hole = 0;

	@Setter
	private Integer score = 0;

	@Override
	public int compareTo(Golfer other) {
		return this.getName().compareTo(other.getName());
	}

	private boolean isValidHole(int hole) {
		return hole >= 1 && hole <= 18;
	}

	public Golfer on(int hole) {

		Assert.isTrue(isValidHole(hole), () -> String.format("Hole [%d] must be 1 through 18", hole));

		this.hole = hole;

		return this;
	}

	public Golfer shot(int score) {
		this.score = score;
		return this;
	}
}
