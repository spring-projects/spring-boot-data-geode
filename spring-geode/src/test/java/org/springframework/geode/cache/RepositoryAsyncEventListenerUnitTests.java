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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.InOrder;

import org.apache.geode.cache.Operation;
import org.apache.geode.cache.asyncqueue.AsyncEvent;

import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AbstractAsyncEventOperationRepositoryFunction;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventError;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventErrorHandler;
import org.springframework.geode.cache.RepositoryAsyncEventListener.AsyncEventOperationRepositoryFunction;
import org.springframework.geode.cache.RepositoryAsyncEventListener.CreateUpdateAsyncEventRepositoryFunction;
import org.springframework.geode.cache.RepositoryAsyncEventListener.RemoveAsyncEventRepositoryFunction;
import org.springframework.lang.NonNull;

/**
 * Unit Tests for {@link RepositoryAsyncEventListener}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Operation
 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.geode.cache.RepositoryAsyncEventListener
 * @since 1.4.0
 */
@SuppressWarnings({ "rawtypes", "unchecked"})
public class RepositoryAsyncEventListenerUnitTests {

	@Test
	public void constructRepositoryAsyncEventListener() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener<?, ?> listener = new RepositoryAsyncEventListener<>(mockRepository);

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);

		verifyNoInteractions(mockRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructRepositoryAsyncEventListenerWithNullRepositoryThrowsIllegalArgumentException() {

		try {
			new RepositoryAsyncEventListener<>(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CrudRepository must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void setAndGetAsyncEventErrorHandler() {

		AsyncEventErrorHandler mockAsyncEventErrorHandler = mock(AsyncEventErrorHandler.class);

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener<>(mockRepository);

		assertThat(listener.getAsyncEventErrorHandler())
			.isEqualTo(RepositoryAsyncEventListener.DEFAULT_ASYNC_EVENT_ERROR_HANDLER);

		listener.setAsyncEventErrorHandler(mockAsyncEventErrorHandler);

		assertThat(listener.getAsyncEventErrorHandler()).isEqualTo(mockAsyncEventErrorHandler);

		listener.setAsyncEventErrorHandler(null);

		assertThat(listener.getAsyncEventErrorHandler())
			.isEqualTo(RepositoryAsyncEventListener.DEFAULT_ASYNC_EVENT_ERROR_HANDLER);

		verifyNoInteractions(mockAsyncEventErrorHandler, mockRepository);
	}

	@Test
	public void getRepositoryFunctionsHandlesCreateUpdateAndRemove() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener<>(mockRepository);

		List<AsyncEventOperationRepositoryFunction<Object, Object>> repositoryFunctions =
			listener.getRepositoryFunctions();

		assertThat(repositoryFunctions).isNotNull();

		List<Class<?>> repositoryFunctionTypes = repositoryFunctions.stream()
			.map(Object::getClass)
			.collect(Collectors.toList());

		assertThat(repositoryFunctionTypes).containsExactly(
			CreateUpdateAsyncEventRepositoryFunction.class,
			RemoveAsyncEventRepositoryFunction.class
		);

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void registerAndUnregisterAsyncEventOperationRepositoryFunction() {

		AsyncEventOperationRepositoryFunction mockFunction = mock(AsyncEventOperationRepositoryFunction.class);

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener<>(mockRepository);

		assertThat(listener.getRepositoryFunctions()).hasSize(2);
		assertThat(listener.getRepositoryFunctions().get(0).getClass())
			.isEqualTo(CreateUpdateAsyncEventRepositoryFunction.class);
		assertThat(listener.getRepositoryFunctions().get(1).getClass())
			.isEqualTo(RemoveAsyncEventRepositoryFunction.class);
		assertThat(listener.register(mockFunction)).isTrue();
		assertThat(listener.register(null)).isFalse();
		assertThat(listener.getRepositoryFunctions()).hasSize(3);
		assertThat(listener.getRepositoryFunctions()).contains(mockFunction);
		assertThat(listener.getRepositoryFunctions().get(0)).isEqualTo(mockFunction);
		assertThat(listener.getRepositoryFunctions().get(1).getClass())
			.isEqualTo(CreateUpdateAsyncEventRepositoryFunction.class);
		assertThat(listener.getRepositoryFunctions().get(2).getClass())
			.isEqualTo(RemoveAsyncEventRepositoryFunction.class);
		assertThat(listener.unregister(null)).isFalse();
		assertThat(listener.unregister(mockFunction)).isTrue();
		assertThat(listener.getRepositoryFunctions()).hasSize(2);
		assertThat(listener.getRepositoryFunctions().get(0).getClass())
			.isEqualTo(CreateUpdateAsyncEventRepositoryFunction.class);
		assertThat(listener.getRepositoryFunctions().get(1).getClass())
			.isEqualTo(RemoveAsyncEventRepositoryFunction.class);

		verifyNoInteractions(mockFunction, mockRepository);
	}

	@Test
	public void processEventsIsNullSafe() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener<>(mockRepository);

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);
		assertThat(listener.processEvents(null)).isTrue();

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void processEventsIsSuccessful() {

		AsyncEvent mockEventOne = mock(AsyncEvent.class, "AsyncEventOne");
		AsyncEvent mockEventTwo = mock(AsyncEvent.class, "AsyncEventTwo");
		AsyncEvent mockEventThree = mock(AsyncEvent.class, "AsyncEventThree");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionA =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionA");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionB =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionB");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionC =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionC");

		CrudRepository mockRepository = mock(CrudRepository.class);

		doAnswer(invocation -> {

			AsyncEvent event = invocation.getArgument(0);

			return mockEventOne.equals(event) || mockEventThree.equals(event);

		}).when(mockRepositoryFunctionA).canProcess(isA(AsyncEvent.class));

		doAnswer(invocation -> {
			mockRepository.save(invocation.getArgument(0));
			return true;
		}).when(mockRepositoryFunctionA).apply(isA(AsyncEvent.class));

		doAnswer(invocation -> {

			AsyncEvent event = invocation.getArgument(0);

			return mockEventTwo.equals(event) || mockEventThree.equals(event);

		}).when(mockRepositoryFunctionB).canProcess(isA(AsyncEvent.class));

		doAnswer(invocation -> {
			mockRepository.delete(invocation.getArgument(0));
			return true;
		}).when(mockRepositoryFunctionB).apply(isA(AsyncEvent.class));

		doReturn(false).when(mockRepositoryFunctionC).canProcess(any());

		List<AsyncEvent> mockEvents = Arrays.asList(mockEventOne, mockEventTwo, mockEventThree);

		RepositoryAsyncEventListener listener = spy(new RepositoryAsyncEventListener(mockRepository));

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);

		doReturn(Arrays.asList(mockRepositoryFunctionC, mockRepositoryFunctionA, mockRepositoryFunctionB))
			.when(listener).getRepositoryFunctions();

		assertThat(listener.processEvents(mockEvents)).isTrue();

		InOrder order =
			inOrder(mockRepository, mockRepositoryFunctionA, mockRepositoryFunctionB, mockRepositoryFunctionC);

		order.verify(mockRepositoryFunctionC, times(1)).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionA, times(1)).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionB, never()).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionA, times(1)).apply(eq(mockEventOne));
		order.verify(mockRepository, times(1)).save(eq(mockEventOne));
		order.verify(mockRepositoryFunctionC, times(1)).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionA, times(1)).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionB, times(1)).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionB, times(1)).apply(eq(mockEventTwo));
		order.verify(mockRepository, times(1)).delete(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionC, times(1)).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionA, times(1)).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionB, never()).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionA, times(1)).apply(eq(mockEventThree));
		order.verify(mockRepository, times(1)).save(eq(mockEventThree));

		verify(mockRepositoryFunctionA, never()).apply(eq(mockEventTwo));
		verify(mockRepositoryFunctionB, never()).apply(eq(mockEventOne));
		verify(mockRepositoryFunctionB, never()).apply(eq(mockEventThree));
		verify(mockRepositoryFunctionC, never()).apply(any());
		verify(mockRepository, never()).delete(eq(mockEventOne));
		verify(mockRepository, never()).delete(eq(mockEventThree));
		verify(mockRepository, never()).save(eq(mockEventTwo));

		verifyNoInteractions(mockEventOne, mockEventTwo, mockEventThree);
	}

	@Test
	public void processEventsIsUnsuccessfulWhenASingleFunctionApplyReturnsFalse() {

		AsyncEvent mockEventOne = mock(AsyncEvent.class, "AsyncEventOne");
		AsyncEvent mockEventTwo = mock(AsyncEvent.class, "AsyncEventTwo");
		AsyncEvent mockEventThree = mock(AsyncEvent.class, "AsyncEventThree");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionOne =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionOne");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionTwo =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionTwo");

		AsyncEventOperationRepositoryFunction mockRepositoryFunctionThree =
			mock(AsyncEventOperationRepositoryFunction.class, "RepositoryFunctionThree");

		doReturn(true).when(mockRepositoryFunctionOne).canProcess(eq(mockEventOne));
		doReturn(true).when(mockRepositoryFunctionOne).apply(eq(mockEventOne));
		doReturn(true).when(mockRepositoryFunctionTwo).canProcess(eq(mockEventTwo));
		doReturn(false).when(mockRepositoryFunctionTwo).apply(eq(mockEventTwo));
		doReturn(true).when(mockRepositoryFunctionThree).canProcess(eq(mockEventThree));
		doReturn(true).when(mockRepositoryFunctionThree).apply(eq(mockEventThree));

		CrudRepository mockRepository = mock(CrudRepository.class);

		List<AsyncEvent> mockEvents = Arrays.asList(mockEventOne, mockEventTwo, mockEventThree);

		RepositoryAsyncEventListener listener = spy(new RepositoryAsyncEventListener(mockRepository));

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);

		doReturn(Arrays.asList(mockRepositoryFunctionOne, mockRepositoryFunctionTwo, mockRepositoryFunctionThree))
			.when(listener).getRepositoryFunctions();

		assertThat(listener.processEvents(mockEvents)).isFalse();

		InOrder order =
			inOrder(mockRepository, mockRepositoryFunctionOne, mockRepositoryFunctionTwo, mockRepositoryFunctionThree);

		order.verify(mockRepositoryFunctionOne, times(1)).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionTwo, never()).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionThree, never()).canProcess(eq(mockEventOne));
		order.verify(mockRepositoryFunctionOne, times(1)).apply(eq(mockEventOne));
		order.verify(mockRepositoryFunctionOne, times(1)).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionTwo, times(1)).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionThree, never()).canProcess(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionTwo, times(1)).apply(eq(mockEventTwo));
		order.verify(mockRepositoryFunctionOne, times(1)).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionTwo, times(1)).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionThree, times(1)).canProcess(eq(mockEventThree));
		order.verify(mockRepositoryFunctionThree, times(1)).apply(eq(mockEventThree));

		verify(mockRepositoryFunctionOne, never()).apply(eq(mockEventTwo));
		verify(mockRepositoryFunctionOne, never()).apply(eq(mockEventThree));
		verify(mockRepositoryFunctionTwo, never()).apply(eq(mockEventOne));
		verify(mockRepositoryFunctionTwo, never()).apply(eq(mockEventThree));
		verify(mockRepositoryFunctionThree, never()).apply(eq(mockEventOne));
		verify(mockRepositoryFunctionThree, never()).apply(eq(mockEventTwo));

		verifyNoInteractions(mockRepository, mockEventOne, mockEventTwo, mockEventThree);
	}

	@Test
	public void processEventsIsUnsuccessfulWhenNoFunctionCanProcessEvent() {

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		AsyncEventOperationRepositoryFunction mockRepositoryFunction =
			mock(AsyncEventOperationRepositoryFunction.class);

		doReturn(false).when(mockRepositoryFunction).canProcess(any());

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = spy(new RepositoryAsyncEventListener(mockRepository));

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);

		doReturn(Collections.singletonList(mockRepositoryFunction)).when(listener).getRepositoryFunctions();

		assertThat(listener.processEvents(Collections.singletonList(mockEvent))).isFalse();

		verify(mockRepositoryFunction, times(1)).canProcess(eq(mockEvent));
		verify(mockRepositoryFunction, never()).apply(any());

		verifyNoInteractions(mockEvent, mockRepository);
	}

	@Test
	public void processNoEventsIsSuccessful() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener<?, ?> listener = new RepositoryAsyncEventListener<>(mockRepository);

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);
		assertThat(listener.processEvents(Collections.emptyList())).isTrue();

		verifyNoInteractions(mockRepository);
	}

	@Test
	public void processEventsCountsInvocations() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener<?, ?> listener = spy(new RepositoryAsyncEventListener<>(mockRepository));

		doReturn(true).when(listener).doProcessEvents(any());

		assertThat(listener).isNotNull();
		assertThat(listener.getRepository()).isEqualTo(mockRepository);
		assertThat(listener.getFiredCount()).isZero();
		assertThat(listener.hasFired()).isFalse();
		assertThat(listener.hasFiredSinceLastCheck()).isFalse();

		listener.processEvents(Collections.emptyList());

		assertThat(listener.getFiredCount()).isOne();
		assertThat(listener.hasFired()).isTrue();
		assertThat(listener.hasFiredSinceLastCheck()).isTrue();
		assertThat(listener.getFiredCount()).isOne();
		assertThat(listener.hasFired()).isTrue();
		assertThat(listener.hasFiredSinceLastCheck()).isFalse();

		listener.processEvents(Collections.singletonList(mock(AsyncEvent.class)));
		listener.processEvents(Collections.emptyList());

		assertThat(listener.getFiredCount()).isEqualTo(3);
		assertThat(listener.hasFired()).isTrue();
		assertThat(listener.hasFiredSinceLastCheck()).isTrue();
		assertThat(listener.getFiredCount()).isEqualTo(3);
		assertThat(listener.hasFired()).isTrue();
		assertThat(listener.hasFiredSinceLastCheck()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void processEventsCountsInvocationsEvenWhenAnExceptionIsThrown() {

		CrudRepository<?, ?> mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener<?, ?> listener = spy(new RepositoryAsyncEventListener<>(mockRepository));

		doThrow(new IllegalStateException("TEST")).when(listener).doProcessEvents(any());

		assertThat(listener).isNotNull();
		assertThat(listener.getFiredCount()).isZero();
		assertThat(listener.hasFired()).isFalse();
		assertThat(listener.hasFiredSinceLastCheck()).isFalse();

		try {
			listener.processEvents(Collections.singletonList(mock(AsyncEvent.class)));
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("TEST");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {

			assertThat(listener.getFiredCount()).isOne();
			assertThat(listener.hasFired()).isTrue();
			assertThat(listener.hasFiredSinceLastCheck()).isTrue();
			assertThat(listener.getFiredCount()).isOne();
			assertThat(listener.hasFired()).isTrue();
			assertThat(listener.hasFiredSinceLastCheck()).isFalse();
		}
	}

	@Test
	public void constructAsyncEventError() {

		AsyncEvent<?, ?> mockEvent = mock(AsyncEvent.class);

		Throwable cause = new RuntimeException("test");

		AsyncEventError eventError = new AsyncEventError(mockEvent, cause);

		assertThat(eventError).isNotNull();
		assertThat(eventError.getCause()).isEqualTo(cause);
		assertThat(eventError.getEvent()).isEqualTo(mockEvent);

		assertThat(eventError.toString())
			.isEqualTo("Error [test] thrown when processing AsyncEvent [%s]", mockEvent);

		verifyNoInteractions(mockEvent);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructAsyncEventErrorWithNullCauseThrowsIllegalArgumentException() {

		try {
			new AsyncEventError(mock(AsyncEvent.class), null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Cause must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructAsyncEventErrorWithNullEventThrowsIllegalArgumentException() {

		try {
			new AsyncEventError(null, new RuntimeException("test"));
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("AsyncEvent must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void abstractAsyncEventOperationRepositoryFunctionApplyWhenFunctionCanProcessEvent() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		Object entity = "test";

		doReturn(true).when(repositoryFunction).canProcess(eq(mockEvent));
		doReturn(entity).when(repositoryFunction).resolveEntity(eq(mockEvent));
		doCallRealMethod().when(repositoryFunction).apply(any());

		assertThat(repositoryFunction.apply(mockEvent)).isTrue();

		InOrder order = inOrder(repositoryFunction);

		order.verify(repositoryFunction, times(1)).apply(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).canProcess(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).resolveEntity(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).doRepositoryOp(eq(entity));

		verifyNoMoreInteractions(repositoryFunction);
		verifyNoInteractions(mockEvent);
	}

	@Test
	public void abstractAsyncEventOperationRepositoryFunctionApplyWhenFunctionCannotProcessEvent() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doReturn(false).when(repositoryFunction).canProcess(any());
		doCallRealMethod().when(repositoryFunction).apply(any());

		assertThat(repositoryFunction.apply(mockEvent)).isFalse();

		verify(repositoryFunction, times(1)).apply(eq(mockEvent));
		verify(repositoryFunction, times(1)).canProcess(eq(mockEvent));

		verifyNoMoreInteractions(repositoryFunction);
		verifyNoInteractions(mockEvent);
	}

	@Test
	public void abstractAsyncEventOperationRepositoryFunctionApplyWhenRepositoryOperationThrowsException() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		AsyncEventErrorHandler mockEventErrorHandler = mock(AsyncEventErrorHandler.class);

		Object entity = "mock";

		doCallRealMethod().when(repositoryFunction).apply(any());
		doReturn(true).when(repositoryFunction).canProcess(eq(mockEvent));
		doReturn(entity).when(repositoryFunction).resolveEntity(eq(mockEvent));
		doThrow(new QueryTimeoutException("TEST")).when(repositoryFunction).doRepositoryOp(eq(entity));
		doReturn(mockEventErrorHandler).when(repositoryFunction).getErrorHandler();

		doAnswer(invocation -> {

			AsyncEventError eventError = invocation.getArgument(0);

			assertThat(eventError).isNotNull();
			assertThat(eventError.getCause()).isInstanceOf(QueryTimeoutException.class);
			assertThat(eventError.getCause().getMessage()).isEqualTo("TEST");
			assertThat(eventError.getCause()).hasNoCause();
			assertThat(eventError.getEvent()).isEqualTo(mockEvent);

			return false;

		}).when(mockEventErrorHandler).apply(any());

		assertThat(repositoryFunction.apply(mockEvent)).isFalse();

		InOrder order = inOrder(mockEventErrorHandler, repositoryFunction);

		order.verify(repositoryFunction, times(1)).apply(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).canProcess(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).resolveEntity(eq(mockEvent));
		order.verify(repositoryFunction, times(1)).doRepositoryOp(eq(entity));
		order.verify(repositoryFunction, times(1)).getErrorHandler();
		order.verify(mockEventErrorHandler, times(1)).apply(isA(AsyncEventError.class));

		verifyNoMoreInteractions(repositoryFunction, mockEventErrorHandler);
		verifyNoInteractions(mockEvent);
	}

	@Test
	public void asyncEventOperationRepositoryFunctionGetErrorHandlerCallsRepositoryAsyncEventListenerGetAsyncEventErrorHandler() {

		AsyncEventErrorHandler mockErrorHandler = mock(AsyncEventErrorHandler.class);

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener(mockRepository);

		listener.setAsyncEventErrorHandler(mockErrorHandler);

		listener = spy(listener);

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			new TestAsyncEventOperationRepositoryFunction(listener);

		assertThat(repositoryFunction.getListener()).isSameAs(listener);
		assertThat(repositoryFunction.getErrorHandler()).isEqualTo(mockErrorHandler);

		verify(listener, times(1)).getAsyncEventErrorHandler();
		verifyNoMoreInteractions(listener);
		verifyNoInteractions(mockRepository);
	}

	@Test
	public void asyncEventOperationRepositoryFunctionGetRepositoryCallsRepositoryAsyncEventListenerGetRepository() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = spy(new RepositoryAsyncEventListener(mockRepository));

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			new TestAsyncEventOperationRepositoryFunction(listener);

		assertThat(repositoryFunction.getListener()).isSameAs(listener);
		assertThat(repositoryFunction.getRepository()).isEqualTo(mockRepository);

		verify(listener, times(1)).getRepository();
		verifyNoMoreInteractions(listener);
		verifyNoInteractions(mockRepository);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructAsyncEventOperationRepositoryFunctionWithNullRepositoryThrowsIllegalArgumentException() {

		try {
			new TestAsyncEventOperationRepositoryFunction(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("RepositoryAsyncEventListener must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void resolveEntityIsSuccessful() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).resolveEntity(any());
		doReturn("mock").when(mockEvent).getDeserializedValue();

		assertThat(repositoryFunction.resolveEntity(mockEvent)).isEqualTo("mock");

		verify(mockEvent, times(1)).getDeserializedValue();
		verifyNoMoreInteractions(mockEvent);
	}

	@Test(expected = IllegalArgumentException.class)
	public void resolveEntityFromNullEventThrowsIllegalArgumentException() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		doCallRealMethod().when(repositoryFunction).resolveEntity(any());

		try {
			repositoryFunction.resolveEntity(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("AsyncEvent must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void resolveNulEntityThrowsIllegalStateException() {

		AbstractAsyncEventOperationRepositoryFunction repositoryFunction =
			mock(AbstractAsyncEventOperationRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).resolveEntity(any());
		doReturn(null).when(mockEvent).getDeserializedValue();

		try {
			repositoryFunction.resolveEntity(mockEvent);
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("The entity (deserialized value) was null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockEvent, times(1)).getDeserializedValue();
		}
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionCanProcessCreateEventReturnsTrue() {

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			mock(CreateUpdateAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(Operation.CREATE).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isTrue();

		verify(mockEvent, times(1)).getOperation();
		verifyNoMoreInteractions(mockEvent);
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionCanProcessUpdateEventReturnsTrue() {

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			mock(CreateUpdateAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(Operation.UPDATE).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isTrue();

		verify(mockEvent, times(1)).getOperation();
		verifyNoMoreInteractions(mockEvent);
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionCanProcessEventWithNullOperationReturnsFalse() {

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			mock(CreateUpdateAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(null).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isFalse();

		verify(mockEvent, times(1)).getOperation();
		verifyNoMoreInteractions(mockEvent);
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionCanProcessNullEventReturnsFalse() {

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			mock(CreateUpdateAsyncEventRepositoryFunction.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		assertThat(repositoryFunction.canProcess(null)).isFalse();
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionCanProcessRemoveEventReturnsFalse() {

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			mock(CreateUpdateAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(Operation.REMOVE).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isFalse();

		verify(mockEvent, times(1)).getOperation();
		verifyNoMoreInteractions(mockEvent);
	}

	@Test
	public void createUpdateAsyncEventRepositoryFunctionDoRepositoryOpCallsCrudRepositorySave() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener(mockRepository);

		CreateUpdateAsyncEventRepositoryFunction repositoryFunction =
			new CreateUpdateAsyncEventRepositoryFunction(listener);

		doReturn("TEST").when(mockRepository).save(any());

		assertThat(repositoryFunction.getListener()).isEqualTo(listener);
		assertThat(repositoryFunction.getRepository()).isEqualTo(mockRepository);
		assertThat(repositoryFunction.doRepositoryOp("MOCK")).isEqualTo("TEST");

		verify(mockRepository, times(1)).save(eq("MOCK"));
		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	public void removeAsyncEventRepositoryFunctionCanProcessRemoveEventReturnsTrue() {

		RemoveAsyncEventRepositoryFunction repositoryFunction = mock(RemoveAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(Operation.REMOVE).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isTrue();

		verify(mockEvent, times(1)).getOperation();
	}

	@Test
	public void removeAsyncEventRepositoryFunctionCanProcessCreateEventReturnsFalse() {

		RemoveAsyncEventRepositoryFunction repositoryFunction = mock(RemoveAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(Operation.CREATE).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isFalse();

		verify(mockEvent, times(1)).getOperation();
	}

	@Test
	public void removeAsyncEventRepositoryFunctionCanProcessEventWithNullOperationReturnsFalse() {

		RemoveAsyncEventRepositoryFunction repositoryFunction = mock(RemoveAsyncEventRepositoryFunction.class);

		AsyncEvent mockEvent = mock(AsyncEvent.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());
		doReturn(null).when(mockEvent).getOperation();

		assertThat(repositoryFunction.canProcess(mockEvent)).isFalse();

		verify(mockEvent, times(1)).getOperation();
	}

	@Test
	public void removeAsyncEventRepositoryFunctionCanProcessNullEventReturnsFalse() {

		RemoveAsyncEventRepositoryFunction repositoryFunction = mock(RemoveAsyncEventRepositoryFunction.class);

		doCallRealMethod().when(repositoryFunction).canProcess(any());

		assertThat(repositoryFunction.canProcess(null)).isFalse();
	}

	@Test
	public void removeAsyncEventRepositoryFunctionDoRepositoryOpCallsCrudRepositoryDelete() {

		CrudRepository mockRepository = mock(CrudRepository.class);

		RepositoryAsyncEventListener listener = new RepositoryAsyncEventListener(mockRepository);

		RemoveAsyncEventRepositoryFunction repositoryFunction = new RemoveAsyncEventRepositoryFunction(listener);

		assertThat(repositoryFunction.getListener()).isEqualTo(listener);
		assertThat(repositoryFunction.getRepository()).isEqualTo(mockRepository);
		assertThat(repositoryFunction.doRepositoryOp("MOCK")).isNull();

		verify(mockRepository, times(1)).delete(eq("MOCK"));
		verifyNoMoreInteractions(mockRepository);
	}

	private static final class TestAsyncEventOperationRepositoryFunction<T, ID>
			extends AbstractAsyncEventOperationRepositoryFunction<T, ID> {


		private TestAsyncEventOperationRepositoryFunction(RepositoryAsyncEventListener<T, ID> listener) {
			super(listener);
		}

		@Override
		protected <R> R doRepositoryOp(@NonNull T entity) {
			throw new UnsupportedOperationException("Not Implemented");
		}
	}
}
