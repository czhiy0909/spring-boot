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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MyExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertyState}.
 *
 * @author Phillip Webb
 */
public class ConfigurationPropertyStateTests {

	@Rule
	public MyExpectedException thrown = MyExpectedException.none();

	@Test
	public void searchWhenIterableIsNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class, "Source must not be null",
				() -> ConfigurationPropertyState.search(null, (e) -> true));
	}

	@Test
	public void searchWhenPredicateIsNullShouldThrowException() {
		this.thrown.expect(IllegalArgumentException.class, "Predicate must not be null",
				() -> ConfigurationPropertyState.search(Collections.emptyList(), null));
	}

	@Test
	public void searchWhenContainsItemShouldReturnPresent() {
		List<String> source = Arrays.asList("a", "b", "c");
		ConfigurationPropertyState result = ConfigurationPropertyState.search(source,
				"b"::equals);
		assertThat(result).isEqualTo(ConfigurationPropertyState.PRESENT);
	}

	@Test
	public void searchWhenContainsNoItemShouldReturnAbsent() {
		List<String> source = Arrays.asList("a", "x", "c");
		ConfigurationPropertyState result = ConfigurationPropertyState.search(source,
				"b"::equals);
		assertThat(result).isEqualTo(ConfigurationPropertyState.ABSENT);
	}

}
