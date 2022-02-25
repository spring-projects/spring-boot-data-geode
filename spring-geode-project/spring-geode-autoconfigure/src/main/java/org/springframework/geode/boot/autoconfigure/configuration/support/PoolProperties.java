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
package org.springframework.geode.boot.autoconfigure.configuration.support;

import java.util.Properties;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolFactory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} used to configure an Apache Geode client {@link Pool}.
 *
 * The configuration {@link Properties} are based on well-known, documented Spring Data for Apache Geode (SDG)
 * {@link Properties}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolFactory
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class PoolProperties {

	private static final boolean DEFAULT_READY_FOR_EVENTS = false;

	private boolean multiUserAuthentication = PoolFactory.DEFAULT_MULTIUSER_AUTHENTICATION;
	private boolean prSingleHopEnabled = PoolFactory.DEFAULT_PR_SINGLE_HOP_ENABLED;
	private boolean readyForEvents = DEFAULT_READY_FOR_EVENTS;
	private boolean subscriptionEnabled = PoolFactory.DEFAULT_SUBSCRIPTION_ENABLED;
	private boolean threadLocalConnections = false;

	private int freeConnectionTimeout = PoolFactory.DEFAULT_FREE_CONNECTION_TIMEOUT;
	private int loadConditioningInterval = PoolFactory.DEFAULT_LOAD_CONDITIONING_INTERVAL;
	private int maxConnections = PoolFactory.DEFAULT_MAX_CONNECTIONS;
	private int minConnections = PoolFactory.DEFAULT_MIN_CONNECTIONS;
	private int readTimeout = PoolFactory.DEFAULT_READ_TIMEOUT;
	private int retryAttempts = PoolFactory.DEFAULT_RETRY_ATTEMPTS;
	private int socketBufferSize = PoolFactory.DEFAULT_SOCKET_BUFFER_SIZE;
	private int statisticInterval = PoolFactory.DEFAULT_STATISTIC_INTERVAL;
	private int subscriptionAckInterval = PoolFactory.DEFAULT_SUBSCRIPTION_ACK_INTERVAL;
	private int subscriptionMessageTrackingTimeout = PoolFactory.DEFAULT_SUBSCRIPTION_MESSAGE_TRACKING_TIMEOUT;
	private int subscriptionRedundancy = PoolFactory.DEFAULT_SUBSCRIPTION_REDUNDANCY;

	private long idleTimeout = PoolFactory.DEFAULT_IDLE_TIMEOUT;
	private long pingInterval = PoolFactory.DEFAULT_PING_INTERVAL;

	private String serverGroup = PoolFactory.DEFAULT_SERVER_GROUP;

	private String[] locators = {};
	private String[] servers = {};

	public int getFreeConnectionTimeout() {
		return this.freeConnectionTimeout;
	}

	public void setFreeConnectionTimeout(int freeConnectionTimeout) {
		this.freeConnectionTimeout = freeConnectionTimeout;
	}

	public long getIdleTimeout() {
		return this.idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public int getLoadConditioningInterval() {
		return this.loadConditioningInterval;
	}

	public void setLoadConditioningInterval(int loadConditioningInterval) {
		this.loadConditioningInterval = loadConditioningInterval;
	}

	public String[] getLocators() {
		return locators;
	}

	public void setLocators(String[] locators) {
		this.locators = locators;
	}

	public int getMaxConnections() {
		return this.maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getMinConnections() {
		return this.minConnections;
	}

	public void setMinConnections(int minConnections) {
		this.minConnections = minConnections;
	}

	public boolean isMultiUserAuthentication() {
		return this.multiUserAuthentication;
	}

	public void setMultiUserAuthentication(boolean multiUserAuthentication) {
		this.multiUserAuthentication = multiUserAuthentication;
	}

	public long getPingInterval() {
		return this.pingInterval;
	}

	public void setPingInterval(long pingInterval) {
		this.pingInterval = pingInterval;
	}

	public boolean isPrSingleHopEnabled() {
		return this.prSingleHopEnabled;
	}

	public void setPrSingleHopEnabled(boolean prSingleHopEnabled) {
		this.prSingleHopEnabled = prSingleHopEnabled;
	}

	public int getReadTimeout() {
		return this.readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean isReadyForEvents() {
		return this.readyForEvents;
	}

	public void setReadyForEvents(boolean readyForEvents) {
		this.readyForEvents = readyForEvents;
	}

	public int getRetryAttempts() {
		return this.retryAttempts;
	}

	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}

	public String getServerGroup() {
		return this.serverGroup;
	}

	public void setServerGroup(String serverGroup) {
		this.serverGroup = serverGroup;
	}

	public String[] getServers() {
		return this.servers;
	}

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public int getSocketBufferSize() {
		return this.socketBufferSize;
	}

	public void setSocketBufferSize(int socketBufferSize) {
		this.socketBufferSize = socketBufferSize;
	}

	public int getStatisticInterval() {
		return this.statisticInterval;
	}

	public void setStatisticInterval(int statisticInterval) {
		this.statisticInterval = statisticInterval;
	}

	public int getSubscriptionAckInterval() {
		return this.subscriptionAckInterval;
	}

	public void setSubscriptionAckInterval(int subscriptionAckInterval) {
		this.subscriptionAckInterval = subscriptionAckInterval;
	}

	public boolean isSubscriptionEnabled() {
		return this.subscriptionEnabled;
	}

	public void setSubscriptionEnabled(boolean subscriptionEnabled) {
		this.subscriptionEnabled = subscriptionEnabled;
	}

	public int getSubscriptionMessageTrackingTimeout() {
		return this.subscriptionMessageTrackingTimeout;
	}

	public void setSubscriptionMessageTrackingTimeout(int subscriptionMessageTrackingTimeout) {
		this.subscriptionMessageTrackingTimeout = subscriptionMessageTrackingTimeout;
	}

	public int getSubscriptionRedundancy() {
		return this.subscriptionRedundancy;
	}

	public void setSubscriptionRedundancy(int subscriptionRedundancy) {
		this.subscriptionRedundancy = subscriptionRedundancy;
	}

	public boolean isThreadLocalConnections() {
		return this.threadLocalConnections;
	}

	public void setThreadLocalConnections(boolean threadLocalConnections) {
		this.threadLocalConnections = threadLocalConnections;
	}
}
