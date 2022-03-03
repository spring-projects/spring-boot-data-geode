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
package org.springframework.geode.logging.slf4j.logback;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.helpers.NOPAppender;

/**
 * {@link DelegatingAppender} is an SLF4J {@link Appender} that delegates to the configured {@link Appender}.
 *
 * If no {@link Appender} was configured, then the {@link DelegatingAppender} delegates to the {@link NOPAppender}.
 *
 * @author John Blum
 * @see ch.qos.logback.core.Appender
 * @see ch.qos.logback.core.AppenderBase
 * @see ch.qos.logback.core.helpers.NOPAppender
 * @since 1.3.0
 */
public class DelegatingAppender<T> extends AppenderBase<T> {

	@SuppressWarnings("rawtypes")
	protected static final Appender DEFAULT_APPENDER = new NOPAppender<>();

	protected static final String DEFAULT_NAME = "delegate";

	public DelegatingAppender() {

		Optional.ofNullable(LoggerFactory.getILoggerFactory())
			.filter(it -> Objects.isNull(DEFAULT_APPENDER.getContext()))
			.filter(Context.class::isInstance)
			.map(Context.class::cast)
			.ifPresent(DEFAULT_APPENDER::setContext);

		this.name = DEFAULT_NAME;
	}

	private volatile Appender<T> appender;

	public void setAppender(Appender<T> appender) {
		this.appender = appender;
	}

	@SuppressWarnings("unchecked")
	protected Appender<T> getAppender() {
		return Optional.ofNullable(this.appender).orElse(DEFAULT_APPENDER);
	}

	@Override
	protected void append(T eventObject) {
		getAppender().doAppend(eventObject);
	}
}
