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

package org.springframework.boot.data.geode.autoconfigure;

import org.apache.geode.cache.client.ClientCache;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableSsl;

/**
 * The SslAutoConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass({ ClientCacheFactoryBean.class, ClientCache.class })
@Conditional(SslAutoConfiguration.EnableSslCondition.class)
@AutoConfigureBefore(ClientCacheAutoConfiguration.class)
@EnableSsl
@SuppressWarnings("unused")
public class SslAutoConfiguration {

	@SuppressWarnings("unused")
	static class EnableSslCondition extends AnyNestedCondition {

		public EnableSslCondition() {
			super(ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(prefix = "spring.data.gemfire.security.ssl", name = { "keystore", "truststore", })
		static class SpringDataGeodeSslContextCondition {}

		@ConditionalOnProperty({ "gemfire.ssl-keystore", "gemfire.ssl-truststore", "ssl-keystore", "ssl-truststore", })
		static class StandaloneApacheGeodeSslContextCondition {}
	}
}
