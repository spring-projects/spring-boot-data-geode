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
package org.springframework.geode.boot.autoconfigure.security.tls;

import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.geode.util.GeodeAssertions;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing the configuration of TLS (e.g. SSL) in a Cloud Platform Environment/Context (e.g. PCF)
 * using live Apache Geode objects.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.geode.boot.autoconfigure.security.tls.AbstractTlsEnabledAutoConfigurationIntegrationTests
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.3.0
 */
@ActiveProfiles("tls")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TlsEnabledAutoConfigurationIntegrationTests.TestConfiguration.class,
	properties = {
		"VCAP_APPLICATION={ \"name\": \"TlsEnabledAutoConfigurationIntegrationTests\", \"uris\": [] }",
		"VCAP_SERVICES={ \"p-cloudcache\": [{ \"credentials\": { \"tls-enabled\": \"true\" }, \"name\": \"jblum-pcc\", \"tags\": [ \"gemfire\", \"cloudcache\", \"database\", \"pivotal\" ]}]}"
	}
)
@SuppressWarnings("unused")
public class TlsEnabledAutoConfigurationIntegrationTests extends AbstractTlsEnabledAutoConfigurationIntegrationTests {

	@Before
	public void testWithLiveGeodeObjects() {
		GeodeAssertions.assertThat(this.clientCache).isInstanceOfGemFireCacheImpl();
		GeodeAssertions.assertThat(this.clientCache.getDistributedSystem()).isInstanceOfInternalDistributedSystem();
	}

	@SpringBootApplication
	@Profile("tls")
	static class TestConfiguration { }

}
