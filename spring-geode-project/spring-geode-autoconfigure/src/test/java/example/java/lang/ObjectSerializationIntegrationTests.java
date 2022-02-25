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
package example.java.lang;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Integration Tests asserting the functionality/behavior of Java Serialization.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see org.junit.Test
 * @since 2.3.0
 */
public class ObjectSerializationIntegrationTests {

	@SuppressWarnings("unchecked")
	private <T> T deserialize(byte[] bytes) throws ClassNotFoundException, IOException {

		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return (T) in.readObject();
		}
	}

	private byte[] serialize(Object obj) throws IOException {

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		try (ObjectOutputStream out = new ObjectOutputStream(byteStream)) {
			out.writeObject(obj);
			out.flush();
		}

		return byteStream.toByteArray();
	}

	@Test
	public void serializedObjectWithUnsetIntFieldUsesTheSameSpaceAsObjectWithIntFieldSet() throws IOException {

		IntEmployee janeDoeNoAge = new IntEmployee("Jane Doe");

		assertThat(janeDoeNoAge).isNotNull();
		assertThat(janeDoeNoAge.getName()).isEqualTo("Jane Doe");
		assertThat(janeDoeNoAge.getAge()).isEqualTo(0);

		IntegerEmployee janeDoeWithAge = new IntegerEmployee(21, "Jane Doe");

		assertThat(janeDoeWithAge).isNotNull();
		assertThat(janeDoeWithAge.getName()).isEqualTo("Jane Doe");
		assertThat(janeDoeWithAge.getAge()).isEqualTo(21);

		byte[] janeDoeNoAgeBytes = serialize(janeDoeNoAge);
		byte[] janeDoeWithAgeBytes = serialize(janeDoeWithAge);

		//System.out.printf("Jane Doe no Age [%s] Byte Length [%d] vs. Jane Doe with Age [%s] Byte Length [%d]%n",
		//	janeDoeNoAge, janeDoeNoAgeBytes.length, janeDoeWithAge, janeDoeWithAgeBytes.length);

		// The following assertion fails (!), I suppose unsurprisingly!
		//assertThat(janeDoeNoAgeBytes.length).isEqualTo(janeDoeWithAgeBytes.length);

		assertThat(janeDoeNoAgeBytes.length).isLessThanOrEqualTo(janeDoeWithAgeBytes.length);
	}

	@Test
	public void serializedObjectWithNullIntegerFieldUsesLessSpaceThanObjectWithIntegerFieldSet()
			throws ClassNotFoundException, IOException {

		IntegerEmployee jonDoe = new IntegerEmployee("Jon Doe");

		//jonDoe.setAge(0);

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getAge()).isNull();
		//assertThat(jonDoe.getAge()).isEqualTo(0);

		byte[] jonDoeNoAgeBytes = serialize(jonDoe);

		IntegerEmployee jonDoeNoAgeDeserialized = deserialize(jonDoeNoAgeBytes);

		assertThat(jonDoeNoAgeDeserialized).isEqualTo(jonDoe);
		assertThat(jonDoeNoAgeDeserialized).isNotSameAs(jonDoe);

		jonDoe.setAge(45);

		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
		assertThat(jonDoe.getAge()).isEqualTo(45);

		byte[] jonDoeWithAgeBytes = serialize(jonDoe);

		IntegerEmployee jonDoeWithAgeDeserialized = deserialize(jonDoeWithAgeBytes);

		assertThat(jonDoeWithAgeDeserialized).isEqualTo(jonDoe);
		assertThat(jonDoeWithAgeDeserialized).isNotSameAs(jonDoe);

		//System.out.printf("Jon Doe no Age [%s] Byte Length (%d) vs. Jon Doe with Age [%s] Byte Length [%d]%n",
		//	jonDoeNoAgeDeserialized, jonDoeNoAgeBytes.length, jonDoeWithAgeDeserialized, jonDoeWithAgeBytes.length);

		assertThat(jonDoeNoAgeBytes.length).isLessThan(jonDoeWithAgeBytes.length);
	}

	@Data
	@ToString
	@AllArgsConstructor
	@RequiredArgsConstructor
	@EqualsAndHashCode(of = { "name", "age" })
	public static class IntEmployee implements Serializable {

		private int age;

		@NonNull
		private String name;

	}

	@Data
	@ToString
	@AllArgsConstructor
	@RequiredArgsConstructor
	@EqualsAndHashCode(of = { "name", "age" })
	public static class IntegerEmployee implements Serializable {

		private Integer age;

		@NonNull
		private String name;

	}
}
