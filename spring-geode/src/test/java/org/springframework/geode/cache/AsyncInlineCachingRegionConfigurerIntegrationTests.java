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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

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
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Tests for {@link AsyncInlineCachingRegionConfigurer} and {@link RepositoryAsyncEventListener}.
 *
 * @author John Blum
 * @see org.junit.Test
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
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.4.0
 */
@RunWith(SpringRunner.class)
@SuppressWarnings("unused")
public class AsyncInlineCachingRegionConfigurerIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private Cache peerCache;

	@Autowired
	@Qualifier("usersRepository")
	private CrudRepository<User, Long> usersRepository;

	@Resource(name = "Users")
	private Region<Long, User> users;

	@Before
	@SuppressWarnings("unchecked")
	public void forwardRegionDataAccessOperationsToAsyncEventQueueListener() {

		assertThat(this.peerCache).isNotNull();
		assertThat(this.users).isNotNull();
		assertThat(this.users.getName()).isEqualTo("Users");
		assertThat(this.usersRepository).isNotNull();

		RegionAttributes<Long, User> usersAttributes = this.users.getAttributes();

		assertThat(usersAttributes).isNotNull();

		Set<String> asyncEventQueueIds = usersAttributes.getAsyncEventQueueIds();

		assertThat(asyncEventQueueIds).isNotNull();
		assertThat(asyncEventQueueIds).hasSize(1);

		String asyncEventQueueId = asyncEventQueueIds.iterator().next();

		assertThat(asyncEventQueueId).isNotBlank();

		AsyncEventQueue asyncEventQueue = this.peerCache.getAsyncEventQueue(asyncEventQueueId);

		assertThat(asyncEventQueue).isNotNull();
		assertThat(asyncEventQueue.getId()).isEqualTo(asyncEventQueueId);

		AsyncEventListener listener = asyncEventQueue.getAsyncEventListener();

		assertThat(listener).isInstanceOf(RepositoryAsyncEventListener.class);
		assertThat(((RepositoryAsyncEventListener<User, Long>) listener).getRepository())
			.isEqualTo(this.usersRepository);

		Map<Long, User> data = new HashMap<>();

		doAnswer(invocation -> {

			Long key = invocation.getArgument(0);

			User newUser = invocation.getArgument(1);
			User existingUser = data.put(key, newUser);

			AsyncEvent<Long, User> mockEvent = mock(AsyncEvent.class, withSettings().lenient());

			Operation operation = existingUser != null ? Operation.UPDATE : Operation.CREATE;

			doReturn(key).when(mockEvent).getKey();
			doReturn(newUser).when(mockEvent).getDeserializedValue();
			doReturn(operation).when(mockEvent).getOperation();
			doReturn(this.users).when(mockEvent).getRegion();

			listener.processEvents(Collections.singletonList(mockEvent));

			return existingUser;

		}).when(this.users).put(anyLong(), any());

		doAnswer(invocation -> {

			Long key = invocation.getArgument(0);

			User user = data.remove(key);

			AsyncEvent<Long, User> mockEvent = mock(AsyncEvent.class, withSettings().lenient());

			doReturn(key).when(mockEvent).getKey();
			doReturn(user).when(mockEvent).getDeserializedValue();
			doReturn(Operation.REMOVE).when(mockEvent).getOperation();
			doReturn(this.users).when(mockEvent).getRegion();

			listener.processEvents(Collections.singletonList(mockEvent));

			return user;

		}).when(this.users).remove(anyLong());
	}

	@Test
	public void asyncEventQueueEventsProcessedByListener() {

		User jonDoe = User.newUser(1L, "Jon Doe");
		User janeDoe = User.newUser(jonDoe.getId(), "Jane Doe");

		this.users.put(jonDoe.getId(), jonDoe);
		this.users.put(janeDoe.getId(), janeDoe);
		this.users.remove(janeDoe.getId());

		InOrder order = inOrder(this.usersRepository);

		order.verify(this.usersRepository, times(1)).save(eq(jonDoe));
		order.verify(this.usersRepository, times(1)).save(eq(janeDoe));
		order.verify(this.usersRepository, times(1)).delete(eq(janeDoe));

		verifyNoMoreInteractions(this.usersRepository);
	}

	@PeerCacheApplication
	@EnableGemFireMockObjects
	@EnableEntityDefinedRegions(basePackageClasses = User.class)
	static class GeodeConfiguration {

		@Bean
		AsyncInlineCachingRegionConfigurer<User, Long> asyncInlineCachingUsersRegionConfigurer(
				@Qualifier("usersRepository") CrudRepository<User, Long> usersRepository) {

			return AsyncInlineCachingRegionConfigurer.create(usersRepository, "Users");
		}

		@Bean
		@SuppressWarnings("unchecked")
		CrudRepository<User, Long> usersRepository() {
			return mock(CrudRepository.class);
		}
	}

	@Getter
	@EqualsAndHashCode
	@ToString(of = "name")
	@RequiredArgsConstructor(staticName = "newUser")
	@org.springframework.data.gemfire.mapping.annotation.Region("Users")
	static class User {

		@NonNull
		private Long id;

		@NonNull
		private String name;

	}
}
