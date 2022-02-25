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
package org.springframework.geode.cache;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DiskStore;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory;
import org.apache.geode.cache.wan.GatewayEventFilter;
import org.apache.geode.cache.wan.GatewayEventSubstitutionFilter;
import org.apache.geode.cache.wan.GatewaySender;
import org.apache.geode.cache.wan.GatewaySender.OrderPolicy;

import org.springframework.data.gemfire.PeerRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.RegionConfigurer;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A Spring Data for Apache Geode {@link RegionConfigurer} implementation used to configure a target {@link Region}
 * to use {@literal Asynchronous Inline Caching} based on the Spring Data {@link CrudRepository Repositories}
 * abstraction.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see java.util.function.Predicate
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
 * @see org.apache.geode.cache.wan.GatewayEventFilter
 * @see org.apache.geode.cache.wan.GatewayEventSubstitutionFilter
 * @see org.apache.geode.cache.wan.GatewaySender.OrderPolicy
 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.RegionConfigurer
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler
 * @since 1.4.0
 */
public class AsyncInlineCachingRegionConfigurer<T, ID> implements RegionConfigurer {

	protected static final Predicate<String> DEFAULT_REGION_BEAN_NAME_PREDICATE = beanName -> false;

	/**
	 * Factory method used to construct a new instance of {@link AsyncInlineCachingRegionConfigurer} initialized with
	 * the given Spring Data {@link CrudRepository} and {@link Predicate} identifying the target {@link Region}
	 * on which to configure {@literal Asynchronous Inline Caching}.
	 *
	 * @param <T> {@link Class type} of the entity.
	 * @param <ID> {@link Class type} of the identifier, or {@link Region} key.
	 * @param repository {@link CrudRepository} used to perform data access operations on an external data source
	 * triggered by cache events and operations on the identified {@link Region}; must not be {@literal null}.
	 * @param regionBeanName {@link Predicate} used to identify the {@link Region} by {@link String name} on which
	 * {@literal Asynchronous Inline Caching} will be configured.
	 * @return a new {@link AsyncInlineCachingRegionConfigurer}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see #AsyncInlineCachingRegionConfigurer(CrudRepository, Predicate)
	 * @see org.springframework.data.repository.CrudRepository
	 * @see java.util.function.Predicate
	 */
	public static <T, ID> AsyncInlineCachingRegionConfigurer<T, ID> create(@NonNull CrudRepository<T, ID> repository,
			@Nullable Predicate<String> regionBeanName) {

		return new AsyncInlineCachingRegionConfigurer<>(repository, regionBeanName);
	}

	/**
	 * Factory method used to construct a new instance of {@link AsyncInlineCachingRegionConfigurer} initialized with
	 * the given Spring Data {@link CrudRepository} and {@link String} identifying the target {@link Region}
	 * on which to configure {@literal Asynchronous Inline Caching}.
	 *
	 * @param <T> {@link Class type} of the entity.
	 * @param <ID> {@link Class type} of the identifier, or {@link Region} key.
	 * @param repository {@link CrudRepository} used to perform data access operations on an external data source
	 * triggered by cache events and operations on the identified {@link Region}; must not be {@literal null}.
	 * @param regionBeanName {@link String} used to identify the {@link Region} by {@link String name} on which
	 * {@literal Asynchronous Inline Caching} will be configured.
	 * @return a new {@link AsyncInlineCachingRegionConfigurer}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 * @see #create(CrudRepository, Predicate)
	 * @see java.lang.String
	 */
	public static <T, ID> AsyncInlineCachingRegionConfigurer<T, ID> create(@NonNull CrudRepository<T, ID> repository,
			@Nullable String regionBeanName) {

		return create(repository, Predicate.isEqual(regionBeanName));
	}

	private AsyncEventErrorHandler asyncEventErrorHandler;

	private Boolean batchConflationEnabled;
	private Boolean diskSynchronous;
	private Boolean forwardExpirationDestroy;
	private Boolean parallel;
	private Boolean persistent;
	private Boolean pauseEventDispatching;

	private final CrudRepository<T, ID> repository;

	private Function<AsyncEventListener, AsyncEventListener> asyncEventListenerPostProcessor;

	private Function<AsyncEventQueue, AsyncEventQueue> asyncEventQueuePostProcessor;

	private Function<AsyncEventQueueFactory, AsyncEventQueueFactory> asyncEventQueueFactoryPostProcessor;

	private Integer batchSize;
	private Integer batchTimeInterval;
	private Integer dispatcherThreads;
	private Integer maximumQueueMemory;

	@SuppressWarnings("rawtypes")
	private GatewayEventSubstitutionFilter gatewayEventSubstitutionFilter;

	private GatewaySender.OrderPolicy orderPolicy;

	private List<GatewayEventFilter> gatewayEventFilters;

	private final Predicate<String> regionBeanName;

	private String diskStoreName;

	/**
	 * Constructs a new instance of {@link AsyncInlineCachingRegionConfigurer} initialized with the given
	 * {@link CrudRepository} and {@link Predicate} identifying the {@link Region} on which
	 * {@literal Asynchronous Inline Caching} will be configured.
	 *
	 * @param repository {@link CrudRepository} used to perform data access operations on an external data source
	 * triggered by cache events and operations on the identified {@link Region}; must not be {@literal null}.
	 * @param regionBeanName {@link Predicate} used to identify the {@link Region} by {@link String name} on which
	 * {@literal Asynchronous Inline Caching} will be configured.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 * @see java.util.function.Predicate
	 */
	public AsyncInlineCachingRegionConfigurer(@NonNull CrudRepository<T, ID> repository,
			@Nullable Predicate<String> regionBeanName) {

		Assert.notNull(repository, "CrudRepository must not be null");

		this.repository = repository;
		this.regionBeanName = regionBeanName != null ? regionBeanName : DEFAULT_REGION_BEAN_NAME_PREDICATE;
	}

	/**
	 * Gets the {@link Predicate} identifying the {@link Region} on which {@literal Asynchronous Inline Caching}
	 * will be configured.
	 *
	 * @return the {@link Predicate} used to match the {@link Region} by {@link String name} on which
	 * {@literal Asynchronous Inline Caching} will be configured; never {@literal null}.
	 * @see java.util.function.Predicate
	 */
	protected @NonNull Predicate<String> getRegionBeanName() {
		return this.regionBeanName;
	}

	/**
	 * Gets the Spring Data {@link CrudRepository} used to perform data access operations on an external data source
	 * triggered cache events and operations on the target {@link Region}.
	 *
	 * @return the Spring Data {@link CrudRepository} used to perform data access operations on an external data source
	 * triggered cache events and operations on the target {@link Region}; never {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 */
	protected @NonNull CrudRepository<T, ID> getRepository() {
		return this.repository;
	}

	/**
	 * Configures the target {@link Region} by {@link String name} with {@literal Asynchronous Inline Caching}
	 * functionality.
	 *
	 * Effectively, this Configurer creates an {@link AsyncEventQueue} attached to the target {@link Region} with a
	 * registered {@link RepositoryAsyncEventListener} to perform asynchronous data access operations triggered by
	 * cache operations to an external, backend data source using a Spring Data {@link CrudRepository}.
	 *
	 * @param beanName {@link String} specifying the name of the target {@link Region} and bean name
	 * in the Spring container.
	 * @param bean {@link PeerRegionFactoryBean} containing the configuration of the target {@link Region}
	 * in the Spring container.
	 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
	 * @see #newAsyncEventQueue(Cache, String)
	 */
	@Override
	public void configure(String beanName, PeerRegionFactoryBean<?, ?> bean) {

		if (getRegionBeanName().test(beanName)) {

			AsyncEventQueue queue = newAsyncEventQueue((Cache) bean.getCache(), beanName);

			bean.addAsyncEventQueues(ArrayUtils.asArray(queue));
		}
	}

	/**
	 * Generates a new {@link String ID} for the {@link AsyncEventQueue}.
	 *
	 * @param regionBeanName {@link String name} of the target {@link Region}.
	 * @return a new {@link String ID} for the {@link AsyncEventQueue}.
	 */
	protected @NonNull String generateId(@NonNull String regionBeanName) {

		Assert.hasText(regionBeanName, () -> String.format("Region bean name [%s] must be specified", regionBeanName));

		return regionBeanName.concat(String.format("-AEQ-%s", UUID.randomUUID().toString()));
	}

	/**
	 * Constructs a new instance of an {@link AsyncEventQueue} to attach to the target {@link Region} configured for
	 * {@literal Asynchronous Inline Caching}.
	 *
	 * @param peerCache reference to the {@link Cache peer cache}; must not be {@literal null}.
	 * @param regionBeanName {@link String name} of the target {@link Region}; must not be {@literal null}.
	 * @return a new {@link AsyncEventQueue}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
	 * @see org.apache.geode.cache.Cache
	 * @see #generateId(String)
	 * @see #newAsyncEventQueueFactory(Cache)
	 * @see #newAsyncEventQueue(AsyncEventQueueFactory, String, AsyncEventListener)
	 * @see #newRepositoryAsyncEventListener()
	 * @see #postProcess(AsyncEventListener)
	 * @see #postProcess(AsyncEventQueue)
	 * @see #postProcess(AsyncEventQueueFactory)
	 */
	protected AsyncEventQueue newAsyncEventQueue(@NonNull Cache peerCache, @NonNull String regionBeanName) {

		AsyncEventQueueFactory asyncEventQueueFactory = newAsyncEventQueueFactory(peerCache);

		Optional.ofNullable(this.batchConflationEnabled).ifPresent(asyncEventQueueFactory::setBatchConflationEnabled);
		Optional.ofNullable(this.batchSize).ifPresent(asyncEventQueueFactory::setBatchSize);
		Optional.ofNullable(this.batchTimeInterval).ifPresent(asyncEventQueueFactory::setBatchTimeInterval);
		Optional.ofNullable(this.diskStoreName).filter(StringUtils::hasText).ifPresent(asyncEventQueueFactory::setDiskStoreName);
		Optional.ofNullable(this.diskSynchronous).ifPresent(asyncEventQueueFactory::setDiskSynchronous);
		Optional.ofNullable(this.dispatcherThreads).ifPresent(asyncEventQueueFactory::setDispatcherThreads);
		Optional.ofNullable(this.forwardExpirationDestroy).ifPresent(asyncEventQueueFactory::setForwardExpirationDestroy);
		Optional.ofNullable(this.gatewayEventSubstitutionFilter).ifPresent(asyncEventQueueFactory::setGatewayEventSubstitutionListener);
		Optional.ofNullable(this.maximumQueueMemory).ifPresent(asyncEventQueueFactory::setMaximumQueueMemory);
		Optional.ofNullable(this.orderPolicy).ifPresent(asyncEventQueueFactory::setOrderPolicy);
		Optional.ofNullable(this.parallel).ifPresent(asyncEventQueueFactory::setParallel);
		Optional.ofNullable(this.persistent).ifPresent(asyncEventQueueFactory::setPersistent);

		CollectionUtils.nullSafeList(this.gatewayEventFilters).stream()
			.filter(Objects::nonNull)
			.forEach(asyncEventQueueFactory::addGatewayEventFilter);

		if (Boolean.TRUE.equals(this.pauseEventDispatching)) {
			asyncEventQueueFactory.pauseEventDispatching();
		}

		String asyncEventQueueId = generateId(regionBeanName);

		AsyncEventListener asyncEventListener = newRepositoryAsyncEventListener();

		asyncEventListener = postProcess(asyncEventListener);
		asyncEventQueueFactory = postProcess(asyncEventQueueFactory);

		AsyncEventQueue asyncEventQueue =
			newAsyncEventQueue(asyncEventQueueFactory, asyncEventQueueId, asyncEventListener);

		asyncEventQueue = postProcess(asyncEventQueue);

		return asyncEventQueue;
	}

	/**
	 * Constructs (creates) a new instance of {@link AsyncEventQueue} using the given {@link AsyncEventQueueFactory}
	 * with the given {@link String AEQ ID} and {@link AsyncEventListener}.
	 *
	 * @param factory {@link AsyncEventQueueFactory} used to create the {@link AsyncEventQueue};
	 * must not be {@literal null}.
	 * @param asyncEventQueueId {@link String} containing the {@literal ID} for the {@link AsyncEventQueue};
	 * must not be {@literal null}.
	 * @param listener {@link AsyncEventListener} registered with the {@link AsyncEventQueue} to process cache events
	 * from the {@link AsyncEventQueue} attached to the {@link Region}.
	 * @return a new {@link AsyncEventQueue} with the {@link String ID} and registered {@link AsyncEventListener}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
	 */
	protected @NonNull AsyncEventQueue newAsyncEventQueue(@NonNull AsyncEventQueueFactory factory,
			@NonNull String asyncEventQueueId, @NonNull AsyncEventListener listener) {

		return factory.create(asyncEventQueueId, listener);
	}

	/**
	 * Constructs (creates) a new instance of the {@link AsyncEventQueueFactory} from the given {@literal peer}
	 * {@link Cache}.
	 *
	 * @param peerCache {@literal Peer} {@link Cache} instance used to create an instance of
	 * the {@link AsyncEventQueueFactory}; must not be {@literal null}.
	 * @return a new instance of {@link AsyncEventQueueFactory} to create a {@link AsyncEventQueue}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
	 * @see org.apache.geode.cache.Cache
	 */
	protected @NonNull AsyncEventQueueFactory newAsyncEventQueueFactory(@NonNull Cache peerCache) {
		return peerCache.createAsyncEventQueueFactory();
	}

	/**
	 * Constructs a new Apache Geode {@link AsyncEventListener} to register on an {@link AsyncEventQueue} attached to
	 * the target {@link Region}, which uses the {@link CrudRepository} to perform data access operations on an external
	 * backend data source asynchronously when cache events and operations occur on the target {@link Region}.
	 *
	 * @return a new {@link RepositoryAsyncEventListener}.
	 * @see org.springframework.geode.cache.RepositoryAsyncEventListener
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
	 * @see #newRepositoryAsyncEventListener(CrudRepository)
	 * @see #getRepository()
	 */
	protected @NonNull AsyncEventListener newRepositoryAsyncEventListener() {
		return newRepositoryAsyncEventListener(getRepository());
	}

	/**
	 * Constructs a new Apache Geode {@link AsyncEventListener} to register on an {@link AsyncEventQueue} attached to
	 * the target {@link Region}, which uses the given {@link CrudRepository} to perform data access operations on an
	 * external, backend data source asynchronously when cache events and operations occur on the target {@link Region}.
	 *
	 * @param repository Spring Data {@link CrudRepository} used to perform data access operations on the external,
	 * backend data source; must not be {@literal null}.
	 * @return a new {@link RepositoryAsyncEventListener}.
	 * @see org.springframework.geode.cache.RepositoryAsyncEventListener
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
	 * @see #getRepository()
	 */
	protected @NonNull AsyncEventListener newRepositoryAsyncEventListener(@NonNull CrudRepository<T, ID> repository) {
		return new RepositoryAsyncEventListener<>(repository);
	}

	/**
	 * Applies the user-defined {@link Function} to the framework constructed/provided {@link AsyncEventListener}
	 * for post processing.
	 *
	 * @param asyncEventListener {@link AsyncEventListener} constructed by the framework and post processed by
	 * end-user code encapsulated in the {@link #applyToListener(Function) configured} {@link Function}.
	 * @return the post-processed {@link AsyncEventListener}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
	 * @see #applyToListener(Function)
	 */
	protected @NonNull AsyncEventListener postProcess(@NonNull AsyncEventListener asyncEventListener) {
		return resolveAsyncEventListenerPostProcessor().apply(asyncEventListener);
	}

	/**
	 * Applies the user-defined {@link Function} to the framework constructed/provided {@link AsyncEventQueue}
	 * for post processing.
	 *
	 * @param asyncEventQueue {@link AsyncEventQueue} constructed by the framework and post processed by
	 * end-user code encapsulated in the {@link #applyToQueue(Function) configured} {@link Function}.
	 * @return the post-processed {@link AsyncEventQueue}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
	 * @see #applyToQueue(Function)
	 */
	protected @NonNull AsyncEventQueue postProcess(@NonNull AsyncEventQueue asyncEventQueue) {

		Function<AsyncEventQueue, AsyncEventQueue> asyncEventQueuePostProcessor =
			this.asyncEventQueuePostProcessor;

		return asyncEventQueuePostProcessor != null
			? asyncEventQueuePostProcessor.apply(asyncEventQueue)
			: asyncEventQueue;
	}

	/**
	 * Applies the user-defined {@link Function} to the framework constructed/provided {@link AsyncEventQueueFactory}
	 * for post processing.
	 *
	 * @param asyncEventQueueFactory {@link AsyncEventQueueFactory} constructed by the framework and post processed by
	 * end-user code encapsulated in the {@link #applyToQueueFactory(Function) configured} {@link Function}.
	 * @return the post-processed {@link AsyncEventQueueFactory}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
	 * @see #applyToQueueFactory(Function)
	 */
	protected @NonNull AsyncEventQueueFactory postProcess(@NonNull AsyncEventQueueFactory asyncEventQueueFactory) {

		Function<AsyncEventQueueFactory, AsyncEventQueueFactory> asyncEventQueueFactoryPostProcessor =
			this.asyncEventQueueFactoryPostProcessor;

		return asyncEventQueueFactoryPostProcessor != null
			? asyncEventQueueFactoryPostProcessor.apply(asyncEventQueueFactory)
			: asyncEventQueueFactory;
	}

	@SuppressWarnings("unchecked")
	private @NonNull Function<AsyncEventListener, AsyncEventListener> resolveAsyncEventListenerPostProcessor() {

		AsyncEventErrorHandler asyncEventErrorHandler = this.asyncEventErrorHandler;

		Function<AsyncEventListener, AsyncEventListener> resolvedListenerPostProcessor = asyncEventErrorHandler != null
			? listener -> {

				if (listener instanceof RepositoryAsyncEventListener) {
					((RepositoryAsyncEventListener<T, ID>) listener).setAsyncEventErrorHandler(asyncEventErrorHandler);
				}

				return listener;
			}
			: Function.identity();

		Function<AsyncEventListener, AsyncEventListener> asyncEventListenerPostProcessor =
			this.asyncEventListenerPostProcessor;

		if (asyncEventListenerPostProcessor != null) {
			resolvedListenerPostProcessor = resolvedListenerPostProcessor.andThen(asyncEventListenerPostProcessor);
		}

		return resolvedListenerPostProcessor;
	}

	/**
	 * Builder method used to configure the given user-defined {@link Function} applied to the framework constructed
	 * and provided {@link AsyncEventListener} for post processing.
	 *
	 * @param asyncEventListenerPostProcessor user-defined {@link Function} encapsulating the logic applied to
	 * the framework constructed/provided {@link AsyncEventListener} for post-processing.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
	 * @see java.util.function.Function
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> applyToListener(
			@Nullable Function<AsyncEventListener, AsyncEventListener> asyncEventListenerPostProcessor) {

		this.asyncEventListenerPostProcessor = asyncEventListenerPostProcessor;

		return this;
	}

	/**
	 * Builder method used to configure the given user-defined {@link Function} applied to the framework constructed
	 * and provided {@link AsyncEventQueue} for post processing.
	 *
	 * @param asyncEventQueuePostProcessor user-defined {@link Function} encapsulating the logic applied to
	 * the framework constructed {@link AsyncEventQueue} for post-processing.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
	 * @see java.util.function.Function
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> applyToQueue(
			@Nullable Function<AsyncEventQueue, AsyncEventQueue> asyncEventQueuePostProcessor) {

		this.asyncEventQueuePostProcessor = asyncEventQueuePostProcessor;

		return this;
	}

	/**
	 * Builder method used to configure the given user-defined {@link Function} applied to the framework constructed
	 * and provided {@link AsyncEventQueueFactory} for post processing.
	 *
	 * @param asyncEventQueueFactoryPostProcessor user-defined {@link Function} encapsulating the logic applied to
	 * the framework constructed {@link AsyncEventQueueFactory} for post-processing.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
	 * @see java.util.function.Function
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> applyToQueueFactory(
			@Nullable Function<AsyncEventQueueFactory, AsyncEventQueueFactory> asyncEventQueueFactoryPostProcessor) {

		this.asyncEventQueueFactoryPostProcessor = asyncEventQueueFactoryPostProcessor;

		return this;
	}

	/**
	 * Builder method used to configure a {@link AsyncEventErrorHandler} to handle errors thrown while processing
	 * {@link AsyncEvent AsyncEvents} in the {@link AsyncEventListener}.
	 *
	 * @param errorHandler {@link AsyncEventErrorHandler} used to handle errors thrown while processing
	 * {@link AsyncEvent AsyncEvents} in the {@link AsyncEventListener}.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withAsyncEventErrorHandler(
			@Nullable AsyncEventErrorHandler errorHandler) {

		this.asyncEventErrorHandler = errorHandler;

		return this;
	}

	/**
	 * Builder method used to enable all {@link AsyncEventQueue AEQs} attached to {@link Region Regions} hosted
	 * and distributed across the cache cluster to process cache events.
	 *
	 * Default is {@literal false}, or {@literal serial}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see #withSerialQueue()
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withParallelQueue() {
		this.parallel = true;
		return this;
	}

	/**
	 * Builder method used to enable the {@link AsyncEventQueue} to persist cache events to disk in order to
	 * preserve unprocessed cache events while offline.
	 *
	 * Keep in mind that the {@link AsyncEventQueue} must be persistent if the data {@link Region}
	 * to which the AEQ is attached is persistent.
	 *
	 * Default is {@literal false}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withPersistentQueue() {
		this.persistent = true;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} to conflate cache events in the queue.
	 *
	 * When conflation is enabled, the AEQ listener will only receive the latest update in the AEQ for cache entry
	 * based on key.
	 *
	 * Defaults to {@literal false}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueBatchConflationEnabled() {
		this.batchConflationEnabled = true;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} {@link Integer batch size}, which determines
	 * the number (i.e. threshold) of cache events that will trigger the AEQ listener, before any set period of time.
	 *
	 * The batch size is often used in tandem with the batch time interval, which determines when the AEQ listener
	 * will be invoked after a period of time if the batch size is not reached within the period so that cache events
	 * can also be processed in a timely manner if they are occurring infrequently.
	 *
	 * Defaults to {@literal 100}.
	 *
	 * @param batchSize the {@link Integer number} of cache events in the queue before the AEQ listener is called.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see #withQueueBatchTimeInterval(Duration)
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} {@link Duration batch time interval} determining
	 * when the AEQ listener will be trigger before the number of cache events reaches any set size.
	 *
	 * The {@link Duration} is converted to milliseconds (ms), as expected by the configuration
	 * of the {@link AsyncEventQueue}.
	 *
	 * The batch time interval is often used in tandem with batch size, which determines for how many cache events
	 * in the queue will trigger the AEQ listener. If cache events are occurring rather frequently, then the batch size
	 * can help reduce memory consumption by processing the cache events before the batch time interval expires.
	 *
	 * Defaults to {@literal 5 ms}.
	 *
	 * @param batchTimeInterval {@link Duration} of time to determine when the AEQ listener should be invoked with
	 * any existing cache events in the queue.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueBatchTimeInterval(Duration batchTimeInterval) {

		this.batchTimeInterval = batchTimeInterval != null
			? Long.valueOf(batchTimeInterval.toMillis()).intValue()
			: null;

		return this;
	}

	/**
	 * Builder method used to configure the {@link String name} of the {@link DiskStore} used by
	 * the {@link AsyncEventQueue} to persist or overflow cache events.
	 *
	 * By default, the AEQ will write cache events to the {@literal DEFAULT} {@link DiskStore}.
	 *
	 * @param diskStoreName {@link String name} of the {@link DiskStore}.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueDiskStore(String diskStoreName) {
		this.diskStoreName = diskStoreName;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} to perform all disk write operations synchronously.
	 *
	 * Default is {@literal true}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueDiskSynchronizationEnabled() {
		this.diskSynchronous = true;
		return this;
	}

	/**
	 * Builder method to configure the number of {@link Thread Threads} to process the cache events (contents)
	 * in the {@link AsyncEventQueue} when the queue is parallel.
	 *
	 * When a queue is parallel, the total number of queues is determined by the number of Geode members
	 * hosting the {@link Region} to which the queue is attached.
	 *
	 * When a queue is serial and multiple dispatcher threads are configured, Geode creates an additional copy of
	 * the queue for each thread on each Geode member that hosts the queue.  When the queue is serial and multiple
	 * dispatcher threads are configure, then you can use the {@link GatewaySender} {@link OrderPolicy} to control
	 * the distribution of cache events from the queue by the threads.
	 *
	 * Default is {@literal 5}.
	 *
	 * @param dispatcherThreadCount {@link Integer number} of dispatcher {@link Thread Threads} processing cache events
	 * in the queue.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueDispatcherThreadCount(int dispatcherThreadCount) {
		this.dispatcherThreads = dispatcherThreadCount;
		return this;
	}

	/**
	 * Builder method used to configure whether the {@link AsyncEventQueue} is currently processing cache events
	 * or is paused.
	 *
	 * When paused, cache events will not be dispatched to the AEQ listener for processing. Call the
	 * {@link AsyncEventQueue#resumeEventDispatching()} to resume cache event processing and AEQ listener callbacks.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueEventDispatchingPaused() {
		this.pauseEventDispatching = true;
		return this;
	}

	/**
	 * Builder method to configure the {@link AsyncEventQueue} with a {@link List} of
	 * {@link GatewayEventFilter GatewayEventFilters} to filter cache events sent to the configured AEQ listener.
	 *
	 * @param eventFilters {@link List} of {@link GatewayEventFilter GatewayEventFilters} used to control and filter
	 * the cache events sent to the configured AEQ listener.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.wan.GatewayEventFilter
	 * @see java.util.List
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueEventFilters(List<GatewayEventFilter> eventFilters) {
		this.gatewayEventFilters = eventFilters;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} with a
	 * {@link GatewayEventSubstitutionFilter cache event substitution filter} used to replace (or "substitute")
	 * the original cache entry event value enqueued in the AEQ.
	 *
	 * @param eventSubstitutionFilter {@link GatewayEventSubstitutionFilter} used to replace/substitute the value
	 * in the enqueued cache entry event.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.wan.GatewayEventSubstitutionFilter
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueEventSubstitutionFilter(
			@Nullable GatewayEventSubstitutionFilter<ID, T> eventSubstitutionFilter) {

		this.gatewayEventSubstitutionFilter = eventSubstitutionFilter;

		return this;
	}

	/**
	 * Builder method used to configure whether cache {@link Region} entry destroyed events due to expiration
	 * are forwarded to the {@link AsyncEventQueue}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueForwardedExpirationDestroyEvents() {
		this.forwardExpirationDestroy = true;
		return this;
	}

	/**
	 * Builder method used to configure the maximum JVM Heap memory in megabytes used by the {@link AsyncEventQueue}.
	 *
	 * After the maximum memory threshold is reached then the AEQ overflows cache events to disk.
	 *
	 * Default to {@literal 100 MB}.
	 *
	 * @param maximumMemory {@link Integer} value specifying the maximum amount of memory in megabytes used by the AEQ
	 * to capture cache events.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueMaxMemory(int maximumMemory) {
		this.maximumQueueMemory = maximumMemory;
		return this;
	}

	/**
	 * Builder method used to configure the {@link AsyncEventQueue} order of processing for cache events when the AEQ
	 * is serial and the AEQ is using multiple dispatcher threads.
	 *
	 * @param orderPolicy {@link GatewaySender} {@link OrderPolicy} used to determine the order of processing
	 * for cache events when the AEQ is serial and uses multiple dispatcher threads.
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see org.apache.geode.cache.wan.GatewaySender.OrderPolicy
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withQueueOrderPolicy(
			@Nullable GatewaySender.OrderPolicy orderPolicy) {

		this.orderPolicy = orderPolicy;

		return this;
	}

	/**
	 * Builder method used to enable a single {@link AsyncEventQueue AEQ} attached to a {@link Region Region}
	 * (possibly) hosted and distributed across the cache cluster to process cache events.
	 *
	 * Default is {@literal false}, or {@literal serial}.
	 *
	 * @return this {@link AsyncInlineCachingRegionConfigurer}.
	 * @see #withParallelQueue()
	 */
	public AsyncInlineCachingRegionConfigurer<T, ID> withSerialQueue() {
		this.parallel = false;
		return this;
	}
}
