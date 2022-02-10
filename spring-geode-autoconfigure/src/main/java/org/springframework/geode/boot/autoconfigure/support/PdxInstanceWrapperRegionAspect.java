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
package org.springframework.geode.boot.autoconfigure.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.geode.cache.CacheStatistics;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * A Spring, AspectJ {@link Aspect} advising Apache Geode cache {@link Region} data access operations.
 *
 * Specifically, this {@link Aspect} wraps all {@link PdxInstance} objects returned from cache {@link Region}
 * read data access operations inside a new instance of {@link PdxInstanceWrapper}.
 *
 * @author John Blum
 * @see java.util.Map
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.aspectj.lang.annotation.Around
 * @see org.aspectj.lang.annotation.Aspect
 * @see org.aspectj.lang.annotation.Pointcut
 * @see org.springframework.geode.pdx.PdxInstanceWrapper
 * @since 1.3.0
 */
@Aspect
@SuppressWarnings("unused")
public class PdxInstanceWrapperRegionAspect {

	private static Collection<?> asCollection(Object value) {
		return value instanceof Collection ? (Collection<?>) value : Collections.emptyList();
	}

	private static Map<?, ?> asMap(Object value) {
		return value instanceof Map ? (Map<?, ?>) value : Collections.emptyMap();
	}

	@Pointcut("target(org.apache.geode.cache.Region)")
	private void regionPointcut() { }

	@Pointcut("execution(* org.apache.geode.cache.Region.get(..))")
	private void regionGetPointcut() { }

	@Pointcut("execution(* org.apache.geode.cache.Region.getAll(..))")
	private void regionGetAllPointcut() { }

	@Pointcut("execution(* org.apache.geode.cache.Region.getEntry(..))")
	private void regionGetEntryPointcut() { }

	@Pointcut("execution(* org.apache.geode.cache.Region.selectValue(..))")
	private void regionSelectValuePointcut() {
	}

	@Pointcut("execution(* org.apache.geode.cache.Region.values())")
	private void regionValuesPointcut() { }

	@Around("regionPointcut() && regionGetPointcut()")
	public Object regionGetAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		return PdxInstanceWrapper.from(joinPoint.proceed());
	}

	@Around("regionPointcut() && regionGetAllPointcut()")
	public Object regionGetAllAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		return asMap(joinPoint.proceed()).entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, mapEntry -> PdxInstanceWrapper.from(mapEntry.getValue())));
	}

	@Around("regionPointcut() && regionGetEntryPointcut()")
	public Object regionGetEntryAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		return RegionEntryWrapper.from(joinPoint.proceed());
	}

	@Around("regionPointcut() && regionSelectValuePointcut()")
	public Object regionSelectValueAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		return PdxInstanceWrapper.from(joinPoint.proceed());
	}

	@Around("regionPointcut() && regionValuesPointcut()")
	public Object regionValuesAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

		return asCollection(joinPoint.proceed()).stream()
			.map(PdxInstanceWrapper::from)
			.collect(Collectors.toList());
	}

	public static class RegionEntryWrapper<K, V> implements Region.Entry<K, V> {

		@SuppressWarnings("unchecked")
		public static <T, K, V> T from(T value) {

			return value instanceof Region.Entry
				? (T) new RegionEntryWrapper<>((Region.Entry<K, V>) value)
				: value;
		}

		private final Region.Entry<K, V> delegate;

		protected RegionEntryWrapper(@NonNull Region.Entry<K, V> regionEntry) {

			Assert.notNull(regionEntry, "Region.Entry must not be null");

			this.delegate = regionEntry;
		}

		protected @NonNull Region.Entry<K, V> getDelegate() {
			return this.delegate;
		}

		@Override
		public boolean isDestroyed() {
			return getDelegate().isDestroyed();
		}

		@Override
		public boolean isLocal() {
			return getDelegate().isLocal();
		}

		@Override
		public K getKey() {
			return getDelegate().getKey();
		}

		@Override
		public Region<K, V> getRegion() {
			return getDelegate().getRegion();
		}

		@Override
		public CacheStatistics getStatistics() {
			return getDelegate().getStatistics();
		}

		@Override
		public Object setUserAttribute(Object userAttribute) {
			return getDelegate().setUserAttribute(userAttribute);
		}

		@Override
		public Object getUserAttribute() {
			return getDelegate().getUserAttribute();
		}

		@Override
		public V setValue(V value) {
			return getDelegate().setValue(value);
		}

		@Override
		@SuppressWarnings("unchecked")
		public V getValue() {
			return (V) PdxInstanceWrapper.from(getDelegate().getValue());
		}
	}
}
