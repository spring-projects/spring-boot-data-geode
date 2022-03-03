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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.geode.cache.Operation;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;

import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * An Apache Geode {@link AsyncEventListener} that uses a Spring Data {@link CrudRepository} to perform
 * data access operations to a backend, external data source asynchronously, triggered by cache operations.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see org.apache.geode.cache.Operation
 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
 * @see org.apache.geode.cache.asyncqueue.AsyncEventListener
 * @see org.springframework.data.repository.CrudRepository
 * @since 1.4.0
 */
public class RepositoryAsyncEventListener<T, ID> implements AsyncEventListener {

	protected static final AsyncEventErrorHandler DEFAULT_ASYNC_EVENT_ERROR_HANDLER = eventError -> false;

	private AsyncEventErrorHandler asyncEventErrorHandler = DEFAULT_ASYNC_EVENT_ERROR_HANDLER;

	private final AtomicBoolean hasFired = new AtomicBoolean(false);

	private final AtomicLong firedCount = new AtomicLong(0L);

	private final CrudRepository<T, ID> repository;

	private final List<AsyncEventOperationRepositoryFunction<T, ID>> repositoryFunctions = new CopyOnWriteArrayList<>();

	/**
	 * Constructs a new instance of {@link RepositoryAsyncEventListener} initialized with the given Spring Data
	 * {@link CrudRepository}.
	 *
	 * @param repository Spring Data {@link CrudRepository} used to perform data access operations to a backend,
	 * external data source when triggered by a cache operation; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link CrudRepository} is {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 */
	public RepositoryAsyncEventListener(@NonNull CrudRepository<T, ID> repository) {

		Assert.notNull(repository, "CrudRepository must not be null");

		this.repository = repository;

		this.repositoryFunctions.addAll(Arrays.asList(
			new CreateUpdateAsyncEventRepositoryFunction<>(this),
			new RemoveAsyncEventRepositoryFunction<>(this)
		));
	}

	/**
	 * Determines whether this listener has (ever) been fired (triggered) by the GemFire/Geode AEQ system.
	 *
	 * @return a boolean value indicating whether this listener has been fired (triggered).
	 * @see #hasFiredSinceLastCheck()
	 */
	@SuppressWarnings("unused")
	public boolean hasFired() {
		return getFiredCount() > 0;
	}

	/**
	 * Determines whether this listener has been fired (triggered) by the GemFire/Geode AEQ system
	 * since the last check.
	 *
	 * A call to this method clears the flag.
	 *
	 * @return a boolean value indicating whether this listener has been fired (triggered) since the last check.
	 * @see #hasFired()
	 */
	@SuppressWarnings("unused")
	public boolean hasFiredSinceLastCheck() {
		return this.hasFired.compareAndSet(true, false);
	}

	/**
	 * Determines how many times this listener has been fired (triggered) by the GemFire/Geode AEQ system.
	 *
	 * @return a {@link Long} value indicating how many times this listener has been fired (triggered).
	 */
	@SuppressWarnings("unused")
	public long getFiredCount() {
		return this.firedCount.get();
	}

	/**
	 * Configures an {@link AsyncEventErrorHandler} to handle errors that may occur when this listener is invoked with
	 * a batch of {@link AsyncEvent AsyncEvents}.
	 *
	 * Since the processing of {@link AsyncEvent AsyncEvents} is asynchronous, the {@link AsyncEventErrorHandler} gives
	 * users the opportunity to respond to errors for each {@link AsyncEvent} as it is is processed given this listener
	 * is designed to coordinate data/state changes occurring in an Apache Geode cache with an external data source.
	 *
	 * @param asyncEventErrorHandler {@link AsyncEventErrorHandler} used to handle errors while processing the batch of
	 * {@link AsyncEvent AsyncEvents}.
	 * @see AsyncEventErrorHandler
	 */
	public void setAsyncEventErrorHandler(@Nullable AsyncEventErrorHandler asyncEventErrorHandler) {
		this.asyncEventErrorHandler = asyncEventErrorHandler;
	}

	/**
	 * Gets the configured {@link AsyncEventErrorHandler} used to handle errors that may occur when this listener
	 * is invoked with a batch of {@link AsyncEvent AsyncEvents}.
	 *
	 * Defaults to an {@link AsyncEventErrorHandler} that always returns {@literal false} on any error.
	 *
	 * @return the configured {@link AsyncEventErrorHandler}; never {@literal null}.
	 * @see AsyncEventErrorHandler
	 */
	protected @NonNull AsyncEventErrorHandler getAsyncEventErrorHandler() {
		return this.asyncEventErrorHandler != null ? this.asyncEventErrorHandler : DEFAULT_ASYNC_EVENT_ERROR_HANDLER;
	}

	/**
	 * Gets a reference to the configured Spring Data {@link CrudRepository} used by this {@link AsyncEventListener}
	 * to perform data access operations to a external, backend data source asynchronously when triggered by a cache
	 * operation.
	 *
	 * @return a reference to the configured Spring Data {@link CrudRepository}; never {@literal null}.
	 * @see org.springframework.data.repository.CrudRepository
	 */
	protected @NonNull CrudRepository<T, ID> getRepository() {
		return this.repository;
	}

	/**
	 * Gets a {@link List} of {@link AsyncEventOperationRepositoryFunction} objects used to process
	 * {@link AsyncEvent AsyncEvents} passed to this listener by inspecting the {@link Operation}
	 * on the {@link AsyncEvent} and calling the appropriate {@link CrudRepository} method.
	 *
	 * @return a {@link List} of {@link AsyncEventOperationRepositoryFunction} objects to process
	 * the {@link AsyncEvent AsyncEvents}; never {@literal null}.
	 * @see AsyncEventOperationRepositoryFunction
	 */
	protected @NonNull List<AsyncEventOperationRepositoryFunction<T, ID>> getRepositoryFunctions() {
		return this.repositoryFunctions;
	}

	/**
	 * Processes each {@link AsyncEvent} in order by first determining whether the {@link AsyncEvent} can be processed
	 * by this listener and then invokes the appropriate Spring Data {@link CrudRepository} data access operation
	 * corresponding to the {@link AsyncEvent} {@link Operation}.
	 *
	 * @param events {@link List} of {@link AsyncEvent AsyncEvents} to process.
	 * @return a boolean value indicating whether all {@link AsyncEvent AsyncEvents} were processed successfully
	 * by this listener.
	 * If any {@link AsyncEvent} fails to be processed (just one), then this method will return {@literal false}.
	 * If any {@link AsyncEvent} cannot be handled, then this method will return {@literal false}, even if other
	 * {@link AsyncEvent AsyncEvents} were successfully processed.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
	 * @see AsyncEventOperationRepositoryFunction
	 * @see #getRepositoryFunctions()
	 * @see java.util.List
	 */
	@Override
	public final boolean processEvents(List<AsyncEvent> events) {

		try {
			return doProcessEvents(events);
		}
		finally {
			this.firedCount.incrementAndGet();
			this.hasFired.set(true);
		}
	}

	/**
	 * @see #processEvents(List)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected boolean doProcessEvents(List<AsyncEvent> events) {

		AtomicBoolean result = new AtomicBoolean(true);

		CollectionUtils.nullSafeList(events).stream()
			.filter(Objects::nonNull)
			.forEach(event -> {

				Optional<AsyncEventOperationRepositoryFunction<T, ID>> repositoryFunction =
					getRepositoryFunctions().stream()
						.filter(function -> function.canProcess(event))
						.findFirst();

				boolean processed = Boolean.TRUE.equals(repositoryFunction
					.map(function -> function.apply(event))
					.orElse(false));

				result.compareAndSet(true, processed);
			});

		return result.get();
	}

	/**
	 * Registers a {@link AsyncEventOperationRepositoryFunction} capable of processing {@link AsyncEvent AsyncEvents}
	 * by {@link Operation} and invoking the appropriate Spring Data {@link CrudRepository} data access operation.
	 *
	 * {@link AsyncEventOperationRepositoryFunction AsyncEventOperationRepositoryFunctions} can be registered for
	 * {@link AsyncEvent} {@link Operation Operations} not currently handled by this listener. Alternatively, users can
	 * override existing {@link AsyncEventOperationRepositoryFunction AsyncEventOperationRepositoryFunctions} provided
	 * by this listener to alter the default behavior, or effectively the Spring Data {@link CrudRepository} data access
	 * operation invoked based on the {@link AsyncEvent} {@link Operation}. The {@code repositoryFunction} arguments are
	 * prepended to the {@link List} of registered {@link Function Functions} to implement the override, where the first
	 * {@link Function} found capable of handling the {@link AsyncEvent} {@link Operation} will be applied.
	 *
	 * @param repositoryFunction {@link AsyncEventOperationRepositoryFunction} used to process
	 * {@link AsyncEvent AsyncEvents} by {@link Operation} invoking the appropriate Spring Data {@link CrudRepository}
	 * data access operation; must not be {@literal null}.
	 * @return a boolean value indicating whether the registration was successful.
	 * @see AsyncEventOperationRepositoryFunction
	 * @see #getRepositoryFunctions()
	 */
	public boolean register(@NonNull AsyncEventOperationRepositoryFunction<T, ID> repositoryFunction) {

		if (repositoryFunction != null) {
			getRepositoryFunctions().add(0, repositoryFunction);
			return true;
		}

		return false;
	}

	/**
	 * Unregisters the given {@link AsyncEventOperationRepositoryFunction} from this listener.
	 *
	 * @param repositoryFunction {@link AsyncEventOperationRepositoryFunction} to unregister.
	 * @return a boolean value indicating whether the un-registration was successful.
	 * @see AsyncEventOperationRepositoryFunction
	 * @see #getRepositoryFunctions()
	 */
	public boolean unregister(@Nullable AsyncEventOperationRepositoryFunction<T, ID> repositoryFunction) {
		return getRepositoryFunctions().remove(repositoryFunction);
	}

	/**
	 * {@link AsyncEventError} is a wrapper class encapsulating the {@link AsyncEvent} along with
	 * the {@link Throwable error} that was thrown while processing the event.
	 *
	 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
	 * @see java.lang.Throwable
	 */
	public static class AsyncEventError {

		private final AsyncEvent<?, ?> event;

		private final Throwable cause;

		/**
		 * Constructs a new instance of {@link AsyncEventError} initialized with the required {@link AsyncEvent}
		 * and {@link Throwable} thrown while processing the event.
		 *
		 * @param event processed {@link AsyncEvent}; must not be {@literal null}.
		 * @param cause {@link Throwable error} thrown while processing the event; must not be {@literal null}.
		 * @throws IllegalArgumentException if the {@link AsyncEvent} or the {@link Throwable} are {@literal null}.
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
		 * @see java.lang.Throwable
		 */
		public AsyncEventError(@NonNull AsyncEvent<?, ?> event, @NonNull Throwable cause) {

			Assert.notNull(event, "AsyncEvent must not be null");
			Assert.notNull(cause, "Cause must not be null");

			this.event = event;
			this.cause = cause;
		}

		/**
		 * Gets the {@link Throwable} thrown while processing the {@link AsyncEvent}.
		 *
		 * @return the {@link Throwable} thrown while processing the {@link AsyncEvent}.
		 * @see java.lang.Throwable
		 */
		public @NonNull Throwable getCause() {
			return this.cause;
		}

		/**
		 * Gets the {@link AsyncEvent} being processed when the {@link Throwable error} occurred.
		 *
		 * @return the {@link AsyncEvent} being processed when the {@link Throwable error} occurred.
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
		 */
		public @NonNull AsyncEvent<?, ?> getEvent() {
			return this.event;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public String toString() {
			return String.format("Error [%s] thrown when processing AsyncEvent [%s]",
				getCause().getMessage(), getEvent());
		}
	}

	/**
	 * The {@link AsyncEventErrorHandler} interface is a {@link Function} and {@link FunctionalInterface} used to
	 * handle errors while processing {@link AsyncEvent AsyncEvents}.
	 *
	 * @see java.lang.FunctionalInterface
	 * @see java.util.function.Function
	 * @see AsyncEventError
	 */
	@FunctionalInterface
	public interface AsyncEventErrorHandler extends Function<AsyncEventError, Boolean> { }

	/**
	 * The {@link AsyncEventOperationRepositoryFunction} interface is a {@link Function} and {@link FunctionalInterface}
	 * that translates the {@link AsyncEvent} {@link Operation} into a Spring Data {@link CrudRepository} method
	 * invocation.
	 *
	 * @param <T> {@link Class type} of the entity tied to the event.
	 * @param <ID> {@link Class type} of the identifier of the entity.
	 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
	 * @see java.lang.FunctionalInterface
	 * @see java.util.function.Function
	 */
	@FunctionalInterface
	public interface AsyncEventOperationRepositoryFunction<T, ID> extends Function<AsyncEvent<ID, T>, Boolean> {

		/**
		 * Determines whether the given {@link AsyncEvent} can be processed by this {@link Function}.
		 *
		 * Implementing classes must override this method to specify which {@link AsyncEvent}
		 * {@link Operation Operations} they are capable of processing.
		 *
		 * @param event {@link AsyncEvent} to evaluate.
		 * @return a boolean value indicating whether this {@link Function} is capable of processing
		 * the given {@link AsyncEvent}. Default returns {@literal false}.
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
		 */
		default boolean canProcess(@Nullable AsyncEvent<ID, T> event) {
			return false;
		}
	}

	/**
	 * {@link AbstractAsyncEventOperationRepositoryFunction} is an abstract base class implementing the
	 * {@link AsyncEventOperationRepositoryFunction} interface to provided a default {@literal template} implementation
	 * of the {@link Function#apply(Object)} method.
	 *
	 * @param <T> {@link Class type} of the entity tied to the event.
	 * @param <ID> {@link Class type} of the identifier of the entity.
	 * @see AsyncEventOperationRepositoryFunction
	 */
	public static abstract class AbstractAsyncEventOperationRepositoryFunction<T, ID>
			implements AsyncEventOperationRepositoryFunction<T, ID> {

		private final RepositoryAsyncEventListener<T, ID> listener;

		/**
		 * Constructs an new instance of {@link AbstractAsyncEventOperationRepositoryFunction} initialized with
		 * the given, required {@link RepositoryAsyncEventListener} to which this function is associated.
		 *
		 * @param listener {@link RepositoryAsyncEventListener} processing {@link AsyncEvent AsyncEvents}
		 * by invoking this {@link Function} to handle them.
		 * @throws IllegalArgumentException if {@link RepositoryAsyncEventListener} is {@literal null}.
		 * @see RepositoryAsyncEventListener
		 */
		public AbstractAsyncEventOperationRepositoryFunction(@NonNull RepositoryAsyncEventListener<T, ID> listener) {

			Assert.notNull(listener, "RepositoryAsyncEventListener must not be null");

			this.listener = listener;
		}

		/**
		 * Alias to the {@link RepositoryAsyncEventListener#getAsyncEventErrorHandler() configured}
		 * {@link RepositoryAsyncEventListener} {@link AsyncEventErrorHandler}.
		 *
		 * @return the configured {@link AsyncEventErrorHandler}; never {@literal null}.
		 * @see RepositoryAsyncEventListener#getAsyncEventErrorHandler()
		 * @see AsyncEventErrorHandler
		 * @see #getListener()
		 */
		protected AsyncEventErrorHandler getErrorHandler() {
			return getListener().getAsyncEventErrorHandler();
		}

		/**
		 * Returns a reference to the associated {@link RepositoryAsyncEventListener}.
		 *
		 * @return a reference to the associated {@link RepositoryAsyncEventListener}; never {@literal null}.
		 * @see RepositoryAsyncEventListener
		 */
		protected @NonNull RepositoryAsyncEventListener<T, ID> getListener() {
			return this.listener;
		}

		/**
		 * Alias to the {@link RepositoryAsyncEventListener#getRepository() configured}
		 * {@link RepositoryAsyncEventListener} {@link CrudRepository}.
		 *
		 * @return the configured {@link CrudRepository}; never {@literal null}.
		 * @see org.springframework.data.repository.CrudRepository
		 * @see RepositoryAsyncEventListener#getRepository()
		 * @see #getListener()
		 */
		protected @NonNull CrudRepository<T, ID> getRepository() {
			return getListener().getRepository();
		}

		/**
		 * Processes the given {@link AsyncEvent} by first determining whether the event can be processed by this
		 * {@link Function}, and then proceeds to extract the {@link AsyncEvent#getDeserializedValue() entity}
		 * associated with the event to invoke the appropriate Spring Data {@link CrudRepository} data access operation
		 * determined by the {@link AsyncEvent} {@link Operation}.
		 *
		 * If an {@link Throwable error} is thrown while processing the {@link AsyncEvent}, then the
		 * {@link AsyncEventErrorHandler} is called to handle the error and perform any necessary/required
		 * post-processing actions.
		 *
		 * {@link AsyncEventErrorHandler} can be implemented to retry the operation with incremental backoff, based on
		 * count or time, record the failure, perform resource cleanup actions, whatever is necessary and appropriate
		 * to the application use case.
		 *
		 * @param event {@link AsyncEvent} to process.
		 * @return a boolean value indicating whether the event was successfully processed.
		 * @throws IllegalStateException if the resolve entity is {@literal null}.
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
		 * @see AsyncEventErrorHandler
		 * @see #canProcess(AsyncEvent)
		 * @see #doRepositoryOp(Object)
		 * @see #resolveEntity(AsyncEvent)
		 * @see #getErrorHandler()
		 */
		@Override
		public Boolean apply(@Nullable AsyncEvent<ID, T> event) {

			try {
				if (canProcess(event)) {

					T entity = resolveEntity(event);

					doRepositoryOp(entity);

					return true;
				}

				return false;
			}
			catch (Throwable cause) {
				return getErrorHandler().apply(new AsyncEventError(event, cause));
			}
		}

		/**
		 * Invokes the appropriate Spring Data {@link CrudRepository} data access operation based on the
		 * {@link AsyncEvent} {@link Operation} as determined by {@link AsyncEvent#getOperation()}.
		 *
		 * @param <R> {@link Class type} of the Spring Data {@link CrudRepository} data access operation return value.
		 * @param entity entity to process.
		 * @return the result of invoking the Spring Data {@link CrudRepository} data access operation.
		 * @see org.springframework.data.repository.CrudRepository
		 */
		protected abstract <R> R doRepositoryOp(@NonNull T entity);

		/**
		 * Resolves the {@link AsyncEvent#getDeserializedValue() entity} associated with the {@link AsyncEvent}.
		 *
		 * @param event {@link AsyncEvent} from which to resolve the entity.
		 * @return the resolve entity from the {@link AsyncEvent}.
		 * @throws IllegalArgumentException if {@link AsyncEvent} is {@literal null}.
		 * @throws IllegalStateException if the resolved {@link AsyncEvent#getDeserializedValue() entity}
		 * is {@literal null}.
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent#getDeserializedValue()
		 * @see org.apache.geode.cache.asyncqueue.AsyncEvent
		 */
		protected T resolveEntity(@NonNull AsyncEvent<ID, T> event) {

			Assert.notNull(event, "AsyncEvent must not be null");

			T entity = event.getDeserializedValue();

			Assert.state(entity != null, "The entity (deserialized value) was null");

			return entity;
		}
	}

	/**
	 * An {@link AsyncEventOperationRepositoryFunction} capable of handling {@link Operation#CREATE}
	 * and {@link Operation#UPDATE} {@link AsyncEvent AsyncEvents}.
	 *
	 * Invokes the {@link CrudRepository#save(Object)} data access operation.
	 *
	 * @param <T> {@link Class type} of the entity tied to the event.
	 * @param <ID> {@link Class type} of the identifier of the entity.
	 */
	public static class CreateUpdateAsyncEventRepositoryFunction<T, ID>
			extends AbstractAsyncEventOperationRepositoryFunction<T, ID> {

		/**
		 * Constructs a new instance of {@link CreateUpdateAsyncEventRepositoryFunction} initialized with the given,
		 * required {@link RepositoryAsyncEventListener}.
		 *
		 * @param listener {@link RepositoryAsyncEventListener} forwarding {@link AsyncEvent AsyncEvents} for processing
		 * by this {@link Function}
		 * @see RepositoryAsyncEventListener
		 */
		public CreateUpdateAsyncEventRepositoryFunction(@NonNull RepositoryAsyncEventListener<T, ID> listener) {
			super(listener);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean canProcess(@Nullable AsyncEvent<ID, T> event) {

			Operation operation = event != null ? event.getOperation() : null;

			return operation != null && (operation.isCreate() || operation.isUpdate());
		}

		/**
		 * @inheritDoc
		 */
		@Override
		@SuppressWarnings("unchecked")
		protected <R> R doRepositoryOp(T entity) {
			return (R) getRepository().save(entity);
		}
	}

	/**
	 * An {@link Function} implementation capable of handling {@link Operation#REMOVE} {@link AsyncEvent AsyncEvents}.
	 *
	 * Invokes the {@link CrudRepository#delete(Object)} data access operation.
	 *
	 * @param <T> {@link Class type} of the entity tied to the event.
	 * @param <ID> {@link Class type} of the identifier of the entity.
	 */
	public static class RemoveAsyncEventRepositoryFunction<T, ID>
			extends AbstractAsyncEventOperationRepositoryFunction<T, ID> {

		/**
		 * Constructs a new instance of {@link RemoveAsyncEventRepositoryFunction} initialized with the given, required
		 * {@link RepositoryAsyncEventListener}.
		 *
		 * @param listener {@link RepositoryAsyncEventListener} forwarding {@link AsyncEvent AsyncEvents} for processing
		 * by this {@link Function}
		 * @see RepositoryAsyncEventListener
		 */
		public RemoveAsyncEventRepositoryFunction(@NonNull RepositoryAsyncEventListener<T, ID> listener) {
			super(listener);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean canProcess(@Nullable AsyncEvent<ID, T> event) {

			Operation operation = event != null ? event.getOperation() : null;

			return Operation.REMOVE.equals(operation);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		protected <R> R doRepositoryOp(T entity) {
			getRepository().delete(entity);
			return null;
		}
	}
}
