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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.geode.cache.Region;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.geode.data.AbstractCacheDataImporterExporter;
import org.springframework.geode.data.CacheDataExporter;
import org.springframework.geode.data.CacheDataImporter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The {@link JsonCacheDataImporterExporter} class is a {@link CacheDataImporter} and {@link CacheDataExporter}
 * implementation that can export/import JSON data to/from a {@link Resource} given a target {@link Region}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.core.io.Resource
 * @see org.springframework.geode.data.AbstractCacheDataImporterExporter
 * @see org.springframework.geode.data.CacheDataExporter
 * @see org.springframework.geode.data.CacheDataImporter
 * @see org.springframework.stereotype.Component
 * @since 1.3.0
 */
@Component
@SuppressWarnings("rawtypes")
public class JsonCacheDataImporterExporter extends AbstractCacheDataImporterExporter {

	protected static final String RESOURCE_NAME_PATTERN = "data-%s.json";

	@NonNull @Override
	public Region doExportFrom(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		String regionName = region.getName();

		return region;
	}

	@NonNull @Override @SuppressWarnings("unchecked")
	public Region doImportInto(@NonNull Region region) {

		Assert.notNull(region, "Region must not be null");

		// TODO: handle a JSON array of objects and PdxInstances!!!
		getResource(region)
			.filter(Resource::exists)
			.map(this::getContent)
			.map(this::toPdxInstance)
			.ifPresent(pdxInstance -> region.put(getIdentifier(pdxInstance), pdxInstance));

		return region;
	}

	private @NonNull byte[] getContent(@NonNull Resource resource) {

		try (InputStream in = resource.getInputStream()){

			ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());

			byte[] buffer = new byte[32768];

			for (int bytesRead = in.read(buffer); bytesRead != -1; bytesRead = in.read(buffer)) {
				out.write(buffer, 0, bytesRead);
				out.flush();
			}

			return out.toByteArray();
		}
		catch (IOException cause) {
			throw new DataAccessResourceFailureException(String.format("Failed to read from Resource [%s]",
				resource.getDescription()), cause);
		}
	}

	private Optional<Resource> getResource(@NonNull Region region) {

		String regionName = region.getName().toLowerCase();
		String resourceName = String.format(RESOURCE_NAME_PATTERN, regionName);

		return getApplicationContext()
			.map(it -> it.getResource(String.format("classpath:%s", resourceName)));
	}

	protected @NonNull Object getIdentifier(@NonNull PdxInstance pdxInstance) {

		String idField = CollectionUtils.nullSafeList(pdxInstance.getFieldNames()).stream()
			.filter(pdxInstance::isIdentityField)
			.findFirst()
			.orElseThrow(() -> newIllegalStateException("PdxInstance for type [%s] has no declared identify field",
				pdxInstance.getClassName()));

		return pdxInstance.getField(idField);
	}

	protected PdxInstance toPdxInstance(byte[] json) {
		return JSONFormatter.fromJSON(json);
	}
}
