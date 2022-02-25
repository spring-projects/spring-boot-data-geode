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
package org.springframework.geode.core.env;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.geode.core.env.EnvironmentMapAdapter.EnvironmentEntry;

/**
 * Unit Tests for {@link EnvironmentMapAdapter}.
 *
 * @author John Blum
 * @see java.util.Map
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.core.env.EnumerablePropertySource
 * @see org.springframework.core.env.Environment
 * @see org.springframework.geode.core.env.EnvironmentMapAdapter
 * @since 1.3.1
 */
public class EnvironmentMapAdapterUnitTests {

	@Test
	public void constructNewEnvironmentMapAdapter() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentMapAdapter adapter = new EnvironmentMapAdapter(mockEnvironment);

		assertThat(adapter).isNotNull();
		assertThat(adapter.getEnvironment()).isEqualTo(mockEnvironment);

		verifyNoInteractions(mockEnvironment);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructNewEnvironmentMapAdapterWithNullEnvironment() {

		try {
			new EnvironmentMapAdapter(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Environment must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void fromEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentMapAdapter adapter = EnvironmentMapAdapter.from(mockEnvironment);

		assertThat(adapter).isNotNull();
		assertThat(adapter.getEnvironment()).isEqualTo(mockEnvironment);

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	@SuppressWarnings("all")
	public void containsExistingKeyReturnsTrue() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(true).when(mockEnvironment).containsProperty("1");

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).containsKey(1)).isTrue();

		verify(mockEnvironment, times(1)).containsProperty(eq("1"));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void containsNonExistingKeyReturnsFalse() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(false).when(mockEnvironment).containsProperty(any());

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).containsKey("test")).isFalse();

		verify(mockEnvironment, times(1)).containsProperty(eq("test"));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void containsNullKeyIsNullSafeReturnsFalse() {

		Environment mockEnvironment = mock(Environment.class);

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).containsKey(null)).isFalse();

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	@SuppressWarnings("all")
	public void getExistingKeyReturnsEnvironmentPropertyValue() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn("test").when(mockEnvironment).getProperty(eq("1"));

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).get(1)).isEqualTo("test");

		verify(mockEnvironment, times(1)).getProperty(eq("1"));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void getNonExistingKeyReturnsNull() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn(null).when(mockEnvironment).getProperty(any());

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).get("test")).isNull();

		verify(mockEnvironment, times(1)).getProperty(eq("test"));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test
	public void getNullKeyIsNullSafeReturnsNull() {

		Environment mockEnvironment = mock(Environment.class);

		assertThat(EnvironmentMapAdapter.from(mockEnvironment).get(null)).isNull();

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	@SuppressWarnings("all")
	public void entrySetFromConfigurableEnvironment() {

		ConfigurableEnvironment mockEnvironment = mock(ConfigurableEnvironment.class);

		EnumerablePropertySource<?> mockPropertySourceOne = mock(EnumerablePropertySource.class, "PropertySourceOne");
		EnumerablePropertySource<?> mockPropertySourceTwo = mock(EnumerablePropertySource.class, "PropertySourceTwo");

		Map<String, String> map = MapBuilder.<String, String>newMapBuilder()
			.put("propertyNameOne", "one")
			.put("propertyNameTwo", "two")
			.put("propertyNameThree", "three")
			.build();

		MutablePropertySources mutablePropertySources = new MutablePropertySources();

		mutablePropertySources.addLast(mockPropertySourceOne);
		mutablePropertySources.addLast(mockPropertySourceTwo);

		doAnswer(invocation -> map.get(invocation.getArgument(0))).when(mockEnvironment).getProperty(any());
		doReturn(mutablePropertySources).when(mockEnvironment).getPropertySources();
		doReturn(ArrayUtils.asArray("propertyNameOne")).when(mockPropertySourceOne).getPropertyNames();
		doReturn(ArrayUtils.asArray("propertyNameTwo", "propertyNameThree"))
			.when(mockPropertySourceTwo).getPropertyNames();

		EnvironmentMapAdapter adapter = EnvironmentMapAdapter.from(mockEnvironment);

		assertThat(adapter).isNotNull();
		assertThat(adapter.getEnvironment()).isEqualTo(mockEnvironment);

		Set<Map.Entry<String, String>> entrySet = adapter.entrySet();

		assertThat(entrySet).isNotNull();
		assertThat(entrySet).hasSize(3);
		assertThat(entrySet.stream().map(Map.Entry::getKey).sorted().collect(Collectors.toList()))
			.containsExactly("propertyNameOne", "propertyNameThree", "propertyNameTwo");

		entrySet.forEach(entry -> assertThat(entry.getValue()).isEqualTo(map.get(entry.getKey())));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void entrySetForNonConfigurableEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		try {
			EnvironmentMapAdapter.from(mockEnvironment).entrySet();
		}
		catch (UnsupportedOperationException expected) {

			assertThat(expected).hasMessage("Unable to determine the entrySet from the Environment [%s]",
				mockEnvironment.getClass().getName());

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockEnvironment);
		}
	}

	@Test
	public void constructNewEnvironmentEntry() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentEntry entry = new EnvironmentEntry(mockEnvironment, "testKey");

		assertThat(entry).isNotNull();
		assertThat(entry.getEnvironment()).isEqualTo(mockEnvironment);
		assertThat(entry.getKey()).isEqualTo("testKey");

		verifyNoInteractions(mockEnvironment);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructNewEnvironmentEntryWithNullEnvironment() {

		try {
			new EnvironmentEntry(null, "testKey");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Environment must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	public void testConstructNewEnvironmentEntryWithInvalidKey(String key) {

		Environment mockEnvironment = mock(Environment.class);

		try {
			new EnvironmentEntry(mockEnvironment, key);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Key [%s] must be specified", key);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockEnvironment);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructNewEnvironmentEntryWithBlankKey() {
		testConstructNewEnvironmentEntryWithInvalidKey("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructNewEnvironmentEntryWithEmptyKey() {
		testConstructNewEnvironmentEntryWithInvalidKey("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructNewEnvironmentEntryWithNullKey() {
		testConstructNewEnvironmentEntryWithInvalidKey(null);
	}

	@Test
	public void environmentEntryGetValueCallsGetKeyReturnsEnviromentPropertyValue() {

		Environment mockEnvironment = mock(Environment.class);

		doReturn("test").when(mockEnvironment).getProperty(eq("1"));

		EnvironmentEntry entry = new EnvironmentEntry(mockEnvironment, "1");

		assertThat(entry).isNotNull();
		assertThat(entry.getEnvironment()).isEqualTo(mockEnvironment);
		assertThat(entry.getKey()).isEqualTo("1");
		assertThat(entry.getValue()).isEqualTo("test");

		verify(mockEnvironment, times(1)).getProperty(eq("1"));
		verifyNoMoreInteractions(mockEnvironment);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void environmentEntrySetValueThrowsUnsupportedOperationException() {

		Environment mockEnvironment = mock(Environment.class);

		try {
			new EnvironmentEntry(mockEnvironment, "1").setValue("test");
		}
		catch (UnsupportedOperationException expected) {

			assertThat(expected).hasMessage("Setting the value of Environment property [1] is not supported");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			verifyNoInteractions(mockEnvironment);
		}
	}
}
