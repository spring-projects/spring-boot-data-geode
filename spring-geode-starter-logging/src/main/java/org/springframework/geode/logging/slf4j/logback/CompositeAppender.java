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
package org.springframework.geode.logging.slf4j.logback;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;

/**
 * {@link CompositeAppender} is an {@link Appender} implementation implementing
 * the <a href="https://en.wikipedia.org/wiki/Composite_pattern">Composite Software Design Pattern</a>
 *
 * The {@literal Composite Software Design Pattern} enables two or more {@link Appender} objects to be composed
 * and treated as a single instance of {@link Appender}.
 *
 * @author John Blum
 * @see ch.qos.logback.core.Appender
 * @see ch.qos.logback.core.AppenderBase
 * @since 1.3.0
 */
public class CompositeAppender<T> extends AppenderBase<T> {

	protected static final String DEFAULT_NAME = "composite";

	private final Appender<T> one;
	private final Appender<T> two;

	/**
	 * Factory method used to compose two {@link Appender} objects into a {@literal Composite} {@link Appender}.
	 *
	 * @param <T> {@link Class type} of {@link Appender} to compose.
	 * @param one first {@link Appender} to compose.
	 * @param two second {@link Appender} to compose.
	 * @return {@link Appender} one if {@link Appender} two is {@literal null};
	 * Return {@link Appender} two if {@link Appender} one is {@literal null}.
	 * Otherwise, return a {@literal Composite} {@link Appender} composed of {@link Appender} one
	 * and {@link Appender} two.
	 * @see ch.qos.logback.core.Appender
	 */
	public static <T> Appender<T> compose(Appender<T> one, Appender<T> two) {
		return one == null ? two : two == null ? one : new CompositeAppender<>(one, two);
	}

	/**
	 * Constructs a new instance of {@link CompositeAppender} composed of {@link Appender} one and {@link Appender} two.
	 *
	 * @param one first {@link Appender} in the composite.
	 * @param two second {@link Appender} in the composite.
	 * @see ch.qos.logback.core.Appender
	 */
	private CompositeAppender(Appender<T> one, Appender<T> two) {

		this.one = one;
		this.two = two;
		this.name = DEFAULT_NAME;
		this.started = true;
	}

	protected Appender<T> getAppenderOne() {
		return this.one;
	}

	protected Appender<T> getAppenderTwo() {
		return this.two;
	}

	@Override
	public void setContext(Context context) {

		super.setContext(context);

		getAppenderOne().setContext(context);
		getAppenderTwo().setContext(context);
	}

	@Override
	public Context getContext() {

		Context context = super.getContext();

		context = context != null ? context : getAppenderOne().getContext();
		context = context != null ? context : getAppenderTwo().getContext();

		return context;
	}

	@Override
	protected void append(T eventObject) {
		getAppenderOne().doAppend(eventObject);
		getAppenderTwo().doAppend(eventObject);
	}
}
