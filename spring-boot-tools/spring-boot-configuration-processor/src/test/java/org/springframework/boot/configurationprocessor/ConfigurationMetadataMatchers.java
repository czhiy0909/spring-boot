/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.configurationprocessor;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.GroupMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.PropertyMetadata;

/**
 * Hamcrest {@link Matcher} to help test {@link ConfigurationMetadata}.
 *
 * @author Phillip Webb
 */
public class ConfigurationMetadataMatchers {

	public static ContainsItemMatcher containsProperty(String name) {
		return new ContainsItemMatcher(PropertyMetadata.class, name);
	}

	public static ContainsItemMatcher containsProperty(String name, Class<?> type) {
		return new ContainsItemMatcher(PropertyMetadata.class, name).ofDataType(type);
	}

	public static ContainsItemMatcher containsProperty(String name, String type) {
		return new ContainsItemMatcher(PropertyMetadata.class, name).ofDataType(type);
	}

	public static ContainsItemMatcher containsGroup(String name) {
		return new ContainsItemMatcher(GroupMetadata.class, name);
	}

	public static ContainsItemMatcher containsGroup(String name, Class<?> type) {
		return new ContainsItemMatcher(GroupMetadata.class, name).ofDataType(type);
	}

	public static ContainsItemMatcher containsGroup(String name, String type) {
		return new ContainsItemMatcher(GroupMetadata.class, name).ofDataType(type);
	}

	public static class ContainsItemMatcher extends BaseMatcher<ConfigurationMetadata> {

		private final Class<?> itemType;

		private final String name;

		private final String dataType;

		private final Class<?> sourceType;

		private final String description;

		public ContainsItemMatcher(Class<?> itemType, String name) {
			this(itemType, name, null, null, null);
		}

		public ContainsItemMatcher(Class<?> itemType, String name, String dataType,
				Class<?> sourceType, String description) {
			this.itemType = itemType;
			this.name = name;
			this.dataType = dataType;
			this.sourceType = sourceType;
			this.description = description;
		}

		@Override
		public boolean matches(Object item) {
			ConfigurationMetadata metadata = (ConfigurationMetadata) item;
			ItemMetadata itemMetadata = getFirstPropertyWithName(metadata, this.name);
			if (itemMetadata == null) {
				return false;
			}
			if (this.dataType != null) {
				if (!(itemMetadata instanceof PropertyMetadata)) {
					throw new IllegalStateException(
							"GroupMetadata items have no data type");
				}
				if (!this.dataType
						.equals(((PropertyMetadata) itemMetadata).getDataType())) {
					return false;
				}
			}
			if (this.sourceType != null
					&& !this.sourceType.getName().equals(itemMetadata.getSourceType())) {
				return false;
			}
			if (this.description != null
					&& !this.description.equals(itemMetadata.getDescription())) {
				return false;
			}
			return true;
		}

		@Override
		public void describeMismatch(Object item, Description description) {
			ConfigurationMetadata metadata = (ConfigurationMetadata) item;
			ItemMetadata property = getFirstPropertyWithName(metadata, this.name);
			if (property == null) {
				description.appendText("missing property " + this.name);
			}
			else {
				description.appendText("was property ").appendValue(property);
			}
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("metadata containing " + this.name);
			if (this.dataType != null) {
				description.appendText(" dataType ").appendValue(this.dataType);
			}
			if (this.sourceType != null) {
				description.appendText(" sourceType ").appendValue(this.sourceType);
			}
			if (this.description != null) {
				description.appendText(" description ").appendValue(this.description);
			}
		}

		public ContainsItemMatcher ofDataType(Class<?> dataType) {
			return new ContainsItemMatcher(this.itemType, this.name, dataType.getName(),
					this.sourceType, this.description);
		}

		public ContainsItemMatcher ofDataType(String dataType) {
			return new ContainsItemMatcher(this.itemType, this.name, dataType,
					this.sourceType, this.description);
		}

		public ContainsItemMatcher fromSource(Class<?> sourceType) {
			return new ContainsItemMatcher(this.itemType, this.name, this.dataType,
					sourceType, this.description);
		}

		public ContainsItemMatcher withDescription(String description) {
			return new ContainsItemMatcher(this.itemType, this.name, this.dataType,
					this.sourceType, description);
		}

		private ItemMetadata getFirstPropertyWithName(ConfigurationMetadata metadata,
				String name) {
			for (ItemMetadata item : metadata.getItems()) {
				if (this.itemType.isInstance(item) && name.equals(item.getName())) {
					return item;
				}
			}
			return null;
		}

	}

}
