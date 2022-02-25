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
package org.springframework.geode.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Unit Tests for {@link SpringExtensions}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.core.util.SpringExtensions
 * @since 1.0.0
 */
public class SpringExtensionsUnitTests {

	private void verifyBeanDefinitionInteractions(BeanDefinition mockBeanDefinition) {

		verify(mockBeanDefinition, times(1)).getBeanClassName();
		verify(mockBeanDefinition, times(1)).getDescription();
		verify(mockBeanDefinition, times(1)).getOriginatingBeanDefinition();
		verify(mockBeanDefinition, times(1)).getParentName();
		verify(mockBeanDefinition, times(1)).getResourceDescription();
		verify(mockBeanDefinition, times(1)).getSource();

		verifyNoMoreInteractions(mockBeanDefinition);
	}

	@Test
	public void getBeanDefinitionMetadataFromApplicationContext() {

		BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		ConfigurableListableBeanFactoryBeanDefinitionRegistry mockBeanDefinitionRegistry =
			mock(ConfigurableListableBeanFactoryBeanDefinitionRegistry.class);

		String beanName = "TestBeanName";

		doReturn(mockBeanDefinitionRegistry).when(mockApplicationContext).getBeanFactory();
		doReturn(mockBeanDefinition).when(mockBeanDefinitionRegistry).getBeanDefinition(eq(beanName));

		String actualJson = SpringExtensions.getBeanDefinitionMetadata(beanName, mockApplicationContext);

		assertThat(actualJson).isNotEmpty();
		assertThat(actualJson).contains(beanName);

		verifyBeanDefinitionInteractions(mockBeanDefinition);
	}

	@Test
	public void getBeanDefinitionMetadataFromNullApplicationContextReturnsEmptyJsonObject() {

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", (ApplicationContext) null))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);
	}

	@Test
	public void getBeanDefinitionMetadataFromNonConfigurableApplicationContextReturnsEmptyJsonObject() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", mockApplicationContext))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);

		verifyNoInteractions(mockApplicationContext);
	}

	@Test
	public void getBeanDefinitionMetadataFromApplicationContextWithNullBeanFactoryReturnsEmptyJsonObject() {

		ConfigurableApplicationContext mockApplicationContext = mock(ConfigurableApplicationContext.class);

		doReturn(null).when(mockApplicationContext).getBeanFactory();

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", mockApplicationContext))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);

		verify(mockApplicationContext, times(1)).getBeanFactory();

		verifyNoMoreInteractions(mockApplicationContext);
	}

	@Test
	public void getBeanDefinitionMetadataFromNullBeanFactoryReturnsEmptyJsonObject() {

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", (BeanFactory) null))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);
	}

	@Test
	public void getBeanDefinitionMetadataFromNullBeanDefinitionRegistryReturnsEmptyJsonObject() {

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", (BeanDefinitionRegistry) null))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);
	}

	private void testGetBeanDefinitionMetadataFromInvalidBeanNameReturnsEmptyJsonObject(String beanName) {

		BeanDefinitionRegistry mockBeanDefinitionRegistry = mock(BeanDefinitionRegistry.class);

		assertThat(SpringExtensions.getBeanDefinitionMetadata(beanName, mockBeanDefinitionRegistry))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);

		verifyNoInteractions(mockBeanDefinitionRegistry);
	}

	@Test
	public void getBeanDefinitionMetadataFromBlankBeanNameReturnsEmptyJsonObject() {
		testGetBeanDefinitionMetadataFromInvalidBeanNameReturnsEmptyJsonObject("  ");
	}

	@Test
	public void getBeanDefinitionMetadataFromEmptyBeanNameReturnsEmptyJsonObject() {
		testGetBeanDefinitionMetadataFromInvalidBeanNameReturnsEmptyJsonObject("");
	}

	@Test
	public void getBeanDefinitionMetadataFromNullBeanNameReturnsEmptyJsonObject() {
		testGetBeanDefinitionMetadataFromInvalidBeanNameReturnsEmptyJsonObject(null);
	}

	@Test
	public void getBeanDefinitionMetadataFromBeanDefinitionRegistryReturningNullBeanDefinitionReturnsEmptyJsonObject() {

		BeanDefinitionRegistry mockBeanDefinitionRegistry = mock(BeanDefinitionRegistry.class);

		doReturn(null).when(mockBeanDefinitionRegistry).getBeanDefinition(any());

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", mockBeanDefinitionRegistry))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);

		verify(mockBeanDefinitionRegistry, times(1)).getBeanDefinition(eq("TestBeanName"));

		verifyNoMoreInteractions(mockBeanDefinitionRegistry);
	}

	@Test
	public void getBeanDefinitionMetadataForBeanDefinitionReturnsJson() {

		BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
		BeanDefinition mockOriginatingBeanDefinition = mock(BeanDefinition.class);

		Object testSource = new Object();

		doReturn("test.bean.ClassName").when(mockBeanDefinition).getBeanClassName();
		doReturn("TestDescription").when(mockBeanDefinition).getDescription();
		doReturn(mockOriginatingBeanDefinition).when(mockBeanDefinition).getOriginatingBeanDefinition();
		doReturn("TestParentName").when(mockBeanDefinition).getParentName();
		doReturn("TestResourceDescription").when(mockBeanDefinition).getResourceDescription();
		doReturn(testSource).when(mockBeanDefinition).getSource();

		String expectedJson = String.format(SpringExtensions.BEAN_DEFINITION_METADATA_JSON,
			"TestBeanName", "test.bean.ClassName", "TestDescription", mockOriginatingBeanDefinition, "TestParentName",
			"TestResourceDescription", testSource);

		String actualJson = SpringExtensions.getBeanDefinitionMetadata("TestBeanName", mockBeanDefinition);

		assertThat(actualJson).isEqualTo(expectedJson);

		verifyBeanDefinitionInteractions(mockBeanDefinition);
		verifyNoInteractions(mockOriginatingBeanDefinition);
	}

	@Test
	public void getBeanDefinitionMetadataForNullBeanDefinitionReturnsEmptyJsonObject() {

		assertThat(SpringExtensions.getBeanDefinitionMetadata("TestBeanName", (BeanDefinition) null))
			.isEqualTo(SpringExtensions.EMPTY_JSON_OBJECT);
	}

	private interface ConfigurableListableBeanFactoryBeanDefinitionRegistry
		extends BeanDefinitionRegistry, ConfigurableListableBeanFactory { }

}
