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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.Test;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * Unit Tests for {@link ObjectAwareUtils}.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.beans.factory.BeanClassLoaderAware
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.geode.core.util.ObjectAwareUtils
 * @since 1.3.1
 */
public class ObjectAwareUtilsUnitTests {

	@Test
	public void initializeApplicationContextAwareObjectWithApplicationContext() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		ApplicationContextAware mockApplicationContextAware = mock(ApplicationContextAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.applicationContextAwareObjectInitializer(mockApplicationContext);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockApplicationContextAware);

		verify(mockApplicationContextAware, times(1))
			.setApplicationContext(eq(mockApplicationContext));
		verifyNoMoreInteractions(mockApplicationContextAware);
		verifyNoInteractions(mockApplicationContext);
	}

	@Test
	public void initializeApplicationContextAwareObjectWithNull() {

		ApplicationContextAware mockApplicationContextAware = mock(ApplicationContextAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.applicationContextAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockApplicationContextAware);

		verifyNoInteractions(mockApplicationContextAware);
	}

	@Test
	public void initializeNonApplicationContextAwareObjectsWithApplicationContext() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.applicationContextAwareObjectInitializer(mockApplicationContext);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("TEST");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockApplicationContext);
	}

	@Test
	public void initializeApplicationEventPublisherAwareObjectWithApplicationEventPublisher() {

		ApplicationEventPublisher mockApplicationEventPublisher = mock(ApplicationEventPublisher.class);

		ApplicationEventPublisherAware mockApplicationEventPublisherAware = mock(ApplicationEventPublisherAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.applicationEventPublisherAwareObjectInitializer(mockApplicationEventPublisher);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockApplicationEventPublisherAware);

		verify(mockApplicationEventPublisherAware, times(1))
			.setApplicationEventPublisher(eq(mockApplicationEventPublisher));
		verifyNoMoreInteractions(mockApplicationEventPublisherAware);
		verifyNoInteractions(mockApplicationEventPublisher);
	}

	@Test
	public void initializeApplicationEventPublisherAwareObjectWithNull() {

		ApplicationEventPublisherAware mockApplicationEventPublisherAware = mock(ApplicationEventPublisherAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.applicationEventPublisherAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockApplicationEventPublisherAware);

		verifyNoInteractions(mockApplicationEventPublisherAware);
	}

	@Test
	public void initializeNonApplicationEventPublisherAwareObjectsWithApplicationEventPublisher() {

		ApplicationEventPublisher mockApplicationEventPublisher = mock(ApplicationEventPublisher.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.applicationEventPublisherAwareObjectInitializer(mockApplicationEventPublisher);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("MOCK");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockApplicationEventPublisher);
	}

	@Test
	public void initializeBeanClassLoaderAwareObjectWithBeanClassLoader() {

		ClassLoader mockClassLoader = mock(ClassLoader.class);

		BeanClassLoaderAware mockBeanClassLoaderAware = mock(BeanClassLoaderAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.beanClassLoaderAwareObjectInitializer(mockClassLoader);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockBeanClassLoaderAware);

		verify(mockBeanClassLoaderAware, times(1)).setBeanClassLoader(eq(mockClassLoader));
		verifyNoMoreInteractions(mockBeanClassLoaderAware);
		verifyNoInteractions(mockClassLoader);
	}

	@Test
	public void initializeBeanClassLoaderAwareObjectWithNull() {

		BeanClassLoaderAware mockBeanClassLoaderAware = mock(BeanClassLoaderAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.beanClassLoaderAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockBeanClassLoaderAware);

		verifyNoInteractions(mockBeanClassLoaderAware);
	}

	@Test
	public void initializeNonBeanClassLoaderAwareObjectsWithBeanClassLoader() {

		ClassLoader mockClassLoader = mock(ClassLoader.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.beanClassLoaderAwareObjectInitializer(mockClassLoader);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("STUB");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockClassLoader);
	}

	@Test
	public void initializeBeanFactoryAwareObjectWithBeanFactory() {

		BeanFactory mockBeanFactory = mock(BeanFactory.class);

		BeanFactoryAware mockBeanFactoryAware = mock(BeanFactoryAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanFactoryAwareObjectInitializer(mockBeanFactory);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockBeanFactoryAware);

		verify(mockBeanFactoryAware, times(1)).setBeanFactory(eq(mockBeanFactory));
		verifyNoMoreInteractions(mockBeanFactoryAware);
		verifyNoInteractions(mockBeanFactory);
	}

	@Test
	public void initializeBeanFactoryAwareObjectWithNull() {

		BeanFactoryAware mockBeanFactoryAware = mock(BeanFactoryAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanFactoryAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockBeanFactoryAware);

		verifyNoInteractions(mockBeanFactoryAware);
	}

	@Test
	public void initializeNonBeanFactoryAwareObjectsWithBeanFactory() {

		BeanFactory mockBeanFactory = mock(BeanFactory.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanFactoryAwareObjectInitializer(mockBeanFactory);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("MOCK");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockBeanFactory);
	}

	@Test
	public void initializeBeanNameAwareObjectWithBeanName() {

		String beanName = "TestBeanName";

		BeanNameAware mockBeanNameAware = mock(BeanNameAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanNameAwareObjectInitializer(beanName);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockBeanNameAware);

		verify(mockBeanNameAware, times(1)).setBeanName(eq(beanName));
		verifyNoMoreInteractions(mockBeanNameAware);
	}

	public void testInitializesBeanNameAwareObjectWithBeanName(String beanName) {

		BeanNameAware mockBeanNameAware = mock(BeanNameAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanNameAwareObjectInitializer(beanName);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockBeanNameAware);

		verifyNoMoreInteractions(mockBeanNameAware);
	}

	@Test
	public void initializesBeanNameAwareObjectWithBlankBeanName() {
		testInitializesBeanNameAwareObjectWithBeanName("  ");
	}

	@Test
	public void initializesBeanNameAwareObjectWithEmptyBeanName() {
		testInitializesBeanNameAwareObjectWithBeanName("");
	}

	@Test
	public void initializesBeanNameAwareObjectWithNullBeanName() {
		testInitializesBeanNameAwareObjectWithBeanName(null);
	}

	@Test
	public void initializeNonBeanNameAwareObjectsWithBeanName() {

		String beanName = "TestBeanName";

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.beanNameAwareObjectInitializer(beanName);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("MOCK");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);
	}

	@Test
	public void initializeEnvironmentAwareObjectWithEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		EnvironmentAware mockEnvironmentAware = mock(EnvironmentAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.environmentAwareObjectInitializer(mockEnvironment);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockEnvironmentAware);

		verify(mockEnvironmentAware, times(1)).setEnvironment(eq(mockEnvironment));
		verifyNoMoreInteractions(mockEnvironmentAware);
		verifyNoInteractions(mockEnvironment);
	}

	@Test
	public void initializeEnvironmentAwareObjectWithNull() {

		EnvironmentAware mockEnvironmentAware = mock(EnvironmentAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.environmentAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockEnvironmentAware);

		verifyNoInteractions(mockEnvironmentAware);
	}

	@Test
	public void initializeNonEnvironmentAwareObjectsWithEnvironment() {

		Environment mockEnvironment = mock(Environment.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.environmentAwareObjectInitializer(mockEnvironment);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("MOCK");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockEnvironment);
	}

	@Test
	public void initializeResourceLoaderAwareObjectWithResourceLoader() {

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		ResourceLoaderAware mockResourceLoaderAware = mock(ResourceLoaderAware.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.resourceLoaderAwareObjectInitializer(mockResourceLoader);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(mockResourceLoaderAware);

		verify(mockResourceLoaderAware, times(1)).setResourceLoader(eq(mockResourceLoader));
		verifyNoMoreInteractions(mockResourceLoaderAware);
		verifyNoInteractions(mockResourceLoader);
	}

	@Test
	public void initializeResourceLoaderAwareObjectWithNull() {

		ResourceLoaderAware mockResourceLoaderAware = mock(ResourceLoaderAware.class);

		Consumer<Object> objectAwareInitializer = ObjectAwareUtils.resourceLoaderAwareObjectInitializer(null);

		assertThat(objectAwareInitializer).isSameAs(ObjectAwareUtils.NO_OP);

		objectAwareInitializer.accept(mockResourceLoaderAware);

		verifyNoInteractions(mockResourceLoaderAware);
	}

	@Test
	public void initializeNonResourceLoaderAwareObjectsWithResourceLoader() {

		ResourceLoader mockResourceLoader = mock(ResourceLoader.class);

		Consumer<Object> objectAwareInitializer =
			ObjectAwareUtils.resourceLoaderAwareObjectInitializer(mockResourceLoader);

		assertThat(objectAwareInitializer).isNotNull();

		objectAwareInitializer.accept(new Object());
		objectAwareInitializer.accept("TEST");
		objectAwareInitializer.accept(1234);
		objectAwareInitializer.accept(null);

		verifyNoInteractions(mockResourceLoader);
	}
}
