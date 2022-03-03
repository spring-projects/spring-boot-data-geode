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
package example.app.caching.near.client.service.support;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.util.StringUtils;

/**
 * Generates random phone number using a few well-defined area codes.
 *
 * @author John Blum
 * @since 1.1.0
 */
// tag::class[]
public class PhoneNumberGenerator {

	private static int CALIFORNIA_AREA_CODE = 707;
	private static int IOWA_AREA_CODE = 319;
	private static int MONTANA_AREA_CODE = 406;
	private static int NEW_YORK_AREA_CODE = 914;
	private static int OREGON_AREA_CODE = 503;
	private static int WASHINGTON_AREA_CODE = 206;
	private static int WISCONSIN_AREA_CODE = 608;

	private static final List<Integer> PHONE_NUMBER_AREA_CODES = Arrays.asList(
		CALIFORNIA_AREA_CODE,
		IOWA_AREA_CODE,
		MONTANA_AREA_CODE,
		NEW_YORK_AREA_CODE,
		OREGON_AREA_CODE,
		WASHINGTON_AREA_CODE,
		WISCONSIN_AREA_CODE
	);

	private static final Random index = new Random(System.currentTimeMillis());

	public static String generate(String phoneNumber) {

		if (!StringUtils.hasText(phoneNumber)) {

			phoneNumber = String.valueOf(PHONE_NUMBER_AREA_CODES.get(index.nextInt(PHONE_NUMBER_AREA_CODES.size())))
				.concat("-")
				.concat(String.valueOf(index.nextInt(9)))
				.concat(String.valueOf(index.nextInt(9)))
				.concat(String.valueOf(index.nextInt(9)))
				.concat("-")
				.concat(String.valueOf(index.nextInt(9)))
				.concat(String.valueOf(index.nextInt(9)))
				.concat(String.valueOf(index.nextInt(9)))
				.concat(String.valueOf(index.nextInt(9)));
		}

		return phoneNumber;
	}
}
// end::class[]
