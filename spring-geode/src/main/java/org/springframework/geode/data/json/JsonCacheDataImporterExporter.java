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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.data.AbstractCacheDataImporterExporter;
import org.springframework.geode.data.CacheDataExporter;
import org.springframework.geode.data.CacheDataImporter;
import org.springframework.geode.data.json.converter.JsonToPdxArrayConverter;
import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.geode.data.json.converter.support.JSONFormatterPdxToJsonConverter;
import org.springframework.geode.data.json.converter.support.JacksonJsonToPdxConverter;
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

	protected static final String ARRAY_BEGIN = "[";
	protected static final String ARRAY_END = "]";
	protected static final String AT_IDENTIFIER_FIELD_NAME = PdxInstanceWrapper.AT_IDENTIFIER_FIELD_NAME;
	protected static final String CLASSPATH_RESOURCE_PREFIX = ResourceLoader.CLASSPATH_URL_PREFIX;
	protected static final String COMMA_SPACE = ", ";
	protected static final String EMPTY_STRING = "";
	protected static final String FILESYSTEM_RESOURCE_PREFIX = "file://";
	protected static final String ID_FIELD_NAME = PdxInstanceWrapper.ID_FIELD_NAME;
	protected static final String NO_FIELD_NAME = "";
	protected static final String RESOURCE_NAME_PATTERN = "data-%s.json";

	private JsonToPdxArrayConverter jsonToPdxArrayConverter = newJsonToPdxArrayConverter();

	private ObjectToJsonConverter objectToJsonConverter = newObjectToJsonConverter();

	// TODO configure via an SPI
	private @NonNull JsonToPdxArrayConverter newJsonToPdxArrayConverter() {
		return new JacksonJsonToPdxConverter();
	}

	// TODO configure via an SPI
	private @NonNull ObjectToJsonConverter newObjectToJsonConverter() {
		return new JSONFormatterPdxToJsonConverter();
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
	 * Gets a reference to the configured {@link ObjectToJsonConverter}.
	 *
	 * @return a reference to the configured {@link ObjectToJsonConverter}.
	 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
	 */
	protected @NonNull ObjectToJsonConverter getObjectToJsonConverter() {
		return this.objectToJsonConverter;
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
			.map(this::toPdxArray)
			.map(Arrays::stream)
			.ifPresent(pdxInstances -> pdxInstances.forEach(pdxInstance -> {
				pdxInstance = postProcess(pdxInstance);
				region.put(getIdentifier(pdxInstance), pdxInstance);
			}));

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
	 * Determines the {@link Object identifier} (a.k.a. {@link PdxInstance#isIdentityField(String) identity})
	 * for the given {@link PdxInstance}.
	 *
	 * @param pdxInstance {@link PdxInstance} to evaluate; must not be {@literal null}.
	 * @return the {@link Object identifier} for the given {@link PdxInstance}; never {@literal null}.
	 * @throws IllegalArgumentException if {@link PdxInstance} is {@literal null}.
	 * @throws IllegalStateException if the {@link PdxInstance} does not have an id.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #getId(PdxInstance)
	 */
	protected @NonNull Object getIdentifier(@NonNull PdxInstance pdxInstance) {

		Assert.notNull(pdxInstance, "PdxInstance must not be null");

		Optional<String> idFieldName = CollectionUtils.nullSafeList(pdxInstance.getFieldNames()).stream()
			.filter(StringUtils::hasText)
			.filter(pdxInstance::isIdentityField)
			.findFirst();

		return idFieldName
			.map(pdxInstance::getField)
			.orElseGet(() -> getId(pdxInstance));
	}

	/**
	 * Searches for a PDX {@link String field name} called {@literal id} on the given {@link PdxInstance}
	 * and returns its {@link Object value} as the {@link Object identifier} for the {@link PdxInstance}.
	 *
	 * @param pdxInstance {@link PdxInstance} to evaluate; must not be {@literal null}.
	 * @return the {@link Object value} of the {@literal id} {@link String field} on the {@link PdxInstance}.
	 * @throws IllegalStateException if the {@link PdxInstance} does not have an id.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #getAtIdentifier(PdxInstance)
	 */
	protected @NonNull Object getId(@NonNull PdxInstance pdxInstance) {

		Assert.notNull(pdxInstance, "PdxInstance must not be null");

		return Optional.of(pdxInstance)
			.filter(it -> it.hasField(ID_FIELD_NAME))
			.map(it -> it.getField(ID_FIELD_NAME))
			.orElseGet(() -> getAtIdentifier(pdxInstance));
	}

	/**
	 * Searches for a PDX {@link String field} declared by the {@literal @identifier} metadata {@link String field}
	 * on the given {@link PdxInstance} and returns the {@link Object value} of this {@link String field}
	 * as the {@link Object identifier} for the {@link PdxInstance}.
	 *
	 * @param pdxInstance {@link PdxInstance} to evaluate; most not be {@literal null}.
	 * @return the {@link Object value} of the {@link String field} declared in the {@literal @identifier} metadata
	 * {@link String field} on the {@link PdxInstance}.
	 * @throws IllegalArgumentException if {@link PdxInstance} is {@literal null}.
	 * @throws IllegalStateException if the {@link PdxInstance} does not have an id.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected @NonNull Object getAtIdentifier(@NonNull PdxInstance pdxInstance) {

		Assert.notNull(pdxInstance, "PdxInstance must not be null");

		return Optional.of(pdxInstance)
			.filter(pdx -> pdx.hasField(AT_IDENTIFIER_FIELD_NAME))
			.map(pdx -> pdx.getField(AT_IDENTIFIER_FIELD_NAME))
			.map(String::valueOf)
			.filter(pdxInstance::hasField)
			.map(pdxInstance::getField)
			.orElseThrow(() -> newIllegalStateException(String.format("PdxInstance for type [%1$s] has no %2$s",
				pdxInstance.getClassName(), resolveMessageForIdentifierError(pdxInstance))));
	}

	private @NonNull String resolveMessageForIdentifierError(@NonNull PdxInstance pdxInstance) {

		String message = "declared identifier";

		if (pdxInstance.hasField(ID_FIELD_NAME)) {
			message = "id";
		}
		else if (pdxInstance.hasField(AT_IDENTIFIER_FIELD_NAME)) {

			Object atIdentifierFieldValue = pdxInstance.getField(AT_IDENTIFIER_FIELD_NAME);

			String resolvedIdentifierFieldName = Objects.nonNull(atIdentifierFieldValue)
				? atIdentifierFieldValue.toString().trim()
				: NO_FIELD_NAME;

			boolean identifierFieldNameWasDeclaredAndIsValid = pdxInstance.hasField(resolvedIdentifierFieldName);

			Object identifier = identifierFieldNameWasDeclaredAndIsValid
				? pdxInstance.getField(resolvedIdentifierFieldName)
				: null;

			message = identifierFieldNameWasDeclaredAndIsValid
				? String.format("value [%s] for field [%s] declared in [%s]", identifier, resolvedIdentifierFieldName,
				AT_IDENTIFIER_FIELD_NAME)
				: String.format("field [%s] declared in [%s]", resolvedIdentifierFieldName, AT_IDENTIFIER_FIELD_NAME);
		}

		return message;
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
		return String.format("%1$s%2$s%3$s", FILESYSTEM_RESOURCE_PREFIX, System.getProperty("user.dir"), File.separator);
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

	// TODO Replace implementation with AbstractObjectArrayToJsonConverter.convert(:Iterable) method.
	/**
	 * Convert {@link Object values} contained in the {@link Region} to JSON.
	 *
	 * @param region {@link Region} to process; must not be {@literal null}.
	 * @return a JSON {@link String} containing the {@link Object values} from the given {@link Region}.
	 * @see org.apache.geode.cache.Region
	 * @see #toJson(Object)
	 */
	@SuppressWarnings("unchecked")
	protected @NonNull String toJson(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		StringBuilder json = new StringBuilder(ARRAY_BEGIN);

		boolean addComma = false;

		for (Object value : CollectionUtils.nullSafeCollection(CacheUtils.collectValues(region))) {
			json.append(addComma ? COMMA_SPACE : EMPTY_STRING);
			json.append(toJson(value));
			addComma = true;
		}

		json.append(ARRAY_END);

		return json.toString();
	}

	/**
	 * Converts the given {@link Object} into JSON.
	 *
	 * @param source {@link Object} to convert into JSON.
	 * @return a {@link String} containing the JSON generated from the given {@link Object}.
	 * @see #getObjectToJsonConverter()
	 */
	protected @NonNull String toJson(@NonNull Object source) {
		return getObjectToJsonConverter().convert(source);
	}

	/**
	 * Converts the array of {@link Byte#TYPE bytes} containing multiple JSON objects
	 * into an array of {@link PdxInstance PdxInstances}.
	 *
	 * @param json array of {@link Byte#TYPE bytes} containing the JSON to convert to PDX.
	 * @return an array of {@link PdxInstance PdxInstances} for each JSON object.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #getJsonToPdxArrayConverter()
	 */
	protected @NonNull PdxInstance[] toPdxArray(@NonNull byte[] json) {
		return getJsonToPdxArrayConverter().convert(json);
	}
}
