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
package org.springframework.geode.data.json.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * A Spring {@link Converter} interface extension defining a contract to convert
 * from an {@link Object} into a {@link String JSON}.
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.lang.Object
 * @see java.lang.String
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.3.0
 */
@FunctionalInterface
public interface ObjectToJsonConverter extends Converter<Object, String> {

}
