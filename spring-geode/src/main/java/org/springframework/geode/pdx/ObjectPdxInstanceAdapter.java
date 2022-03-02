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
package org.springframework.geode.pdx;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.geode.pdx.PdxFieldDoesNotExistException;
import org.apache.geode.pdx.PdxFieldTypeMismatchException;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.WritablePdxInstance;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * A {@link PdxInstance} implementation that adapts (wraps) a non-null {@link Object} as a {@link PdxInstance}.
 *
 * @author John Blum
 * @see java.beans.PropertyDescriptor
 * @see java.lang.reflect.Field
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.WritablePdxInstance
 * @see org.springframework.beans.BeanWrapper
 * @see org.springframework.beans.PropertyAccessor
 * @see org.springframework.beans.PropertyAccessorFactory
 * @since 1.3.0
 */
public class ObjectPdxInstanceAdapter implements PdxInstance {

	protected static final String CLASS_PROPERTY_NAME = "class";
	protected static final String ID_PROPERTY_NAME = "id";

	private static void assertCondition(boolean condition, Supplier<RuntimeException> runtimeExceptionSupplier) {
		if (!condition) {
			throw runtimeExceptionSupplier.get();
		}
	}

	/**
	 * Factory method used to construct a new instance of the {@link ObjectPdxInstanceAdapter} from
	 * the given {@literal target} {@link Object}.
	 *
	 * @param target {@link Object} to adapt as a {@link PdxInstance}; must not be {@literal null}.
	 * @return a new instance of {@link ObjectPdxInstanceAdapter}.
	 * @throws IllegalArgumentException if {@link Object} is {@literal null}.
	 * @see #ObjectPdxInstanceAdapter(Object)
	 */
	public static ObjectPdxInstanceAdapter from(@NonNull Object target) {
		return new ObjectPdxInstanceAdapter(target);
	}

	/**
	 * Null-safe factory method used to unwrap the given {@link PdxInstance}, returning the underlying, target
	 * {@link PdxInstance#getObject() Object} upon which this {@link PdxInstance} is based.
	 *
	 * @param pdxInstance {@link PdxInstance} to unwrap.
	 * @return the underlying, target {@link PdxInstance#getObject() Object} from the given {@link PdxInstance}.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	public static @Nullable Object unwrap(@Nullable PdxInstance pdxInstance) {

		return pdxInstance instanceof ObjectPdxInstanceAdapter
			? pdxInstance.getObject()
			: pdxInstance;
	}

	private final AtomicReference<String> resolvedIdentityFieldName = new AtomicReference<>(null);

	private transient final BeanWrapper beanWrapper;

	private final Object target;

	/**
	 * Constructs a new instance of {@link ObjectPdxInstanceAdapter} initialized with the given {@link Object}.
	 *
	 * @param target {@link Object} to adapt as a {@link PdxInstance}; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link Object} is {@literal null}.
	 * @see java.lang.Object
	 */
	public ObjectPdxInstanceAdapter(Object target) {

		Assert.notNull(target, "Object to adapt must not be null");

		this.target = target;
		this.beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
	}

	/**
	 * Returns a {@link BeanWrapper} wrapping the {@literal target} {@link Object} in order to access the {@link Object}
	 * as a Java bean using JavaBeans conventions.
	 *
	 * @return a {@link BeanWrapper} for the {@literal target} {@link Object}; never {@literal null}.
	 * @see org.springframework.beans.BeanWrapper
	 */
	protected @NonNull BeanWrapper getBeanWrapper() {
		return this.beanWrapper;
	}

	/**
	 * Returns the {@link Class#getName()} of the underlying, target {@link Object}.
	 *
	 * @return the {@link Class#getName()} of the underlying, target {@link Object}.
	 * @see java.lang.Object#getClass()
	 * @see java.lang.Class#getName()
	 * @see #getObject()
	 */
	@Override
	public String getClassName() {
		return getObject().getClass().getName();
	}

	/**
	 * Determines whether this {@link PdxInstance} can be deserialized back into an {@link Object}.
	 *
	 * This method effectively returns {@literal true} since this {@link PdxInstance} implementation is an adapter
	 * for an underlying, target {@link Object} in the first place.
	 *
	 * @return a boolean value indicating whether this {@link PdxInstance} can be deserialized
	 * back into an {@link Object}.
	 * @see #getObject()
	 */
	@Override
	public boolean isDeserializable() {
		return getObject() != null;
	}

	/**
	 * Determines whether the underlying, target {@link Object} is an {@link Enum enumerated value} {@link Class type}.
	 *
	 * @return a boolean value indicating whether the underlying, target {@link Object}
	 * is an {@link Enum enumerated value} {@link Class type}.
	 * @see java.lang.Object#getClass()
	 * @see java.lang.Class#isEnum()
	 * @see #getObject()
	 */
	@Override
	public boolean isEnum() {
		return getObject().getClass().isEnum();
	}

	/**
	 * Returns the {@link Object value} for the {@link PropertyDescriptor property} identified by
	 * the given {@link String field name} on the underlying, target {@link Object}.
	 *
	 * @param fieldName {@link String} containing the name of the field to get the {@link Object value} for.
	 * @return the {@link Object value} for the {@link PropertyDescriptor property} identified by
	 * the given {@link String field name} on the underlying, target {@link Object}.
	 * @see org.springframework.beans.BeanWrapper#getPropertyValue(String)
	 * @see #getBeanWrapper()
	 */
	@Override
	public Object getField(String fieldName) {

		BeanWrapper beanWrapper = getBeanWrapper();

		return beanWrapper.isReadableProperty(fieldName)
			? beanWrapper.getPropertyValue(fieldName)
			: null;
	}

	/**
	 * Returns a {@link List} of {@link String field names} based on the {@link PropertyDescriptor propeties}
	 * from the underlying, target {@link Object}.
	 *
	 * @return a {@link List} of {@link String field names} / {@link PropertyDescriptor properties} serialized
	 * in the PDX bytes for the underlying, target {@link Object}.
	 * @see org.springframework.beans.BeanWrapper#getPropertyDescriptors()
	 * @see java.beans.PropertyDescriptor
	 * @see #getBeanWrapper()
	 */
	@Override
	public List<String> getFieldNames() {

		PropertyDescriptor[] propertyDescriptors =
			ArrayUtils.nullSafeArray(getBeanWrapper().getPropertyDescriptors(), PropertyDescriptor.class);

		return Arrays.stream(propertyDescriptors)
			.map(PropertyDescriptor::getName)
			.filter(propertyName -> !CLASS_PROPERTY_NAME.equals(propertyName))
			.collect(Collectors.toList());
	}

	/**
	 * Determines whether the given {@link String field name} is an identifier for this {@link PdxInstance}.
	 *
	 * @param fieldName {@link String} containing the name of the field to evaluate.
	 * @return a boolean value indicating whether the given {@link String field name} is an identifier for
	 * this {@link PdxInstance}.
	 * @see #resolveIdentityFieldNameFromProperty(BeanWrapper)
	 */
	@Override
	public boolean isIdentityField(String fieldName) {

		String resolvedIdentityFieldName = this.resolvedIdentityFieldName.updateAndGet(it ->
			StringUtils.hasText(it) ? it : resolveIdentityFieldNameFromProperty());

		return StringUtils.hasText(resolvedIdentityFieldName) && resolvedIdentityFieldName.equals(fieldName);
	}

	// Identifier Search Algorithm: @Id Property -> @Id Field -> "id" Property

	@Nullable String resolveIdentityFieldNameFromProperty() {
		return resolveIdentityFieldNameFromProperty(getBeanWrapper());
	}

	private @Nullable String resolveIdentityFieldNameFromProperty(@NonNull BeanWrapper beanWrapper) {

		List<PropertyDescriptor> properties =
			Arrays.asList(ArrayUtils.nullSafeArray(beanWrapper.getPropertyDescriptors(), PropertyDescriptor.class));

		Optional<PropertyDescriptor> atIdAnnotatedProperty = properties.stream()
			.filter(this::isAtIdAnnotatedProperty)
			.findFirst();

		return atIdAnnotatedProperty
			.map(PropertyDescriptor::getName)
			.orElseGet(() -> resolveIdentityFieldNameFromField(beanWrapper));
	}

	private boolean isAtIdAnnotatedProperty(@Nullable PropertyDescriptor propertyDescriptor) {

		return Optional.ofNullable(propertyDescriptor)
			.map(PropertyDescriptor::getReadMethod)
			.map(method -> AnnotationUtils.findAnnotation(method, Id.class))
			.isPresent();
	}

	private @Nullable String resolveIdentityFieldNameFromField(@NonNull BeanWrapper beanWrapper) {

		List<Field> fields =
			Arrays.asList(ArrayUtils.nullSafeArray(beanWrapper.getWrappedClass().getDeclaredFields(), Field.class));

		Optional<PropertyDescriptor> atIdAnnotatedProperty = fields.stream()
			.map(field -> getPropertyForAtIdAnnotatedField(beanWrapper, field))
			.filter(Objects::nonNull)
			.findFirst();

		return atIdAnnotatedProperty
			.map(PropertyDescriptor::getName)
			.orElseGet(() -> beanWrapper.isReadableProperty(ID_PROPERTY_NAME)
				? ID_PROPERTY_NAME
				: null);
	}

	private @Nullable PropertyDescriptor getPropertyForAtIdAnnotatedField(@NonNull BeanWrapper beanWrapper,
			@Nullable Field field) {

		return Optional.ofNullable(field)
			.filter(it -> beanWrapper.isReadableProperty(it.getName()))
			.filter(it -> Objects.nonNull(AnnotationUtils.findAnnotation(it, Id.class)))
			.map(it -> beanWrapper.getPropertyDescriptor(it.getName()))
			.orElse(null);
	}

	/**
	 * Returns the {@literal target} {@link Object} being adapted by this {@link PdxInstance}.
	 *
	 * @return the {@literal target} {@link Object} being adapted by this {@link PdxInstance}; never {@literal null}.
	 * @see java.lang.Object
	 */
	@Override
	public Object getObject() {
		return this.target;
	}

	ObjectPdxInstanceAdapter getParent() {
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public WritablePdxInstance createWriter() {

		return new WritablePdxInstance() {

			@Override
			public void setField(String fieldName, Object value) {
				withPropertyAccessorFor(fieldName, value).setPropertyValue(fieldName, value);
			}

			private PropertyAccessor withPropertyAccessorFor(String fieldName, Object value) {

				assertFieldIsPresent(fieldName);

				BeanWrapper beanWrapper = getBeanWrapper();

				assertFieldIsWritable(beanWrapper, fieldName);
				assertValueIsTypeMatch(beanWrapper, fieldName, value);

				return beanWrapper;
			}

			private void assertFieldIsPresent(String fieldName) {

				Supplier<String> pdxFieldNotFoundExceptionMessageSupplier = () ->
					String.format("Field [%1$s] does not exist on Object [%2$s]", fieldName, getClassName());

				assertCondition(hasField(fieldName),
					() -> new PdxFieldDoesNotExistException(pdxFieldNotFoundExceptionMessageSupplier.get()));
			}

			private void assertFieldIsWritable(BeanWrapper beanWrapper, String fieldName) {

				Supplier<String> pdxFieldNotWritableExceptionMessageSupplier = () ->
					String.format("Field [%1$s] of Object [%2$s] is not writable", fieldName, getClassName());

				assertCondition(beanWrapper.isWritableProperty(fieldName),
					() -> new PdxFieldNotWritableException(pdxFieldNotWritableExceptionMessageSupplier.get()));
			}

			private void assertValueIsTypeMatch(BeanWrapper beanWrapper, String fieldName, Object value) {

				PropertyDescriptor property = beanWrapper.getPropertyDescriptor(fieldName);

				Supplier<String> typeMismatchExceptionMessageSupplier = () ->
					String.format("Value [%1$s] of type [%2$s] does not match field [%3$s] of type [%4$s] on Object [%5$s]",
						value, ObjectUtils.nullSafeClassName(value), fieldName, property.getPropertyType().getName(), getClassName());

				assertCondition(isTypeMatch(property, value),
					() -> new PdxFieldTypeMismatchException(typeMismatchExceptionMessageSupplier.get()));
			}

			private boolean isTypeMatch(PropertyDescriptor property, Object value) {
				return value == null || property.getPropertyType().isInstance(value);
			}

			@Override
			public String getClassName() {
				return getParent().getClassName();
			}

			@Override
			public boolean isDeserializable() {
				return getParent().isDeserializable();
			}

			@Override
			public boolean isEnum() {
				return getParent().isEnum();
			}

			@Override
			public Object getField(String fieldName) {
				return getParent().getField(fieldName);
			}

			@Override
			public List<String> getFieldNames() {
				return getParent().getFieldNames();
			}

			@Override
			public boolean isIdentityField(String fieldName) {
				return getParent().isIdentityField(fieldName);
			}

			@Override
			public Object getObject() {
				return getParent().getObject();
			}

			@Override
			public WritablePdxInstance createWriter() {
				return this;
			}

			@Override
			public boolean hasField(String fieldName) {
				return getParent().hasField(fieldName);
			}
		};
	}

	/**
	 * Determines whether the given {@link String field name} is a {@link PropertyDescriptor property}
	 * on the underlying, target {@link Object}.
	 *
	 * @param fieldName {@link String} containing the name of the field to match against
	 * a {@link PropertyDescriptor property} from the underlying, target {@link Object}.
	 * @return a boolean value that determines whether the given {@link String field name}
	 * is a {@link PropertyDescriptor property} on the underlying, target {@link Object}.
	 * @see #getFieldNames()
	 */
	@Override
	public boolean hasField(String fieldName) {
		return getFieldNames().contains(fieldName);
	}
}
