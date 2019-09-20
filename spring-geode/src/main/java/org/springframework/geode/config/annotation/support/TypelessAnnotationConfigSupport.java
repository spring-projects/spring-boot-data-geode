/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.geode.config.annotation.support;

import java.lang.annotation.Annotation;

import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;

/**
 * The {@link TypelessAnnotationConfigSupport} class is an extension of SDG's {@link AbstractAnnotationConfigSupport}
 * based class for resolving {@link AnnotatedTypeMetadata}, however, is not based on any specific {@link Annotation}.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @since 1.2.0
 */
public class TypelessAnnotationConfigSupport extends AbstractAnnotationConfigSupport {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return null;
	}
}
