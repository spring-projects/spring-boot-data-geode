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

package org.springframework.geode.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

/**
 * The {@link ConditionalOnMissingProperty} annotation is a Spring {@link Conditional} used to conditionally enable
 * or disable functionality based on the absence of any declared properties.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Conditional
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMissingPropertyCondition.class)
@SuppressWarnings("unused")
public @interface ConditionalOnMissingProperty {

	/**
	 * The {@link String names} of the properties to test.
	 *
	 * If a {@link String prefix} has been defined, it is applied to compute the full key of each property.
	 *
	 * For instance, if the {@link String prefix} is {@code app.config} and one value is {@code my-value},
	 * the full key would be {@code app.config.my-value}.
	 *
	 * Use dashed notation to specify each property, that is all lower case with a "-" to separate words
	 * (e.g. {@code my-long-property}).
	 *
	 * @return the {@link String names} of the properties to test.
	 * @see #prefix()
	 */
	@AliasFor("value")
	String[] name() default {};

	/**
	 * A {@link String prefix} that should be applied to each property.
	 *
	 * The {@link String prefix} automatically ends with a dot if not specified. A valid {@link String prefix}
	 * is defined by one or more words separated with dots (e.g. {@code "acme.system.feature"}).
	 *
	 * @return the property {@link String prefix}.
	 * @see #name()
	 */
	String prefix() default "";

	/**
	 * Alias for {@link #name()}.
	 *
	 * @return the {@link String names} of the properties to test.
	 * @see #name()
	 */
	@AliasFor("name")
	String[] value() default {};

}
