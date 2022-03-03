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

import static org.springframework.geode.util.GeodeAssertions.assertThat;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.geode.internal.Sendable;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.WritablePdxInstance;

/**
 * The {@link PdxInstanceWrapper} class is an implementation of the {@link PdxInstance} interface
 * wrapping an existing {@link PdxInstance} object and decorating the functionality.
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.WritablePdxInstance
 * @since 1.3.0
 */
public class PdxInstanceWrapper implements PdxInstance, Sendable {

	public static final String AT_IDENTIFIER_FIELD_NAME = "@identifier";
	public static final String AT_TYPE_FIELD_NAME = "@type";
	public static final String CLASS_NAME_PROPERTY = "className";
	public static final String ID_FIELD_NAME = "id";
	protected static final String NO_FIELD_NAME = "";

	protected static final String ARRAY_BEGIN = "[";
	protected static final String ARRAY_END = "]";
	protected static final String COMMA = ",";
	protected static final String EMPTY_STRING = "";
	protected static final String FIELD_TYPE_VALUE = "\"%1$s\"(%2$s): \"%3$s\"";
	protected static final String INDENT_STRING = "\t";
	protected static final String NEW_LINE = "\n";
	protected static final String COMMA_NEW_LINE = "," + NEW_LINE;
	protected static final String COMMA_SPACE = COMMA + " ";
	protected static final String OBJECT_BEGIN = "{";
	protected static final String OBJECT_END = "}";

	/**
	 * Smart, {@literal null-safe} factory method used to evaluate the given {@link Object} and wrap the {@link Object}
	 * in a new instance of {@link PdxInstanceWrapper} if the {@link Object} is an instance of {@link PdxInstance}
	 * or return the given {@link Object} as is.
	 *
	 * @param target {@link Object} to evaluate
	 * @return the {@link Object} wrapped in a new instance of {@link PdxInstanceWrapper} if {@link Object}
	 * is an instance of {@link PdxInstance}, otherwise returns the given {@link Object}.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see java.lang.Object
	 * @see #from(PdxInstance)
	 */
	public static Object from(Object target) {

		return target instanceof PdxInstance
			? from((PdxInstance) target)
			: target;
	}

	/**
	 * Factory method used to construct a new instance of {@link PdxInstanceWrapper} initialized with the given,
	 * required {@link PdxInstance} used to back the wrapper.
	 *
	 * @param pdxInstance {@link PdxInstance} object used to back this wrapper; must not be {@literal null}.
	 * @return a new instance of {@link PdxInstanceWrapper} initialized with the given {@link PdxInstance}.
	 * @throws IllegalArgumentException if {@link PdxInstance} is {@literal null}.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #PdxInstanceWrapper(PdxInstance)
	 */
	public static PdxInstanceWrapper from(PdxInstance pdxInstance) {

		return pdxInstance instanceof PdxInstanceWrapper
			? (PdxInstanceWrapper) pdxInstance
			: new PdxInstanceWrapper(pdxInstance);
	}

	/**
	 * Null-safe factory method used to unwrap the given {@link PdxInstance}.
	 *
	 * If the given {@link PdxInstance} is an instance of {@link PdxInstanceWrapper} then this factory method will
	 * unwrap the {@link PdxInstanceWrapper} returning the underlying, {@link PdxInstanceWrapper#getDelegate() delegate}
	 * {@link PdxInstance}.  Otherwise, the given {@link PdxInstance} is returned.
	 *
	 * @param pdxInstance {@link PdxInstance} to unwrap; may be {@literal null}.
	 * @return the unwrapped {@link PdxInstance}.
	 * @see org.apache.geode.pdx.PdxInstance
	 * @see #getDelegate()
	 */
	public static PdxInstance unwrap(PdxInstance pdxInstance) {

		return pdxInstance instanceof PdxInstanceWrapper
			? ((PdxInstanceWrapper) pdxInstance).getDelegate()
			: pdxInstance;
	}

	private final PdxInstance delegate;

	/**
	 * Constructs a new instance of {@link PdxInstanceWrapper} initialized with the given, required {@link PdxInstance}
	 * object used to back this wrapper.
	 *
	 * @param pdxInstance {@link PdxInstance} object used to back this wrapper; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link PdxInstance} is {@literal null}.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	public PdxInstanceWrapper(PdxInstance pdxInstance) {

		assertThat(pdxInstance).isNotNull();

		this.delegate = pdxInstance;
	}

	/**
	 * Returns a reference to the configured, underlying {@link PdxInstance} backing this wrapper.
	 *
	 * @return a reference to the configured, underlying {@link PdxInstance} backing this wrapper;
	 * never {@literal null}.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	public PdxInstance getDelegate() {
		return this.delegate;
	}

	/**
	 * Returns an {@link Optional} reference to a configured Jackson {@link ObjectMapper} used to
	 * deserialize the {@link String JSON} generated from {@link PdxInstance PDX} back into an {@link Object}.
	 *
	 * This method is meant ot be overridden by {@link Class subclasses}.
	 *
	 * @return an {@link Optional} {@link ObjectMapper}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 * @see java.util.Optional
	 */
	protected Optional<ObjectMapper> getObjectMapper() {

		ObjectMapper objectMapper = newObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
			.findAndRegisterModules();

		return Optional.of(objectMapper);
	}

	/**
	 * Constructs a new instance of Jackson's {@link ObjectMapper}.
	 *
	 * @return a new instance of Jackson's {@link ObjectMapper}; never {@literal null}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 */
	ObjectMapper newObjectMapper() {
		return new ObjectMapper();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String getClassName() {
		return getDelegate().getClassName();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isDeserializable() {
		return getDelegate().isDeserializable();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isEnum() {
		return getDelegate().isEnum();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Object getField(String fieldName) {
		return getDelegate().getField(fieldName);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<String> getFieldNames() {
		return getDelegate().getFieldNames();
	}

	/**
	 * Determines the {@link Object identifier} for, or {@link PdxInstance#isIdentityField(String) identity} of,
	 * this {@link PdxInstance}.
	 *
	 * @return the {@link Object identifier} for this {@link PdxInstance}; never {@literal null}.
	 * @throws IllegalStateException if the {@link PdxInstance} does not have an id.
	 * @see #isIdentityField(String)
	 * @see #getField(String)
	 * @see #getFieldNames()
	 * @see #getId()
	 */
	public Object getIdentifier() {

		Optional<String> identityFieldName = nullSafeList(getFieldNames()).stream()
			.filter(this::hasText)
			.filter(this::isIdentityField)
			.findFirst();

		return identityFieldName
			.map(this::getField)
			.orElseGet(this::getId);
	}

	/**
	 * Searches for a PDX {@link String field name} called {@literal id} on this {@link PdxInstance}
	 * and returns its {@link Object value} as the {@link Object identifier} for,
	 * or {@link PdxInstance#isIdentityField(String) identity} of, this {@link PdxInstance}.
	 *
	 * @return the {@link Object value} of the {@literal id} {@link String field} on this {@link PdxInstance}.
	 * @throws IllegalStateException if this {@link PdxInstance} does not have an id.
	 * @see #getAtIdentifier()
	 * @see #getField(String)
	 * @see #hasField(String)
	 */
	protected Object getId() {

		return hasField(ID_FIELD_NAME)
			? getField(ID_FIELD_NAME)
			: getAtIdentifier();
	}

	/**
	 * Searches for a PDX {@link String field} declared by the {@literal @identifier} metadata {@link String field}
	 * on this {@link PdxInstance} and returns the {@link Object value} of this {@link String field}
	 * as the {@link Object identifier} for, or {@link PdxInstance#isIdentityField(String) identity} of,
	 * this {@link PdxInstance}.
	 *
	 * @return the {@link Object value} of the {@link String field} declared in the {@literal @identifier} metadata
	 * {@link String field} on this {@link PdxInstance}.
	 * @throws IllegalStateException if the {@link PdxInstance} does not have an id.
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	protected Object getAtIdentifier() {

		return Optional.of(AT_IDENTIFIER_FIELD_NAME)
			.filter(this::hasField)
			.map(this::getField)
			.map(String::valueOf)
			.filter(this::hasField)
			.map(this::getField)
			.orElseThrow(() -> new IllegalStateException(String.format("PdxInstance for type [%1$s] has no %2$s",
				getClassName(), resolveMessageForIdentifierError(this))));
	}

	private String resolveMessageForIdentifierError(PdxInstance pdxInstance) {

		String message = "declared identifier";

		if (pdxInstance.hasField(ID_FIELD_NAME)) {
			message = "id";
		}
		else if (pdxInstance.hasField(AT_IDENTIFIER_FIELD_NAME)) {

			Object atIdentifierFieldValue = pdxInstance.getField(AT_IDENTIFIER_FIELD_NAME);

			String resolvedIdentifierFieldName = Objects.nonNull(atIdentifierFieldValue)
				? atIdentifierFieldValue.toString().trim()
				: NO_FIELD_NAME;

			boolean identifierFieldNameWasDeclaredAndIsValid = pdxInstance.hasField(resolvedIdentifierFieldName);

			Object identifier = identifierFieldNameWasDeclaredAndIsValid
				? pdxInstance.getField(resolvedIdentifierFieldName)
				: null;

			String ifMessage = "value [%s] for field [%s] declared in [%s]";
			String elseMessage = "field [%s] declared in [%s]";

			message = identifierFieldNameWasDeclaredAndIsValid
				? String.format(ifMessage, identifier, resolvedIdentifierFieldName, AT_IDENTIFIER_FIELD_NAME)
				: String.format(elseMessage, resolvedIdentifierFieldName, AT_IDENTIFIER_FIELD_NAME);
		}

		return message;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isIdentityField(String fieldName) {
		return getDelegate().isIdentityField(fieldName);
	}

	/**
	 * Materializes an {@link Object} from the PDX bytes described by this {@link PdxInstance}.
	 *
	 * If these PDX bytes describe an {@link Object} parsed from JSON, then the JSON is reconstructed from
	 * this {@link PdxInstance} and mapped to an instance of the {@link Class type} identified by
	 * the {@literal @type} metadata PDX {@link String field} using Jackson's {@link ObjectMapper}.
	 *
	 * @return an {@link Object} constructed from the PDX bytes described by this {@link PdxInstance}.
	 * @see com.fasterxml.jackson.databind.ObjectMapper
	 * @see java.lang.Object
	 * @see #getObjectMapper()
	 */
	@Override
	public Object getObject() {

		return getObjectMapper()
			.filter(objectMapper -> JSONFormatter.JSON_CLASSNAME.equals(getClassName()))
			.filter(objectMapper -> hasField(AT_TYPE_FIELD_NAME))
			.<Object>map(objectMapper ->  {
				try {

					String typeName = String.valueOf(getField(AT_TYPE_FIELD_NAME));

					Class<?> type = Class.forName(typeName);

					String json = jsonFormatterToJson(getDelegate());

					return objectMapper.readValue(json, type);
				}
				catch (Throwable ignore) {
					// TODO Log Throwable?
					return null;
				}
			})
			.orElseGet(() -> getDelegate().getObject());
	}

	/**
	 * Calls {@link JSONFormatter#toJSON(PdxInstance)} to convert the {@link PdxInstance} into {@link String JSON}.
	 *
	 * @param pdxInstance {@link PdxInstance} to convert to {@link String JSON}.
	 * @return {@link String JSON} generated from the given {@link PdxInstance}.
	 * @see org.apache.geode.pdx.JSONFormatter#toJSON(PdxInstance)
	 * @see org.apache.geode.pdx.PdxInstance
	 */
	String jsonFormatterToJson(PdxInstance pdxInstance) {
		return JSONFormatter.toJSON(pdxInstance);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public WritablePdxInstance createWriter() {
		return getDelegate().createWriter();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean hasField(String fieldName) {
		return getDelegate().hasField(fieldName);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void sendTo(DataOutput out) throws IOException {

		PdxInstance delegate = getDelegate();

		if (delegate instanceof Sendable) {
			((Sendable) delegate).sendTo(out);
		}
	}

	/**
	 * Returns a {@link String} representation of this {@link PdxInstance}.
	 *
	 * @return a {@link String} representation of this {@link PdxInstance}.
	 * @see java.lang.String
	 */
	@Override
	public String toString() {
		//return getDelegate().toString();
		return toString(this);
	}

	private String toString(PdxInstance pdx) {
		return toString(pdx, "");
	}

	private String toString(PdxInstance pdx, String indent) {

		if (Objects.nonNull(pdx)) {

			StringBuilder buffer = new StringBuilder(OBJECT_BEGIN).append(NEW_LINE);

			String fieldIndent = indent + INDENT_STRING;

			buffer.append(fieldIndent).append(formatFieldValue(CLASS_NAME_PROPERTY, pdx.getClassName()));

			for (String fieldName : nullSafeList(pdx.getFieldNames())) {

				Object fieldValue = pdx.getField(fieldName);

				String valueString = toStringObject(fieldValue, fieldIndent);

				buffer.append(COMMA_NEW_LINE);
				buffer.append(fieldIndent).append(formatFieldValue(fieldName, valueString));
			}

			buffer.append(NEW_LINE).append(indent).append(OBJECT_END);

			return buffer.toString();
		}
		else {
			return null;
		}
	}

	private String toStringArray(Object value, String indent) {

		Object[] array = (Object[]) value;

		StringBuilder buffer = new StringBuilder(ARRAY_BEGIN);

		boolean addComma = false;

		for (Object element : array) {
			buffer.append(addComma ? COMMA_SPACE : EMPTY_STRING);
			buffer.append(toStringObject(element, indent));
			addComma = true;
		}

		buffer.append(ARRAY_END);

		return buffer.toString();
	}

	private String toStringObject(Object value, String indent) {

		return isPdxInstance(value) ? toString((PdxInstance) value, indent)
			: isArray(value) ? toStringArray(value, indent)
			: String.valueOf(value);
	}

	private String formatFieldValue(String fieldName, Object fieldValue) {
		return String.format(FIELD_TYPE_VALUE, fieldName, nullSafeType(fieldValue), fieldValue);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean isArray(Object value) {
		return Objects.nonNull(value) && value.getClass().isArray();
	}

	private boolean isPdxInstance(Object value) {
		return value instanceof PdxInstance;
	}

	private <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : Collections.emptyList();
	}

	private Class<?> nullSafeType(Object value) {
		return value != null ? value.getClass() : Object.class;
	}
}
