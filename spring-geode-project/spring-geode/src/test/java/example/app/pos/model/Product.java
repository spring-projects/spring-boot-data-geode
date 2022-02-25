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
package example.app.pos.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The {@link Product} class models a physical product for purchase.
 *
 * @author John Blum
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.3.0
 */
@Region("Products")
@Getter
@ToString(of = "name")
@EqualsAndHashCode(of = "name")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(staticName = "newProduct")
@SuppressWarnings("unused")
public class Product {

	@Id @NonNull
	private String name;

	// TODO Introduce a Price type, perhaps (?), encapsulating amount and currency (e.g. USD)
	private BigDecimal price;

	private Category category;

	public Product havingPrice(BigDecimal price) {
		this.price = price;
		return this;
	}

	public Product in(Category category) {
		this.category = category;
		return this;
	}

	/**
	 * @see <a href="https://www.marketingstudyguide.com/list-examples-classifying-consumer-products/">List of examples for classifying consumer products</a>
	 */
	public enum Category {

		CONVENIENCE,
		SHOPPING,
		SPECIALTY,
		UNSOUGHT

	}
}
