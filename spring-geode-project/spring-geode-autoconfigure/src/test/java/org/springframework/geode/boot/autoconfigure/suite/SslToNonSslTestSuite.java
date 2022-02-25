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
package org.springframework.geode.boot.autoconfigure.suite;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.springframework.geode.boot.autoconfigure.cluster.aware.SecureClusterAwareConfigurationIntegrationTests;
import org.springframework.geode.boot.autoconfigure.security.ssl.AutoConfiguredSslIntegrationTests;
import org.springframework.geode.boot.autoconfigure.topology.clientserver.SpringBootApacheGeodeClientServerIntegrationTests;

/**
 * Test Suite that combines Apache Geode SSL Integration Tests with Non-SSL Integration Tests.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runners.Suite
 * @since 1.5.0
 */
@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AutoConfiguredSslIntegrationTests.class,
	SecureClusterAwareConfigurationIntegrationTests.class,
	SpringBootApacheGeodeClientServerIntegrationTests.class
})
public class SslToNonSslTestSuite {

}
