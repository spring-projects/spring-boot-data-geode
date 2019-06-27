/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.geode.core.env;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.geode.core.env.support.CloudCacheService;
import org.springframework.geode.core.env.support.Service;
import org.springframework.geode.core.env.support.User;
import org.springframework.geode.core.util.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * The {@link VcapPropertySource} class is a Spring {@link PropertySource} to process
 * {@literal VCAP} environment properties in Pivotal CloudFoundry.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see java.net.URL
 * @see java.util.Properties
 * @see java.util.function.Predicate
 * @see org.springframework.core.env.EnumerablePropertySource
 * @see org.springframework.core.env.Environment
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.geode.core.env.support.CloudCacheService
 * @see org.springframework.geode.core.env.support.Service
 * @see org.springframework.geode.core.env.support.User
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class VcapPropertySource extends PropertySource<EnumerablePropertySource<?>> implements Iterable<String> {

	private static final String CLOUD_CACHE_TAG_NAME = "cloudcache";
	private static final String GEMFIRE_TAG_NAME = "gemfire";
	private static final String THIS_PROPERTY_SOURCE_NAME = "boot.data.gemfire.vcap";
	private static final String VCAP_APPLICATION_PROPERTY = "vcap.application.";
	private static final String VCAP_APPLICATION_NAME_PROPERTY = VCAP_APPLICATION_PROPERTY + "name";
	private static final String VCAP_APPLICATION_URIS_PROPERTY = VCAP_APPLICATION_PROPERTY + "uris";
	private static final String VCAP_PROPERTY_SOURCE_NAME = "vcap";
	private static final String VCAP_SERVICES_PROPERTY = "vcap.services.";
	private static final String VCAP_SERVICES_SERVICE_NAME_GFSH_URL_PROPERTY = VCAP_SERVICES_PROPERTY + "%s.credentials.urls.gfsh";
	private static final String VCAP_SERVICES_SERVICE_NAME_LOCATORS_PROPERTY = VCAP_SERVICES_PROPERTY + "%s.credentials.locators";
	private static final String VCAP_SERVICES_SERVICE_NAME_USERS_PROPERTY = VCAP_SERVICES_PROPERTY + "%s.credentials.users[%d]";

	private static final Predicate<Object> CLOUD_CACHE_SERVICE_PREDICATE =
		propertyValue -> String.valueOf(propertyValue).toLowerCase().contains(CLOUD_CACHE_TAG_NAME);

	private static final Predicate<Object> GEMFIRE_SERVICE_PREDICATE =
		propertyValue -> String.valueOf(propertyValue).toLowerCase().contains(GEMFIRE_TAG_NAME);

	private static final Predicate<Object> CLOUD_CACHE_AND_GEMFIRE_SERVICE_PREDICATE =
		CLOUD_CACHE_SERVICE_PREDICATE.and(GEMFIRE_SERVICE_PREDICATE);

	private static final Predicate<String> VCAP_APPLICATION_PROPERTIES_PREDICATE =
		propertyName -> String.valueOf(propertyName).trim().toLowerCase().startsWith(VCAP_APPLICATION_PROPERTY);

	private static final Predicate<PropertySource> VCAP_REQUIRED_PROPERTIES_PREDICATE =
		propertySource -> propertySource.containsProperty(VCAP_APPLICATION_NAME_PROPERTY)
			&& propertySource.containsProperty(VCAP_APPLICATION_URIS_PROPERTY);

	private static final Predicate<String> VCAP_SERVICES_PROPERTIES_PREDICATE =
		propertyName -> String.valueOf(propertyName).trim().toLowerCase().startsWith(VCAP_SERVICES_PROPERTY);

	public static VcapPropertySource from(Environment environment) {

		return Optional.ofNullable(environment)
			.filter(ConfigurableEnvironment.class::isInstance)
			.map(ConfigurableEnvironment.class::cast)
			.map(ConfigurableEnvironment::getPropertySources)
			.map(propertySources -> propertySources.get(VCAP_PROPERTY_SOURCE_NAME))
			.map(VcapPropertySource::from)
			.orElseThrow(() -> newIllegalArgumentException(
				"Environment was not configurable or does not contain an enumerable [%s] PropertySource",
					VCAP_PROPERTY_SOURCE_NAME));
	}

	public static VcapPropertySource from(Properties properties) {

		return Optional.ofNullable(properties)
			.map(it -> new PropertiesPropertySource(THIS_PROPERTY_SOURCE_NAME, properties))
			.filter(VCAP_REQUIRED_PROPERTIES_PREDICATE)
			.map(VcapPropertySource::new)
			.orElseThrow(() -> newIllegalArgumentException("Properties are required"));
	}

	public static VcapPropertySource from(PropertySource<?> propertySource) {

		return Optional.ofNullable(propertySource)
			.filter(it -> VCAP_PROPERTY_SOURCE_NAME.equals(it.getName()))
			.filter(VCAP_REQUIRED_PROPERTIES_PREDICATE)
			.filter(EnumerablePropertySource.class::isInstance)
			.map(EnumerablePropertySource.class::cast)
			.map(VcapPropertySource::new)
			.orElseThrow(() -> newIllegalArgumentException(
				"An EnumerablePropertySource named [%s] containing VCAP properties is required",
					VCAP_PROPERTY_SOURCE_NAME));
	}

	private Predicate<Object> vcapServicePredicate;

	/**
	 * Constructs a new {@link PropertySource} from the existing, required {@link EnumerablePropertySource} instance
	 * with the default name, {@literal boot.data.gemfire.vcap}, containing the {@literal VCAP} environment variable
	 * configuration.
	 *
	 * @param propertySource existing, required {@link EnumerablePropertySource} containing the {@literal VCAP}
	 * environment variables.
	 * @throws IllegalArgumentException if the {@literal EnumerablePropertySource} is {@literal null}.
	 * @see org.springframework.core.env.EnumerablePropertySource
	 */
	private VcapPropertySource(EnumerablePropertySource<?> propertySource) {
		super(THIS_PROPERTY_SOURCE_NAME, propertySource);
	}

	protected Set<String> findAllPropertiesByNameMatching(Predicate<String> predicate) {
		return findAllPropertiesByNameMatching(this, predicate);
	}

	protected Set<String> findAllPropertiesByNameMatching(Iterable<String> properties, Predicate<String> predicate) {

		return StreamSupport.stream(properties.spliterator(), false)
			.filter(predicate)
			.collect(Collectors.toSet());
	}

	protected Set<String> findAllPropertiesByValueMatching(Predicate<Object> predicate) {
		return findAllPropertiesByValueMatching(this, predicate);
	}

	protected Set<String> findAllPropertiesByValueMatching(Iterable<String> properties, Predicate<Object> predicate) {

		return StreamSupport.stream(properties.spliterator(), false)
			.filter(propertyName -> predicate.test(getProperty(propertyName)))
			.collect(Collectors.toSet());
	}

	public Set<String> findAllVcapApplicationProperties() {
		return findAllPropertiesByNameMatching(VCAP_APPLICATION_PROPERTIES_PREDICATE);
	}

	public Set<String> findAllVcapServicesProperties() {
		return findAllPropertiesByNameMatching(VCAP_SERVICES_PROPERTIES_PREDICATE);
	}

	public Optional<CloudCacheService> findFirstCloudCacheService() {

		return findFirstCloudCacheServiceName()
			.map(serviceName -> {

				CloudCacheService service = CloudCacheService.with(serviceName);

				Object locators = getProperty(String.format(VCAP_SERVICES_SERVICE_NAME_LOCATORS_PROPERTY, service));

				Optional.ofNullable(locators)
					.map(String::valueOf)
					.filter(StringUtils::hasText)
					.ifPresent(service::withLocators);

				Object gfshUrl = getProperty(String.format(VCAP_SERVICES_SERVICE_NAME_GFSH_URL_PROPERTY, service));

				Optional.ofNullable(gfshUrl)
					.map(String::valueOf)
					.filter(StringUtils::hasText)
					.map(urlString -> ObjectUtils.doOperationSafely(() -> new URL(urlString)))
					.ifPresent(service::withGfshUrl);

				return service;
			});
	}

	public CloudCacheService requireFirstCloudCacheService() {

		return findFirstCloudCacheService().orElseThrow(() ->
			newIllegalStateException("Unable to resolve a CloudCache Service Instance"));
	}

	public Optional<String> findFirstCloudCacheServiceName() {

		Iterable<String> vcapServicesProperties = findAllVcapServicesProperties();

		Predicate<Object> vcapServicePredicate = resolveVcapServicePredicate();

		return findAllPropertiesByValueMatching(vcapServicesProperties, vcapServicePredicate).stream()
			.filter(propertyName -> propertyName.endsWith(".tags"))
			.map(propertyName -> propertyName.substring(VCAP_SERVICES_PROPERTY.length()))
			.map(propertyName -> propertyName.substring(0, propertyName.indexOf(".")))
			.filter(StringUtils::hasText)
			.min(String.CASE_INSENSITIVE_ORDER);
	}

	public String requireFirstCloudCacheServiceName() {

		String tags = String.format("%1$s, %2$s", CLOUD_CACHE_TAG_NAME, GEMFIRE_TAG_NAME);

		return findFirstCloudCacheServiceName()
			.orElseThrow(() -> newIllegalStateException("No service with tags [%s] was found", tags));
	}

	public Optional<User> findFirstUserByRoleClusterOperator(Service service) {

		String serviceName = service.getName();
		String userPropertyName = String.format(VCAP_SERVICES_SERVICE_NAME_USERS_PROPERTY, serviceName, 0);

		for (int index = 1; containsProperty(userPropertyName+".roles"); index++) {

			String roles = String.valueOf(getProperty(userPropertyName+".roles"));

			if (roles.contains(User.Role.CLUSTER_OPERATOR.name().toLowerCase())) {
				break;
			}

			userPropertyName = String.format(VCAP_SERVICES_SERVICE_NAME_USERS_PROPERTY, serviceName, index);
		}

		if (containsProperty(userPropertyName+".username")) {

			String username = String.valueOf(getProperty(userPropertyName+".username"));
			String password = String.valueOf(getProperty(userPropertyName+".password"));

			User user = User.with(username)
				.withPassword(password)
				.withRole(User.Role.CLUSTER_OPERATOR);

			return Optional.of(user);
		}

		return Optional.empty();
	}

	protected Predicate<Object> resolveVcapServicePredicate() {

		return this.vcapServicePredicate != null
			? this.vcapServicePredicate
			: CLOUD_CACHE_AND_GEMFIRE_SERVICE_PREDICATE;
	}

	@Nullable
	@Override
	public Object getProperty(String name) {
		return getSource().getProperty(name);
	}

	@Override
	@SuppressWarnings("all")
	public Iterator<String> iterator() {
		return Collections.unmodifiableList(Arrays.asList(getSource().getPropertyNames())).iterator();
	}

	public VcapPropertySource withVcapServicePredicate(Predicate<Object> vcapServicePredicate) {

		this.vcapServicePredicate = vcapServicePredicate;

		return this;
	}
}
