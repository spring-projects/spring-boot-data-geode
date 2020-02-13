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

import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;

/**
 * {@link StringAppender} is a {@link Appender} implementation that captures all log events/statements in-memory
 * appended to a {@link String} using optionally either a builder or a buffer.
 *
 * @author John Blum
 * @see java.lang.StringBuilder
 * @see java.lang.StringBuffer
 * @see ch.qos.logback.core.Appender
 * @see ch.qos.logback.core.AppenderBase
 * @see ch.qos.logback.core.Context
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class StringAppender extends AppenderBase<ILoggingEvent> {

	protected static final String DEFAULT_NAME = "string";
	protected static final String NEWLINE = "\n";

	@FunctionalInterface
	interface StringAppenderWrapper {
		void append(CharSequence charSequence);
	}

	protected static final StringAppenderWrapper stringBuilderAppenderWrapper = new StringAppenderWrapper() {

		private final StringBuilder stringBuilder = new StringBuilder();

		@Override
		public void append(CharSequence charSequence) {
			this.stringBuilder.append(charSequence);
			this.stringBuilder.append(NEWLINE);
		}

		@Override
		public java.lang.String toString() {
			return this.stringBuilder.toString();
		}
	};

	protected static final StringAppenderWrapper stringBufferAppenderWrapper = new StringAppenderWrapper() {

		private final StringBuffer stringBuffer = new StringBuffer();

		@Override
		public void append(CharSequence charSequence) {
			this.stringBuffer.append(charSequence);
			this.stringBuffer.append(NEWLINE);
		}

		@Override
		public java.lang.String toString() {
			return this.stringBuffer.toString();
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static class Builder {

		private static final boolean DEFAULT_REPLACE = false;

		private boolean replace = false;
		private boolean useSynchronization = false;

		private Context context;

		private DelegatingAppender delegate;

		private ch.qos.logback.classic.Logger logger;

		private String name;

		public Builder applyTo(DelegatingAppender<?> delegate) {
			return applyTo(delegate, DEFAULT_REPLACE);
		}

		public Builder applyTo(DelegatingAppender<?> delegate, boolean replace) {
			this.delegate = delegate;
			this.replace = replace;
			return this;
		}

		public Builder applyTo(ch.qos.logback.classic.Logger logger) {
			this.logger = logger;
			return this;
		}

		public Builder setContext(Context context) {
			this.context = context;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder useSynchronization() {
			this.useSynchronization = true;
			return this;
		}

		private Optional<DelegatingAppender> getDelegate() {
			return Optional.ofNullable(this.delegate);
		}

		private Optional<ch.qos.logback.classic.Logger> getLogger() {
			return Optional.ofNullable(this.logger);
		}

		private Context resolveContext() {

			return this.context != null
				? this.context
				: Optional.ofNullable(LoggerFactory.getILoggerFactory())
					.filter(Context.class::isInstance)
					.map(Context.class::cast)
					.orElse(null);
		}

		private String resolveName() {
			return this.name != null && !this.name.trim().isEmpty() ? this.name : DEFAULT_NAME;
		}

		private StringAppenderWrapper resolveStringAppenderWrapper() {
			return this.useSynchronization ? stringBufferAppenderWrapper : stringBuilderAppenderWrapper;
		}

		public StringAppender build() {

			StringAppender stringAppender =
				new StringAppender(resolveStringAppenderWrapper());

			stringAppender.setContext(resolveContext());
			stringAppender.setName(resolveName());

			getDelegate().ifPresent(delegate -> {

				Appender appender = this.replace ? stringAppender
					: CompositeAppender.compose(delegate.getAppender(), stringAppender);

				delegate.setAppender(appender);
			});

			getLogger().ifPresent(logger -> logger.addAppender(stringAppender));

			return stringAppender;
		}

		public StringAppender buildAndStart() {

			StringAppender stringAppender = build();

			stringAppender.start();

			return stringAppender;
		}
	}

	private final StringAppenderWrapper stringAppenderWrapper;

	protected StringAppender(StringAppenderWrapper stringAppenderWrapper) {

		if (stringAppenderWrapper == null) {
			throw new IllegalArgumentException("StringAppenderWrapper must not be null");
		}

		this.stringAppenderWrapper = stringAppenderWrapper;
	}

	public String getLogOutput() {
		return getStringAppenderWrapper().toString();
	}

	protected StringAppenderWrapper getStringAppenderWrapper() {
		return this.stringAppenderWrapper;
	}

	@Override
	protected void append(ILoggingEvent loggingEvent) {

		Optional.ofNullable(loggingEvent)
			.map(event -> preProcessLogMessage(toString(event)))
			.filter(this::isValidLogMessage)
			.ifPresent(getStringAppenderWrapper()::append);
	}

	protected boolean isValidLogMessage(String message) {
		return message != null && !message.isEmpty();
	}

	protected String preProcessLogMessage(String message) {
		return message != null ? message.trim() : null;
	}

	protected String toString(ILoggingEvent loggingEvent) {
		return loggingEvent != null ? loggingEvent.getFormattedMessage() : null;
	}
}
