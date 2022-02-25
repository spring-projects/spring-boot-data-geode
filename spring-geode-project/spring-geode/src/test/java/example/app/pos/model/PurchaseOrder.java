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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * The {@link PurchaseOrder} class models an actual purchase agreement for {@link Product products}
 * ordered by a consumer.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.3.0
 */
@Getter
@ToString
@Region("PurchaseOrders")
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@SuppressWarnings("unused")
public class PurchaseOrder implements Iterable<LineItem> {

	@Id
	private Long id;

	/*
	private LineItem[] lineItemArray = {
		LineItem.newLineItem(Product.newProduct("Test Product")
			.in(Product.Category.SPECIALTY)
			.havingPrice(BigDecimal.valueOf(99.99)))
			.withQuantity(2)
	};
	*/

	//@Getter(AccessLevel.PROTECTED)
	// Ideally! However, Jackson ObjectMapper does not handle protected correctly!
	// In fact, ideally, no getter at all; why can't Jackson's ObjectMapper handle field mapping(?); argh!
	private List<LineItem> lineItems = new ArrayList<>();

	/*
	private Map<Object, LineItem> lineItemMap = Collections.singletonMap("MockProduct",
		LineItem.newLineItem(Product.newProduct("Mock Product")
			.in(Product.Category.SPECIALTY)
			.havingPrice(BigDecimal.valueOf(100.00)))
			.withQuantity(2));
	*/

	public Optional<LineItem> findBy(String productName) {

		return StreamSupport.stream(this.spliterator(), false)
			.filter(item -> item.getProduct().getName().equals(productName))
			.findFirst();
	}

	public BigDecimal getTotal() {

		return StreamSupport.stream(this.spliterator(), false)
			.map(LineItem::getTotal)
			.reduce(BigDecimal::add)
			.orElse(BigDecimal.ZERO);
	}

	public PurchaseOrder add(@NonNull LineItem lineItem) {

		Assert.notNull(lineItem, "LineItem must not be null");

		this.lineItems.add(lineItem);

		return this;
	}

	public PurchaseOrder identifiedAs(Long id) {
		this.id = id;
		return this;
	}

	@NonNull @Override
	public Iterator<LineItem> iterator() {
		return Collections.unmodifiableList(this.lineItems).iterator();
	}

	public int size() {
		return this.lineItems.size();
	}
}
