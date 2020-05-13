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
package org.springframework.geode.data.json.converter.support;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.geode.data.json.converter.JsonToPdxConverter;
import org.springframework.geode.pdx.PdxInstanceWrapper;
import org.springframework.lang.NonNull;

/**
 * A {@link JsonToPdxConverter} implementation using the Apache Geode {@link JSONFormatter} to convert
 * from a {@literal JSON} {@link String} to a {@link PdxInstance}.
 *
 * @author John Blum
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.JsonToPdxConverter
 * @since 1.3.0
 */
public class JSONFormatterJsonToPdxConverter implements JsonToPdxConverter {

	/**
	 * @inheritDoc
	 */
	@Override
	public final @NonNull PdxInstance convert(@NonNull String json) {
		return convertJsonToPdx(json);
	}

	/**
	 * Converts the given {@link String JSON} to {@link PdxInstance PDX}.
	 *
	 * @param json {@link String} containing JSON to convert to PDX; must not be {@literal null}.
	 * @return JSON for the given {@link PdxInstance PDX}.
	 * @see org.springframework.geode.pdx.PdxInstanceWrapper#from(PdxInstance)
	 * @see org.apache.geode.pdx.JSONFormatter#fromJSON(String)
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #jsonFormatterFromJson(String)
	 * @see #wrap(PdxInstance)
	 */
	protected @NonNull PdxInstance convertJsonToPdx(@NonNull String json) {
		return wrap(jsonFormatterFromJson(json));
	}

	/**
	 * Converts {@link String JSON} into {@link PdxInstance PDX} using {@link JSONFormatter#fromJSON(String)}.
	 *
	 * @param json {@link String JSON} to convert to {@link PdxInstance PDX}; must not be {@literal null}.
	 * @return {@link PdxInstance PDX} generated from the given, required {@link String JSON}; never {@literal null}.
	 * @see org.apache.geode.pdx.JSONFormatter#fromJSON(String)
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected @NonNull PdxInstance jsonFormatterFromJson(@NonNull String json) {
		return JSONFormatter.fromJSON(json);
	}

	/**
	 * Wraps the given {@link PdxInstance} in a new instance of {@link PdxInstanceWrapper}.
	 *
	 * @param pdxInstance {@link PdxInstance} to wrap.
	 * @return a new instance of {@link PdxInstanceWrapper} wrapping the given {@link PdxInstance}.
	 * @see org.springframework.geode.pdx.PdxInstanceWrapper
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected @NonNull PdxInstanceWrapper wrap(@NonNull PdxInstance pdxInstance) {
		return PdxInstanceWrapper.from(pdxInstance);
	}
}
