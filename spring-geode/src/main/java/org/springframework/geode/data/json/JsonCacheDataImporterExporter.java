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
package org.springframework.geode.data.json;

import java.util.Arrays;

import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.data.CacheDataExporter;
import org.springframework.geode.data.CacheDataImporter;
import org.springframework.geode.data.json.converter.AbstractObjectArrayToJsonConverter;
import org.springframework.geode.data.json.converter.JsonToPdxArrayConverter;
import org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter;
import org.springframework.geode.pdx.ObjectPdxInstanceAdapter;
import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.geode.util.CacheUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The {@link JsonCacheDataImporterExporter} class is a {@link CacheDataImporter} and {@link CacheDataExporter}
 * implementation that can export/import JSON data to/from a {@link Resource} given a target {@link Region}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.data.CacheDataExporter
 * @see org.springframework.geode.data.CacheDataImporter
 * @see org.springframework.geode.data.json.converter.JsonToPdxArrayConverter
 * @see org.springframework.geode.data.json.converter.ObjectArrayToJsonConverter
 * @see org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter
 * @see org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter
 * @see org.springframework.geode.pdx.ObjectPdxInstanceAdapter
 * @see org.springframework.geode.pdx.PdxInstanceWrapper
 * @see org.springframework.stereotype.Component
 * @since 1.3.0
 */
@Component
@SuppressWarnings("rawtypes")
public class JsonCacheDataImporterExporter extends ResourceCapableCacheDataImporterExporter {

	protected static final PdxInstance[] EMPTY_PDX_INSTANCE_ARRAY = {};

	@Autowired(required = false)
	private JsonToPdxArrayConverter jsonToPdxArrayConverter;

	private final RegionValuesToJsonConverter regionValuesToJsonConverter = new RegionValuesToJsonConverter();

	/**
	 * Determines whether the given array is empty or not. An array is not empty if the array reference
	 * is not {@literal null} and contains at least 1 element.
	 *
	 * @param <T> {@link Class type} of the array elements.
	 * @param array {@link Object} array to evaluate.
	 * @return a boolean value indicating whether the array is empty or not.
	 */
	@SuppressWarnings("unchecked")
	private static <T> boolean isNotEmpty(T... array) {
		return array != null && array.length > 0;
	}

	/**
	 * Initializes the JSON to PDX (array) converter.
	 *
	 * @see #newJsonToPdxArrayConverter()
	 */
	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();

		this.jsonToPdxArrayConverter = this.jsonToPdxArrayConverter != null
			? this.jsonToPdxArrayConverter
			: newJsonToPdxArrayConverter();
	}

	private @NonNull JsonToPdxArrayConverter newJsonToPdxArrayConverter() {
		return new JacksonJsonToPdxConverter();
	}

	/**
	 * Returns a reference to the configured {@link JsonToPdxArrayConverter}.
	 *
	 * @return a reference to the configured {@link JsonToPdxArrayConverter}.
	 * @see org.springframework.geode.data.json.converter.JsonToPdxArrayConverter
	 */
	protected @NonNull JsonToPdxArrayConverter getJsonToPdxArrayConverter() {
		return this.jsonToPdxArrayConverter;
	}

	/**
	 * @inheritDoc
	 */
	@NonNull @Override
	public Region doExportFrom(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		getExportResourceResolver()
			.resolve(region)
			.ifPresent(resource -> {

				String json = toJson(region);

				getLogger().debug("Saving JSON [{}] from Region [{}]", json, region.getName());

				getResourceWriter().write(resource, json.getBytes());
			});

		return region;
	}

	/**
	 * @inheritDoc
	 */
	@NonNull @Override
	public Region doImportInto(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		getImportResourceResolver()
			.resolve(region)
			.map(this.getResourceReader()::read)
			.map(this::toPdx)
			.ifPresent(pdxInstances -> regionPutPdx(region, pdxInstances));

		return region;
	}

	/**
	 * Puts all PDX data from the {@link PdxInstance} array into the target {@link Region} mapped to
	 * the PDX {@link PdxInstance#isIdentityField(String) identifier} as the {@literal key}.
	 *
	 * @param region target {@link Region} to store the PDX data; must not be {@literal null}
	 * @param pdx {@link PdxInstance} array containing the PDX data to store in the target {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see org.apache.geode.cache.Region#put(Object, Object)
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	@SuppressWarnings("unchecked")
	void regionPutPdx(@NonNull Region region, @Nullable PdxInstance[] pdx) {

		Arrays.stream(ArrayUtils.nullSafeArray(pdx, PdxInstance.class)).forEach(pdxInstance ->
			region.put(resolveKey(pdxInstance), resolveValue(pdxInstance)));
	}

	/**
	 * Post processes the given {{@link PdxInstance}.
	 *
	 * @param pdxInstance {@link PdxInstance} to process.
	 * @return the {@link PdxInstance}.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected PdxInstance postProcess(PdxInstance pdxInstance) {
		return pdxInstance;
	}

	/**
	 * Resolves the {@link Object key} used to map the given {@link PdxInstance} as the {@link Object value}
	 * for the {@code Region.Entry} stored in the {@link Region}.
	 *
	 * @param pdxInstance {@link PdxInstance} used to resolve the {@link Object key}.
	 * @return the resolved {@link Object key}.
	 * @see org.springframework.geode.pdx.PdxInstanceWrapper#getIdentifier()
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected @NonNull Object resolveKey(@NonNull PdxInstance pdxInstance) {
		return PdxInstanceWrapper.from(pdxInstance).getIdentifier();
	}

	/**
	 * Resolves the {@link Object value} to store in the {@link Region} from the given {@link PdxInstance}.
	 *
	 * If the given {@link PdxInstance} is an instance of {@link PdxInstanceWrapper} then this method will return
	 * the underlying, {@link PdxInstanceWrapper#getDelegate() delegate} {@link PdxInstance}.
	 *
	 * If the given {@link PdxInstance} is an instance of {@link ObjectPdxInstanceAdapter} then this method will return
	 * the underlying, {@link ObjectPdxInstanceAdapter#getObject() Object}.
	 *
	 * Otherwise, the given {@link PdxInstance} is returned.
	 *
	 * @param pdxInstance {@link PdxInstance} to unwrap.
	 * @return the resolved {@link Object value}.
	 * @see org.springframework.geode.pdx.ObjectPdxInstanceAdapter#unwrap(PdxInstance)
	 * @see org.springframework.geode.pdx.PdxInstanceWrapper#unwrap(PdxInstance)
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #postProcess(PdxInstance)
	 */
	protected @Nullable Object resolveValue(@Nullable PdxInstance pdxInstance) {
		return ObjectPdxInstanceAdapter.unwrap(PdxInstanceWrapper.unwrap(postProcess(pdxInstance)));
	}

	/**
	 * Convert {@link Object values} contained in the {@link Region} to {@link String JSON}.
	 *
	 * @param region {@link Region} to process; must not be {@literal null}.
	 * @return {@link String JSON} containing the {@link Object values} from the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 */
	@SuppressWarnings("unchecked")
	protected @NonNull String toJson(@NonNull Region region) {
		return this.regionValuesToJsonConverter.convert(region);
	}

	/**
	 * Converts the array of {@link Byte#TYPE bytes} containing multiple {@link String JSON} objects
	 * into an array of {@link PdxInstance PdxInstances}.
	 *
	 * @param json array of {@link Byte#TYPE bytes} containing the {@link String JSON} to convert to PDX.
	 * @return an array of {@link PdxInstance PdxInstances} for each {@link String JSON} object.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #getJsonToPdxArrayConverter()
	 */
	protected @NonNull PdxInstance[] toPdx(@NonNull byte[] json) {

		return isNotEmpty(json)
			? getJsonToPdxArrayConverter().convert(json)
			: EMPTY_PDX_INSTANCE_ARRAY;
	}

	/**
	 * Converts all {@link Region#values() values} in the targeted {@link Region} into {@literal JSON}.
	 *
	 * The converter is capable of handling both {@link Object Objects} and PDX.
	 *
	 * @see org.springframework.geode.data.json.converter.AbstractObjectArrayToJsonConverter
	 */
	static class RegionValuesToJsonConverter extends AbstractObjectArrayToJsonConverter {

		@NonNull <K, V> String convert(@NonNull Region<K, V> region) {

			Assert.notNull(region, "Region must not be null");

			return super.convert(CollectionUtils.nullSafeCollection(CacheUtils.collectValues(region)));
		}
	}
}
