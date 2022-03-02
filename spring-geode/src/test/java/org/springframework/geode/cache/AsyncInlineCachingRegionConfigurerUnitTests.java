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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;
import org.mockito.InOrder;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory;
import org.apache.geode.cache.wan.GatewayEventFilter;
import org.apache.geode.cache.wan.GatewayEventSubstitutionFilter;
import org.apache.geode.cache.wan.GatewaySender;

import org.springframework.data.gemfire.PeerRegionFactoryBean;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler;

/**
 * Unit Tests for {@link AsyncInlineCachingRegionConfigurer}.
 *
 * @author John Blum
 * @see java.util.function.Predicate
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueueFactory
 * @see org.apache.geode.cache.wan.GatewayEventFilter
 * @see org.apache.geode.cache.wan.GatewayEventSubstitutionFilter
 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler
 * @since 1.4.0
 */
public class AsyncInlineCachingRegionConfigurerUnitTests {

	@Test
	public void constructAsyncInlineCachingRegionConfigurer() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		Predicate<String> testRegionBeanName = Predicate.isEqual("TestRegion");

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, testRegionBeanName);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isEqualTo(testRegionBeanName);
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void constructAsyncInlineCachingRegionConfigurerWithNullRegionBeanNamePredicate() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, null);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName())
			.isEqualTo(AsyncInlineCachingRegionConfigurer.DEFAULT_REGION_BEAN_NAME_PREDICATE);
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructAsyncInlineCachingRegionConfigurerWithNullRepositoryThrowsIllegalArgumentException() {

		try {
			new AsyncInlineCachingRegionConfigurer<>(null, Predicate.isEqual("MockRegion"));
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CrudRepository must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void createAsyncInlineCachingRegionConfigurerFromCrudRepositoryAndPredicate() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		Predicate<String> testRegionBeanName = Predicate.isEqual("TestRegion");

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, testRegionBeanName);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isEqualTo(testRegionBeanName);
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void createAsyncInlineCachingRegionConfigurerFromCrudRepositoryAndNullPredicate() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, (Predicate<String>) null);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);
		assertThat(regionConfigurer.getRegionBeanName())
			.isEqualTo(AsyncInlineCachingRegionConfigurer.DEFAULT_REGION_BEAN_NAME_PREDICATE);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void createAsyncInlineCachingRegionConfigurerFromCrudRepositoryAndString() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, "MockRegion");

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName().test("MockRegion")).isTrue();
		assertThat(regionConfigurer.getRegionBeanName().test("MOCKREGION")).isFalse();
		assertThat(regionConfigurer.getRegionBeanName().test("mockregion")).isFalse();
		assertThat(regionConfigurer.getRegionBeanName().test("TestRegion")).isFalse();
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void createAsyncInlineCachingRegionConfigurerFromCrudRepositoryAndNullString() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, (String) null);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName().test(null)).isTrue();
		assertThat(regionConfigurer.getRegionBeanName().test("MockRegion")).isFalse();
		assertThat(regionConfigurer.getRegionBeanName().test("TestRegion")).isFalse();
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAsyncInlineCachingRegionConfigurerFromNullCrudRepositoryAndPredicateThrowsIllegalArgumentException() {

		try {
			AsyncInlineCachingRegionConfigurer.create(null, Predicate.isEqual("TestRegion"));
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CrudRepository must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void createAsyncInlineCachingRegionConfigurerFromNullCrudRepositoryAndStringThrowsIllegalArgumentException() {

		try {
			AsyncInlineCachingRegionConfigurer.create(null, "MockRegion");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CrudRepository must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void configureConfiguresRegionForAsyncInlineCachingFunctionality() {

		AsyncEventQueue mockAsyncEventQueue = mock(AsyncEventQueue.class, "Mock AEQ");

		Cache mockCache = mock(Cache.class, "Mock Peer Cache");

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		doReturn(mockCache).when(peerRegionFactoryBean).getCache();
		doReturn(mockAsyncEventQueue).when(regionConfigurer).newAsyncEventQueue(eq(mockCache), eq("TestRegion"));

		regionConfigurer.configure("TestRegion", peerRegionFactoryBean);

		verify(regionConfigurer, times(1))
			.configure(eq("TestRegion"), eq(peerRegionFactoryBean));
		verify(regionConfigurer, times(1)).getRegionBeanName();
		verify(peerRegionFactoryBean, times(1)).getCache();
		verify(regionConfigurer, times(1))
			.newAsyncEventQueue(eq(mockCache), eq("TestRegion"));
		verify(peerRegionFactoryBean, times(1))
			.addAsyncEventQueues(eq(ArrayUtils.asArray(mockAsyncEventQueue)));

		verifyNoMoreInteractions(regionConfigurer, peerRegionFactoryBean);
		verifyNoInteractions(mockAsyncEventQueue, mockCache, mockRepository);
	}

	@Test
	public void configureDoesNothingWhenRegionBeanNameDoesNotMatch() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("NoRegion")));

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		regionConfigurer.configure("MockRegion", peerRegionFactoryBean);
		regionConfigurer.configure("TestRegion", peerRegionFactoryBean);

		verify(regionConfigurer, times(1))
			.configure(eq("MockRegion"), eq(peerRegionFactoryBean));
		verify(regionConfigurer, times(1))
			.configure(eq("TestRegion"), eq(peerRegionFactoryBean));
		verify(regionConfigurer, times(2)).getRegionBeanName();
		verifyNoMoreInteractions(regionConfigurer);
		verifyNoInteractions(mockRepository, peerRegionFactoryBean);
	}

	@Test
	public void generateIdIsSuccessful() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion"));

		assertThat(regionConfigurer.generateId("MockRegion")).startsWith("MockRegion-AEQ-");

		verifyNoInteractions(mockRepository);
	}

	private void testGenerateIdWithInvalidRegionBeanName(String regionBeanName) {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion"));

		try {
			regionConfigurer.generateId(regionBeanName);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region bean name [%s] must be specified", regionBeanName);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockRepository);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateIdWhenRegionBeanNameIsBlank() {
		testGenerateIdWithInvalidRegionBeanName("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateIdWhenRegionBeanNameIsEmpty() {
		testGenerateIdWithInvalidRegionBeanName("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateIdWhenRegionBeanNameIsNull() {
		testGenerateIdWithInvalidRegionBeanName(null);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void newAsyncEventQueueCreatesQueueFromCacheInitializedWithRegionConfigurer() {

		AsyncEventErrorHandler mockAsyncEventErrorHandler = mock(AsyncEventErrorHandler.class);

		AsyncEventQueue mockAsyncEventQueue = mock(AsyncEventQueue.class, "Mock AEQ");

		AsyncEventQueueFactory mockAsyncEventQueueFactory = mock(AsyncEventQueueFactory.class, "Mock AEQ Factory");

		Cache mockCache = mock(Cache.class, "Mock Cache");

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		Duration batchTimeInterval = Duration.ofSeconds(15);

		Function<AsyncEventListener, AsyncEventListener> mockAsyncEventListenerFunction = mock(Function.class);
		Function<AsyncEventQueue, AsyncEventQueue> mockAsyncEventQueueFunction = mock(Function.class);
		Function<AsyncEventQueueFactory, AsyncEventQueueFactory> mockAsyncEventQueueFactoryFunction = mock(Function.class);

		GatewayEventFilter mockEventFilterOne = mock(GatewayEventFilter.class);
		GatewayEventFilter mockEventFilterTwo = mock(GatewayEventFilter.class);

		GatewayEventSubstitutionFilter mockEventSubstitutionFilter = mock(GatewayEventSubstitutionFilter.class);

		RepositoryAsyncEventListener mockAsyncEventListener =
			mock(RepositoryAsyncEventListener.class, "Mock AEQ Listener");

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		doReturn(mockAsyncEventQueueFactory).when(mockCache).createAsyncEventQueueFactory();
		doReturn(mockAsyncEventQueue).when(mockAsyncEventQueueFactory).create(eq("123"), eq(mockAsyncEventListener));
		doReturn("123").when(regionConfigurer).generateId(eq("TestRegion"));
		doReturn(mockAsyncEventListener).when(regionConfigurer).newRepositoryAsyncEventListener();
		doReturn(mockAsyncEventListener).when(mockAsyncEventListenerFunction).apply(eq(mockAsyncEventListener));
		doReturn(mockAsyncEventQueue).when(mockAsyncEventQueueFunction).apply(eq(mockAsyncEventQueue));
		doReturn(mockAsyncEventQueueFactory).when(mockAsyncEventQueueFactoryFunction).apply(eq(mockAsyncEventQueueFactory));

		assertThat(regionConfigurer.applyToListener(mockAsyncEventListenerFunction)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.applyToQueue(mockAsyncEventQueueFunction)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.applyToQueueFactory(mockAsyncEventQueueFactoryFunction)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withAsyncEventErrorHandler(mockAsyncEventErrorHandler)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withParallelQueue()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withPersistentQueue()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchConflationEnabled()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchSize(224)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchTimeInterval(batchTimeInterval)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDiskStore("TestDiskStore")).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDiskSynchronizationEnabled()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDispatcherThreadCount(8)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventDispatchingPaused()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventFilters(Arrays.asList(mockEventFilterOne, mockEventFilterTwo))).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventSubstitutionFilter(mockEventSubstitutionFilter)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueForwardedExpirationDestroyEvents()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueMaxMemory(51)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueOrderPolicy(GatewaySender.OrderPolicy.THREAD)).isSameAs(regionConfigurer);

		// Create the AsyncEventQueue (AEQ)
		assertThat(regionConfigurer.newAsyncEventQueue(mockCache, "TestRegion"))
			.isEqualTo(mockAsyncEventQueue);

		InOrder order = inOrder(regionConfigurer, mockAsyncEventListener, mockAsyncEventQueueFactory, mockCache,
			mockAsyncEventListenerFunction, mockAsyncEventQueueFunction, mockAsyncEventQueueFactoryFunction);

		order.verify(regionConfigurer, times(1)).newAsyncEventQueueFactory(eq(mockCache));
		order.verify(mockCache, times(1)).createAsyncEventQueueFactory();
		order.verify(mockAsyncEventQueueFactory, times(1)).setBatchConflationEnabled(eq(true));
		order.verify(mockAsyncEventQueueFactory, times(1)).setBatchSize(eq(224));
		order.verify(mockAsyncEventQueueFactory, times(1)).setBatchTimeInterval(eq((int) batchTimeInterval.toMillis()));
		order.verify(mockAsyncEventQueueFactory, times(1)).setDiskStoreName(eq("TestDiskStore"));
		order.verify(mockAsyncEventQueueFactory, times(1)).setDiskSynchronous(eq(true));
		order.verify(mockAsyncEventQueueFactory, times(1)).setDispatcherThreads(eq(8));
		order.verify(mockAsyncEventQueueFactory, times(1)).setForwardExpirationDestroy(eq(true));
		order.verify(mockAsyncEventQueueFactory, times(1)).setGatewayEventSubstitutionListener(eq(mockEventSubstitutionFilter));
		order.verify(mockAsyncEventQueueFactory, times(1)).setMaximumQueueMemory(eq(51));
		order.verify(mockAsyncEventQueueFactory, times(1)).setOrderPolicy(eq(GatewaySender.OrderPolicy.THREAD));
		order.verify(mockAsyncEventQueueFactory, times(1)).setParallel(eq(true));
		order.verify(mockAsyncEventQueueFactory, times(1)).setPersistent(eq(true));
		order.verify(mockAsyncEventQueueFactory, times(1)).addGatewayEventFilter(eq(mockEventFilterOne));
		order.verify(mockAsyncEventQueueFactory, times(1)).addGatewayEventFilter(eq(mockEventFilterTwo));
		order.verify(mockAsyncEventQueueFactory, times(1)).pauseEventDispatching();
		order.verify(regionConfigurer, times(1)).generateId(eq("TestRegion"));
		order.verify(regionConfigurer, times(1)).newRepositoryAsyncEventListener();
		order.verify(regionConfigurer, times(1)).postProcess(eq(mockAsyncEventListener));
		order.verify(mockAsyncEventListener, times(1)).setAsyncEventErrorHandler(eq(mockAsyncEventErrorHandler));
		order.verify(mockAsyncEventListenerFunction, times(1)).apply(eq(mockAsyncEventListener));
		order.verify(regionConfigurer, times(1)).postProcess(eq(mockAsyncEventQueueFactory));
		order.verify(mockAsyncEventQueueFactoryFunction, times(1)).apply(eq(mockAsyncEventQueueFactory));
		order.verify(mockAsyncEventQueueFactory, times(1)).create(eq("123"), eq(mockAsyncEventListener));
		order.verify(regionConfigurer, times(1)).postProcess(eq(mockAsyncEventQueue));
		order.verify(mockAsyncEventQueueFunction, times(1)).apply(eq(mockAsyncEventQueue));

		verifyNoMoreInteractions(mockCache, mockAsyncEventListener, mockAsyncEventListenerFunction,
			mockAsyncEventQueueFactory, mockAsyncEventQueueFunction, mockAsyncEventQueueFactoryFunction);

		verifyNoInteractions(mockAsyncEventErrorHandler, mockAsyncEventQueue, mockRepository,
			mockEventFilterOne, mockEventFilterTwo, mockEventSubstitutionFilter);
	}

	@Test
	public void newAsyncEventQueueIsParallel() {

		AsyncEventQueueFactory mockAsyncEventQueueFactory = mock(AsyncEventQueueFactory.class);

		Cache mockCache = mock(Cache.class);

		doReturn(mockAsyncEventQueueFactory).when(mockCache).createAsyncEventQueueFactory();

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(AsyncInlineCachingRegionConfigurer.create(mockRepository, Predicate.isEqual("TestRegion")));

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.withParallelQueue()).isSameAs(regionConfigurer);

		regionConfigurer.newAsyncEventQueue(mockCache, "TestRegion");

		verify(mockAsyncEventQueueFactory, times(1)).setParallel(eq(true));
	}

	@Test
	public void newAsyncEventQueueIsSerial() {

		AsyncEventQueueFactory mockAsyncEventQueueFactory = mock(AsyncEventQueueFactory.class);

		Cache mockCache = mock(Cache.class);

		doReturn(mockAsyncEventQueueFactory).when(mockCache).createAsyncEventQueueFactory();

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(AsyncInlineCachingRegionConfigurer.create(mockRepository, Predicate.isEqual("TestRegion")));

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.withSerialQueue()).isSameAs(regionConfigurer);

		regionConfigurer.newAsyncEventQueue(mockCache, "TestRegion");

		verify(mockAsyncEventQueueFactory, times(1)).setParallel(eq(false));
	}

	@Test
	public void newAsyncEventQueueCreatesQueueFromFactoryWithIdAndListener() {

		AsyncEventListener mockAsyncEventListener = mock(AsyncEventListener.class, "Mock AEQ Listener");

		AsyncEventQueue mockAsyncEventQueue = mock(AsyncEventQueue.class, "Mock AEQ");

		AsyncEventQueueFactory mockAsyncEventQueueFactory = mock(AsyncEventQueueFactory.class, "Mock AEQ Factory");

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		String asyncEventQueueId = "abc123";

		doReturn(mockAsyncEventQueue).when(mockAsyncEventQueueFactory)
			.create(eq(asyncEventQueueId), eq(mockAsyncEventListener));

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion"));

		assertThat(regionConfigurer.newAsyncEventQueue(mockAsyncEventQueueFactory, asyncEventQueueId, mockAsyncEventListener))
			.isEqualTo(mockAsyncEventQueue);

		verify(mockAsyncEventQueueFactory, times(1))
			.create(eq(asyncEventQueueId), eq(mockAsyncEventListener));
		verifyNoMoreInteractions(mockAsyncEventQueueFactory);
		verifyNoInteractions(mockAsyncEventListener, mockAsyncEventQueue, mockRepository);
	}

	@Test
	public void newAsyncEventQueueFactoryCallsCacheCreateAsyncEventQueueFactory() {

		Cache mockCache = mock(Cache.class, "Mock Peer Cache");

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion"));

		regionConfigurer.newAsyncEventQueueFactory(mockCache);

		verify(mockCache, times(1)).createAsyncEventQueueFactory();
		verifyNoMoreInteractions(mockCache);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void newRepositoryAsyncEventListenerReturnsNewListener() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer(mockRepository, Predicate.isEqual("TestRegion")));

		AsyncEventListener listener = regionConfigurer.newRepositoryAsyncEventListener();

		assertThat(listener).isInstanceOf(RepositoryAsyncEventListener.class);
		assertThat(((RepositoryAsyncEventListener<?, ?>) listener).getRepository()).isEqualTo(mockRepository);

		verify(regionConfigurer, times(1)).newRepositoryAsyncEventListener(eq(mockRepository));
		verify(regionConfigurer, times(1)).getRepository();
		verifyNoInteractions(mockRepository);
	}
}
