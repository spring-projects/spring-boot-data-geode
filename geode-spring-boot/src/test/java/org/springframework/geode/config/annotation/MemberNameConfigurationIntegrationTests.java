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

package org.springframework.geode.config.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.GemFireCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link UseMemberName} and {@link MemberNameConfiguration}.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class MemberNameConfigurationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private GemFireCache gemfireCache;

	@Test
	public void memberNameIsCorrect() {

		assertThat(this.gemfireCache).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem()).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem().getProperties()).isNotNull();
		assertThat(this.gemfireCache.getDistributedSystem().getProperties().getProperty("name"))
			.isEqualTo("TestClient");
	}

	@ClientCacheApplication
	@EnableGemFireMockObjects
	@UseMemberName("TestClient")
	static class TestConfiguration { }

}
