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
package example.app.crm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.crm.model.Customer;
import example.app.crm.repo.CustomerRepository;

/**
 * Integration Tests for the {@link CrmApplication}.
 *
 * @author John Blum
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.crm.CrmApplication
 * @see example.app.crm.model.Customer
 * @see example.app.crm.repo.CustomerRepository
 * @since 1.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
	properties = { "spring.boot.data.gemfire.security.ssl.environment.post-processor.enabled=false" },
	webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@SuppressWarnings("unused")
public class CrmApplicationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	public void jonDoeExists() {

		Customer jonDoe = this.customerRepository.findByNameLike("%Doe");

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getId()).isEqualTo(1L);
		assertThat(jonDoe.getName()).isEqualTo("JonDoe");
	}
}
