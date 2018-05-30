/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.core.env.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link CloudCacheService}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.core.env.support.CloudCacheService
 * @since 1.0.0
 */
public class CloudCacheServiceUnitTests {

	@Test
	public void withServiceNameLocatorsAndUrlReturnsNewCloudCacheService() throws Exception {

		URL gfshUrl = new URL("http://localhost:7070/v1/gemfire");

		CloudCacheService service = CloudCacheService.with("gemfire")
			.withLocators("boombox[123],cardboardbox[456],mailbox[789],xbox[40404]")
			.withGfshUrl(gfshUrl);

		assertThat(service).isNotNull();
		assertThat(service.getName()).isEqualTo("gemfire");
		assertThat(service.getGfshUrl().orElse(null)).isEqualTo(gfshUrl);
		assertThat(service.getLocators().orElse(null))
			.isEqualTo("boombox[123],cardboardbox[456],mailbox[789],xbox[40404]");

		List<CloudCacheService.Locator> locators = service.getLocatorList();

		assertThat(locators).isNotNull();
		assertThat(locators).hasSize(4);
		assertThat(locators.get(0)).isEqualTo(CloudCacheService.Locator.newLocator("boombox", 123));
		assertThat(locators.get(1)).isEqualTo(CloudCacheService.Locator.newLocator("cardboardbox", 456));
		assertThat(locators.get(2)).isEqualTo(CloudCacheService.Locator.newLocator("mailbox", 789));
		assertThat(locators.get(3)).isEqualTo(CloudCacheService.Locator.newLocator("xbox", 40404));
	}

	@Test
	public void parseLocatorWithSingleLetterHostnameAndPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.parse("x [10336]");

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo("x");
		assertThat(locator.getPort()).isEqualTo(10336);
	}

	@Test
	public void parseLocatorWithNoHostnameAndPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.parse("  [1 234] ");

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_HOST);
		assertThat(locator.getPort()).isEqualTo(1234);
	}

	@Test
	public void parseLocatorWithHostnameAndNoPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.parse(" chatterbox  ");

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo("chatterbox");
		assertThat(locator.getPort()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_PORT);
	}

	private void testParseLocatorWithInvalidHostPort(String hostPort) {

		try {
			CloudCacheService.Locator.parse(hostPort);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Locator host/port [%s] is not valid", hostPort);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseLocatorWithBlankHostPort() {
		testParseLocatorWithInvalidHostPort("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseLocatorWithEmptyHostPort() {
		testParseLocatorWithInvalidHostPort("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseLocatorWithNullHostPort() {
		testParseLocatorWithInvalidHostPort(null);
	}

	@Test
	public void parseLocatorsWithMultipleLocatorHostsPorts() {

		List<CloudCacheService.Locator> locators =
			CloudCacheService.Locator.parseLocators("  jukebox[12345], matchbox [6789] ");

		assertThat(locators).isNotNull();
		assertThat(locators).hasSize(2);
		assertThat(locators).containsExactly(
			CloudCacheService.Locator.newLocator("jukebox", 12345),
			CloudCacheService.Locator.newLocator("matchbox", 6789)
		);
	}

	@Test
	public void parseLocatorsWithNoLocatorHostPort() {

		List<CloudCacheService.Locator> locators = CloudCacheService.Locator.parseLocators("  ");

		assertThat(locators).isNotNull();
		assertThat(locators).isEmpty();
	}

	@Test
	public void parseLocatorsWithSingleLocatorHostPort() {

		List<CloudCacheService.Locator> locators = CloudCacheService.Locator.parseLocators("skullbox[2345]");

		assertThat(locators).isNotNull();
		assertThat(locators).hasSize(1);
		assertThat(locators).containsExactly(CloudCacheService.Locator.newLocator("skullbox", 2345));
	}

	@Test
	public void newLocatorWithHostAndPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.newLocator("toybox", 8008);

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo("toybox");
		assertThat(locator.getPort()).isEqualTo(8008);
	}

	@Test
	public void newLocatorWithHostname() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.newLocator("unbox");

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo("unbox");
		assertThat(locator.getPort()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_PORT);
	}

	@Test
	public void newLocatorWithPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.newLocator(6789);

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_HOST);
		assertThat(locator.getPort()).isEqualTo(6789);
	}

	@Test
	public void newLocatorWithDefaultHostPort() {

		CloudCacheService.Locator locator = CloudCacheService.Locator.newLocator();

		assertThat(locator).isNotNull();
		assertThat(locator.getHost()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_HOST);
		assertThat(locator.getPort()).isEqualTo(CloudCacheService.Locator.DEFAULT_LOCATOR_PORT);
	}

	private void testNewLocatorWithInvalidHost(String host) {

		try {
			CloudCacheService.Locator.newLocator(host);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Host [%s] is required", host);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void newLocatorWithBlankHostnameThrowsIllegalArgumentException() {
		testNewLocatorWithInvalidHost("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newLocatorWithEmptyHostnameThrowsIllegalArgumentException() {
		testNewLocatorWithInvalidHost("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void newLocatorWithNullHostnameThrowsIllegalArgumentException() {
		testNewLocatorWithInvalidHost(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void newLocatorWithInvalidPortNumberThrowsIllegalArgumentException() {

		try {
			CloudCacheService.Locator.newLocator(-2345);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Port [-2345] must be greater than equal to 0");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void locatorToStringPrintsHostPort() {
		assertThat(CloudCacheService.Locator.newLocator("skullbox", 1234).toString())
			.isEqualTo("skullbox[1234]");
	}
}
