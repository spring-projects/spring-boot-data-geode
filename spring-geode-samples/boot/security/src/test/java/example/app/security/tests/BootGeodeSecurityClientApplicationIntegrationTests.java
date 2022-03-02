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
package example.app.security.tests;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ServerOperationException;
import org.apache.geode.security.NotAuthorizedException;

import org.apache.shiro.authz.UnauthorizedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.security.client.BootGeodeSecurityClientApplication;
import example.app.security.client.model.Customer;
import example.app.security.server.BootGeodeSecurityServerApplication;

/**
 * Integration Tests for {@link BootGeodeSecurityClientApplication} and {@link BootGeodeSecurityServerApplication}.
 *
 * @author Patrick Johsnon
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.security.NotAuthorizedException
 * @see org.apache.shiro.authz.UnauthorizedException
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.dao.DataAccessResourceFailureException
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.security.client.BootGeodeSecurityClientApplication
 * @see example.app.security.server.BootGeodeSecurityServerApplication
 * @since 1.3.0
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
	classes = BootGeodeSecurityClientApplication.class,
	properties = {
		"spring.boot.data.gemfire.security.auth.environment.post-processor.enabled=true",
		"spring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=true"
	}
)
@SuppressWarnings("unused")
public class BootGeodeSecurityClientApplicationIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(BootGeodeSecurityServerApplication.class);
	}

	@Autowired
	@Qualifier("customersTemplate")
	@SuppressWarnings("unused")
	private GemfireTemplate customersTemplate;

	@Test(expected = DataAccessResourceFailureException.class)
	public void dataReadNotAllowed() {

		try {
			this.customersTemplate.get(2L);
		}
		catch (DataAccessResourceFailureException expected) {

			assertThat(expected).hasCauseInstanceOf(ServerOperationException.class);
			assertThat(expected.getCause()).hasMessageContaining("remote server");
			assertThat(expected.getCause()).hasCauseInstanceOf(NotAuthorizedException.class);
			assertThat(expected.getCause().getCause()).hasMessageContaining("jdoe not authorized for DATA:READ");
			assertThat(expected.getCause().getCause()).hasCauseInstanceOf(UnauthorizedException.class);
			assertThat(expected.getCause().getCause().getCause())
				.hasMessageContaining("Subject does not have permission [DATA:READ");
			assertThat(expected.getCause().getCause().getCause()).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void dataWriteAllowed() {

		Customer samanthaRogers = Customer.newCustomer(3L, "Samantha Rogers");

		assertThat(this.customersTemplate.put(samanthaRogers.getId(), samanthaRogers)).isNull();
	}
}
