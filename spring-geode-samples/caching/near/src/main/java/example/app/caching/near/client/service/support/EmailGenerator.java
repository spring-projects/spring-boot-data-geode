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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Generates {@link String email addresses} given a person's {@link String name}.
 *
 * @author John Blum
 * @since 1.1.0
 */
// tag::class[]
public class EmailGenerator {

	private static final String AT_APPLE_COM = "@apple.com";
	private static final String AT_COMCAST_NET = "@comcast.net";
	private static final String AT_GMAIL_COM = "@gmail.com";
	private static final String AT_HOME_ORG = "@home.org";
	private static final String AT_MICROSOFT_COM = "@microsoft.com";
	private static final String AT_NASA_GOV = "@nasa.gov";
	private static final String AT_PIVOTAL_IO = "@pivotal.io";
	private static final String AT_YAHOO_COM = "@yahoo.com";

	private static final List<String> AT_EMAIL_ADDRESSES = Arrays.asList(
		AT_APPLE_COM,
		AT_COMCAST_NET,
		AT_GMAIL_COM,
		AT_HOME_ORG,
		AT_MICROSOFT_COM,
		AT_NASA_GOV,
		AT_PIVOTAL_IO,
		AT_YAHOO_COM
	);

	private static final Random index = new Random(System.currentTimeMillis());

	public static String generate(String name, String email) {

		Assert.hasText(name, "Name is required");

		if (!StringUtils.hasText(email)) {

			name = name.toLowerCase();
			name = StringUtils.trimAllWhitespace(name);
			email = String.format("%1$s%2$s", name,
				AT_EMAIL_ADDRESSES.get(index.nextInt(AT_EMAIL_ADDRESSES.size())));
		}

		return email;
	}
}
// end::class[]
