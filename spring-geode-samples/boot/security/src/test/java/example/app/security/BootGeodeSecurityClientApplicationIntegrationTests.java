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
package example.app.security;

import example.app.security.client.BootGeodeSecurityClientApplication;
import example.app.security.client.model.Customer;
import example.app.security.server.BootGeodeSecurityServerApplication;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ServerOperationException;
import org.apache.geode.security.NotAuthorizedException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration Tests for {@link BootGeodeSecurityClientApplication} and {@link BootGeodeSecurityServerApplication}.
 *
 * @author Patrick Johsnon
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.security.client.BootGeodeSecurityClientApplication
 * @since 1.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootGeodeSecurityClientApplication.class)
public class BootGeodeSecurityClientApplicationIntegrationTests extends ClientServerIntegrationTestsSupport {

	private static ProcessWrapper geodeServer;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@BeforeClass
	public static void setup() throws IOException {
		int availablePort = findAvailablePort();

		geodeServer = run(BootGeodeSecurityServerApplication.class,
				String.format("-D%s=%d", GEMFIRE_CACHE_SERVER_PORT_PROPERTY, availablePort));

		waitForServerToStart("localhost", availablePort);

		System.setProperty(GEMFIRE_POOL_SERVERS_PROPERTY, String.format("%s[%d]", "localhost", availablePort));
	}

	@Test
	public void dataReadNotAllowed() {
		Exception exception = assertThrows(ServerOperationException.class, () -> customers.get(2L).getName());
		assertThat(exception.getCause()).isInstanceOf(NotAuthorizedException.class);
		assertThat(exception.getCause().getMessage()).contains("jdoe not authorized for DATA:READ");
	}

	@Test
	public void dataWriteAllowed() {
		customers.put(3L, Customer.newCustomer(3L, "Samantha Rogers"));
	}

	@AfterClass
	public static void cleanup() {
		stop(geodeServer);
		System.clearProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY);
	}
}
