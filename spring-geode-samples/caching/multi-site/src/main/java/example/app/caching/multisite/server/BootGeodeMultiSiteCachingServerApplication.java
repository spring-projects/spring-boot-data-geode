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
package example.app.caching.multisite.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.wan.GatewayReceiver;
import org.apache.geode.cache.wan.GatewaySender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.PeerRegionFactoryBean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.data.gemfire.wan.GatewayReceiverFactoryBean;
import org.springframework.data.gemfire.wan.GatewaySenderFactoryBean;

import example.app.caching.multisite.client.model.Customer;

/**
 * The {@link BootGeodeMultiSiteCachingServerApplication} class is a Spring Boot, Apache Geode {@literal peer}
 * {@link CacheServer} application serving cache clients.
 *
 * This Apache Geode peer member server data node can also connect to a remote cluster over a WAN using Gateways.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.apache.geode.cache.wan.GatewayReceiver
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.ReplicatedRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @see org.springframework.data.gemfire.wan.GatewayReceiverFactoryBean
 * @see org.springframework.data.gemfire.wan.GatewaySenderFactoryBean
 * @since 1.3.0
 */
// tag::class[]
@SpringBootApplication
@SuppressWarnings("unused")
public class BootGeodeMultiSiteCachingServerApplication {

	private static final boolean PERSISTENT = false;

	private static final int GATEWAY_RECEIVER_END_PORT = 29779;
	private static final int GATEWAY_RECEIVER_START_PORT = 13339;

	private static final String CUSTOMERS_BY_NAME_REGION = "CustomersByName";
	private static final String GATEWAY_RECEIVER_HOSTNAME_FOR_SENDERS = "localhost";

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGeodeMultiSiteCachingServerApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	// tag::geode-server-configuration[]
	@CacheServerApplication(name = "BootGeodeMultiSiteCachingServerApplication", port = 0)
	static class GeodeServerConfiguration {

		@Bean(CUSTOMERS_BY_NAME_REGION)
		ReplicatedRegionFactoryBean<String, Customer> customersByNameRegion(Cache cache,
				@Autowired(required = false) List<RegionConfigurer> regionConfigurers) {

			ReplicatedRegionFactoryBean<String, Customer> customersByName = new ReplicatedRegionFactoryBean<>();

			customersByName.setCache(cache);
			customersByName.setPersistent(PERSISTENT);
			customersByName.setRegionConfigurers(regionConfigurers);

			return customersByName;
		}

		// tag::server-application-runner[]
		@Bean
		ApplicationRunner geodeClusterObjectsBootstrappedAssertionRunner(Environment environment, Cache cache,
				Region<?, ?> customersByName, GatewayReceiver gatewayReceiver, GatewaySender gatewaySender) {

			return args -> {

				assertThat(cache).isNotNull();
				assertThat(cache.getName()).startsWith(BootGeodeMultiSiteCachingServerApplication.class.getSimpleName());
				assertThat(customersByName).isNotNull();
				assertThat(customersByName.getAttributes()).isNotNull();
				assertThat(customersByName.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.REPLICATE);
				assertThat(customersByName.getAttributes().getGatewaySenderIds()).containsExactly(gatewaySender.getId());
				assertThat(customersByName.getName()).isEqualTo(CUSTOMERS_BY_NAME_REGION);
				assertThat(customersByName.getRegionService()).isEqualTo(cache);
				assertThat(cache.getRegion(RegionUtils.toRegionPath(CUSTOMERS_BY_NAME_REGION))).isEqualTo(customersByName);
				assertThat(gatewayReceiver).isNotNull();
				assertThat(gatewayReceiver.isRunning()).isTrue();
				assertThat(cache.getGatewayReceivers()).containsExactly(gatewayReceiver);
				assertThat(gatewaySender).isNotNull();
				assertThat(gatewaySender.isRunning()).isTrue();
				assertThat(cache.getGatewaySenders().stream().map(GatewaySender::getId).collect(Collectors.toSet()))
					.containsExactly(gatewaySender.getId());

				System.err.printf("Apache Geode Cluster [%s] configured and bootstrapped successfully!%n",
					environment.getProperty("spring.application.name", "UNKNOWN"));
			};
		}
		// end::server-application-runner[]
	}
	// end::geode-server-configuration[]

	// tag::locator-manager-configuration[]
	@Configuration
	@EnableLocator
	@EnableManager(start = true)
	@Profile("locator-manager")
	static class GeodeLocatorManagerConfiguration { }
	// end::locator-manager-configuration[]

	// tag::gateway-configuration[]
	// tag::gateway-receiver-configuration[]
	@Configuration
	@Profile("gateway-receiver")
	static class GeodeGatewayReceiverConfiguration {

		@Bean
		GatewayReceiverFactoryBean gatewayReceiver(Cache cache) {

			GatewayReceiverFactoryBean gatewayReceiver = new GatewayReceiverFactoryBean(cache);

			gatewayReceiver.setHostnameForSenders(GATEWAY_RECEIVER_HOSTNAME_FOR_SENDERS);
			gatewayReceiver.setStartPort(GATEWAY_RECEIVER_START_PORT);
			gatewayReceiver.setEndPort(GATEWAY_RECEIVER_END_PORT);

			return gatewayReceiver;
		}
	}
	// end::gateway-receiver-configuration[]

	// tag::gateway-sender-configuration[]
	@Configuration
	@Profile("gateway-sender")
	static class GeodeGatewaySenderConfiguration {

		@Bean
		GatewaySenderFactoryBean customersByNameGatewaySender(Cache cache,
				@Value("${geode.distributed-system.remote.id:1}") int remoteDistributedSystemId) {

			GatewaySenderFactoryBean gatewaySender = new GatewaySenderFactoryBean(cache);

			gatewaySender.setPersistent(PERSISTENT);
			gatewaySender.setRemoteDistributedSystemId(remoteDistributedSystemId);

			return gatewaySender;
		}

		@Bean
		RegionConfigurer customersByNameConfigurer(GatewaySender gatewaySender) {

			return new RegionConfigurer() {

				@Override
				public void configure(String beanName, PeerRegionFactoryBean<?, ?> regionBean) {

					if (CUSTOMERS_BY_NAME_REGION.equals(beanName)) {
						regionBean.setGatewaySenders(ArrayUtils.asArray(gatewaySender));
					}
				}
			};
		}
	}
	// end::gateway-sender-configuration[]
	// end::gateway-configuration[]
}
// end::class[]
