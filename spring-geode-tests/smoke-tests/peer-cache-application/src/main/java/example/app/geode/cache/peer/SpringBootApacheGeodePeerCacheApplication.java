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
package example.app.geode.cache.peer;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.ForcedDisconnectException;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.distributed.DistributedSystem;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.geode.distributed.event.ApplicationContextMembershipListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Spring Boot, Apache Geode {@literal peer} {@link Cache} application configured and bootstrapped with the SBDG
 * framework, thereby overriding the convention of creating a {@link ClientCache} instance by default.
 *
 * Additionally, this example class further explores how the Spring Boot configured and bootstrapped Apache Geode
 * {@link PeerCacheApplication} might be self-aware that it is a peer member of a {@link DistributedSystem} (cluster),
 * handling a possible {@link ForcedDisconnectException} and subsequent auto-reconnect, along with
 * cluster configuration.
 *
 * This code and associated Smoke Tests is purely experimental/exploratory.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @since 1.3.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class SpringBootApacheGeodePeerCacheApplication {

	public static void main(String[] args) {

		new SpringApplicationBuilder(SpringBootApacheGeodePeerCacheApplication.class)
			//.contextClass(AnnotationConfigWebApplicationContext.class)
			//.web(WebApplicationType.SERVLET)
			.build()
			.run(args);
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	public ApplicationRunner peerCacheAssertRunner(Cache peerCache) {

		return args -> {

			assertThat(peerCache).isNotNull();
			assertThat(peerCache.getName()).isEqualTo(SpringBootApacheGeodePeerCacheApplication.class.getSimpleName());
			assertThat(peerCache.getDistributedSystem()).isNotNull();
			assertThat(peerCache.getDistributedSystem().getProperties()).isNotNull();
			assertThat(peerCache.getDistributedSystem().getProperties().getProperty("disable-auto-reconnect"))
				.isEqualTo("false");
			assertThat(peerCache.getDistributedSystem().getProperties().getProperty("use-cluster-configuration"))
				.isEqualTo("true");

			this.logger.info("Peer Cache [{}] configured and bootstrapped successfully!", peerCache.getName());
			//System.err.printf("Peer Cache [%s] configured and bootstrapped successfully!%n", peerCache.getName());
		};
	}

	@PeerCacheApplication(name = "SpringBootApacheGeodePeerCacheApplication",
		enableAutoReconnect = true, useClusterConfiguration = true)
	static class GeodeConfiguration {

		@Bean
		ApplicationContextMembershipListener applicationContextMembershipListener(
				ConfigurableApplicationContext applicationContext, Cache peerCache) {

			return new ApplicationContextMembershipListener(applicationContext).register(peerCache);
		}
	}
}
