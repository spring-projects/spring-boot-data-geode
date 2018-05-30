/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.springframework.geode.core.env.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link User}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.geode.core.env.support.User
 * @since 1.0.0
 */
public class UserUnitTests {

	@Test
	public void withNameReturnsNewUser() {

		User user = User.with("root");

		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("root");
		assertThat(user.getPassword().isPresent()).isFalse();
		assertThat(user.getRole().isPresent()).isFalse();
	}

	@Test
	public void withNamePasswordAndRoleReturnsNewUser() {

		User jdoe = User.with("jdoe")
			.withPassword("p@55w0rd!")
			.withRole(User.Role.CLUSTER_OPERATOR);

		assertThat(jdoe).isNotNull();
		assertThat(jdoe.getName()).isEqualTo("jdoe");
		assertThat(jdoe.getPassword().orElse(null)).isEqualTo("p@55w0rd!");
		assertThat(jdoe.getRole().orElse(null)).isEqualTo(User.Role.CLUSTER_OPERATOR);
	}

	private void testWithInvalidNameThrowsIllegalArgumentException(String name) {

		try {
			User.with(name);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("User name [%s] is required", name);
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void withBlankUserNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withEmptyUserNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNullUserNameThrowsIllegalArgumentException() {
		testWithInvalidNameThrowsIllegalArgumentException(null);
	}

	@Test
	public void compareToReturnsEqualValue() {
		assertThat(User.with("root").compareTo(User.with("root"))).isEqualTo(0);
	}

	@Test
	public void compareToReturnsNegativeValue() {
		assertThat(User.with("jdoe").compareTo(User.with("root"))).isLessThan(0);
	}

	@Test
	public void compareToReturnsPositiveValue() {
		assertThat(User.with("root").compareTo(User.with("jdoe"))).isGreaterThan(0);
	}

	@Test
	@SuppressWarnings("all")
	public void equalsObjectsReturnsFalse() {
		assertThat(User.with("admin").equals("admin")).isFalse();
	}

	@Test
	public void equalsWithDifferentObjectsReturnsFalse() {

		User admin = User.with("admin").withRole(User.Role.CLUSTER_OPERATOR);
		User root = User.with("root").withRole(User.Role.CLUSTER_OPERATOR);

		assertThat(root.equals(admin)).isFalse();
	}

	@Test
	public void equalsWithEqualObjectsReturnsTrue() {

		User root = User.with("root").withRole(User.Role.CLUSTER_OPERATOR);
		User rootToo = User.with("root").withPassword("test").withRole(User.Role.DEVELOPER);

		assertThat(root.equals(rootToo)).isTrue();
	}

	@Test
	@SuppressWarnings("all")
	public void equalsWithIdenticalObjectsReturnsTrue() {

		User root = User.with("root");

		assertThat(root.equals(root)).isTrue();
	}

	@Test
	public void hashCodeIsCorrect() {

		User user = User.with("root");

		int hashCode = user.hashCode();

		assertThat(hashCode).isNotZero();
		assertThat(hashCode).isEqualTo(user.hashCode());

		user.withPassword("test").withRole(User.Role.DEVELOPER);

		assertThat(user.hashCode()).isEqualTo(hashCode);
		assertThat(user.hashCode()).isNotEqualTo(User.with("anotherUser").hashCode());
	}

	@Test
	public void toStringReturnsUserName() {
		assertThat(User.with("root").toString()).isEqualTo("root");
	}

	@Test
	public void roleOfEmptyNameReturnsNull() {
		assertThat(User.Role.of("")).isNull();
		assertThat(User.Role.of("  ")).isNull();
	}

	@Test
	public void roleOfInvalidNameReturnsNull() {
		assertThat(User.Role.of("invalid")).isNull();
	}

	@Test
	public void roleOfNulReturnsNull() {
		assertThat(User.Role.of(null)).isNull();
	}

	@Test
	public void roleOfRoleNamesEqualsRole() {
		Arrays.stream(User.Role.values()).forEach(role -> {
			assertThat(User.Role.of(role.name())).isEqualTo(role);
			assertThat(User.Role.of(role.toString())).isEqualTo(role);
		});
	}

	@Test
	public void isClusterOperator() {
		assertThat(User.Role.CLUSTER_OPERATOR.isClusterOperator()).isTrue();
	}

	@Test
	public void isNotClusterOperator() {
		assertThat(User.Role.DEVELOPER.isClusterOperator()).isFalse();
	}

	@Test
	public void isDeveloper() {
		assertThat(User.Role.DEVELOPER.isDeveloper()).isTrue();
	}

	@Test
	public void isNotDeveloper() {
		assertThat(User.Role.CLUSTER_OPERATOR.isDeveloper()).isFalse();
	}

	@Test
	public void toStringReturnsLowercaseName() {
		Arrays.stream(User.Role.values())
			.forEach(role -> assertThat(role.toString()).isEqualTo(role.name().toLowerCase()));
	}
}
