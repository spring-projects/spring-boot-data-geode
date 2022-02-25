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

package org.springframework.geode.boot.autoconfigure.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * The {@link OnMissingPropertyCondition} class is a {@link SpringBootCondition}, Spring {@link Condition} type
 * asserting whether the specified, declared properties are missing.
 *
 * @author John Blum
 * @see org.springframework.boot.autoconfigure.condition.ConditionOutcome
 * @see org.springframework.boot.autoconfigure.condition.SpringBootCondition
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.env.PropertyResolver
 * @see org.springframework.core.type.AnnotatedTypeMetadata
 * @see org.springframework.geode.boot.autoconfigure.condition.ConditionalOnMissingProperty
 * @since 1.0.0
 */
public class OnMissingPropertyCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

		String annotationName = ConditionalOnMissingProperty.class.getName();

		Collection<AnnotationAttributes> annotationAttributesCollection =
			toAnnotationAttributesFromMultiValueMap(metadata.getAllAnnotationAttributes(annotationName));

		PropertyResolver propertyResolver = getPropertyResolver(context);

		Collection<String> allMatchingProperties = new ArrayList<>();

		annotationAttributesCollection.forEach(annotationAttributes -> {

			List<String> propertyNames = collectPropertyNames(annotationAttributes);

			allMatchingProperties.addAll(findMatchingProperties(propertyResolver, propertyNames));
		});

		return determineConditionOutcome(allMatchingProperties);
	}

	@SuppressWarnings("unchecked")
	private <T extends Collection<AnnotationAttributes>> T toAnnotationAttributesFromMultiValueMap(
			MultiValueMap<String, Object> map) {

		List<AnnotationAttributes> annotationAttributesList = new ArrayList<>();

		map.forEach((key, value) -> {
			for (int index = 0, size = value.size(); index < size; index++) {

				AnnotationAttributes annotationAttributes =
					resolveAnnotationAttributes(annotationAttributesList, index);

				annotationAttributes.put(key, value.get(index));
			}
		});

		return (T) annotationAttributesList;
	}

	private AnnotationAttributes resolveAnnotationAttributes(List<AnnotationAttributes> annotationAttributesList,
			int index) {

		if (index < annotationAttributesList.size()) {
			return annotationAttributesList.get(index);
		}
		else {
			AnnotationAttributes newAnnotationAttributes = new AnnotationAttributes();
			annotationAttributesList.add(newAnnotationAttributes);
			return newAnnotationAttributes;
		}
	}

	private PropertyResolver getPropertyResolver(ConditionContext context) {
		return context.getEnvironment();
	}

	private List<String> collectPropertyNames(AnnotationAttributes annotationAttributes) {

		String prefix = getPrefix(annotationAttributes);

		String[] names = getNames(annotationAttributes);

		return Arrays.stream(names).map(name -> prefix + name).collect(Collectors.toList());
	}

	private String[] getNames(AnnotationAttributes annotationAttributes) {

		String[] names = annotationAttributes.getStringArray("name");
		String[] values = annotationAttributes.getStringArray("value");

		Assert.isTrue(names.length > 0 || values.length > 0,
			String.format("The name or value attribute of @%s is required",
				ConditionalOnMissingProperty.class.getSimpleName()));

		// TODO remove; not needed when using @AliasFor.
		/*
		Assert.isTrue(names.length * values.length == 0,
			String.format("The name and value attributes of @%s are exclusive",
				ConditionalOnMissingProperty.class.getSimpleName()));
		*/

		return names.length > 0 ? names : values;
	}

	private String getPrefix(AnnotationAttributes annotationAttributes) {

		String prefix = annotationAttributes.getString("prefix");

		return StringUtils.hasText(prefix) ? prefix.trim().endsWith(".") ? prefix.trim() : prefix.trim() + "." : "";
	}

	private Collection<String> findMatchingProperties(PropertyResolver propertyResolver, List<String> propertyNames) {
		return propertyNames.stream().filter(propertyResolver::containsProperty).collect(Collectors.toSet());
	}

	private ConditionOutcome determineConditionOutcome(Collection<String> matchingProperties) {

		if (!matchingProperties.isEmpty()) {

			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnMissingProperty.class)
				.found("property already defined", "properties already defined")
				.items(matchingProperties));
		}

		return ConditionOutcome.match();
	}
}
