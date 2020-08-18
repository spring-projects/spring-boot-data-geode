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
package org.springframework.geode.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.Test;

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
 * @see org.springframework.data.gemfire.PeerRegionFactoryBean
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @since 1.4.0
 */
public class AsyncInlineCachingRegionConfigurerUnitTests {

	@Test
	public void constructAsyncInlineCachingRegionConfigurer() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		Predicate<String> regionBeanName = Predicate.isEqual("TestRegion");

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, regionBeanName);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isEqualTo(regionBeanName);
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void constructAsyncInlineCachingRegionConfigurerWithNullRegionBeanNamePredicate() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, null);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName()).isNotNull();
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

		Predicate<String> regionBeanName = Predicate.isEqual("TestRegion");

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, regionBeanName);

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);
		assertThat(regionConfigurer.getRegionBeanName()).isEqualTo(regionBeanName);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void createAsyncInlineCachingRegionConfigurerFromCrudRepositoryAndString() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			AsyncInlineCachingRegionConfigurer.create(mockRepository, "MockRegion");

		assertThat(regionConfigurer).isNotNull();
		assertThat(regionConfigurer.getRepository()).isEqualTo(mockRepository);
		assertThat(regionConfigurer.getRegionBeanName()).isNotNull();
		assertThat(regionConfigurer.getRegionBeanName().test("MockRegion")).isTrue();
		assertThat(regionConfigurer.getRegionBeanName().test("TestRegion")).isFalse();

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

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		doReturn(mockCache).when(peerRegionFactoryBean).getCache();
		doReturn(mockAsyncEventQueue).when(regionConfigurer).newAsyncEventQueue(eq(mockCache), eq("TestRegion"));

		regionConfigurer.configure("TestRegion", peerRegionFactoryBean);

		verify(regionConfigurer, times(1))
			.configure(eq("TestRegion"), eq(peerRegionFactoryBean));
		verify(regionConfigurer, times(1)).getRegionBeanName();
		verify(regionConfigurer, times(1))
			.newAsyncEventQueue(eq(mockCache), eq("TestRegion"));
		verify(peerRegionFactoryBean, times(1)).getCache();
		verify(peerRegionFactoryBean, times(1))
			.setAsyncEventQueues(eq(ArrayUtils.asArray(mockAsyncEventQueue)));

		verifyNoMoreInteractions(regionConfigurer, peerRegionFactoryBean);
		verifyNoInteractions(mockAsyncEventQueue, mockCache, mockRepository);
	}

	@Test
	public void configureDoesNothingWhenRegionBeanNameDoesNotMatch() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		PeerRegionFactoryBean<?, ?> peerRegionFactoryBean = mock(PeerRegionFactoryBean.class);

		regionConfigurer.configure("MockRegion", peerRegionFactoryBean);

		verify(regionConfigurer, times(1))
			.configure(eq("MockRegion"), eq(peerRegionFactoryBean));
		verify(regionConfigurer, times(1)).getRegionBeanName();
		verifyNoMoreInteractions(regionConfigurer);
		verifyNoInteractions(peerRegionFactoryBean);
	}

	@Test
	public void generateIdIsSuccessful() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion"));

		assertThat(regionConfigurer.generateId("MockRegion")).startsWith("MockRegion-AEQ:");

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
	public void newAsyncEventQueueCreatesAsyncEventQueueFromCacheInitializedWithRegionConfigurer() {

		AsyncEventListener mockAsyncEventListener = mock(AsyncEventListener.class, "Mock AEQ Listener");

		AsyncEventQueue mockAsyncEventQueue = mock(AsyncEventQueue.class, "Mock AEQ");

		AsyncEventQueueFactory mockAsyncEventQueueFactory = mock(AsyncEventQueueFactory.class, "Mock AEQ Factory");

		Cache mockCache = mock(Cache.class, "Mock Cache");

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		Duration batchTimeInterval = Duration.ofSeconds(15);

		GatewayEventFilter mockEventFilterOne = mock(GatewayEventFilter.class);
		GatewayEventFilter mockEventFilterTwo = mock(GatewayEventFilter.class);

		GatewayEventSubstitutionFilter mockEventSubstitutionFilter = mock(GatewayEventSubstitutionFilter.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		doReturn(mockAsyncEventQueueFactory).when(mockCache).createAsyncEventQueueFactory();
		doReturn(mockAsyncEventQueue).when(mockAsyncEventQueueFactory).create(eq("123"), eq(mockAsyncEventListener));
		doReturn("123").when(regionConfigurer).generateId(eq("TestRegion"));
		doReturn(mockAsyncEventListener).when(regionConfigurer).newRepositoryAsyncEventListener();

		assertThat(regionConfigurer.withParallelQueue()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withPersistentQueue()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchConflationEnabled()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchSize(224)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueBatchTimeInterval(batchTimeInterval)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDiskStore("TestDiskStore")).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDiskSynchronizationEnabled()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueDispatcherThreadCount(8)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventDispatchingPaused()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventFilters(Arrays.asList(mockEventFilterOne, mockEventFilterTwo)))
			.isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueEventSubstitutionFilter(mockEventSubstitutionFilter))
			.isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueForwardedExpirationDestroyEvents()).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueMaxMemory(51)).isSameAs(regionConfigurer);
		assertThat(regionConfigurer.withQueueOrderPolicy(GatewaySender.OrderPolicy.THREAD)).isSameAs(regionConfigurer);

		// Create the AsyncEventQueue (AEQ)
		assertThat(regionConfigurer.newAsyncEventQueue(mockCache, "TestRegion"))
			.isEqualTo(mockAsyncEventQueue);

		verify(mockCache, times(1)).createAsyncEventQueueFactory();
		verify(mockAsyncEventQueueFactory, times(1)).setBatchConflationEnabled(eq(true));
		verify(mockAsyncEventQueueFactory, times(1)).setBatchSize(eq(224));
		verify(mockAsyncEventQueueFactory, times(1)).setBatchTimeInterval(eq((int) batchTimeInterval.toMillis()));
		verify(mockAsyncEventQueueFactory, times(1)).setDiskStoreName(eq("TestDiskStore"));
		verify(mockAsyncEventQueueFactory, times(1)).setDiskSynchronous(eq(true));
		verify(mockAsyncEventQueueFactory, times(1)).setDispatcherThreads(eq(8));
		verify(mockAsyncEventQueueFactory, times(1)).setForwardExpirationDestroy(eq(true));
		verify(mockAsyncEventQueueFactory, times(1)).setGatewayEventSubstitutionListener(eq(mockEventSubstitutionFilter));
		verify(mockAsyncEventQueueFactory, times(1)).setMaximumQueueMemory(eq(51));
		verify(mockAsyncEventQueueFactory, times(1)).setOrderPolicy(eq(GatewaySender.OrderPolicy.THREAD));
		verify(mockAsyncEventQueueFactory, times(1)).setParallel(eq(true));
		verify(mockAsyncEventQueueFactory, times(1)).setPersistent(eq(true));
		verify(mockAsyncEventQueueFactory, times(1)).addGatewayEventFilter(eq(mockEventFilterOne));
		verify(mockAsyncEventQueueFactory, times(1)).addGatewayEventFilter(eq(mockEventFilterTwo));
		verify(mockAsyncEventQueueFactory, times(1)).pauseEventDispatching();
		verify(mockAsyncEventQueueFactory, times(1)).create(eq("123"), eq(mockAsyncEventListener));
		verify(regionConfigurer, times(1)).generateId(eq("TestRegion"));
		verify(regionConfigurer, times(1)).newRepositoryAsyncEventListener();

		verifyNoMoreInteractions(mockCache, mockAsyncEventQueueFactory);

		verifyNoInteractions(mockAsyncEventListener, mockAsyncEventQueue, mockRepository, mockEventFilterOne,
			mockEventFilterTwo, mockEventSubstitutionFilter);
	}

	@Test
	public void newRepositoryAsyncEventListener() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		AsyncInlineCachingRegionConfigurer<?, ?> regionConfigurer =
			spy(new AsyncInlineCachingRegionConfigurer<>(mockRepository, Predicate.isEqual("TestRegion")));

		AsyncEventListener listener = regionConfigurer.newRepositoryAsyncEventListener();

		assertThat(listener).isInstanceOf(RepositoryAsyncEventListener.class);
		assertThat(((RepositoryAsyncEventListener<?, ?>) listener).getRepository()).isEqualTo(mockRepository);

		verify(regionConfigurer, times(1)).getRepository();
		verifyNoInteractions(mockRepository);
	}
}
