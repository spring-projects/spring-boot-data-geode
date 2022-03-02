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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Operation;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;
import org.apache.geode.cache.asyncqueue.AsyncEventQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AbstractAsyncEventOperationRepositoryFunction;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventError;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventOperationRepositoryFunction;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Integration Tests for {@link AsyncInlineCachingRegionConfigurer} and {@link RepositoryAsyncEventListener}
 * with custom {@link AsyncEventErrorHandler} and user-defined {@link AsyncEventOperationRepositoryFunction}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Operation
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.AsyncInlineCachingRegionConfigurer
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AbstractAsyncEventOperationRepositoryFunction
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventOperationRepositoryFunction
 * @since 1.4.0
 */
@RunWith(SpringRunner.class)
@SuppressWarnings({ "unused" })
public class AsyncInlineCachingRegionConfigurerWithUserDefinedAsyncEventOperationRepositoryFunctionAndCustomAsyncEventErrorHandlerIntegrationTests
		extends IntegrationTestsSupport {

	private final AtomicReference<AsyncEventError> asyncEventErrorReference = new AtomicReference<>(null);

	@Autowired
	private AsyncEventErrorHandler errorHandler;

	@Autowired
	private Cache peerCache;

	@Autowired
	@Qualifier("processRepository")
	private CrudRepository<Process, Long> processRepository;

	@Resource(name = "Processes")
	private Region<Long, Process> processes;

	@Before
	@SuppressWarnings("unchecked")
	public void forwardRegionOperationsToAsyncEventQueueListener() {

		assertThat(this.errorHandler).isNotNull();
		assertThat(this.peerCache).isNotNull();
		assertThat(this.processes).isNotNull();
		assertThat(this.processes.getName()).isEqualTo("Processes");
		assertThat(this.processRepository).isNotNull();

		RegionAttributes<Long, Process> processesAttributes = this.processes.getAttributes();

		assertThat(processesAttributes).isNotNull();

		Set<String> asyncEventQueueIds = processesAttributes.getAsyncEventQueueIds();

		assertThat(asyncEventQueueIds).isNotNull();
		assertThat(asyncEventQueueIds).hasSize(1);

		String asyncEventQueueId = asyncEventQueueIds.iterator().next();

		assertThat(asyncEventQueueId).isNotBlank();

		AsyncEventQueue asyncEventQueue = this.peerCache.getAsyncEventQueue(asyncEventQueueId);

		assertThat(asyncEventQueue).isNotNull();
		assertThat(asyncEventQueue.getId()).isEqualTo(asyncEventQueueId);

		AsyncEventListener listener = asyncEventQueue.getAsyncEventListener();

		assertThat(listener).isInstanceOf(RepositoryAsyncEventListener.class);
		assertThat(((RepositoryAsyncEventListener<Process, Long>) listener).getAsyncEventErrorHandler())
			.isEqualTo(this.errorHandler);
		assertThat(((RepositoryAsyncEventListener<Process, Long>) listener).getRepository())
			.isEqualTo(this.processRepository);

		doAnswer(invocation -> {

			Long key = invocation.getArgument(0);

			Process process = this.processes.get(key);

			AsyncEvent<Long, Process> mockEvent = mock(AsyncEvent.class);

			doReturn(key).when(mockEvent).getKey();
			doReturn(process).when(mockEvent).getDeserializedValue();
			doReturn(Operation.CONTAINS_KEY).when(mockEvent).getOperation();
			doReturn(this.processes).when(mockEvent).getRegion();

			listener.processEvents(Collections.singletonList(mockEvent));

			return process != null;

		}).when(this.processes).containsKey(anyLong());

		doAnswer(invocation -> {

			AsyncEventError eventError = invocation.getArgument(0);

			assertThat(eventError).isNotNull();
			assertThat(eventError.getCause()).isInstanceOf(IllegalStateException.class);
			assertThat(eventError.getCause().getMessage()).isEqualTo("The entity (deserialized value) was null");
			assertThat(eventError.getCause()).hasNoCause();
			assertThat(eventError.getEvent()).isNotNull();

			this.asyncEventErrorReference.set(eventError);

			return false;

		}).when(this.errorHandler).apply(isA(AsyncEventError.class));
	}

	@Test
	public void asyncEventQueueEventsProcessedByListener() {

		Process process = Process.newProcess(21L, "Test Process");

		assertThat(this.processes.containsKey(process.getId())).isFalse();
		assertThat(this.asyncEventErrorReference.get()).isNotNull();

		verify(this.errorHandler, times(1))
			.apply(eq(this.asyncEventErrorReference.getAndUpdate(value -> null)));
		verifyNoInteractions(this.processRepository);

		assertThat(this.processes.put(process.getId(), process)).isNull();
		assertThat(this.processes.containsKey(process.getId())).isTrue();
		assertThat(this.asyncEventErrorReference.get()).isNull();

		verify(this.processRepository, times(1)).existsById(eq(process.getId()));
		verifyNoMoreInteractions(this.errorHandler, this.processRepository);
	}

	@PeerCacheApplication
	@EnableGemFireMockObjects
	@EnableEntityDefinedRegions
	static class GeodeConfiguration {

		@Bean
		AsyncEventErrorHandler mockAsyncEventErrorHandler() {
			return mock(AsyncEventErrorHandler.class);
		}

		@Bean
		@SuppressWarnings("unchecked")
		AsyncInlineCachingRegionConfigurer<Process, Long> asyncInlineCachingProcessesRegionConfigurer(
				AsyncEventErrorHandler asyncEventErrorHandler,
				@Qualifier("processRepository") CrudRepository<Process, Long> processRepository
		) {

			return AsyncInlineCachingRegionConfigurer.create(processRepository, "Processes")
				.withAsyncEventErrorHandler(asyncEventErrorHandler)
				.applyToListener(listener -> {

					if (listener instanceof RepositoryAsyncEventListener) {

						RepositoryAsyncEventListener<Process, Long> repositoryListener =
							(RepositoryAsyncEventListener<Process, Long>) listener;

						repositoryListener.register(
							new AbstractAsyncEventOperationRepositoryFunction<Process, Long>(repositoryListener) {

								@Override
								public boolean canProcess(@Nullable AsyncEvent<Long, Process> event) {
									return event != null && Operation.CONTAINS_KEY.equals(event.getOperation());
								}

								@Override
								protected Object doRepositoryOp(@NonNull Process process) {
									return getRepository().existsById(process.getId());
								}
							});
					}

					return listener;
				});
		}

		@Bean
		@SuppressWarnings("unchecked")
		CrudRepository<Process, Long> processRepository() {
			return mock(CrudRepository.class);
		}
	}

	@Getter
	@EqualsAndHashCode
	@ToString(of = "name")
	@AllArgsConstructor(staticName = "newProcess")
	@org.springframework.data.gemfire.mapping.annotation.Region("Processes")
	static class Process {

		@Id
		private Long id;

		@NonNull
		private String name;

	}
}
