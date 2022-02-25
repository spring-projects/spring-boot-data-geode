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
package org.springframework.geode.expression;

import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Spring {@link PropertyAccessor} implementation that knows how to access properties
 * from {@link Environment} and {@link EnvironmentCapable} objects.
 *
 * @author John Blum
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.EnvironmentCapable
 * @see org.springframework.expression.PropertyAccessor
 * @since 1.3.1
 */
public class SmartEnvironmentAccessor implements PropertyAccessor {

	/**
	 * Factory method used to construct a new instance of {@link SmartEnvironmentAccessor}.
	 *
	 * @return a new instance of {@link SmartEnvironmentAccessor}.
	 */
	public static @NonNull SmartEnvironmentAccessor create() {
		return new SmartEnvironmentAccessor();
	}

	/**
	 * @inheritDoc
	 */
	@Nullable @Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class[] { Environment.class, EnvironmentCapable.class };
	}

	private Optional<Environment> asEnvironment(@Nullable Object target) {

		Environment environment = target instanceof Environment ? (Environment) target
			: target instanceof EnvironmentCapable ? ((EnvironmentCapable) target).getEnvironment()
			: null;

		return Optional.ofNullable(environment);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean canRead(EvaluationContext context, @Nullable Object target, String name) {

		return asEnvironment(target)
			.filter(environment -> environment.containsProperty(name))
			.isPresent();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public TypedValue read(EvaluationContext context, @Nullable Object target, String name) {

		String value = asEnvironment(target)
			.map(environment -> environment.getProperty(name))
			.orElse(null);

		return new TypedValue(value);
	}

	/**
	 * @inheritDoc
	 * @return {@literal false}.
	 */
	@Override
	public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) {
		return false;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object newValue) { }

}
