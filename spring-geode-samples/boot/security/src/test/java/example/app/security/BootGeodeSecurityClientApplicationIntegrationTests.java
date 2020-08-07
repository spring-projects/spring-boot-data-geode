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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.security.client.BootGeodeSecurityClientApplication
 * @see example.app.security.server.BootGeodeSecurityServerApplication
 * @since 1.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootGeodeSecurityClientApplication.class)
public class BootGeodeSecurityClientApplicationIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	@Autowired
	@Qualifier("customersTemplate")
	@SuppressWarnings("unused")
	private GemfireTemplate customersTemplate;

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(BootGeodeSecurityServerApplication.class);
	}

	@Test
	public void dataReadNotAllowed() {

		Exception exception = assertThrows(DataAccessResourceFailureException.class, () -> this.customersTemplate.get(2L));

		assertThat(exception).hasCauseInstanceOf(ServerOperationException.class);
		assertThat(exception.getCause()).hasMessageContaining("remote server");
		assertThat(exception.getCause()).hasCauseInstanceOf(NotAuthorizedException.class);
		assertThat(exception.getCause().getCause()).hasMessageContaining("jdoe not authorized for DATA:READ");
		assertThat(exception.getCause().getCause()).hasCauseInstanceOf(UnauthorizedException.class);
		assertThat(exception.getCause().getCause().getCause())
			.hasMessageContaining("Subject does not have permission [DATA:READ");
		assertThat(exception.getCause().getCause().getCause()).hasNoCause();
	}

	@Test
	public void dataWriteAllowed() {

		Customer samanthaRogers = Customer.newCustomer(3L, "Samantha Rogers");

		assertThat(this.customersTemplate.put(samanthaRogers.getId(), samanthaRogers)).isNull();
	}
}
