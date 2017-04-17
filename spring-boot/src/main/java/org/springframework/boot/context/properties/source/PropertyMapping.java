/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties.source;

import java.util.function.Function;

import org.springframework.core.env.PropertySource;

/**
 * Details a mapping between a {@link PropertySource} item and a
 * {@link ConfigurationPropertySource} item.
 *
 * @author Phillip Webb
 * @see PropertySourceConfigurationPropertySource
 */
class PropertyMapping {

	private final String propertySourceName;

	private final ConfigurationPropertyName configurationPropertyName;

	private final Function<Object, Object> valueExtractor;

	/**
	 * Create a new {@link PropertyMapper} instance.
	 * @param propertySourceName the {@link PropertySource} name
	 * @param configurationPropertyName the {@link ConfigurationPropertySource}
	 * {@link ConfigurationPropertyName}
	 */
	PropertyMapping(String propertySourceName,
			ConfigurationPropertyName configurationPropertyName) {
		this(propertySourceName, configurationPropertyName, Function.identity());
	}

	/**
	 * Create a new {@link PropertyMapper} instance.
	 * @param propertySourceName the {@link PropertySource} name
	 * @param configurationPropertyName the {@link ConfigurationPropertySource}
	 * {@link ConfigurationPropertyName}
	 * @param valueExtractor the extractor used to obtain the value
	 */
	PropertyMapping(String propertySourceName,
			ConfigurationPropertyName configurationPropertyName,
			Function<Object, Object> valueExtractor) {
		this.propertySourceName = propertySourceName;
		this.configurationPropertyName = configurationPropertyName;
		this.valueExtractor = valueExtractor;
	}

	/**
	 * Return the mapped {@link PropertySource} name.
	 * @return the property source name (never {@code null})
	 */
	public String getPropertySourceName() {
		return this.propertySourceName;

	}

	/**
	 * Return the mapped {@link ConfigurationPropertySource}
	 * {@link ConfigurationPropertyName}.
	 * @return the configuration property source name (never {@code null})
	 */
	public ConfigurationPropertyName getConfigurationPropertyName() {
		return this.configurationPropertyName;

	}

	/**
	 * Return a function that can be used to extract the {@link PropertySource} value.
	 * @return the value extractor (never {@code null})
	 */
	public Function<Object, Object> getValueExtractor() {
		return this.valueExtractor;
	}

	/**
	 * Return if this mapping is applicable for the given
	 * {@link ConfigurationPropertyName}.
	 * @param name the name to check
	 * @return if the mapping is applicable
	 */
	public boolean isApplicable(ConfigurationPropertyName name) {
		return this.configurationPropertyName.equals(name);
	}

}
