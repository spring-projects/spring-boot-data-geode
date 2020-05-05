/*
 * Copyright 2020 the original author or authors.
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

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The {@link LineItem} class models a {@link Product} purchase on a {@link PurchaseOrder}.
 *
 * @author John Blum
 * @since 1.3.0
 */
@Getter
@RequiredArgsConstructor(staticName = "newLineItem")
@SuppressWarnings("unused")
public class LineItem {

	@NonNull
	private Product product;

	private Integer quantity = 1;

	public String getDescription() {
		return getProduct().getName();
	}

	public BigDecimal getTotal() {
		return getUnitPrice().multiply(BigDecimal.valueOf(getQuantity()));
	}

	public BigDecimal getUnitPrice() {
		return getProduct().getPrice();
	}

	public LineItem withQuantity(int quantity) {
		Assert.isTrue(quantity > 0, "Quantity must be greater than equal to 1");
		this.quantity = quantity;
		return this;
	}

	@Override
	public String toString() {
		return String.format("Purchasing [%d] of Product [%s]", getQuantity(), getProduct());
	}
}
