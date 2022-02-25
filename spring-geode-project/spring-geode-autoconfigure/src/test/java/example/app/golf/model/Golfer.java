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
package example.app.golf.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * An Abstract Data Type (ADT) that models a person who golfs.
 *
 * @author John Blum
 * @since 1.3.0
 */
@Region("Golfers")
@Getter
@EqualsAndHashCode(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(staticName = "newGolfer")
public class Golfer {

	@Id @NonNull
	private Long id;

	@NonNull
	private String name;

	private Integer handicap;

	public Golfer withHandicap(int handicap) {
		this.handicap = handicap;
		return this;
	}

	@Override
	public String toString() {
		return String.format("%s:%d", getName(), getHandicap());
	}
}
