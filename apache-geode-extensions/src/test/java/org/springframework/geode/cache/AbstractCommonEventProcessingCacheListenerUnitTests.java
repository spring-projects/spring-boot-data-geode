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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.RegionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit Tests for {@link AbstractCommonEventProcessingCacheListener}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.CacheListener
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractCommonEventProcessingCacheListenerUnitTests {

	@Mock
	private EntryEvent<Object, Object> mockEntryEvent;

	@Mock
	private RegionEvent<Object, Object> mockRegionEvent;

	@Spy
	private AbstractCommonEventProcessingCacheListener<Object, Object> cacheListener;

	@Test
	public void afterCreateCallsProcessEntryEventWithCreate() {

		this.cacheListener.afterCreate(this.mockEntryEvent);

		verify(this.cacheListener, times(1)).afterCreate(eq(this.mockEntryEvent));

		verify(this.cacheListener, times(1)).processEntryEvent(eq(this.mockEntryEvent),
			eq(AbstractCommonEventProcessingCacheListener.EntryEventType.CREATE));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterDestroyCallsProcessEntryEventWithDestroy() {

		this.cacheListener.afterDestroy(this.mockEntryEvent);

		verify(this.cacheListener, times(1)).afterDestroy(eq(this.mockEntryEvent));

		verify(this.cacheListener, times(1)).processEntryEvent(eq(this.mockEntryEvent),
			eq(AbstractCommonEventProcessingCacheListener.EntryEventType.DESTROY));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterInvalidateCallsProcessEntryEventWithInvalidate() {

		this.cacheListener.afterInvalidate(this.mockEntryEvent);

		verify(this.cacheListener, times(1)).afterInvalidate(eq(this.mockEntryEvent));

		verify(this.cacheListener, times(1)).processEntryEvent(eq(this.mockEntryEvent),
			eq(AbstractCommonEventProcessingCacheListener.EntryEventType.INVALIDATE));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterUpdateCallsProcessEntryEventWithUpdate() {

		this.cacheListener.afterUpdate(this.mockEntryEvent);

		verify(this.cacheListener, times(1)).afterUpdate(eq(this.mockEntryEvent));

		verify(this.cacheListener, times(1)).processEntryEvent(eq(this.mockEntryEvent),
			eq(AbstractCommonEventProcessingCacheListener.EntryEventType.UPDATE));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterRegionClearCallsProcessRegionEventWithClear() {

		this.cacheListener.afterRegionClear(this.mockRegionEvent);

		verify(this.cacheListener, times(1)).afterRegionClear(eq(this.mockRegionEvent));

		verify(this.cacheListener, times(1)).processRegionEvent(eq(this.mockRegionEvent),
			eq(AbstractCommonEventProcessingCacheListener.RegionEventType.CLEAR));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterRegionCreateCallsProcessRegionEventWithCreate() {

		this.cacheListener.afterRegionCreate(this.mockRegionEvent);

		verify(this.cacheListener, times(1)).afterRegionCreate(eq(this.mockRegionEvent));

		verify(this.cacheListener, times(1)).processRegionEvent(eq(this.mockRegionEvent),
			eq(AbstractCommonEventProcessingCacheListener.RegionEventType.CREATE));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterRegionDestroyCallsProcessRegionEventWithCreate() {

		this.cacheListener.afterRegionDestroy(this.mockRegionEvent);

		verify(this.cacheListener, times(1)).afterRegionDestroy(eq(this.mockRegionEvent));

		verify(this.cacheListener, times(1)).processRegionEvent(eq(this.mockRegionEvent),
			eq(AbstractCommonEventProcessingCacheListener.RegionEventType.DESTROY));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterRegionInvalidateCallsProcessRegionEventWithInvalidate() {

		this.cacheListener.afterRegionInvalidate(this.mockRegionEvent);

		verify(this.cacheListener, times(1)).afterRegionInvalidate(eq(this.mockRegionEvent));

		verify(this.cacheListener, times(1)).processRegionEvent(eq(this.mockRegionEvent),
			eq(AbstractCommonEventProcessingCacheListener.RegionEventType.INVALIDATE));

		verifyNoMoreInteractions(this.cacheListener);
	}

	@Test
	public void afterRegionLiveCallsProcessRegionEventWithLive() {

		this.cacheListener.afterRegionLive(this.mockRegionEvent);

		verify(this.cacheListener, times(1)).afterRegionLive(eq(this.mockRegionEvent));

		verify(this.cacheListener, times(1)).processRegionEvent(eq(this.mockRegionEvent),
			eq(AbstractCommonEventProcessingCacheListener.RegionEventType.LIVE));

		verifyNoMoreInteractions(this.cacheListener);
	}
}
