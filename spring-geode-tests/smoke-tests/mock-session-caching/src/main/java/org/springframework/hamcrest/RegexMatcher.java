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
package org.springframework.hamcrest;

import java.util.regex.Pattern;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A Hamcrest {@link Matcher} using a Java {@link Pattern} with a {@link String Regular Expression}
 * to match a {@link String} argument.
 *
 * @author John Blum
 * @see java.util.regex.Pattern
 * @see org.hamcrest.BaseMatcher
 * @see org.hamcrest.Matcher
 * @since 1.4.0
 */
public class RegexMatcher extends BaseMatcher<String> {

	private final Pattern pattern;

	public static RegexMatcher from(@NonNull String regex) {
		return new RegexMatcher(regex);
	}

	public RegexMatcher(@NonNull String regex) {

		Assert.hasText(regex, () -> String.format("Regular Expression [%s] is required", regex));

		this.pattern = Pattern.compile(regex);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("Matches text to the Regular Expression (Pattern) [%s]",
			this.pattern.toString()));
	}

	@Override
	public boolean matches(Object actual) {
		return this.pattern.matcher(String.valueOf(actual)).matches();
	}
}
