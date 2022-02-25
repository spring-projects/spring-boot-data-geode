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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Test;

import org.apache.geode.pdx.PdxFieldDoesNotExistException;
import org.apache.geode.pdx.PdxFieldTypeMismatchException;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.WritablePdxInstance;

import org.springframework.beans.BeanWrapper;
import org.springframework.data.annotation.Id;

import example.app.crm.model.Customer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Unit Tests for {@link ObjectPdxInstanceAdapter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.geode.pdx.ObjectPdxInstanceAdapter
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class ObjectPdxInstanceAdapterUnitTests {

	@Test
	public void constructObjectPdxInstanceAdapter() {

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		ObjectPdxInstanceAdapter adapter = new ObjectPdxInstanceAdapter(jonDoe);

		assertThat(adapter).isNotNull();
		assertThat(adapter.getObject()).isSameAs(jonDoe);

		BeanWrapper beanWrapper = adapter.getBeanWrapper();

		assertThat(beanWrapper).isNotNull();
		assertThat(beanWrapper.getWrappedInstance()).isEqualTo(jonDoe);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructObjectPdxInstanceAdapterWithNullThrowsIllegalArgumentException() {

		try {
			new ObjectPdxInstanceAdapter(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Object to adapt must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void fromObjectReturnsAdapter() {

		Object target = new Object();

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(target);

		assertThat(adapter).isNotNull();
		assertThat(adapter.getObject()).isSameAs(target);
	}

	@Test
	public void unwrapNullIsNullSafeReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.unwrap(null)).isNull();
	}

	@Test
	public void unwrapPdxInstanceReturnsPdxInstance() {

		PdxInstance mockPdxInstance = mock(PdxInstance.class);

		assertThat(ObjectPdxInstanceAdapter.unwrap(mockPdxInstance)).isSameAs(mockPdxInstance);
	}

	@Test
	public void unwrapObjectPdxInstanceAdapterReturnsObject() {

		Object target = new Object();

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(target);

		assertThat(adapter).isNotNull();
		assertThat(ObjectPdxInstanceAdapter.unwrap(adapter)).isEqualTo(target);
	}

	@Test
	public void getClassNameReturnsObjectClassName() {
		assertThat(ObjectPdxInstanceAdapter.from(Customer.newCustomer(2L, "Jane Doe")).getClassName())
			.isEqualTo(Customer.class.getName());
	}

	@Test
	public void isDeserializableWhenObjectIsPresentReturnsTrue() {
		assertThat(ObjectPdxInstanceAdapter.from("TEST").isDeserializable()).isTrue();
	}

	@Test
	public void isDeserializableWhenObjectIsNotPresentReturnsFalse() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("NULL"));

		doReturn(null).when(adapter).getObject();

		assertThat(adapter.isDeserializable()).isFalse();

		verify(adapter, times(1)).getObject();
	}

	@Test
	public void isEnumWhenObjectIsAnEnumeratedValueReturnsTrue() {
		assertThat(ObjectPdxInstanceAdapter.from(TestEnum.ONE).isEnum()).isTrue();
	}

	@Test
	public void isEnumWhenObjectIsPojoReturnsFalse() {
		assertThat(ObjectPdxInstanceAdapter.from("TEST").isEnum()).isFalse();
	}

	@Test
	public void getFieldReturnsPropertyValue() {

		Customer pieDoe = Customer.newCustomer(3L, "Pie Doe");

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(pieDoe);

		assertThat(adapter.getField("id")).isEqualTo(pieDoe.getId());
		assertThat(adapter.getField("name")).isEqualTo(pieDoe.getName());
	}

	@Test
	public void getNonExistingFieldReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from("TEST").getField("nonExistingField")).isNull();
	}

	@Test
	public void getNonReadableExistingFieldReturnsNull() {

		WriteOnlyBean bean = new WriteOnlyBean();

		bean.value = "TEST";

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(bean);

		assertThat(adapter.getField("value")).isNull();
	}

	@Test
	public void getFieldNamesReturnsPropertyNames() {

		Customer sourDoe = Customer.newCustomer(4L, "Sour Doe");

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(sourDoe);

		assertThat(adapter.getFieldNames()).contains("id", "name");
	}

	@Test
	public void getFieldNamesReturnsEmptyList() {
		assertThat(ObjectPdxInstanceAdapter.from(new NoPropertyNoFieldBean()).getFieldNames()).isEmpty();
	}

	@Test
	public void isIdentityFieldWithIdentifierAndNonIdentifierFields() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn("isbn").when(adapter).resolveIdentityFieldNameFromProperty();

		assertThat(adapter.isIdentityField("accountNumber")).isFalse();
		assertThat(adapter.isIdentityField("id")).isFalse();
		assertThat(adapter.isIdentityField("isbn")).isTrue();
		assertThat(adapter.isIdentityField("sessionId")).isFalse();
		assertThat(adapter.isIdentityField("isbn")).isTrue();
		assertThat(adapter.isIdentityField("ssn")).isFalse();

		verify(adapter, times(1)).resolveIdentityFieldNameFromProperty();
	}

	@Test
	public void resolvesIdentityFieldNameFromAtIdAnnotatedFieldBean() {
		assertThat(ObjectPdxInstanceAdapter.from(new AtIdAnnotatedFieldBean()).resolveIdentityFieldNameFromProperty())
			.isEqualTo("ssn");
	}

	@Test
	public void resolvesIdentifyFieldNameFromAtIdAnnotatedPropertyBean() {
		assertThat(ObjectPdxInstanceAdapter.from(new AtIdAnnotatedPropertyBean())
			.resolveIdentityFieldNameFromProperty()).isEqualTo("accountNumber");
	}

	@Test
	public void resolvesIdentifyFieldNameFromAtIdAnnotatedPropertyBeanSubclass() {
		assertThat(ObjectPdxInstanceAdapter.from(new AtIdAnnotatedPropertyBeanSubclass())
			.resolveIdentityFieldNameFromProperty()).isEqualTo("accountNumber");
	}

	@Test
	public void resolvesIdentityFieldNameFromIdNamedPropertyBean() {
		assertThat(ObjectPdxInstanceAdapter.from(new IdNamedPropertyBean()).resolveIdentityFieldNameFromProperty())
			.isEqualTo("id");
	}

	@Test
	public void resolvesIdentityFieldNameFromIdNamedPropertyBeanSubclass() {
		assertThat(ObjectPdxInstanceAdapter.from(new IdNamedPropertyBeanSubclass()).resolveIdentityFieldNameFromProperty())
			.isEqualTo("id");
	}

	@Test
	public void resolvesIdentityFieldNameFromOverridenAtIdAnnotatedFieldBean() {
		assertThat(ObjectPdxInstanceAdapter.from(new AtIdAnnotatedFieldOverridesNonPublicAtIdAnnotatedPropertyBean())
			.resolveIdentityFieldNameFromProperty()).isEqualTo("ssn");
	}

	@Test
	public void resolvesIdentityFieldNameFromOverriddenIdNamedPropertyBean() {
		assertThat(ObjectPdxInstanceAdapter.from(new IdNamedPropertyOverridesNonPublicAtIdAnnotatedFieldBean())
			.resolveIdentityFieldNameFromProperty()).isEqualTo("id");
	}

	@Test
	public void resolveIdentityFieldNameFromAtIdAnnotatedFieldBeanSubclassReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new AtIdAnnotatedFieldBeanSubclass())
			.resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void resolveIdentityFieldNameFromNonPublicAtIdAnnotatedPropertyBeanReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new NonPublicAtIdAnnotatedPropertyBean())
			.resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void resolveIdentityFieldNameFromNonPublicIdNamedPropertyBeanReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new NonPublicIdNamedPropertyBean())
			.resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void resolveIdentityFieldNameFromWriteOnlyAtIdAnnotatedFieldBeanReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new WriteOnlyAtIdAnnotatedFieldBean())
			.resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void resolveIdentityFieldNameFromWriteOnlyAtIdAnnotatedPropertyBeanReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new WriteOnlyAtIdAnnotatedPropertyBean())
			.resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void resolveIdentifyFieldNameFromBeanWithNoPropertiesOrFieldsReturnsNull() {
		assertThat(ObjectPdxInstanceAdapter.from(new NoPropertyNoFieldBean()).resolveIdentityFieldNameFromProperty()).isNull();
	}

	@Test
	public void getObjectReturnsTarget() {

		Object target = new Object();

		assertThat(ObjectPdxInstanceAdapter.from(target).getObject()).isSameAs(target);
	}

	@Test
	public void setFieldSetsProperty() {

		Customer dillDoe = Customer.newCustomer(10L, "Dill Doe");

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from(dillDoe);

		assertThat(adapter.getField("name")).isEqualTo(dillDoe.getName());

		adapter.createWriter().setField("name", "Hoe Doe");

		assertThat(adapter.getField("name")).isEqualTo("Hoe Doe");
		assertThat(dillDoe.getName()).isEqualTo("Hoe Doe");
	}

	@Test(expected = PdxFieldDoesNotExistException.class)
	public void setNonExistingFieldThrowsPdxFieldDoesNotExistException() {

		try {
			ObjectPdxInstanceAdapter.from(new NoPropertyNoFieldBean())
				.createWriter()
				.setField("nonExistingField", "TEST");
		}
		catch (PdxFieldDoesNotExistException expected) {

			assertThat(expected).hasMessage("Field [nonExistingField] does not exist on Object [%s]",
				NoPropertyNoFieldBean.class.getName());

			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = PdxFieldNotWritableException.class)
	public void setNonWritableFieldThrowsPdxFieldNotWritableException() {

		ReadOnlyBean bean = new ReadOnlyBean();

		assertThat(bean.getValue()).isEqualTo("TEST");

		try {
			ObjectPdxInstanceAdapter.from(bean).createWriter().setField("value", "MOCK");
		}
		catch (PdxFieldNotWritableException expected) {

			assertThat(expected).hasMessage("Field [value] of Object [%s] is not writable",
				ReadOnlyBean.class.getName());

			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			assertThat(bean.getValue()).isEqualTo("TEST");
		}
	}

	@Test(expected = PdxFieldTypeMismatchException.class)
	public void setFieldWithValueHavingTypeMismatchThrowsPdxFieldTypeMismatchException() {

		CharacterValueBean bean = new CharacterValueBean();

		assertThat(bean.getValue()).isEqualTo('X');

		try {
			ObjectPdxInstanceAdapter.from(bean).createWriter().setField("value", "Y");
		}
		catch (PdxFieldTypeMismatchException expected) {

			String expectedExceptionMessage =
				String.format("Value [Y] of type [java.lang.String] does not match field [value] of type [java.lang.Character] on Object [%s]",
					CharacterValueBean.class.getName());

			assertThat(expected).hasMessage(expectedExceptionMessage);
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			assertThat(bean.getValue()).isEqualTo('X');
		}
	}

	@Test
	public void writablePdxInstanceGetClassNameCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().getClassName();

		verify(adapter, times(1)).getClassName();
	}

	@Test
	public void writablePdxInstanceIsDeserializableCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().isDeserializable();

		verify(adapter, times(1)).isDeserializable();
	}

	@Test
	public void writablePdxInstanceIsEnumCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().isEnum();

		verify(adapter, times(1)).isEnum();
	}

	@Test
	public void writablePdxInstanceGetFieldCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().getField("testFieldName");

		verify(adapter, times(1)).getField(eq("testFieldName"));
	}

	@Test
	public void writablePdxInstanceGetFieldNamesCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().getFieldNames();

		verify(adapter, times(1)).getFieldNames();
	}

	@Test
	public void writablePdxInstanceIsIdentityFieldCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().isIdentityField("isbn");

		verify(adapter, times(1)).isIdentityField(eq("isbn"));
	}

	@Test
	public void writablePdxInstanceGetObjectCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		assertThat(adapter.createWriter().getObject()).isEqualTo("TEST");

		verify(adapter, times(1)).getObject();
	}

	@Test
	public void writablePdxInstanceCreateWriterReturnsThis() {

		ObjectPdxInstanceAdapter adapter = ObjectPdxInstanceAdapter.from("TEST");

		WritablePdxInstance writer = adapter.createWriter();

		assertThat(writer).isNotNull();
		assertThat(writer.createWriter()).isSameAs(writer);
	}

	@Test
	public void writablePdxInstanceHasFieldCallsParent() {

		ObjectPdxInstanceAdapter adapter = spy(ObjectPdxInstanceAdapter.from("TEST"));

		doReturn(adapter).when(adapter).getParent();

		adapter.createWriter().hasField("testFieldName");

		verify(adapter, times(1)).hasField(eq("testFieldName"));
	}

	// TEST OBJECTS

	enum TestEnum {
		ONE,
		TWO
	}

	// PASS
	static class AtIdAnnotatedFieldBean {

		@Id @Getter
		private String ssn = "123-45-6789";

	}

	// FAIL
	static class AtIdAnnotatedFieldBeanSubclass extends AtIdAnnotatedFieldBean { }

	// PASS
	static class AtIdAnnotatedPropertyBean {

		@Getter
		private Object id;

		@Id
		public String getAccountNumber() {
			return "0x-123456789";
		}
	}

	// PASS
	static class AtIdAnnotatedPropertyBeanSubclass extends AtIdAnnotatedPropertyBean { }

	// PASS
	static class IdNamedPropertyBean {

		public Integer getId() {
			return 1;
		}
	}

	// PASS
	static class IdNamedPropertyBeanSubclass extends IdNamedPropertyBean { }

	// FAIL
	static class NonPublicAtIdAnnotatedPropertyBean {

		@Id
		protected String getIdentifier() {
			return UUID.randomUUID().toString();
		}
	}

	// FAIL
	static class NonPublicIdNamedPropertyBean {

		@Getter(AccessLevel.PROTECTED)
		private Object id;

	}

	// PASS
	static class AtIdAnnotatedFieldOverridesNonPublicAtIdAnnotatedPropertyBean {

		@Id @Getter
		private Object ssn;

		@Id
		protected Object getAccountNumber() {
			return "123";
		}
	}

	// PASS
	static class IdNamedPropertyOverridesNonPublicAtIdAnnotatedFieldBean {

		@Id @Getter(AccessLevel.PROTECTED) @Setter
		private Object accountNumber;

		public Object getId() {
			return 10;
		}
	}

	// FAIL
	static class WriteOnlyAtIdAnnotatedFieldBean {

		@Id @Setter
		private Object identifier;

	}

	// FAIL
	static class WriteOnlyAtIdAnnotatedPropertyBean {

		@Id
		public void setIdentifier(Object id) { }

	}

	static class CharacterValueBean {

		@Getter @Setter
		private Character value = 'X';

	}

	static class NoPropertyNoFieldBean { }

	static class ReadOnlyBean {

		@Getter
		private final Object value = "TEST";

	}

	static class WriteOnlyBean {

		@Setter
		private Object value;

	}
}
