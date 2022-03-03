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
package example.app.caching.inline.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract Data Type (ADT) and persistent entity modeling the results of a mathematical calculation.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see javax.persistence.Entity
 * @see javax.persistence.IdClass
 * @see javax.persistence.Table
 * @since 1.1.0
 */
// tag::class[]
@Entity
@Getter
@IdClass(ResultHolder.ResultKey.class)
@EqualsAndHashCode(of = { "operand", "operator" })
@RequiredArgsConstructor(staticName = "of")
@Table(name = "Calculations")
public class ResultHolder implements Serializable {

	@Id @NonNull
	private Integer operand;

	@Id
	@NonNull
	@Enumerated(EnumType.STRING)
	private Operator operator;

	@NonNull
	private Integer result;

	protected ResultHolder() { }

	@Override
	public String toString() {
		return getOperator().toString(getOperand(), getResult());
	}

	@Getter
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "of")
	public static class ResultKey implements Serializable {

		@NonNull
		private Integer operand;

		@NonNull
		private Operator operator;

		protected ResultKey() { }

	}
}
// end::class[]
