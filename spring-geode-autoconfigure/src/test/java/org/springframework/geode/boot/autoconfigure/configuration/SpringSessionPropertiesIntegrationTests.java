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
package org.springframework.geode.boot.autoconfigure.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.pdx.PdxSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;
import org.springframework.geode.boot.autoconfigure.PdxSerializationAutoConfiguration;
import org.springframework.session.data.gemfire.serialization.SessionSerializer;
import org.springframework.session.data.gemfire.serialization.pdx.support.PdxSerializerSessionSerializerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for {@link SpringSessionProperties}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.geode.boot.autoconfigure.configuration.SpringSessionProperties
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("session-config-test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SuppressWarnings("unused")
public class SpringSessionPropertiesIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private ClientCache clientCache;

	@Autowired
	@Qualifier("MockSessionSerializer")
	@SuppressWarnings("rawtypes")
	private SessionSerializer mockSessionSerializer;

	@Autowired
	private SpringSessionProperties springSessionProperties;

	@Before
	public void setup() {

		assertThat(this.clientCache).isNotNull();
		assertThat(this.springSessionProperties).isNotNull();
	}

	@Test
	public void springSessionPropertiesConfigurationIsCorrect() {

		assertThat(this.springSessionProperties.getCache()).isNotNull();
		assertThat(this.springSessionProperties.getCache().getClient().getPool()).isNotNull();
		assertThat(this.springSessionProperties.getCache().getClient().getPool().getName()).isEqualTo("DEAD");
		assertThat(this.springSessionProperties.getCache().getClient().getRegion()).isNotNull();
		assertThat(this.springSessionProperties.getCache().getClient().getRegion().getShortcut())
			.isEqualTo(ClientRegionShortcut.LOCAL);
		assertThat(this.springSessionProperties.getCache().getServer()).isNotNull();
		assertThat(this.springSessionProperties.getCache().getServer().getRegion()).isNotNull();
		assertThat(this.springSessionProperties.getCache().getServer().getRegion().getShortcut())
			.isEqualTo(RegionShortcut.REPLICATE);
		assertThat(this.springSessionProperties.getSession()).isNotNull();
		assertThat(this.springSessionProperties.getSession().getAttributes()).isNotNull();
		assertThat(this.springSessionProperties.getSession().getAttributes().getIndexable())
			.containsExactly("firstName", "lastName");
		assertThat(this.springSessionProperties.getSession().getExpiration()).isNotNull();
		assertThat(this.springSessionProperties.getSession().getExpiration().getMaxInactiveIntervalSeconds())
			.isEqualTo(300);
		assertThat(this.springSessionProperties.getSession().getRegion()).isNotNull();
		assertThat(this.springSessionProperties.getSession().getRegion().getName()).isEqualTo("TestSessions");
		assertThat(this.springSessionProperties.getSession().getSerializer()).isNotNull();
		assertThat(this.springSessionProperties.getSession().getSerializer().getBeanName())
			.isEqualTo("MockSessionSerializer");
	}

	@Test
	public void clientCacheConfigurationIsCorrect() {

		PdxSerializer pdxSerializer = this.clientCache.getPdxSerializer();

		assertThat(pdxSerializer).isInstanceOf(PdxSerializerSessionSerializerAdapter.class);
		assertThat(((PdxSerializerSessionSerializerAdapter<?>) pdxSerializer).getSessionSerializer())
			.isEqualTo(mockSessionSerializer);
	}

	@Test
	public void sessionsRegionConfigurationIsCorrect() {

		Region<?, ?> sessionsRegion = this.clientCache.getRegion("/TestSessions");

		assertThat(sessionsRegion).isNotNull();
		assertThat(sessionsRegion.getName()).isEqualTo("TestSessions");
		assertThat(sessionsRegion.getAttributes()).isNotNull();
		assertThat(sessionsRegion.getAttributes().getPoolName()).isEqualTo("DEAD");
		assertThat(sessionsRegion.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.NORMAL);
		assertThat(sessionsRegion.getAttributes().getEntryIdleTimeout()).isNotNull();
		assertThat(sessionsRegion.getAttributes().getEntryIdleTimeout().getTimeout()).isEqualTo(300);
	}

	@EnableGemFireMockObjects
	@SpringBootApplication(exclude = {
		PdxSerializationAutoConfiguration.class,
		ContinuousQueryAutoConfiguration.class
	})
	static class TestConfiguration {

		@Bean("DEAD")
		Pool deadPool() {
			return mock(Pool.class);
		}

		@Bean("MockSessionSerializer")
		@SuppressWarnings("rawtypes")
		SessionSerializer mockSessionSerializer() {
			return mock(SessionSerializer.class);
		}
	}
}
