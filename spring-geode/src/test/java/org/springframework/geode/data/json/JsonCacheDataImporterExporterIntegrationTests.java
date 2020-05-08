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
package org.springframework.geode.data.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.Scope;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxInstanceFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import example.app.crm.model.Customer;
import example.app.pos.model.LineItem;
import example.app.pos.model.Product;
import example.app.pos.model.PurchaseOrder;

/**
 * Integration Test for {@link JsonCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class JsonCacheDataImporterExporterIntegrationTests extends IntegrationTestsSupport {

	private static final String EXPORT_ENABLED_PROPERTY = "spring.boot.data.gemfire.cache.data.export.enabled";

	private static volatile Supplier<Resource> resourceSupplier;

	private static volatile Supplier<StringWriter> writerSupplier;

	private ConfigurableApplicationContext applicationContext;

	@Before
	public void initializeSuppliers() {
		resourceSupplier = () -> null;
		writerSupplier = StringWriter::new;
	}

	@After
	public void closeApplicationContext() {

		Optional.ofNullable(this.applicationContext)
			.ifPresent(ConfigurableApplicationContext::close);

		this.applicationContext = null;
	}

	private ConfigurableApplicationContext newApplicationContext(Class<?>... componentClasses) {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

		applicationContext.register(componentClasses);
		applicationContext.registerShutdownHook();
		applicationContext.refresh();

		this.applicationContext = applicationContext;

		return applicationContext;
	}

	private Class<?>[] withResource(String path) {

		if (StringUtils.hasText(path)) {
			resourceSupplier = () -> new ClassPathResource(path);
		}

		return new Class[] { TestGeodeConfiguration.class };
	}

	private <K, V> Region<K, V> assertExampleRegion(Region<K, V> example) {

		assertThat(example).isNotNull();
		assertThat(example.getName()).isEqualTo("Example");
		assertThat(example.getAttributes()).isNotNull();
		assertThat(example.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.NORMAL);
		assertThat(example.getAttributes().getScope()).isEqualTo(Scope.LOCAL);

		return example;
	}

	@SuppressWarnings("unchecked")
	private <K, V> Region<K, V> getExampleRegion(ConfigurableApplicationContext applicationContext) {
		return assertExampleRegion(applicationContext.getBean("Example", Region.class));
	}

	@Test
	public void exampleRegionContainsJonDoe() {

		Region<?, ?> example = getExampleRegion(newApplicationContext(withResource("data-example-jondoe.json")));

		assertThat(example).hasSize(1);

		Object value = example.values().stream()
			.findFirst()
			.orElse(null);

		assertThat(value).isInstanceOf(PdxInstance.class);

		PdxInstance pdxInstance = (PdxInstance) value;

		assertThat(pdxInstance.getField("id")).isEqualTo((byte) 1);
		assertThat(pdxInstance.getField("name")).isEqualTo("Jon Doe");
		assertThat(pdxInstance.getField("@type")).isEqualTo(Customer.class.getName());

		Customer jonDoe = (Customer) pdxInstance.getObject();

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getId()).isEqualTo(1);
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");
	}

	@Test
	public void exampleRegionContainsDoeFamily() {

		Region<?, ?> example =
			getExampleRegion(newApplicationContext(withResource("data-example-doefamily.json")));

		assertThat(example).hasSize(9);

		Set<Customer> customers = example.values().stream()
			.filter(PdxInstance.class::isInstance)
			.map(PdxInstance.class::cast)
			.map(PdxInstance::getObject)
			.filter(Customer.class::isInstance)
			.map(Customer.class::cast)
			.collect(Collectors.toSet());

		assertThat(customers).hasSize(example.size());

		assertThat(customers).containsExactlyInAnyOrder(
			Customer.newCustomer(1L, "Jon Doe"),
			Customer.newCustomer(2L, "Jane Doe"),
			Customer.newCustomer(3L, "Cookie Doe"),
			Customer.newCustomer(4L, "Fro Doe"),
			Customer.newCustomer(5L, "Ginger Doe"),
			Customer.newCustomer(6L, "Hoe Doe"),
			Customer.newCustomer(7L, "Joe Doe"),
			Customer.newCustomer(8L, "Pie Doe"),
			Customer.newCustomer(9L, "Sour Doe")
		);
	}

	private void assertPurchaseOrder(Object purchaseOrder, int expectedNumberOfLineItems) {

		assertThat(purchaseOrder).isInstanceOf(PdxInstance.class);
		assertThat(((PdxInstance) purchaseOrder).hasField("@type"));
		assertThat(((PdxInstance) purchaseOrder).hasField("id"));
		assertThat(((PdxInstance) purchaseOrder).hasField("lineItems"));
		assertThat(((PdxInstance) purchaseOrder).getField("@type")).isEqualTo(PurchaseOrder.class.getName());

		Object lineItems = ((PdxInstance) purchaseOrder).getField("lineItems");

		assertThat(lineItems).isInstanceOf(Collection.class);
		assertThat((Collection<?>) lineItems).hasSize(expectedNumberOfLineItems);

		((Collection<?>) lineItems).forEach(this::assertLineItem);
	}

	private void assertLineItem(Object lineItem) {

		assertThat(lineItem).isInstanceOf(PdxInstance.class);
		assertThat(((PdxInstance) lineItem).hasField("@type")).isTrue();
		assertThat(((PdxInstance) lineItem).hasField("product")).isTrue();
		assertThat(((PdxInstance) lineItem).hasField("quantity")).isTrue();
		assertThat(((PdxInstance) lineItem).getField("@type")).isEqualTo(LineItem.class.getName());
		assertProduct(((PdxInstance) lineItem).getField("product"));
	}

	private void assertProduct(Object product) {

		assertThat(product).isInstanceOf(PdxInstance.class);
		assertThat(((PdxInstance) product).hasField("@type")).isTrue();
		assertThat(((PdxInstance) product).hasField("name")).isTrue();
		assertThat(((PdxInstance) product).hasField("price")).isTrue();
		assertThat(((PdxInstance) product).hasField("category")).isTrue();
		assertThat(((PdxInstance) product).getField("@type")).isEqualTo(Product.class.getName());
	}

	private void assertPurchaseOrder(PurchaseOrder purchaseOrder, Long expectedId, List<LineItem> expectedLineItems,
			BigDecimal expectedTotal) {

		assertThat(purchaseOrder).isNotNull();
		assertThat(purchaseOrder).hasSize(expectedLineItems.size());
		assertThat(purchaseOrder.getId()).isEqualTo(expectedId);
		assertThat(purchaseOrder.getTotal()).isEqualTo(expectedTotal);

		expectedLineItems.forEach(lineItem ->
			assertLineItem(purchaseOrder.findBy(lineItem.getProduct().getName()).orElse(null), lineItem));
	}

	private void assertLineItem(LineItem actual, LineItem expected) {

		assertThat(actual).isNotNull();
		assertThat(actual.getQuantity()).isEqualTo(expected.getQuantity());
		assertProduct(actual.getProduct(), expected.getProduct());
	}

	private void assertProduct(Product actual, Product expected) {

		assertThat(actual).isNotNull();
		assertThat(actual.getCategory()).isEqualTo(expected.getCategory());
		assertThat(actual.getName()).isEqualTo(expected.getName());
		assertThat(actual.getPrice()).isEqualTo(expected.getPrice());
	}

	// TODO include in API
	private <T> T asType(Object source, Class<T> type) {

		Object target = source instanceof PdxInstance
			? ((PdxInstance) source).getObject()
			: source;

		return target == null ? null
			: Optional.of(target)
			.filter(type::isInstance)
			.map(type::cast)
			.orElseThrow(() -> newIllegalArgumentException("Object [%s] is not an instance of type [%s]",
				ObjectUtils.nullSafeClassName(source), type.getName()));
	}

	private void log(String message, Object... args) {
		System.err.printf(message, args);
		System.err.flush();
	}

	private ObjectMapper newObjectMapper() {

		return new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	// TODO include in API
	private PdxInstance serializeToPdx(RegionService regionService, Object value) {

		PdxInstanceFactory pdxFactory = regionService.createPdxInstanceFactory(value.getClass().getName());

		pdxFactory.writeObject("target", value);

		PdxInstance pdxInstance = pdxFactory.create();

		return Optional.ofNullable(pdxInstance.getField("target"))
			.filter(PdxInstance.class::isInstance)
			.map(PdxInstance.class::cast)
			.orElseThrow(() -> newIllegalArgumentException("Expected to convert Object of type [%s] into PDX",
				value.getClass().getName()));
	}

	@Test
	public void exampleRegionContainsPurchaseOrder() {

		Region<?, ?> example =
			getExampleRegion(newApplicationContext(withResource("data-example-purchaseorder.json")));

		assertThat(example).hasSize(1);

		Object value = example.values().stream()
			.findFirst()
			.orElse(null);

		assertPurchaseOrder(value, 3);

		List<LineItem> expectedLineItems = Arrays.asList(
			LineItem.newLineItem(Product.newProduct("Apple iPad").havingPrice(BigDecimal.valueOf(1499.0d)).in(Product.Category.SHOPPING)).withQuantity(2), // 2998.00
			LineItem.newLineItem(Product.newProduct("Apple iPhone").havingPrice(BigDecimal.valueOf(1249.0d)).in(Product.Category.SHOPPING)).withQuantity(3), // 3747.00
			LineItem.newLineItem(Product.newProduct("Apple iPod").havingPrice(BigDecimal.valueOf(599.0d)).in(Product.Category.SHOPPING)).withQuantity(1) // 599.00
		);

		PurchaseOrder purchaseOrder = (PurchaseOrder) ((PdxInstance) value).getObject();

		assertPurchaseOrder(purchaseOrder, 1L, expectedLineItems, BigDecimal.valueOf(7344.0d));
	}

	@Test
	public void exportFromExampleRegionToJson() {

		try {
			System.setProperty(EXPORT_ENABLED_PROPERTY, Boolean.TRUE.toString());

			StringWriter writer = new StringWriter();

			writerSupplier = () -> writer;

			Region<Long, Customer> example =
				getExampleRegion(newApplicationContext(withResource("data-example.json")));

			assertExampleRegion(example);
			assertThat(example).isEmpty();

			Customer jonDoe = Customer.newCustomer(42L, "Play Doe");

			example.put(jonDoe.getId(), jonDoe);

			assertThat(example).hasSize(1);
			assertThat(example.get(42L)).isEqualTo(jonDoe);

			closeApplicationContext();

			String actualJson = writer.toString();
			String expectedJson = String.format("[{\"@type\":\"%s\",\"id\":42,\"name\":\"Play Doe\"}]",
				jonDoe.getClass().getName());

			assertThat(actualJson).isEqualTo(expectedJson);
		}
		finally {
			System.clearProperty(EXPORT_ENABLED_PROPERTY);
		}
	}

	@Test
	public void exportFromExampleRegionImportsIntoExampleRegion() throws IOException {

		try {
			System.setProperty(EXPORT_ENABLED_PROPERTY, Boolean.TRUE.toString());

			StringWriter writer = new StringWriter();

			writerSupplier = () -> writer;

			Region<Long, PurchaseOrder> example =
				getExampleRegion(newApplicationContext(withResource("data-example.json")));

			assertExampleRegion(example);
			assertThat(example).isEmpty();

			Product golfBalls = Product.newProduct("Titliest ProV1x Golf Balls")
				.havingPrice(BigDecimal.valueOf(34.99d))
				.in(Product.Category.SPECIALTY);

			LineItem lineItem = LineItem.newLineItem(golfBalls)
				.withQuantity(1);

			PurchaseOrder purchaseOrder = new PurchaseOrder()
				.identifiedAs(72L)
				.add(lineItem);

			assertThat(example.put(purchaseOrder.getId(), purchaseOrder)).isNull();
			assertThat(example).hasSize(1);
			assertPurchaseOrder(example.get(purchaseOrder.getId()), purchaseOrder.getId(),
				Collections.singletonList(lineItem), golfBalls.getPrice());

			//log("JSON from JSONFormatter '%s'%n",
			//	JSONFormatter.toJSON(serializeToPdx(example.getRegionService(), purchaseOrder)));

			closeApplicationContext();

			String json = writer.toString();

			assertThat(json).isNotEmpty();

			//log("JSON '%s'%n", json);
			//log("PurchaseOrder [%s]%n",
			//	newObjectMapper().readValue(json.substring(1, json.length() - 1), PurchaseOrder.class));

			System.clearProperty(EXPORT_ENABLED_PROPERTY);

			Resource mockResource = mock(Resource.class, withSettings().lenient());

			doReturn(true).when(mockResource).exists();
			doReturn("MOCK").when(mockResource).getDescription();
			doReturn(new ByteArrayInputStream(json.getBytes())).when(mockResource).getInputStream();

			resourceSupplier = () -> mockResource;

			example = getExampleRegion(newApplicationContext(withResource(null)));

			assertExampleRegion(example);
			assertThat(example).hasSize(1);

			Object value = example.values().stream().findFirst().orElse(null);

			assertThat(value).isInstanceOf(PdxInstance.class);

			assertPurchaseOrder(asType(value, PurchaseOrder.class),
				purchaseOrder.getId(), Collections.singletonList(lineItem), golfBalls.getPrice());
		}
		finally {
			System.clearProperty(EXPORT_ENABLED_PROPERTY);
		}
	}

	@PeerCacheApplication
	@EnablePdx(readSerialized = true)
	static class TestGeodeConfiguration {

		@Bean("Example")
		LocalRegionFactoryBean<Object, Object> exampleRegion(Cache peerCache) {

			LocalRegionFactoryBean<Object, Object> exampleRegion = new LocalRegionFactoryBean<>();

			exampleRegion.setCache(peerCache);

			return exampleRegion;
		}

		@Bean
		JsonCacheDataImporterExporter exampleRegionDataImporter() {

			return new JsonCacheDataImporterExporter() {

				@Override @SuppressWarnings("rawtypes")
				protected Optional<Resource> getResource(@NonNull Region region, String resourcePrefix) {
					return Optional.ofNullable(resourceSupplier.get());
				}

				@NonNull @Override
				Writer newWriter(@NonNull Resource resource) {
					return writerSupplier.get();
				}
			};
		}
	}
}
