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
package org.springframework.geode.pdx;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionService;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxInstanceFactory;

import org.springframework.geode.cache.SimpleCacheResolver;

/**
 * The {@link PdxInstanceBuilder} class is a <a href="https://en.wikipedia.org/wiki/Builder_pattern">Builder</a>
 * used to construct and initialize a {@link PdxInstance} from different sources.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.RegionService
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.PdxInstanceFactory
 * @see <a href="https://en.wikipedia.org/wiki/Builder_pattern">Builder Software Design Pattern</a>
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class PdxInstanceBuilder {

	/**
	 * Factory method used to construct a new instance of the {@link PdxInstanceBuilder} class.
	 *
	 * This factory method tries to resolve the {@link GemFireCache} instance for the caller
	 * by using {@link SimpleCacheResolver}.
	 *
	 * Alternatively, callers may provider their own {@link GemFireCache} instance by calling
	 * {@link #create(RegionService)}.
	 *
	 * @return a new instance of the {@link PdxInstanceBuilder}.
	 * @throws IllegalArgumentException if a {@link GemFireCache} instance is not present.
	 * @see #create(RegionService)
	 */
	public static PdxInstanceBuilder create() {
		return create(SimpleCacheResolver.getInstance().require());
	}

	/**
	 * Factory method use to construct a new instance of the {@link PdxInstanceBuilder} class initialized with
	 * the given, required {@link RegionService} used by the Builder to performs its functions.
	 *
	 * @param regionService {@link RegionService} instance used by the {@link PdxInstanceBuilder} to perform
	 * its functions;
	 * must not be {@literal null}.
	 * @return an new instance of the {@link PdxInstanceBuilder}.
	 * @throws IllegalArgumentException if {@link GemFireCache} is {@literal null}.
	 * @see org.apache.geode.cache.GemFireCache
	 * @see #PdxInstanceBuilder(RegionService)
	 */
	public static PdxInstanceBuilder create(RegionService regionService) {
		return new PdxInstanceBuilder(regionService);
	}

	private static void assertNotNull(Object target, String message, Object... arguments) {

		if (target == null) {
			throw new IllegalArgumentException(String.format(message, arguments));
		}
	}

	private static <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	private final RegionService regionService;

	private PdxInstanceFactory pdxFactory;

	/**
	 * Constructs a new instance of {@link PdxInstanceBuilder} initialized with the required {@link RegionService}.
	 *
	 * @param regionService {@link RegionService} instance used to perform the functions of the PDX Builder.
	 * @throws IllegalArgumentException if {@link RegionService} is {@literal null}.
	 * @see org.apache.geode.cache.RegionService
	 */
	protected PdxInstanceBuilder(RegionService regionService) {

		assertNotNull(regionService, "RegionService must not be null");

		this.regionService = regionService;
	}

	/**
	 * Returns a reference to the configured {@link RegionService} used to perform the operations of this PDX Builder.
	 *
	 * @return a reference to the configured {@link RegionService}; never {@literal null}.
	 */
	protected RegionService getRegionService() {
		return this.regionService;
	}

	/**
	 * Copies the contents of the existing {@link PdxInstance} to a new {@link PdxInstance} built with this Builder.
	 *
	 * @param pdxInstance {@link PdxInstance} to copy.
	 * @return an instance of the {@link PdxInstanceFactory} used to {@link PdxInstanceFactory#create() create}
	 * the {@link PdxInstance}.
	 * @throws IllegalArgumentException if {@link PdxInstance} is {@literal null}.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see org.apache.geode.pdx.PdxInstanceFactory
	 */
	public PdxInstanceFactory copy(PdxInstance pdxInstance) {

		assertNotNull(pdxInstance, "PdxInstance must not be null");

		PdxInstanceFactory factory = getRegionService().createPdxInstanceFactory(pdxInstance.getClassName());

		nullSafeList(pdxInstance.getFieldNames()).forEach(fieldName -> {

			factory.writeObject(fieldName, pdxInstance.getField(fieldName));

			if (pdxInstance.isIdentityField(fieldName)) {
				factory.markIdentityField(fieldName);
			}
		});

		return factory;
	}

	/**
	 * Constructs a new {@link PdxInstance} from the given, required source {@link Object}.
	 *
	 * @param source {@link Object} being serialized to PDX; must not be {@literal null}.
	 * @return a {@link Factory} used to create the {@link PdxInstance} from the given,
	 * required source {@link Object}, which was serialized to PDX.
	 * @throws IllegalArgumentException if {@link Object source} is {@literal null}.
	 * @see Factory
	 */
	public Factory from(Object source) {

		assertNotNull(source, "Source object to serialize to PDX must not be null");

		RegionService regionService = getRegionService();

		Optional.of(regionService)
			.filter(GemFireCache.class::isInstance)
			.map(GemFireCache.class::cast)
			.map(GemFireCache::getPdxReadSerialized)
			.filter(Boolean.TRUE::equals)
			.orElseThrow(() -> new IllegalStateException("PDX read-serialized must be set to true"));

		PdxInstanceFactory factory = regionService.createPdxInstanceFactory(source.getClass().getName());

		factory.writeObject("source", source);

		AtomicReference<Object> resolvedSource = new AtomicReference<>(null);

		return () -> Optional.of(factory)
			.map(PdxInstanceFactory::create)
			.map(pdxInstance -> resolvedSource.updateAndGet(it -> pdxInstance.getField("source")))
			.filter(PdxInstance.class::isInstance)
			.map(PdxInstance.class::cast)
			.orElseThrow(() -> {

				String message = String.format("Expected an instance of PDX but was an instance of type [%s];"
						+ " Was PDX read-serialized set to true", nullSafeClassName(resolvedSource.get()));

				return new IllegalArgumentException(message);
			});
	}

	private String nullSafeClassName(Object target) {
		return target != null ? target.getClass().getName() : null;
	}

	@FunctionalInterface
	public interface Factory {

		/**
		 * Creates a {@link PdxInstance}.
		 *
		 * @return the created {@link PdxInstance}.
		 * @throws IllegalArgumentException Depending on the implementation, an {@link IllegalArgumentException}
		 * may be thrown if the {@link Object created object} is not of type {@link PdxInstance}.
		 * @throws ClassCastException Depending on the implementation, a {@link ClassCastException} may be thrown
		 * if the {@link Object created object} is not of type {@link PdxInstance}.
		 * @see org.apache.geode.pdx.PdxInstance
		 */
		PdxInstance create();

	}
}
