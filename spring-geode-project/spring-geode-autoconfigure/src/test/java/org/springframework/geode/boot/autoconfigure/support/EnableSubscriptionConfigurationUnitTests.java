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
package org.springframework.geode.boot.autoconfigure.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.util.Optional;

import org.junit.Test;

import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Unit Tests for {@link EnableSubscriptionConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @since 1.2.0
 */
public class EnableSubscriptionConfigurationUnitTests {

	private final EnableSubscriptionConfiguration configuration = new EnableSubscriptionConfiguration();

	@SuppressWarnings("unchecked")
	private <T> T getFieldValue(@Nullable Object target, @NonNull String fieldName) {

		return Optional.ofNullable(target)
			.map(Object::getClass)
			.map(targetType -> ReflectionUtils.findField(targetType, fieldName))
			.map(field -> {
				ReflectionUtils.makeAccessible(field);
				return field;
			})
			.map(field -> (T) ReflectionUtils.getField(field, target))
			.orElseThrow(() -> newIllegalArgumentException("Unable to get value of field [%s] on object of type [%s]",
				fieldName, target != null ? target.getClass().getName() : null));
	}

	@Test
	public void subscriptionEnabledForClientCachePool() {

		ClientCacheFactoryBean clientCacheFactoryBean = new ClientCacheFactoryBean();

		assertThat(clientCacheFactoryBean).isNotNull();
		assertThat(Boolean.TRUE.equals(clientCacheFactoryBean.getSubscriptionEnabled())).isFalse();

		this.configuration.enableSubscriptionClientCacheConfigurer()
			.configure("testClientCache", clientCacheFactoryBean);

		assertThat(clientCacheFactoryBean.getSubscriptionEnabled()).isTrue();
	}

	@Test
	public void subscriptionEnabledForDefaultPool() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("DEFAULT", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isTrue();
	}

	@Test
	public void subscriptionEnabledForGemFirePool() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("gemfirePool", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isTrue();
	}

	@Test
	public void subscriptionEnabledOnGemFirePool() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("gemfirePool", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isTrue();
	}

	@Test
	public void subscriptionNotEnabledForGeodePool() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("geodePool", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();
	}

	@Test
	public void subscriptionNotEnabledForNonPreciseDefaultPoolCase() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("Default", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();
	}

	@Test
	public void subscriptionNotEnabledForTestPool() {

		PoolFactoryBean poolFactoryBean = new PoolFactoryBean();

		assertThat(poolFactoryBean).isNotNull();
		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();

		this.configuration.enableSubscriptionPoolConfigurer().configure("TEST", poolFactoryBean);

		assertThat(this.<Boolean>getFieldValue(poolFactoryBean, "subscriptionEnabled")).isFalse();
	}
}
