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
package org.springframework.geode.data.json;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;

import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.data.AbstractCacheDataImporterExporter;
import org.springframework.geode.data.CacheDataExporter;
import org.springframework.geode.data.CacheDataImporter;
import org.springframework.geode.data.json.converter.AbstractObjectArrayToJsonConverter;
import org.springframework.geode.data.json.converter.JsonToPdxArrayConverter;
import org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter;
import org.springframework.geode.pdx.ObjectPdxInstanceAdapter;
import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.geode.util.CacheUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link JsonCacheDataImporterExporter} class is a {@link CacheDataImporter} and {@link CacheDataExporter}
 * implementation that can export/import JSON data to/from a {@link Resource} given a target {@link Region}.
 *
 * @author John Blum
 * @see java.io.File
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.geode.data.AbstractCacheDataImporterExporter
 * @see org.springframework.geode.data.CacheDataExporter
 * @see org.springframework.geode.data.CacheDataImporter
 * @see org.springframework.geode.data.json.converter.JsonToPdxArrayConverter
 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
 * @see org.springframework.geode.data.json.converter.support.JSONFormatterPdxToJsonConverter
 * @see org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter
 * @see org.springframework.geode.pdx.PdxInstanceWrapper
 * @see org.springframework.stereotype.Component
 * @since 1.3.0
 */
@Component
@SuppressWarnings("rawtypes")
public class JsonCacheDataImporterExporter extends AbstractCacheDataImporterExporter {

	private static final boolean APPEND_TO_FILE = false;

	private static final int CONTENT_PREVIEW_LENGTH = 50;
	private static final int DEFAULT_BUFFER_SIZE = 32768;

	private JsonToPdxArrayConverter jsonToPdxArrayConverter = newJsonToPdxArrayConverter();

	private final RegionValuesToJsonConverter regionValuesToJsonConverter = new RegionValuesToJsonConverter();

	// TODO configure via an SPI or DI
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

		String json = toJson(region);

		getLogger().debug("Saving JSON [{}] from Region [{}]", json, region.getName());

		getResource(region, getResourceLocation())
			.ifPresent(resource -> save(json, resource));

		return region;
	}

	/**
	 * Saves the given {@link String content} (e.g. JSON) to the specified {@link Resource}.
	 *
	 * @param content {@link String} containing the content to save.
	 * @param resource {@link Resource} to save the {@link String content} to; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link Resource} is {@literal null}.
	 * @see org.springframework.core.io.Resource
	 */
	protected void save(@Nullable String content, @NonNull Resource resource) {

		Assert.notNull(resource, "Resource must not be null");

		if (StringUtils.hasText(content)) {
			try (Writer writer = newWriter(resource)) {
				writer.write(content, 0, content.length());
				writer.flush();
			}
			catch (IOException cause) {

				String message = String.format("Failed to save content '%s' to Resource [%s]",
					formatContentForPreview(content), resource.getDescription());

				throw new DataAccessResourceFailureException(message, cause);
			}
		}
	}

	@NonNull Writer newWriter(@NonNull Resource resource) throws IOException {
		return new BufferedWriter(new FileWriter(resource.getFile(), APPEND_TO_FILE));
	}

	@Nullable String formatContentForPreview(@Nullable String content) {

		if (StringUtils.hasText(content)) {

			int length = Math.min(content.length(), CONTENT_PREVIEW_LENGTH);

			content = content.substring(0, length).concat(length < content.length() ? "..." : "");
		}

		return content;
	}

	/**
	 * @inheritDoc
	 */
	@NonNull @Override @SuppressWarnings("unchecked")
	public Region doImportInto(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		getResource(region, CLASSPATH_RESOURCE_PREFIX)
			.filter(Resource::exists)
			.map(this::getContent)
			.map(this::toPdx)
			.map(Arrays::stream)
			.ifPresent(pdxInstances -> pdxInstances.forEach(pdxInstance ->
				region.put(resolveKey(pdxInstance), resolveValue(pdxInstance))));

		return region;
	}

	/**
	 * Gets the contents of the given {@link Resource} as an array of {@literal Byte#TYPE bytes}.
	 *
	 * @param resource {@link Resource} from which to load the content; must not be {@literal null}.
	 * @return an array of {@link Byte#TYPE bytes} containing the contents of the given {@link Resource}.
	 * @throws IllegalArgumentException if {@link Resource} is {@literal null}.
	 * @throws DataAccessResourceFailureException if an {@link IOException} occurs while reading from
	 * and loading the contents of the given {@link Resource}.
	 * @see org.springframework.core.io.Resource
	 */
	protected @NonNull byte[] getContent(@NonNull Resource resource) {

		Assert.notNull(resource, "Resource must not be null");

		try (InputStream in = resource.getInputStream()){

			ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());

			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

			for (int bytesRead = in.read(buffer); bytesRead != -1; bytesRead = in.read(buffer)) {
				out.write(buffer, 0, bytesRead);
				out.flush();
			}

			out.flush();
			out.close();

			return out.toByteArray();
		}
		catch (IOException cause) {
			throw new DataAccessResourceFailureException(String.format("Failed to read from Resource [%s]",
				resource.getDescription()), cause);
		}
	}

	/**
	 * Returns a computed JSON {@link Resource} containing JSON data to be loaded into the given {@link Region}.
	 *
	 * @param region target {@link Region} used to compute the JSON {@link Resource} to load.
	 * @param resourcePrefix {@link String} containing a path prefix (e.g. {@literal classpath:} used to indicate
	 * the location of the {@link Resource} to load.
	 * @return an {@link Optional} JSON {@link Resource} containing JSON data to load into the given {@link Region}.
	 * @see org.springframework.core.io.Resource
	 * @see org.apache.geode.cache.Region
	 * @see java.util.Optional
	 */
	protected Optional<Resource> getResource(@NonNull Region region, @Nullable String resourcePrefix) {

		Assert.notNull(region, "Region must not be null");

		String regionName = region.getName().toLowerCase();
		String resourceName = String.format(RESOURCE_NAME_PATTERN, regionName);
		String resolvedResourcePrefix = StringUtils.hasText(resourcePrefix)
			? resourcePrefix
			: CLASSPATH_RESOURCE_PREFIX;

		Resource resource = getApplicationContext()
			.map(it -> it.getResource(String.format("%1$s%2$s", resolvedResourcePrefix, resourceName)))
			.orElseGet((() -> new ClassPathResource(resourceName)));

		return Optional.of(resource);
	}

	/**
	 * Returns a {@link String file system path} specifying the location for where to export the {@link Resource}.
	 *
	 * @return a {@link String file system path} specifying the location for where to export the {@link Resource}.
	 */
	protected @NonNull String getResourceLocation() {
		return String.format("%1$s%2$s%2$s%3$s%4$s", FILESYSTEM_RESOURCE_PREFIX, RESOURCE_PATH_SEPARATOR,
			System.getProperty("user.dir"), File.separator);
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
	 * for the {@link Region.Entry entry} stored in the {@link Region}.
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
		return getJsonToPdxArrayConverter().convert(json);
	}

	static class RegionValuesToJsonConverter extends AbstractObjectArrayToJsonConverter {

		@NonNull <K, V> String convert(@NonNull Region<K, V> region) {

			Assert.notNull(region, "Region must not be null");

			return super.convert(CollectionUtils.nullSafeCollection(CacheUtils.collectValues(region)));
		}
	}
}
