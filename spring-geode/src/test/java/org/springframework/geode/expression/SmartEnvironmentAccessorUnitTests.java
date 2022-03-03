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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;

/**
 * Unit Tests for {@link SmartEnvironmentAccessor}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.EnvironmentCapable
 * @see org.springframework.geode.expression.SmartEnvironmentAccessor
 * @since 1.3.1
 */
public class SmartEnvironmentAccessorUnitTests {

	@Test
	public void createConstructsNewSmartEnvironmentAccessor() {

		SmartEnvironmentAccessor environmentAccessor = SmartEnvironmentAccessor.create();

		assertThat(environmentAccessor).isNotNull();
	}

	@Test
	public void specificTargetClassesIncludeEnvironmentAndEnvironmentCapableOnly() {
		assertThat(new SmartEnvironmentAccessor().getSpecificTargetClasses())
			.containsExactly(Environment.class, EnvironmentCapable.class);
	}

	@Test
	public void canReadFromEnvironmentContainingPropertyReturnsTrue() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn(true).when(mockEnvironment).containsProperty(anyString());

		assertThat(new SmartEnvironmentAccessor()
			.canRead(mockEvaluationContext, mockEnvironment, "testKey")).isTrue();

		verify(mockEnvironment, times(1)).containsProperty(eq("testKey"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void canReadFromEnvironmentCapableObjectContainingPropertyReturnsTrue() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentCapable mockEnvironmentCapable = mock(EnvironmentCapable.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn(mockEnvironment).when(mockEnvironmentCapable).getEnvironment();
		doReturn(true).when(mockEnvironment).containsProperty(anyString());

		assertThat(new SmartEnvironmentAccessor()
			.canRead(mockEvaluationContext, mockEnvironmentCapable, "testKey")).isTrue();

		verify(mockEnvironmentCapable, times(1)).getEnvironment();
		verify(mockEnvironment, times(1)).containsProperty(eq("testKey"));
		verifyNoMoreInteractions(mockEnvironment, mockEnvironmentCapable);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void canReadFromEnvironmentNotContainingPropertyReturnsFalse() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn(false).when(mockEnvironment).containsProperty(any());

		assertThat(new SmartEnvironmentAccessor()
			.canRead(mockEvaluationContext, mockEnvironment, "testKey")).isFalse();

		verify(mockEnvironment, times(1)).containsProperty(eq("testKey"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void canReadFromNonEnvironmentReturnsFalse() {

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		assertThat(new SmartEnvironmentAccessor()
			.canRead(mockEvaluationContext, new Object(), "testKey")).isFalse();

		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void canReadFromNullTargetObjectIsNullSafeReturnsFalse() {

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		assertThat(new SmartEnvironmentAccessor()
			.canRead(mockEvaluationContext, null, "testKey")).isFalse();

		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void readFromEnvironmentContainingPropertyReturnsValue() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn("test").when(mockEnvironment).getProperty(eq("key"));

		TypedValue typedValue = new SmartEnvironmentAccessor()
			.read(mockEvaluationContext, mockEnvironment, "key");

		assertThat(typedValue).isNotNull();
		assertThat(typedValue.getValue()).isEqualTo("test");

		verify(mockEnvironment, times(1)).getProperty(eq("key"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void readFromEnvironmentCapableObjectContainingPropertyReturnsValue() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentCapable mockEnvironmentCapable = mock(EnvironmentCapable.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn(mockEnvironment).when(mockEnvironmentCapable).getEnvironment();
		doReturn("test").when(mockEnvironment).getProperty(eq("key"));

		TypedValue typedValue = new SmartEnvironmentAccessor()
			.read(mockEvaluationContext, mockEnvironmentCapable, "key");

		assertThat(typedValue).isNotNull();
		assertThat(typedValue.getValue()).isEqualTo("test");

		verify(mockEnvironmentCapable, times(1)).getEnvironment();
		verify(mockEnvironment, times(1)).getProperty(eq("key"));
		verifyNoMoreInteractions(mockEnvironment, mockEnvironmentCapable);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void readFromEnvironmentNotContainingPropertyReturnsNull() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		doReturn(null).when(mockEnvironment).getProperty(any());

		TypedValue typedValue = new SmartEnvironmentAccessor()
			.read(mockEvaluationContext, mockEnvironment, "key");

		assertThat(typedValue).isNotNull();
		assertThat(typedValue.getValue()).isNull();

		verify(mockEnvironment, times(1)).getProperty(eq("key"));
		verifyNoMoreInteractions(mockEnvironment);
		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void readFromNonEnvironmentReturnsNull() {

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		TypedValue typedValue = new SmartEnvironmentAccessor().read(mockEvaluationContext, new Object(), "key");

		assertThat(typedValue).isNotNull();
		assertThat(typedValue.getValue()).isNull();

		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void readFromNullTargetObjectIsNullSafeReturnsNull() {

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		TypedValue typedValue = new SmartEnvironmentAccessor().read(mockEvaluationContext, null, "key");

		assertThat(typedValue).isNotNull();
		assertThat(typedValue.getValue()).isNull();

		verifyNoInteractions(mockEvaluationContext);
	}

	@Test
	public void canWriteReturnsFalse() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		assertThat(new SmartEnvironmentAccessor()
			.canWrite(mockEvaluationContext, mockEnvironment, "testKey")).isFalse();

		verifyNoInteractions(mockEnvironment, mockEvaluationContext);
	}

	@Test
	public void writeDoesNothing() {

		Environment mockEnvironment = mock(Environment.class);

		EvaluationContext mockEvaluationContext = mock(EvaluationContext.class);

		new SmartEnvironmentAccessor()
			.write(mockEvaluationContext, mockEnvironment, "testKey", "testValue");

		verifyNoInteractions(mockEnvironment, mockEvaluationContext);
	}
}
