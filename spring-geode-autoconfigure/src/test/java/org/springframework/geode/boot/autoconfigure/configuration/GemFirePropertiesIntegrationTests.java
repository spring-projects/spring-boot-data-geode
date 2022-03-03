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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.RegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.EnableMemcachedServer;
import org.springframework.data.gemfire.config.annotation.EnableSsl;
import org.springframework.data.gemfire.server.SubscriptionEvictionPolicy;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.geode.boot.autoconfigure.configuration.support.CacheProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.CacheServerProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ClientCacheProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ClientSecurityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ClusterProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.DiskStoreProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.EntityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.LocatorProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.LoggingProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ManagementProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ManagerProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PdxProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PeerCacheProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PeerSecurityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.PoolProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.SecurityProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.ServiceProperties;
import org.springframework.geode.boot.autoconfigure.configuration.support.SslProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for {@link GemFireProperties}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.geode.boot.autoconfigure.GemFirePropertiesAutoConfiguration
 * @see org.springframework.geode.boot.autoconfigure.configuration.GemFireProperties
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@ActiveProfiles("gemfire-config-test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SuppressWarnings("unused")
public class GemFirePropertiesIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private GemFireProperties gemfireProperties;

	@Test
	public void gemfireConfigurationIsCorrect() {

		assertThat(this.gemfireProperties).isNotNull();
		assertThat(this.gemfireProperties.getName()).isEqualTo(GemFirePropertiesIntegrationTests.class.getSimpleName());
		assertThat(this.gemfireProperties.getLocators()).containsExactly("localhost[11235]", "localhost[12480]");
		assertThat(this.gemfireProperties.isUseBeanFactoryLocator()).isTrue();
	}

	@Test
	public void cacheConfigurationIsCorrect() {

		CacheProperties cacheProperties = this.gemfireProperties.getCache();

		assertThat(cacheProperties).isNotNull();
		assertThat(cacheProperties.isCopyOnRead()).isTrue();
		assertThat(cacheProperties.getCriticalHeapPercentage()).isEqualTo(85.5f);
		assertThat(cacheProperties.getCriticalOffHeapPercentage()).isEqualTo(95.0f);
		assertThat(cacheProperties.isEnableAutoRegionLookup()).isFalse();
		assertThat(cacheProperties.getEvictionHeapPercentage()).isEqualTo(50.0f);
		assertThat(cacheProperties.getEvictionOffHeapPercentage()).isEqualTo(75.5f);
		assertThat(cacheProperties.getLogLevel()).isEqualTo("TRACE");
		assertThat(cacheProperties.getName()).isEqualTo("AnotherName");
	}

	@Test
	public void cacheClientConfigurationIsCorrect() {

		ClientCacheProperties clientCacheProperties = this.gemfireProperties.getCache().getClient();

		assertThat(clientCacheProperties).isNotNull();
		assertThat(clientCacheProperties.getDurableClientId()).isEqualTo("123");
		assertThat(clientCacheProperties.getDurableClientTimeout()).isEqualTo(60);
		assertThat(clientCacheProperties.isKeepAlive()).isTrue();
	}

	@Test
	public void cacheCompressionConfigurationIsCorrect() {

		CacheProperties.CompressionProperties compressionProperties =
			this.gemfireProperties.getCache().getCompression();

		assertThat(compressionProperties).isNotNull();
		assertThat(compressionProperties.getCompressorBeanName()).isEqualTo("TestCompressor");
		assertThat(compressionProperties.getRegionNames()).containsExactly("TestRegionOne", "TestRegionTwo");
	}

	@Test
	public void cacheOffHeapConfigurationIsCorrect() {

		CacheProperties.OffHeapProperties offHeapProperties = this.gemfireProperties.getCache().getOffHeap();

		assertThat(offHeapProperties).isNotNull();
		assertThat(offHeapProperties.getMemorySize()).isEqualTo("65535g");
		assertThat(offHeapProperties.getRegionNames()).containsExactly("TestRegionTwo", "TestRegionFour");
	}

	@Test
	public void cachePeerConfigurationIsCorrect() {

		PeerCacheProperties peerCacheProperties = this.gemfireProperties.getCache().getPeer();

		assertThat(peerCacheProperties).isNotNull();
		assertThat(peerCacheProperties.isEnableAutoReconnect()).isTrue();
		assertThat(peerCacheProperties.getLockLease()).isEqualTo(30);
		assertThat(peerCacheProperties.getLockTimeout()).isEqualTo(5);
		assertThat(peerCacheProperties.getMessageSyncInterval()).isEqualTo(2);
		assertThat(peerCacheProperties.getSearchTimeout()).isEqualTo(120);
		assertThat(peerCacheProperties.isUseClusterConfiguration()).isTrue();
	}

	@Test
	public void cacheServerConfigurationIsCorrect() {

		CacheServerProperties cacheServerProperties = this.gemfireProperties.getCache().getServer();

		assertThat(cacheServerProperties).isNotNull();
		assertThat(cacheServerProperties.isAutoStartup()).isFalse();
		assertThat(cacheServerProperties.getBindAddress()).isEqualTo("10.50.100.200");
		assertThat(cacheServerProperties.getHostnameForClients()).isEqualTo("skullbox");
		assertThat(cacheServerProperties.getLoadPollInterval()).isEqualTo(20000L);
		assertThat(cacheServerProperties.getMaxConnections()).isEqualTo(256);
		assertThat(cacheServerProperties.getMaxMessageCount()).isEqualTo(100000);
		assertThat(cacheServerProperties.getMaxThreads()).isEqualTo(16);
		assertThat(cacheServerProperties.getMaxTimeBetweenPings()).isEqualTo(15000);
		assertThat(cacheServerProperties.getMessageTimeToLive()).isEqualTo(60);
		assertThat(cacheServerProperties.getPort()).isEqualTo(12345);
		assertThat(cacheServerProperties.getSocketBufferSize()).isEqualTo(8192);
		assertThat(cacheServerProperties.getSubscriptionCapacity()).isEqualTo(2);
		assertThat(cacheServerProperties.getSubscriptionDiskStoreName()).isEqualTo("TestSubscriptionDiskStore");
		assertThat(cacheServerProperties.getSubscriptionEvictionPolicy()).isEqualTo(SubscriptionEvictionPolicy.ENTRY);
		assertThat(cacheServerProperties.isTcpNoDelay()).isFalse();
	}

	@Test
	public void clusterConfigurationIsCorrect() {

		ClusterProperties clusterProperties = this.gemfireProperties.getCluster();

		assertThat(clusterProperties).isNotNull();
		assertThat(clusterProperties.getRegion()).isNotNull();
		assertThat(clusterProperties.getRegion().getType()).isEqualTo(RegionShortcut.PARTITION_REDUNDANT_PERSISTENT);
	}

	@Test
	public void diskStoreConfigurationIsCorrect() {

		DiskStoreProperties diskStoreProperties = this.gemfireProperties.getDisk();

		assertThat(diskStoreProperties).isNotNull();

		DiskStoreProperties.StoreProperties storeProperties = diskStoreProperties.getStore();

		assertThat(storeProperties).isNotNull();
		assertThat(storeProperties.isAllowForceCompaction()).isTrue();
		assertThat(storeProperties.isAutoCompact()).isFalse();
		assertThat(storeProperties.getCompactionThreshold()).isEqualTo(75);
		assertThat(storeProperties.getDiskUsageCriticalPercentage()).isEqualTo(95.0f);
		assertThat(storeProperties.getDiskUsageWarningPercentage()).isEqualTo(89.0f);
		assertThat(storeProperties.getMaxOplogSize()).isEqualTo(4096L);
		assertThat(storeProperties.getQueueSize()).isEqualTo(1000);
		assertThat(storeProperties.getTimeInterval()).isEqualTo(2000L);
		assertThat(storeProperties.getWriteBufferSize()).isEqualTo(65535);
	}

	@Test
	public void diskStoreDirectoryConfigurationIsCorrect() {

		DiskStoreProperties.DirectoryProperties[] directoryProperties =
			this.gemfireProperties.getDisk().getStore().getDirectory();

		assertThat(directoryProperties).isNotEmpty();
		assertThat(directoryProperties).hasSize(2);
		assertThat(directoryProperties[0].getLocation()).isEqualTo("/path/to/no/where");
		assertThat(directoryProperties[0].getSize()).isEqualTo(8192);
		assertThat(directoryProperties[1].getLocation()).isEqualTo("/path/to/some/where");
		assertThat(directoryProperties[1].getSize()).isEqualTo(2048);
	}

	@Test
	public void entityConfigurationIsCorrect() {

		EntityProperties entityProperties = this.gemfireProperties.getEntities();

		assertThat(entityProperties).isNotNull();
		assertThat(entityProperties.getBasePackages()).isEqualTo("example.app.books.model");
	}

	@Test
	public void locatorConfigurationIsCorrect() {

		LocatorProperties locatorProperties = this.gemfireProperties.getLocator();

		assertThat(locatorProperties).isNotNull();
		assertThat(locatorProperties.getHost()).isEqualTo("mailbox");
		assertThat(locatorProperties.getPort()).isEqualTo(20668);
	}

	@Test
	public void loggingConfigurationIsCorrect() {

		LoggingProperties loggingProperties = this.gemfireProperties.getLogging();

		assertThat(loggingProperties).isNotNull();
		assertThat(loggingProperties.getLevel()).isEqualTo("CONFIG");
		assertThat(loggingProperties.getLogDiskSpaceLimit()).isEqualTo(2048);
		assertThat(loggingProperties.getLogFile()).isEqualTo("/path/to/geode.log");
		assertThat(loggingProperties.getLogFileSizeLimit()).isEqualTo(50);
	}

	@Test
	public void managementConfigurationIsCorrect() {

		ManagementProperties managementProperties = this.gemfireProperties.getManagement();

		assertThat(managementProperties).isNotNull();
		assertThat(managementProperties.isUseHttp()).isTrue();
		assertThat(managementProperties.getHttp()).isNotNull();
		assertThat(managementProperties.getHttp().getHost()).isEqualTo("boombox");
		assertThat(managementProperties.getHttp().getPort()).isEqualTo(8181);
	}

	@Test
	public void managerConfigurationIsCorrect() {

		ManagerProperties managerProperties = this.gemfireProperties.getManager();

		assertThat(managerProperties).isNotNull();
		assertThat(managerProperties.getAccessFile()).isEqualTo("/path/to/access/control/list.sys");
		assertThat(managerProperties.getBindAddress()).isEqualTo("10.100.50.255");
		assertThat(managerProperties.getHostnameForClients()).isEqualTo("bosshog");
		assertThat(managerProperties.getPasswordFile()).isEqualTo("/path/to/password.sys");
		assertThat(managerProperties.getPort()).isEqualTo(9090);
		assertThat(managerProperties.isStart()).isTrue();
		assertThat(managerProperties.getUpdateRate()).isEqualTo(5000);
	}

	@Test
	public void pdxConfigurationIsCorrect() {

		PdxProperties pdxProperties = this.gemfireProperties.getPdx();

		assertThat(pdxProperties).isNotNull();
		assertThat(pdxProperties.getDiskStoreName()).isEqualTo("TestPdxDiskStore");
		assertThat(pdxProperties.isIgnoreUnreadFields()).isTrue();
		assertThat(pdxProperties.isPersistent()).isTrue();
		assertThat(pdxProperties.isReadSerialized()).isTrue();
		assertThat(pdxProperties.getSerializerBeanName()).isEqualTo("TestPdxSerializer");
	}

	@Test
	public void poolConfigurationIsCorrect() {

		PoolProperties poolProperties = this.gemfireProperties.getPool();

		assertThat(poolProperties).isNotNull();
		assertThat(poolProperties.getFreeConnectionTimeout()).isEqualTo(15000);
		assertThat(poolProperties.getIdleTimeout()).isEqualTo(30000L);
		assertThat(poolProperties.getLoadConditioningInterval()).isEqualTo(120000);
		assertThat(poolProperties.getLocators()).containsExactly("boombox[10334]", "mailbox[11235]", "skullbox[20668]");
		assertThat(poolProperties.getMaxConnections()).isEqualTo(100);
		assertThat(poolProperties.getMinConnections()).isEqualTo(10);
		assertThat(poolProperties.isMultiUserAuthentication()).isTrue();
		assertThat(poolProperties.getPingInterval()).isEqualTo(15000L);
		assertThat(poolProperties.isPrSingleHopEnabled()).isFalse();
		assertThat(poolProperties.getReadTimeout()).isEqualTo(5000);
		assertThat(poolProperties.isReadyForEvents()).isTrue();
		assertThat(poolProperties.getRetryAttempts()).isEqualTo(2);
		assertThat(poolProperties.getServerGroup()).isEqualTo("TestServerGroup");
		assertThat(poolProperties.getServers()).containsExactly("cardboardbox[41414]");
		assertThat(poolProperties.getSocketBufferSize()).isEqualTo(65535);
		assertThat(poolProperties.getStatisticInterval()).isEqualTo(500);
		assertThat(poolProperties.getSubscriptionAckInterval()).isEqualTo(500);
		assertThat(poolProperties.isSubscriptionEnabled()).isTrue();
		assertThat(poolProperties.getSubscriptionMessageTrackingTimeout()).isEqualTo(450000);
		assertThat(poolProperties.getSubscriptionRedundancy()).isEqualTo(2);
		assertThat(poolProperties.isThreadLocalConnections()).isTrue();
	}

	@Test
	public void securityConfigurationIsCorrect() {

		SecurityProperties securityProperties = this.gemfireProperties.getSecurity();

		assertThat(securityProperties).isNotNull();
		assertThat(securityProperties.getUsername()).isEqualTo("TestUser");
		assertThat(securityProperties.getPassword()).isEqualTo("TestPassword");
		assertThat(securityProperties.getPropertiesFile()).isEqualTo("/path/to/security.properties");
	}

	@Test
	public void securityClientConfigurationIsCorrect() {

		ClientSecurityProperties clientSecurityProperties = this.gemfireProperties.getSecurity().getClient();

		assertThat(clientSecurityProperties).isNotNull();
		assertThat(clientSecurityProperties.getAccessor()).isEqualTo("TestClientAccessor");
		assertThat(clientSecurityProperties.getAccessorPostProcessor()).isEqualTo("TestClientAccessorPostProcessor");
		assertThat(clientSecurityProperties.getAuthenticationInitializer()).isEqualTo("TestClientAuthenticationInitializer");
		assertThat(clientSecurityProperties.getAuthenticator()).isEqualTo("TestClientAuthenticator");
		assertThat(clientSecurityProperties.getDiffieHellmanAlgorithm()).isEqualTo("RSA");
	}

	@Test
	public void securityLogConfigurationIsCorrect() {

		SecurityProperties.SecurityLogProperties securityLogProperties = this.gemfireProperties.getSecurity().getLog();

		assertThat(securityLogProperties).isNotNull();
		assertThat(securityLogProperties.getFile()).isEqualTo("/path/to/security.log");
		assertThat(securityLogProperties.getLevel()).isEqualTo("info");
	}

	@Test
	public void securityManagerConfigurationIsCorrect() {

		SecurityProperties.SecurityManagerProperties securityManagerProperties =
			this.gemfireProperties.getSecurity().getManager();

		assertThat(securityManagerProperties).isNotNull();
		assertThat(securityManagerProperties.getClassName()).isEqualTo("example.app.security.manager.TestSecurityManager");
	}

	@Test
	public void securityPeerConfigurationIsCorrect() {

		PeerSecurityProperties peerSecurityProperties = this.gemfireProperties.getSecurity().getPeer();

		assertThat(peerSecurityProperties).isNotNull();
		assertThat(peerSecurityProperties.getAuthenticationInitializer()).isEqualTo("TestPeerAuthenticationInitializer");
		assertThat(peerSecurityProperties.getAuthenticator()).isEqualTo("TestPeerAuthenticator");
	}

	@Test
	public void securityPostProcessorConfigurationIsCorrect() {

		SecurityProperties.SecurityPostProcessorProperties securityPostProcessorProperties =
			this.gemfireProperties.getSecurity().getPostProcessor();

		assertThat(securityPostProcessorProperties).isNotNull();
		assertThat(securityPostProcessorProperties.getClassName())
			.isEqualTo("example.app.security.processor.TestSecurityPostProcessor");
	}

	@Test
	public void securityShiroConfigurationIsCorrect() {

		SecurityProperties.ApacheShiroProperties shiroProperties = this.gemfireProperties.getSecurity().getShiro();

		assertThat(shiroProperties).isNotNull();
		assertThat(shiroProperties.getIniResourcePath()).isEqualTo("/path/to/shiro.ini");
	}

	@Test
	public void securitySslConfigurationIsCorrect() {

		SslProperties sslProperties = this.gemfireProperties.getSecurity().getSsl();

		assertThat(sslProperties).isNotNull();
		assertThat(sslProperties.getCiphers()).containsExactly("AES", "DES");
		assertThat(sslProperties.getComponents())
			.containsExactly(EnableSsl.Component.GATEWAY, EnableSsl.Component.LOCATOR, EnableSsl.Component.SERVER);
		assertThat(sslProperties.getKeystore()).isEqualTo("/path/to/keystore.jks");
		assertThat(sslProperties.getProtocols()).containsExactly("any");
		assertThat(sslProperties.getTruststore()).isEqualTo("/path/to/truststore.jks");
	}

	@Test
	public void securitySslCertificateConfigurationIsCorrect() {

		SslProperties.SslCertificateProperties certificateProperties =
			this.gemfireProperties.getSecurity().getSsl().getCertificate();

		assertThat(certificateProperties).isNotNull();
		assertThat(certificateProperties.getAlias()).isNotNull();
		assertThat(certificateProperties.getAlias().getAll()).isEqualTo("Master");
		assertThat(certificateProperties.getAlias().getCluster()).isEqualTo("Custard");
		assertThat(certificateProperties.getAlias().getDefaultAlias()).isEqualTo("RogueOne");
		assertThat(certificateProperties.getAlias().getGateway()).isEqualTo("WAN");
		assertThat(certificateProperties.getAlias().getJmx()).isEqualTo("Manage");
		assertThat(certificateProperties.getAlias().getLocator()).isEqualTo("Local");
		assertThat(certificateProperties.getAlias().getServer()).isEqualTo("Servant");
		assertThat(certificateProperties.getAlias().getWeb()).isEqualTo("WWW");
	}

	@Test
	public void securitySslKeystoreConfigurationIsCorrect() {

		SslProperties.KeyStoreProperties keystoreProperties =
			this.gemfireProperties.getSecurity().getSsl().getKeystoreConfig();

		assertThat(keystoreProperties).isNotNull();
		assertThat(keystoreProperties.getPassword()).isEqualTo("keypass");
		assertThat(keystoreProperties.getType()).isEqualTo("JKS");
	}

	@Test
	public void securitySslTruststoreConfigurationIsCorrect() {

		SslProperties.KeyStoreProperties truststoreProperties =
			this.gemfireProperties.getSecurity().getSsl().getTruststoreConfig();

		assertThat(truststoreProperties).isNotNull();
		assertThat(truststoreProperties.getPassword()).isEqualTo("storepass");
		assertThat(truststoreProperties.getType()).isEqualTo("PKS11");
	}

	@Test
	public void serviceConfigurationIsNotNull() {
		assertThat(this.gemfireProperties.getService()).isNotNull();
	}

	@Test
	public void serviceHttpConfigurationIsCorrect() {

		ServiceProperties.HttpServiceProperties httpProperties = this.gemfireProperties.getService().getHttp();

		assertThat(httpProperties).isNotNull();
		assertThat(httpProperties.getBindAddress()).isEqualTo("10.51.151.255");
		assertThat(httpProperties.getPort()).isEqualTo(8181);
		assertThat(httpProperties.isSslRequireAuthentication()).isTrue();
	}

	@Test
	public void serviceHttpDeveloperRestApiConfigurationIsCorrect() {

		ServiceProperties.DeveloperRestApiProperties developerRestApiProperties =
			this.gemfireProperties.getService().getHttp().getDevRestApi();

		assertThat(developerRestApiProperties).isNotNull();
		assertThat(developerRestApiProperties.isStart()).isTrue();
	}

	@Test
	public void serviceMemcachedConfigurationIsCorrect() {

		ServiceProperties.MemcachedServerProperties memcachedProperties =
			this.gemfireProperties.getService().getMemcached();

		assertThat(memcachedProperties).isNotNull();
		assertThat(memcachedProperties.getPort()).isEqualTo(22422);
		assertThat(memcachedProperties.getProtocol()).isEqualTo(EnableMemcachedServer.MemcachedProtocol.BINARY);
	}

	@Test
	public void serviceRedisConfigurationIsCorrect() {

		ServiceProperties.RedisServerProperties redisProperties = this.gemfireProperties.getService().getRedis();

		assertThat(redisProperties).isNotNull();
		assertThat(redisProperties.getBindAddress()).isEqualTo("10.21.121.242");
		assertThat(redisProperties.getPort()).isEqualTo(3697);
	}

	@SpringBootApplication
	@EnableGemFireMockObjects
	static class TestConfiguration { }

}
