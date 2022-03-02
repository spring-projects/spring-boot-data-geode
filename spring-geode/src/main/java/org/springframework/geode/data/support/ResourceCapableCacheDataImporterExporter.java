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
package org.springframework.geode.data.support;

import static org.springframework.geode.core.util.ObjectUtils.initialize;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.geode.core.env.EnvironmentMapAdapter;
import org.springframework.geode.core.io.ResourceReader;
import org.springframework.geode.core.io.ResourceResolver;
import org.springframework.geode.core.io.ResourceWriter;
import org.springframework.geode.core.io.support.ByteArrayResourceReader;
import org.springframework.geode.core.io.support.FileResourceWriter;
import org.springframework.geode.core.io.support.ResourceLoaderResourceResolver;
import org.springframework.geode.core.io.support.ResourcePrefix;
import org.springframework.geode.core.io.support.ResourceUtils;
import org.springframework.geode.core.util.ObjectAwareUtils;
import org.springframework.geode.data.AbstractCacheDataImporterExporter;
import org.springframework.geode.data.CacheDataImporterExporter;
import org.springframework.geode.expression.SmartEnvironmentAccessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AbstractCacheDataImporterExporter} extension and implementation capable of handling and managing import
 * and export {@link Resource Resources}.
 *
 * @author John Blum
 * @see java.io.File
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.expression.Expression
 * @see org.springframework.expression.ExpressionParser
 * @see org.springframework.expression.spel.SpelParserConfiguration
 * @see org.springframework.expression.spel.standard.SpelExpressionParser
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.geode.core.io.ResourceReader
 * @see org.springframework.geode.core.io.ResourceResolver
 * @see org.springframework.geode.core.io.ResourceWriter
 * @see org.springframework.geode.data.AbstractCacheDataImporterExporter
 * @since 1.3.1
 */
@SuppressWarnings("unused")
public abstract class ResourceCapableCacheDataImporterExporter extends AbstractCacheDataImporterExporter
		implements InitializingBean, ResourceLoaderAware {

	protected static final String CACHE_DATA_EXPORT_RESOURCE_LOCATION_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.export.resource.location";

	protected static final String CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME =
		"spring.boot.data.gemfire.cache.data.import.resource.location";

	protected static final String RESOURCE_NAME_PATTERN = "data-%s.json";

	private ExportResourceResolver exportResourceResolver;

	private ImportResourceResolver importResourceResolver;

	private ResourceLoader resourceLoader;

	private ResourceReader resourceReader;

	private ResourceWriter resourceWriter;

	/**
	 * Initializes the export and import {@link ResourceResolver ResourceResolvers} as needed along with
	 * the {@link ResourceReader reader} and {@link ResourceWriter writer} for the {@link Resource}
	 * used on import and export.
	 */
	@Override
	public void afterPropertiesSet() {

		setExportResourceResolver(initialize(getExportResourceResolver(), FileSystemExportResourceResolver::new));
		setImportResourceResolver(initialize(getImportResourceResolver(), ClassPathImportResourceResolver::new));
		setResourceReader(initialize(getResourceReader(), ByteArrayResourceReader::new));
		setResourceWriter(initialize(getResourceWriter(), FileResourceWriter::new));

		Stream.of(getExportResourceResolver(), getImportResourceResolver())
			.forEach(this.newCompositeObjectAwareInitializer());
	}

	Consumer<Object> newCompositeObjectAwareInitializer() {

		return ObjectAwareUtils.applicationContextAwareObjectInitializer(getApplicationContext().orElse(null))
			.andThen(ObjectAwareUtils.environmentAwareObjectInitializer(getEnvironment().orElse(null)))
			.andThen(ObjectAwareUtils.resourceLoaderAwareObjectInitializer(getResourceLoader().orElse(null)));
	}

	/**
	 * Sets a reference to the configured {@link ExportResourceResolver}.
	 *
	 * @param exportResourceResolver configured {@link ExportResourceResolver} used by this importer/exporter
	 * to resolve {@link Resource Resources} on export.
	 * @see ExportResourceResolver
	 */
	@Autowired(required = false)
	public void setExportResourceResolver(@Nullable ExportResourceResolver exportResourceResolver) {
		this.exportResourceResolver = exportResourceResolver;
	}

	/**
	 * Gets the configured reference to the {@link ExportResourceResolver}.
	 *
	 * The configured {@link ExportResourceResolver} is guaranteed to be {@literal non-null} only if the
	 * {@link #afterPropertiesSet()} initialization method was called after construction of this importer/exporter.
	 * This is definitely true in a Spring context.
	 *
	 * @return the configured reference to the {@link ExportResourceResolver}.
	 * @see ExportResourceResolver
	 */
	protected @NonNull ExportResourceResolver getExportResourceResolver() {
		return this.exportResourceResolver;
	}

	/**
	 * Sets a reference to the configured {@link ImportResourceResolver}.
	 *
	 * @param importResourceResolver configured {@link ImportResourceResolver} used by this importer/exporter
	 * to resolve {@link Resource Resources} on import.
	 * @see ImportResourceResolver
	 */
	@Autowired(required = false)
	public void setImportResourceResolver(@Nullable ImportResourceResolver importResourceResolver) {
		this.importResourceResolver = importResourceResolver;
	}

	/**
	 * Gets the configured reference to the {@link ImportResourceResolver}.
	 *
	 * The configured {@link ImportResourceResolver} is guaranteed to be {@literal non-null} only if the
	 * {@link #afterPropertiesSet()} initialization method was called after construction of this importer/exporter.
	 * This is definitely true in a Spring context.
	 *
	 * @return the configured reference to the {@link ImportResourceResolver}.
	 * @see ImportResourceResolver
	 */
	protected @NonNull ImportResourceResolver getImportResourceResolver() {
		return this.importResourceResolver;
	}

	/**
	 * Configures the {@link ResourceLoader} used by this {@link CacheDataImporterExporter} to resolve
	 * and load {@link Resource Resources}.
	 *
	 * @param resourceLoader {@link ResourceLoader} used to resolve and load {@link Resource Resources}.
	 * @see org.springframework.core.io.ResourceLoader
	 */
	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Returns an {@link Optional} reference to the configured {@link ResourceLoader}
	 * used to load {@link Resource Resources}.
	 *
	 * @return an {@link Optional} reference to the configured {@link ResourceLoader}.
	 * @see org.springframework.core.io.ResourceLoader
	 * @see java.util.Optional
	 */
	protected Optional<ResourceLoader> getResourceLoader() {
		return Optional.ofNullable(this.resourceLoader);
	}

	/**
	 * Sets a reference to the configured {@link ResourceReader}.
	 *
	 * @param resourceReader configured {@link ResourceReader} used by this importer/exporter
	 * to read from a {@link Resource} on import.
	 * @see org.springframework.geode.core.io.ResourceReader
	 */
	@Autowired(required = false)
	public void setResourceReader(@Nullable ResourceReader resourceReader) {
		this.resourceReader = resourceReader;
	}

	/**
	 * Gets the configured {@link ResourceReader} used to read data from a {@link Resource} on {@literal import}.
	 *
	 * The configured {@link ResourceReader} is guaranteed to be {@literal non-null} only if the
	 * {@link #afterPropertiesSet()} initialization method was called after construction of this importer/exporter.
	 * This is definitely true in a Spring context.
	 *
	 * @return the configured {@link ResourceReader}.
	 * @see org.springframework.geode.core.io.ResourceReader
	 */
	protected @NonNull ResourceReader getResourceReader() {
		return this.resourceReader;
	}

	/**
	 * Set a reference to the configured {@link ResourceWriter}.
	 *
	 * @param resourceWriter configured {@link ResourceWriter} used by this importer/exporter
	 * to write to a {@link Resource} on export.
	 * @see org.springframework.geode.core.io.ResourceWriter
	 */
	@Autowired(required = false)
	public void setResourceWriter(@Nullable ResourceWriter resourceWriter) {
		this.resourceWriter = resourceWriter;
	}

	/**
	 * Gets the configured {@link ResourceWriter} used to write data to the {@link Resource} on {@literal export}.
	 *
	 * The configured {@link ResourceWriter} is guaranteed to be {@literal non-null} only if the
	 * {@link #afterPropertiesSet()} initialization method was called after construction of this importer/exporter.
	 * This is definitely true in a Spring context.
	 *
	 * @return the configured {@link ResourceWriter}.
	 * @see org.springframework.geode.core.io.ResourceWriter
	 */
	protected @NonNull ResourceWriter getResourceWriter() {
		return this.resourceWriter;
	}

	/**
	 * {@link ResourceResolver} interface extension used to resolve {@link GemFireCache cache}
	 * {@link Resource Resources}.
	 *
	 * @see org.springframework.geode.core.io.ResourceResolver
	 * @see org.apache.geode.cache.GemFireCache
	 * @see org.apache.geode.cache.Region
	 */
	@FunctionalInterface
	protected interface CacheResourceResolver extends ResourceResolver {

		/**
		 * Tries to resolve a {@link Resource} to a {@link String location} containing data for the given {@link Region}.
		 * The {@link Region} is used to determine the {@link String location} of the {@link Resource} to load.
		 *
		 * @param region {@link Region} used to resolve the {@link Resource}.
		 * @return an {@link Optional} {@link Resource} handle to a {@link String location} containing data
		 * for the given {@link Region}.
		 * @see org.springframework.core.io.Resource
		 * @see org.apache.geode.cache.Region
		 * @see java.util.Optional
		 */
		Optional<Resource> resolve(@NonNull Region<?, ?> region);

		/**
		 * @inheritDoc
		 */
		@Override
		default Optional<Resource> resolve(@NonNull String location) {
			return Optional.empty();
		}
	}

	/**
	 * Abstract base class containing functionality common to all {@link GemFireCache cache} based
	 * {@link ResourceResolver ResourceResolvers}, whether for import or export.
	 *
	 * @see org.springframework.geode.core.io.support.ResourceLoaderResourceResolver
	 * @see org.springframework.context.ApplicationContextAware
	 * @see org.springframework.context.EnvironmentAware
	 * @see CacheResourceResolver
	 */
	protected static abstract class AbstractCacheResourceResolver extends ResourceLoaderResourceResolver
			implements ApplicationContextAware, CacheResourceResolver, EnvironmentAware {

		private ApplicationContext applicationContext;

		private Environment environment;

		private final ExpressionParser expressionParser;

		private final Logger logger = LoggerFactory.getLogger(getClass());

		private final Map<String, Expression> compiledExpressions;

		private final SimpleEvaluationContext.Builder evaluationContextBuilder;

		/**
		 * Constructs a new instance of {@link AbstractCacheResourceResolver}.
		 *
		 * This constructor initializes the SpEL objects used to parse and evaluate SpEL expressions in order to
		 * fully qualify and resolve {@link Resource} {@link String locations} defined as properties
		 * in Spring Boot {@literal application.properties} for Import & Export {@link Resource Resources}.
		 *
		 * @see #newExpressionParser()
		 * @see #newEvaluationContextBuilder()
		 */
		public AbstractCacheResourceResolver() {

			this.expressionParser = newExpressionParser();
			this.evaluationContextBuilder = newEvaluationContextBuilder();
			this.compiledExpressions = new ConcurrentHashMap<>();
		}

		private ExpressionParser newExpressionParser() {

			ClassLoader classLoader = getApplicationContext()
				.map(ApplicationContext::getClassLoader)
				.orElseGet(ClassUtils::getDefaultClassLoader);

			return new SpelExpressionParser(new SpelParserConfiguration(SpelCompilerMode.MIXED, classLoader));
		}

		private SimpleEvaluationContext.Builder newEvaluationContextBuilder() {

			PropertyAccessor[] propertyAccessors = {
				new BeanFactoryAccessor(),
				DataBindingPropertyAccessor.forReadOnlyAccess(),
				SmartEnvironmentAccessor.create()
			};

			SimpleEvaluationContext.Builder builder = SimpleEvaluationContext.forPropertyAccessors(propertyAccessors)
				.withInstanceMethods();

			String conversionServiceBeanName = ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;

			return getApplicationContext()
				.filter(applicationContext -> applicationContext.containsBean(conversionServiceBeanName))
				.map(applicationContext -> applicationContext.getBean(conversionServiceBeanName, ConversionService.class))
				.map(builder::withConversionService)
				.orElse(builder);
		}

		/**
		 * Constructs a new {@link EvaluationContext} used during the evaluation of SpEL {@link String expressions}.
		 *
		 * @return a new {@link EvaluationContext}; never {@literal null}.
		 * @see org.springframework.expression.EvaluationContext
		 */
		protected @NonNull EvaluationContext newEvaluationContext() {
			return this.evaluationContextBuilder.build();
		}

		/**
		 * Configures a reference to the Spring {@link ApplicationContext}.
		 *
		 * @param applicationContext reference to the {@link ApplicationContext}.
		 * @see org.springframework.context.ApplicationContext
		 */
		@Override
		public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		/**
		 * Returns an {@link Optional} reference to a Spring {@link ApplicationContext}.
		 *
		 * @return an {@link Optional} reference to a Spring {@link ApplicationContext}.
		 * @see org.springframework.context.ApplicationContext
		 * @see java.util.Optional
		 */
		protected Optional<ApplicationContext> getApplicationContext() {
			return Optional.ofNullable(this.applicationContext);
		}

		/**
		 * Configures a reference to the Spring {@link Environment}.
		 *
		 * @param environment reference to the {@link Environment}.
		 * @see org.springframework.core.env.Environment
		 */
		@Override
		public void setEnvironment(Environment environment) {
			this.environment = environment;
		}

		/**
		 * Returns an {@link Optional} reference to the Spring {@link Environment}.
		 *
		 * @return an {@link Optional} reference to the Spring {@link Environment}.
		 * @see org.springframework.core.env.Environment
		 * @see java.util.Optional
		 */
		protected Optional<Environment> getEnvironment() {
			return Optional.ofNullable(this.environment);
		}

		/**
		 * Gets the configured {@link ExpressionParser} used to parse SpEL {@link String expressions}.
		 *
		 * @return the configured {@link ExpressionParser}; never {@literal null}.
		 * @see org.springframework.expression.ExpressionParser
		 */
		protected @NonNull ExpressionParser getExpressionParser() {
			return this.expressionParser;
		}

		/**
		 * Return the configured {@link Logger} to log messages.
		 *
		 * @return the configured {@link Logger}.
		 * @see org.slf4j.Logger
		 */
		protected Logger getLogger() {
			return this.logger;
		}

		/**
		 * Gets the configured {@link ParserContext} used by the {@link ExpressionParser} to identify SpEL expressions.
		 *
		 * @return the configured {@link ParserContext}.
		 * @see org.springframework.expression.ParserContext
		 */
		protected @NonNull ParserContext getParserContext() {
			return ParserContext.TEMPLATE_EXPRESSION;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		protected boolean isQualified(@Nullable Resource resource) {
			return super.isQualified(resource) && resource.exists();
		}

		/**
		 * Determines a fully-qualified {@link String resource location} for the given {@link Region}.
		 *
		 * @param region {@link Region} to evaluate; must not be {@literal null}.
		 * @return a fully-qualified {@link String resource location} for the given {@link Region}.
		 * @see Region
		 * @see #getResourceName(Region)
		 * @see #getResourcePath()
		 */
		protected @NonNull String getFullyQualifiedResourceLocation(@NonNull Region<?, ?> region) {
			return String.format("%1$s%2$s", getResourcePath(), getResourceName(region));
		}

		/**
		 * Determines the {@link String location} of a {@link Resource} for the given {@link Region}.
		 *
		 * @param region {@link Region} used to locate the desired {@link Resource}; must not be {@literal null}.
		 * @return a {@link Resource} {@link String location} for the given {@link Region}.
		 * @throws IllegalArgumentException if {@link Region} is {@literal null}.
		 * @see org.apache.geode.cache.Region
		 */
		protected @NonNull String getResourceLocation(@NonNull Region<?, ?> region, @NonNull String propertyName) {

			Assert.notNull(region, "Region must not be null");
			Assert.hasText(propertyName, () -> String.format("Property name [%s] must be specified", propertyName));

			return getEnvironment()
				.filter(environment -> environment.containsProperty(propertyName))
				.map(environment -> environment.getProperty(propertyName))
				.filter(StringUtils::hasText)
				.map(resourceLocation -> evaluate(resourceLocation, region))
				.orElseGet(() -> getFullyQualifiedResourceLocation(region));
		}

		/**
		 * Evaluates the given SpEL {@link String expression}.
		 *
		 * @param expressionString {@link String} containing the SpEL expression to evaluate; must not be {@literal null}.
		 * @param region {@link Region} used to resolve {@literal regionName} variable references
		 * in the {@link String expression}; must not be {@literal null}.
		 * @return the value of the evaluated {@link String expression}.
		 * @see org.springframework.expression.Expression#getValue(EvaluationContext, Object)
		 * @see org.apache.geode.cache.Region
		 * @see #parse(String)
		 */
		protected @Nullable String evaluate(@NonNull String expressionString, @NonNull Region<?, ?> region) {

			EvaluationContext evaluationContext = newEvaluationContext();

			evaluationContext.setVariable("regionName", region.getName().toLowerCase());

			getEnvironment().ifPresent(environment ->
				evaluationContext.setVariable("env", EnvironmentMapAdapter.from(environment)));

			Expression expression = parse(expressionString);

			Object value = getApplicationContext()
				.map(applicationContext -> expression.getValue(evaluationContext, applicationContext))
				.orElseGet(() -> expression.getValue(evaluationContext));

			return value != null ? value.toString() : null;
		}

		/**
		 * Parses the given {@link String expressionString}.
		 *
		 * This method will cache parsed {@link Expression Expressions} to speed up the evaluation process.
		 *
		 * @param expressionString {@link String} containing the SpEL expression to parse.
		 * @return an {@link Expression} object parsed from the given {@link String expression}.
		 * @see org.springframework.expression.ExpressionParser#parseExpression(String, ParserContext)
		 * @see org.springframework.expression.Expression
		 * @see #getExpressionParser()
		 * @see #getParserContext()
		 */
		protected Expression parse(String expressionString) {
			return this.compiledExpressions.computeIfAbsent(expressionString,
				it -> getExpressionParser().parseExpression(it, getParserContext()));
		}

		/**
		 * Determines a {@link String resource name} for the given {@link Region}.
		 *
		 * The default implementation bases the {@link String resource name} on
		 * the {@link Region#getName() Region's lowercase name}.
		 *
		 * @param region {@link Region} to evaluate; must not be {@literal null}.
		 * @return a {@link String resource name} for the given {@link Region}.
		 * @see Region
		 * @see #getResourceName(String)
		 */
		protected @NonNull String getResourceName(@NonNull Region<?, ?> region) {
			return getResourceName(region.getName().toLowerCase());
		}

		/**
		 * Determines a {@link String resource name} for the given {@link String name}.
		 *
		 * @param name {@link String} containing the name to evaluate; must not be {@literal null}.
		 * @return a {@link String resource name} from the given {@link String name}.
		 */
		protected @NonNull String getResourceName(@NonNull String name) {
			return String.format(RESOURCE_NAME_PATTERN, name);
		}

		/**
		 * Get the {@link String base path} for the targeted {@link Resource}.
		 *
		 * @return the {@link String base path} for the targeted {@link Resource}.
		 */
		protected abstract @NonNull String getResourcePath();

	}

	/**
	 * Marker interface extending {@link CacheResourceResolver} for cache data exports.
	 *
	 * @see org.springframework.geode.core.io.ResourceResolver
	 * @see CacheResourceResolver
	 */
	@FunctionalInterface
	public interface ExportResourceResolver extends CacheResourceResolver { }

	/**
	 * Abstract base class extended by export {@link CacheResourceResolver} implementations, providing a template
	 * to resolve the {@link Resource} used for export.
	 *
	 * @see AbstractCacheResourceResolver
	 * @see ExportResourceResolver
	 */
	public static abstract class AbstractExportResourceResolver extends AbstractCacheResourceResolver
			implements ExportResourceResolver {

		/**
		 * @inheritDoc
		 */
		@Override
		public Optional<Resource> resolve(@NonNull Region<?, ?> region) {

			Assert.notNull(region, "Region must not be null");

			String resourceLocation = getResourceLocation(region, CACHE_DATA_EXPORT_RESOURCE_LOCATION_PROPERTY_NAME);

			Optional<Resource> resource = resolve(resourceLocation);

			boolean writable = resource.filter(ResourceUtils::isWritable).isPresent();

			if (!writable) {
				getLogger().warn("Resource [{}] for Region [{}] is not writable",
					resourceLocation, region.getFullPath());
			}

			return resource;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		protected @Nullable Resource onMissingResource(@Nullable Resource resource, @NonNull String location) {

			getLogger().warn("Resource [{}] at location [{}] does not exist; will try to create it on export",
				ResourceUtils.nullSafeGetDescription(resource), location);

			return resource;
		}
	}

	/**
	 * Resolves the {@link Resource} used for {@literal export} from the {@literal filesystem}.
	 */
	public static class FileSystemExportResourceResolver extends AbstractExportResourceResolver {

		@Override
		protected @NonNull String getResourcePath() {
			return String.format("%1$s%2$s%3$s", ResourcePrefix.FILESYSTEM_URL_PREFIX.toUrlPrefix(),
				System.getProperty("user.dir"), File.separator);
		}
	}

	/**
	 * Marker interface extending {@link CacheResourceResolver} for cache data imports.
	 *
	 * @see org.springframework.geode.core.io.ResourceResolver
	 * @see CacheResourceResolver
	 */
	@FunctionalInterface
	public interface ImportResourceResolver extends CacheResourceResolver { }

	/**
	 * Abstract base class extended by import {@link ResourceResolver} implementations, providing a template
	 * to resolve the {@link Resource} to import.
	 *
	 * @see AbstractCacheResourceResolver
	 * @see ImportResourceResolver
	 */
	public static abstract class AbstractImportResourceResolver extends AbstractCacheResourceResolver
			implements ImportResourceResolver {

		/**
		 * @inheritDoc
		 */
		@Override
		public Optional<Resource> resolve(@NonNull Region<?, ?> region) {

			Assert.notNull(region, "Region must not be null");

			String resourceLocation = getResourceLocation(region, CACHE_DATA_IMPORT_RESOURCE_LOCATION_PROPERTY_NAME);

			Optional<Resource> resource = resolve(resourceLocation);

			boolean exists = resource.isPresent();
			boolean readable = exists && resource.filter(Resource::isReadable).isPresent();

			if (!exists) {
				getLogger().warn("Resource [{}] for Region [{}] could not be found; skipping import for Region",
					resourceLocation, region.getFullPath());
			}
			else {
				Assert.state(readable, () -> String.format("Resource [%1$s] for Region [%2$s] is not readable",
					resourceLocation, region.getFullPath()));
			}

			return resource;
		}

		@Nullable @Override
		protected Resource onMissingResource(@Nullable Resource resource, @NonNull String location) {

			getLogger().warn("Resource [{}] at location [{}] does not exist; skipping import",
				ResourceUtils.nullSafeGetDescription(resource), location);

			return null;
		}
	}

	/**
	 * Resolves the {@link Resource} to {@literal import} from the {@literal classpath}.
	 */
	public static class ClassPathImportResourceResolver extends AbstractImportResourceResolver {

		@Override
		protected @NonNull String getResourcePath() {
			return ResourcePrefix.CLASSPATH_URL_PREFIX.toUrlPrefix();
		}
	}
}
