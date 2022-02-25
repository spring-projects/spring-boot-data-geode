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
package org.springframework.geode.data.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.apache.geode.cache.Scope;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.JSONFormatterException;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.PdxInstanceFactory;
import org.apache.geode.pdx.PdxSerializationException;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.geode.core.io.ResourceWriteException;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.geode.data.support.ResourceCapableCacheDataImporterExporter.ImportResourceResolver;
import org.springframework.geode.pdx.PdxInstanceBuilder;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import example.app.crm.model.Customer;
import example.app.pos.model.LineItem;
import example.app.pos.model.Product;
import example.app.pos.model.PurchaseOrder;

/**
 * Integration Tests for {@link JsonCacheDataImporterExporter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.node.ObjectNode
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionService
 * @see org.apache.geode.pdx.JSONFormatter
 * @see org.apache.geode.pdx.PdxInstance
 * @see org.apache.geode.pdx.PdxInstanceFactory
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.Resource
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.geode.pdx.PdxInstanceBuilder
 * @since 1.3.0
 */
@SuppressWarnings("unused")
public class JsonCacheDataImporterExporterIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

	private static final String EXPORT_ENABLED_PROPERTY = "spring.boot.data.gemfire.cache.data.export.enabled";
	private static final String EXPORT_RESOURCE_LOCATION_PROPERTY = "spring.boot.data.gemfire.cache.data.export.resource.location";
	private static final String IMPORT_ENABLED_PROPERTY = "spring.boot.data.gemfire.cache.data.import.enabled";
	private static final String IMPORT_RESOURCE_LOCATION_PROPERTY = "spring.boot.data.gemfire.cache.data.import.resource.location";

	private static volatile Supplier<Boolean> exportEnabledSupplier;
	private static volatile Supplier<Boolean> importEnabledSupplier;
	private static volatile Supplier<ImportResourceResolver> importResourceResolverSupplier;
	private static volatile Supplier<String> resourceLocationSupplier;
	private static volatile Supplier<StringWriter> resourceWriterSupplier;

	@Before
	public void initializeSuppliers() {

		exportEnabledSupplier = () -> false;
		importEnabledSupplier = () -> true;
		importResourceResolverSupplier = () -> null;
		resourceLocationSupplier = () -> "";
		resourceWriterSupplier = StringWriter::new;
	}

	@Override
	protected ConfigurableApplicationContext processBeforeRefresh(ConfigurableApplicationContext applicationContext) {

		applicationContext = super.processBeforeRefresh(applicationContext);

		MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();

		MockPropertySource mockPropertySource = new MockPropertySource(getClass().getName().concat(".MockPropertySource"))
			.withProperty(EXPORT_ENABLED_PROPERTY, exportEnabledSupplier.get())
			.withProperty(EXPORT_RESOURCE_LOCATION_PROPERTY, resourceLocationSupplier.get())
			.withProperty(IMPORT_ENABLED_PROPERTY, importEnabledSupplier.get())
			.withProperty(IMPORT_RESOURCE_LOCATION_PROPERTY, resourceLocationSupplier.get());

		propertySources.addFirst(mockPropertySource);

		return applicationContext;
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

		resourceLocationSupplier = () -> "classpath:data-#{#regionName}-jondoe.json";

		Region<?, ?> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

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

		resourceLocationSupplier = () -> "classpath:data-#{#regionName}-doefamily.json";

		Region<?, ?> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

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

	private void log(String message, Object... args) {
		System.err.printf(message, args);
		System.err.flush();
	}

	private ObjectMapper newObjectMapper() {

		return new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.findAndRegisterModules();
	}

	private PdxInstance serializeToPdx(RegionService regionService, Object value) {

		return PdxInstanceBuilder.create(regionService)
			.from(value)
			.create();
	}

	private PdxInstance serializeToPdx(RegionService regionService, Customer customer) {

		PdxInstanceFactory pdxFactory = regionService.createPdxInstanceFactory(customer.getClass().getName());

		//pdxFactory.writeString("@type", customer.getClass().getName());
		pdxFactory.writeLong("id", customer.getId());
		pdxFactory.writeString("name", customer.getName());
		pdxFactory.markIdentityField("id");

		return pdxFactory.create();
	}

	@Test
	public void exampleRegionContainsComplexPurchaseOrderType() {

		resourceLocationSupplier = () -> "classpath:data-#{#regionName}-purchaseorder.json";

		Region<?, ?> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

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

			StringWriter writer = new StringWriter();

			exportEnabledSupplier = () -> true;
			importEnabledSupplier = () -> false;
			resourceWriterSupplier = () -> writer;

			Region<Long, Customer> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

			assertExampleRegion(example);
			assertThat(example).isEmpty();

			Customer playDoe = Customer.newCustomer(42L, "Play Doe");

			example.put(playDoe.getId(), playDoe);

			assertThat(example).hasSize(1);
			assertThat(example.get(42L)).isEqualTo(playDoe);

			closeApplicationContext();

			String actualJson = StringUtils.trimAllWhitespace(writer.toString());

			String expectedJson = String.format("[{\"@type\":\"%s\",\"id\":42,\"name\":\"PlayDoe\"}]",
				playDoe.getClass().getName());

			assertThat(actualJson).isEqualTo(expectedJson);
		}
		finally {
			System.clearProperty(EXPORT_ENABLED_PROPERTY);
		}
	}

	@Test
	public void exportFromExampleRegionImportsIntoExampleRegion() throws IOException {

		// EXPORT
		StringWriter writer = new StringWriter();

		exportEnabledSupplier = () -> true;
		importEnabledSupplier = () -> false;
		resourceWriterSupplier = () -> writer;

		Region<Long, PurchaseOrder> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

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
		//log("PurchaseOrder from JSON [%s]%n",
		//	newObjectMapper().readValue(json.substring(1, json.length() - 1), PurchaseOrder.class));

		// IMPORT
		Resource mockResource = mock(Resource.class, withSettings().lenient());

		doReturn("MOCK").when(mockResource).getDescription();
		doReturn(new ByteArrayInputStream(json.getBytes())).when(mockResource).getInputStream();

		exportEnabledSupplier = () -> false;
		importEnabledSupplier = () -> true;
		importResourceResolverSupplier = () -> (ImportResourceResolver) region -> Optional.of(mockResource);

		example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class,
			TestImportResourceResolverGeodeConfiguration.class));

		assertExampleRegion(example);
		assertThat(example).hasSize(1);

		Object value = example.values().stream().findFirst().orElse(null);

		assertThat(value).isInstanceOf(PdxInstance.class);

		assertPurchaseOrder(ObjectUtils.asType(value, PurchaseOrder.class),
			purchaseOrder.getId(), Collections.singletonList(lineItem), golfBalls.getPrice());
	}

	@Test
	public void exportImportWithRegionContainingObjectsAndPdxInstances() throws IOException {

		// EXPORT
		StringWriter writer = new StringWriter();

		exportEnabledSupplier = () -> true;
		importEnabledSupplier = () -> false;
		resourceWriterSupplier = () -> writer;

		Region<Object, Object> example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class));

		assertExampleRegion(example);
		assertThat(example).isEmpty();

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");
		Customer janeDoe = Customer.newCustomer(2L, "JaneDoe");

		PdxInstance janeDoePdx = serializeToPdx(example.getRegionService(), janeDoe);

		assertThat(example.put(jonDoe.getId(), jonDoe)).isNull();
		assertThat(example.put(janeDoe.getId(), janeDoePdx)).isNull();
		assertThat(example).hasSize(2);
		assertThat(example.get(jonDoe.getId())).isEqualTo(jonDoe);
		assertThat(example.get(janeDoe.getId())).isInstanceOf(PdxInstance.class);

		closeApplicationContext();

		String json = writer.toString();

		assertThat(json).isNotEmpty();

		// IMPORT
		Resource mockResource = mock(Resource.class, withSettings().lenient());

		doReturn("MOCK").when(mockResource).getDescription();
		doReturn(new ByteArrayInputStream(json.getBytes())).when(mockResource).getInputStream();

		exportEnabledSupplier = () -> false;
		importEnabledSupplier = () -> true;
		importResourceResolverSupplier = () -> region -> Optional.of(mockResource);

		example = getExampleRegion(newApplicationContext(TestGeodeConfiguration.class,

			TestImportResourceResolverGeodeConfiguration.class));
		assertExampleRegion(example);
		assertThat(example).hasSize(2);

		for (Customer doe : Arrays.asList(jonDoe, janeDoe)) {

			Object value = example.get(doe.getId().byteValue());

			assertThat(value).isInstanceOf(PdxInstance.class);
			assertThat(((PdxInstance) value).getObject()).isEqualTo(doe);
		}
	}

	// APACHE GEODE BUG 1!!!
	@Test(expected = JSONFormatterException.class)
	public void geodeJsonFormatterFromJsonCannotParseArrays() throws IOException {

		try {

			byte[] json = FileCopyUtils.copyToByteArray(
				new ClassPathResource("data-example-doefamily.json").getInputStream());

			assertThat(json).isNotNull();
			assertThat(json).isNotEmpty();

			JSONFormatter.fromJSON(json);
		}
		catch (JSONFormatterException expected) {

			// Caused because the JSONFormatter.fromJSON(..) method's JsonParser is not configured correctly!

			assertThat(expected).hasMessageStartingWith("Could not parse JSON document");
			assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
			assertThat(expected.getCause()).hasMessageStartingWith("Array start called when state is NONE");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
	}

	// APACHE GEODE BUG 2!!!
	@Test
	public void geodeJsonFormatterToJsonDoesNotGenerateAtTypeJsonObjectPropertyFromPdxInstanceGetClassName() {

		Cache peerCache = newApplicationContext(TestGeodeConfiguration.class).getBean(Cache.class);

		assertThat(peerCache).isNotNull();

		Customer doeDoe = Customer.newCustomer(13L, "Doe Doe");

		PdxInstance pdx = serializeToPdx(peerCache, doeDoe);

		assertThat(pdx).isNotNull();
		assertThat(pdx.getClassName()).isEqualTo(doeDoe.getClass().getName());

		String json = JSONFormatter.toJSON(pdx);

		assertThat(json).isNotEmpty();
		assertThat(json).contains("\"name\":\"Doe Doe\"");

		PdxInstance pdxFromJson = JSONFormatter.fromJSON(json);

		assertThat(pdxFromJson).isNotNull();
		assertThat(pdxFromJson.getClassName()).isEqualTo(JSONFormatter.JSON_CLASSNAME);
		assertThat(pdxFromJson.hasField("@type")).isFalse();

		Object value = pdxFromJson.getObject();

		assertThat(value).isInstanceOf(PdxInstance.class);
		assertThat(value).isNotEqualTo(doeDoe);

		// Causes ClassCastException!
		// Bug caused by the JSONFormatter.toJSON(:PdxInstance) method not properly setting the '@type' JSON object
		// property from the PdxInstance.getClassName() when the class name is a valid Java class!
		//Customer jonDoeAgain = (Customer) value;
	}

	// APACHE GEODE BUG 3!!!
	@Test(expected = PdxSerializationException.class)
	public void geodePdxInstanceObjectMapperCannotDeserializeJava8Types() {

		try {

			Cache peerCache = newApplicationContext(TestGeodeConfiguration.class).getBean(Cache.class);

			ObjectMapper objectMapper = newObjectMapper();

			TimedType value = TimedType.create().with(LocalDate.now());

			ObjectNode objectNode = objectMapper.valueToTree(value);

			objectNode.put("@type", value.getClass().getName());

			String json = objectNode.toString();

			PdxInstance pdx = JSONFormatter.fromJSON(json);

			// BOOM!
			pdx.getObject();
		}
		catch (PdxSerializationException expected) {

			// Caused because the PdxInstance ObjectMapper is not properly configured (to findAndRegisterModules()
			// or Jackson Module Extensions on the classpath)!

			assertThat(expected).hasMessageStartingWith("Could not deserialize as java class '%s'",
				TimedType.class.getName());
			assertThat(expected.getCause()).isInstanceOf(InvalidDefinitionException.class);
			assertThat(expected.getCause())
				.hasMessageStartingWith("Java 8 date/time type `java.time.LocalDate` not supported by default:"
					+ " add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
	}

	// APACHE GEODE BUG 4!!!
	@Test(expected = PdxSerializationException.class)
	public void geodePdxInstanceObjectMapperCannotDeserializeTypedJsonObjects()
			throws JsonProcessingException {

		try {

			Cache peerCache = newApplicationContext(TestGeodeConfiguration.class).getBean(Cache.class);

			PurchaseOrder purchaseOrder = new PurchaseOrder()
				.add(LineItem.newLineItem(Product.newProduct("Test Product")
					.havingPrice(BigDecimal.valueOf(39.99))
					.in(Product.Category.UNSOUGHT))
					.withQuantity(2))
				.identifiedAs(1L);

			ObjectMapper objectMapper = newObjectMapper()
				.activateDefaultTypingAsProperty(new DefaultBaseTypeLimitingValidator(),
					ObjectMapper.DefaultTyping.EVERYTHING, "@type");

			String json = objectMapper.writeValueAsString(purchaseOrder);

			assertThat(json).isNotEmpty();
			assertThat(json).describedAs("Actual JSON [%s]", json)
				.contains(String.format("\"@type\":\"%s\"", purchaseOrder.getClass().getName()));

			PdxInstance pdx = JSONFormatter.fromJSON(json);

			// BOOM!
			pdx.getObject();
		}
		catch (PdxSerializationException expected) {

			// Caused because the PdxInstance.getObject() method's ObjectMapper is not properly configured!

			assertThat(expected).hasMessageStartingWith("Could not deserialize as java class '%s'",
				PurchaseOrder.class.getName());
			assertThat(expected.getCause()).isInstanceOf(MismatchedInputException.class);
			assertThat(expected.getCause())
				.hasMessageStartingWith("Cannot deserialize value of type `java.lang.Long` from Array value (token `JsonToken.START_ARRAY`)");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
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
		JsonCacheDataImporterExporter exampleRegionDataImporterExporter() {
			return new JsonCacheDataImporterExporter();
		}

		@Bean
		ResourceWriter stringWriterResourceWriter() {

			return (resource, data) -> {

				String json = new String(data);

				Writer writer = resourceWriterSupplier.get();

				try {
					writer.write(json, 0, data.length);
					writer.flush();
				}
				catch (IOException cause) {
					throw new ResourceWriteException(String.format("Failed to write data [%s] to Resource [%s]",
						json, resource.getDescription()), cause);
				}
			};
		}
	}

	@Configuration
	static class TestImportResourceResolverGeodeConfiguration {

		@Bean
		ImportResourceResolver testImportResourceResolver() {
			return importResourceResolverSupplier.get();
		}
	}

	public static class TimedType {

		public static TimedType create() {
			return new TimedType();
		}

		private LocalDate time;

		public LocalDate getTime() {
			return this.time;
		}

		public TimedType with(LocalDate time) {
			this.time = time;
			return this;
		}
	}
}
