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

import java.util.Optional;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

import org.springframework.geode.data.json.converter.ObjectToJsonConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * An {@link ObjectToJsonConverter} implementation using the Apache Geode {@link JSONFormatter} to convert
 * from a {@link PdxInstance} to a {@literal JSON} {@link String}.
 *
 * @author John Blum
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.geode.data.json.converter.ObjectToJsonConverter
 * @see org.springframework.geode.data.json.converter.support.JacksonObjectToJsonConverter
 * @since 1.3.0
 */
public class JSONFormatterPdxToJsonConverter extends JacksonObjectToJsonConverter {

	/**
	 * @inheritDoc
	 */
	@Nullable @Override
	public final String convert(@Nullable Object source) {

		return Optional.ofNullable(source)
			.filter(PdxInstance.class::isInstance)
			.map(PdxInstance.class::cast)
			.map(this::convertPdxToJson)
			.orElseGet(() -> convertObjectToJson(source));
	}

	/**
	 * Converts the given {@link Object} to JSON.
	 *
	 * @param source {@link Object} to convert to JSON.
	 * @return the JSON generated from the given {@link Object}.
	 * @see JacksonObjectToJsonConverter#convert(Object)
	 */
	protected @Nullable String convertObjectToJson(Object source) {
		return super.convert(source);
	}

	/**
	 * Converts the given {@link PdxInstance PDX} to JSON.
	 *
	 * @param pdxInstance {@link PdxInstance} to convert to JSON; must not be {@literal null}.
	 * @return JSON generated from the given {@link PdxInstance}.
	 * @see org.apache.geode.pdx.JSONFormatter#toJSON(PdxInstance)
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected @NonNull String convertPdxToJson(@NonNull PdxInstance pdxInstance) {
		return JSONFormatter.toJSON(pdxInstance);
	}
}
