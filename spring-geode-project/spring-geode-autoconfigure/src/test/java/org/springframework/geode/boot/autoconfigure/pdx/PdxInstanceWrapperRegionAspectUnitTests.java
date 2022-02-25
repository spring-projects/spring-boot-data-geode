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
package org.springframework.geode.boot.autoconfigure.pdx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import org.apache.geode.cache.CacheStatistics;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.geode.boot.autoconfigure.support.PdxInstanceWrapperRegionAspect;
import org.springframework.geode.boot.autoconfigure.support.PdxInstanceWrapperRegionAspect.RegionEntryWrapper;
import org.springframework.geode.pdx.PdxInstanceWrapper;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Unit Tests for {@link PdxInstanceWrapperRegionAspect}
 *
 * @author John Blum
 * @see java.util.Map
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see PdxInstanceWrapperRegionAspect
 * @see PdxInstanceWrapperRegionAspect.RegionEntryWrapper
 * @see org.springframework.geode.pdx.PdxInstanceWrapper
 * @since 1.3.0
 */
public class PdxInstanceWrapperRegionAspectUnitTests {

	private final PdxInstanceWrapperRegionAspect aspect = new PdxInstanceWrapperRegionAspect();

	@Test
	public void regionGetAdviceWrapsPdx() throws Throwable {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		doReturn(mockPdxInstance).when(mockJointPoint).proceed();

		Object pdx = this.aspect.regionGetAdvice(mockJointPoint);

		assertThat(pdx).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) pdx).getDelegate()).isEqualTo(mockPdxInstance);

		verify(mockJointPoint, times(1)).proceed();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void regionGetAdviceReturnsNull() throws Throwable {

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		doReturn(null).when(mockJointPoint).proceed();

		assertThat(this.aspect.regionGetAdvice(mockJointPoint)).isNull();

		verify(mockJointPoint, times(1)).proceed();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void regionGetAllAdviceWrapsPdx() throws Throwable {

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		Map<Object, Object> map = new HashMap<>();

		map.put(1, "TEST");
		map.put(2, mockPdxInstance);

		doReturn(map).when(mockJointPoint).proceed();

		Object result = this.aspect.regionGetAllAdvice(mockJointPoint);

		assertThat(result).isInstanceOf(Map.class);

		Map<Object, Object> mapResult = (Map<Object, Object>) result;

		assertThat(mapResult).hasSize(2);
		assertThat(mapResult.get(1)).isEqualTo("TEST");

		Object value = mapResult.get(2);

		assertThat(value).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) value).getDelegate()).isEqualTo(mockPdxInstance);

		verify(mockJointPoint, times(1)).proceed();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void regionGetEntryAdviceWrapsPdx() throws Throwable {

		Region.Entry<Object, Object> mockRegionEntry = mock(Region.Entry.class);

		ProceedingJoinPoint mockJoinPoint = mock(ProceedingJoinPoint.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		doReturn(mockPdxInstance).when(mockRegionEntry).getValue();
		doReturn(mockRegionEntry).when(mockJoinPoint).proceed();

		Object result = this.aspect.regionGetEntryAdvice(mockJoinPoint);

		assertThat(result).isInstanceOf(Region.Entry.class);
		assertThat(result).isNotSameAs(mockRegionEntry);

		Object regionEntryValue = ((Region.Entry<Object, Object>) result).getValue();

		assertThat(regionEntryValue).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) regionEntryValue).getDelegate()).isEqualTo(mockPdxInstance);

		verify(mockJoinPoint, times(1)).proceed();
		verify(mockRegionEntry, times(1)).getValue();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void regionSelectValueAdviceWrapsPdx() throws Throwable {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		doReturn(mockPdxInstance).when(mockJointPoint).proceed();

		Object pdx = this.aspect.regionSelectValueAdvice(mockJointPoint);

		assertThat(pdx).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) pdx).getDelegate()).isEqualTo(mockPdxInstance);

		verify(mockJointPoint, times(1)).proceed();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	public void regionSelectValueAdviceReturnsNull() throws Throwable {

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		doReturn(null).when(mockJointPoint).proceed();

		assertThat(this.aspect.regionSelectValueAdvice(mockJointPoint)).isNull();

		verify(mockJointPoint, times(1)).proceed();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void regionValuesAdviceWrapsPdx() throws Throwable {

		ProceedingJoinPoint mockJointPoint = mock(ProceedingJoinPoint.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		Collection<Object> regionValues = Arrays.asList("TEST", mockPdxInstance);

		doReturn(regionValues).when(mockJointPoint).proceed();

		Object result = this.aspect.regionValuesAdvice(mockJointPoint);

		assertThat(result).isInstanceOf(Collection.class);
		assertThat(result).isNotSameAs(regionValues);

		Collection<Object> collectionResult = (Collection<Object>) result;

		assertThat(collectionResult).hasSize(2);

		for (Object element : collectionResult) {
			if (element instanceof PdxInstance) {
				assertThat(element).isInstanceOf(PdxInstanceWrapper.class);
				assertThat(((PdxInstanceWrapper) element).getDelegate()).isEqualTo(mockPdxInstance);
			}
			else {
				assertThat(element).isEqualTo("TEST");
			}
		}

		verify(mockJointPoint, times(1)).proceed();
		verifyNoInteractions(mockPdxInstance);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void constructRegionEntryWrapperWithRegionEntry() {

		Region.Entry<Object, Object> mockRegionEntry = mock(Region.Entry.class);

		TestRegionEntryWrapper<Object, Object> wrapper = new TestRegionEntryWrapper<>(mockRegionEntry);

		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getDelegate()).isEqualTo(mockRegionEntry);

		verifyNoInteractions(mockRegionEntry);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructMockRegionEntryWrapperWithNull() {

		try {
			new TestRegionEntryWrapper<>(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Region.Entry must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fromRegionEntryReturnsNonNullRegionEntryWrapper() {

		Region.Entry<Object, Object> mockRegionEntry = mock(Region.Entry.class);

		Object wrapper = RegionEntryWrapper.from(mockRegionEntry);

		assertThat(wrapper).isInstanceOf(RegionEntryWrapper.class);

		verifyNoInteractions(mockRegionEntry);
	}

	@Test
	public void fromObjectReturnsObject() {
		assertThat(RegionEntryWrapper.from("TEST")).isEqualTo("TEST");
	}

	@Test
	public void fromNullReturnsNull() {
		assertThat(RegionEntryWrapper.<Object, Object, Object>from(null)).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void regionEntryWrapperDelegatesAllRegionEntryOperations() {

		AtomicReference<Object> userAttribute = new AtomicReference<>(null);
		AtomicReference<Object> value = new AtomicReference<>(null);

		CacheStatistics mockCacheStatistics = mock(CacheStatistics.class);

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		Region<Object, Object> mockRegion = mock(Region.class);

		Region.Entry<Object, Object> mockRegionEntry = mock(Region.Entry.class);

		doReturn(false).when(mockRegionEntry).isDestroyed();
		doReturn(true).when(mockRegionEntry).isLocal();
		doReturn(2).when(mockRegionEntry).getKey();
		doReturn(mockRegion).when(mockRegionEntry).getRegion();
		doReturn(mockCacheStatistics).when(mockRegionEntry).getStatistics();

		doAnswer(invocation -> userAttribute.getAndUpdate(it -> invocation.getArgument(0)))
			.when(mockRegionEntry).setUserAttribute(any());

		doAnswer(invocation -> userAttribute.get()).when(mockRegionEntry).getUserAttribute();

		doAnswer(invocation -> value.getAndUpdate(it -> invocation.getArgument(0)))
			.when(mockRegionEntry).setValue(any());

		doAnswer(invocation -> value.get()).when(mockRegionEntry).getValue();

		Region.Entry<Object, Object> regionEntryWrapper = RegionEntryWrapper.from(mockRegionEntry);

		assertThat(regionEntryWrapper).isInstanceOf(RegionEntryWrapper.class);
		assertThat(regionEntryWrapper.isDestroyed()).isFalse();
		assertThat(regionEntryWrapper.isLocal()).isTrue();
		assertThat(regionEntryWrapper.getKey()).isEqualTo(2);
		assertThat(regionEntryWrapper.getRegion()).isEqualTo(mockRegion);
		assertThat(regionEntryWrapper.getStatistics()).isEqualTo(mockCacheStatistics);
		assertThat(regionEntryWrapper.setUserAttribute("TEST")).isNull();
		assertThat(regionEntryWrapper.getUserAttribute()).isEqualTo("TEST");
		assertThat(regionEntryWrapper.setValue(42)).isNull();
		assertThat(regionEntryWrapper.getValue()).isEqualTo(42);
		assertThat(regionEntryWrapper.setValue(mockPdxInstance)).isEqualTo(42);

		Object regionValue = regionEntryWrapper.getValue();

		assertThat(regionValue).isInstanceOf(PdxInstanceWrapper.class);
		assertThat(((PdxInstanceWrapper) regionValue).getDelegate()).isEqualTo(mockPdxInstance);

		verify(mockRegionEntry, times(1)).isDestroyed();
		verify(mockRegionEntry, times(1)).isLocal();
		verify(mockRegionEntry, times(1)).getKey();
		verify(mockRegionEntry, times(1)).getRegion();
		verify(mockRegionEntry, times(1)).getStatistics();
		verify(mockRegionEntry, times(1)).setUserAttribute(eq("TEST"));
		verify(mockRegionEntry, times(1)).getUserAttribute();
		verify(mockRegionEntry, times(1)).setValue(eq(42));
		verify(mockRegionEntry, times(1)).setValue(eq(mockPdxInstance));
		verify(mockRegionEntry, times(2)).getValue();
		verifyNoInteractions(mockCacheStatistics, mockRegion);
	}

	static final class TestRegionEntryWrapper<K, V> extends RegionEntryWrapper<K, V> {

		TestRegionEntryWrapper(Region.Entry<K, V> regionEntry) {
			super(regionEntry);
		}

		@Override
		protected Region.Entry<K, V> getDelegate() {
			return super.getDelegate();
		}
	}
}
