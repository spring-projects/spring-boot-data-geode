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
package example.app.caching.inline.async.client.model.support;

import example.app.caching.inline.async.client.model.GolfCourse;

/**
 * A {@literal Builder} class used to build {@link GolfCourse golf courses}.
 *
 * @author John Blum
 * @see example.app.caching.inline.async.client.model.GolfCourse
 * @since 1.4.0
 */
public abstract class GolfCourseBuilder {

	/**
	 * Builds the {@literal Augusta National} {@link GolfCourse}, home of the {@literal Masters} major golf tournament.
	 *
	 * @return a new instance of {@link GolfCourse} modeling {@literal Augusta National} in Augusta, GA; USA.
	 * @see example.app.caching.inline.async.client.model.GolfCourse
	 */
	public static GolfCourse buildAugustaNational() {

		return GolfCourse.newGolfCourse("Augusta National")
			.withHole(1, 4)
			.withHole(2, 5)
			.withHole(3, 4)
			.withHole(4, 3)
			.withHole(5, 4)
			.withHole(6, 3)
			.withHole(7, 4)
			.withHole(8, 5)
			.withHole(9, 4)
			.withHole(10, 4)
			.withHole(11, 4)
			.withHole(12, 3)
			.withHole(13, 5)
			.withHole(14, 4)
			.withHole(15, 5)
			.withHole(16, 3)
			.withHole(17, 4)
			.withHole(18, 4);
	}
}
