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
package org.springframework.geode.cloud.bindings.boot;

import java.util.Map;

import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;
import org.springframework.geode.cloud.bindings.Guards;
import org.springframework.geode.cloud.bindings.MapMapper;
import org.springframework.lang.NonNull;

/**
 * A Spring Cloud Bindings {@link BindingsPropertiesProcessor} for Apache Geode.
 *
 * @author John Blum
 * @see java.util.Map
 * @see org.springframework.cloud.bindings.Bindings
 * @see org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.cloud.bindings.Guards
 * @see org.springframework.geode.cloud.bindings.MapMapper
 * @see Guards
 * @since 1.4.1
 */
@SuppressWarnings("unused")
public class GeodeBindingsPropertiesProcessor implements BindingsPropertiesProcessor {

	public static final String TYPE = "gemfire";

	/**
	 * @inheritDoc
	 */
	@Override
	public void process(@NonNull Environment environment, @NonNull Bindings bindings,
			@NonNull Map<String, Object> properties) {

		if (Guards.isTypeEnabled(environment, TYPE)) {

			bindings.filterBindings(TYPE).forEach(binding -> {

				// TODO - Change! Current mappings and property configuration is based on VMware Tanzu GemFire for VMS
				//  (i.e. PCC in PCF).

				MapMapper mapMapper = new MapMapper(binding.getSecret(), properties);

				mapMapper.from("gemfire.security-username").to("spring.data.gemfire.security.username");
				mapMapper.from("gemfire.security-password").to("spring.data.gemfire.security.password");
				mapMapper.from("gemfire.locators").to("spring.data.gemfire.pool.locators");
				mapMapper.from("gemfire.http-service-bind-address").to("spring.data.gemfire.management.http.host");
				mapMapper.from("gemfire.http-service-port").to("spring.data.gemfire.management.http.port");

				properties.put("spring.data.gemfire.management.require-https", true);
				properties.put("spring.data.gemfire.management.use-http", true);
				properties.put("spring.data.gemfire.security.ssl.use-default-context", true);
			});
		}
	}
}
